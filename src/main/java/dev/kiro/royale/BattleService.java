package dev.kiro.royale;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import static dev.kiro.royale.Models.*;

/** Shared application use case used unchanged by direct, MCP, and smoke adapters. */
public final class BattleService implements AutoCloseable {
    private final BotRegistry registry;
    private final BattleCoordinator coordinator;
    private final BattleEngine engine;
    private final GenuineResultMapper resultMapper;

    public BattleService(RepositoryPaths paths) {
        this(new BotRegistry(paths), new BattleCoordinator(),
                new OfficialBattleRunnerAdapter(paths, Duration.ofSeconds(30), Duration.ofSeconds(120), Duration.ofSeconds(5)),
                new GenuineResultMapper());
    }

    BattleService(BotRegistry registry, BattleCoordinator coordinator, BattleEngine engine,
                  GenuineResultMapper resultMapper) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
        this.coordinator = java.util.Objects.requireNonNull(coordinator, "coordinator");
        this.engine = java.util.Objects.requireNonNull(engine, "engine");
        this.resultMapper = java.util.Objects.requireNonNull(resultMapper, "resultMapper");
    }

    public BotRegistry registry() { return registry; }
    public boolean battleActive() { return coordinator.isActive(); }
    public java.util.Optional<String> readyWebsocketUrl() { return engine.readyEndpoint(); }

    public BattleOutcome run(BattleRequest request) {
        if (request == null) return failure("INVALID_REQUEST", "Battle request is required");
        if (request.botIds() == null || request.botIds().size() != 2 || Set.copyOf(request.botIds()).size() != 2) {
            return failure("INVALID_REQUEST", "Exactly two distinct registered bot IDs are required");
        }
        if (request.rounds() < 1 || request.rounds() > 5) {
            return failure("INVALID_REQUEST", "Rounds must be an integer from 1 through 5");
        }
        BattleCoordinator.Lease lease = coordinator.tryAcquire();
        if (lease == null) return failure("BATTLE_ACTIVE", "Another battle is already active");
        try (lease) {
            List<ValidatedBot> bots;
            try {
                bots = request.botIds().stream().map(registry::resolveValidated).toList();
            } catch (IllegalArgumentException exception) {
                return failure("UNKNOWN_BOT", "A selected bot ID is not registered");
            } catch (IllegalStateException exception) {
                return failure("BOT_INVALID", "A selected bundled bot failed prerequisite validation");
            }
            EngineExecution execution = engine.run(bots, request.rounds(), request.record(), request.showBattle());
            List<BattleResult> results;
            try {
                results = resultMapper.map(execution.completion(), bots);
            } catch (IllegalStateException exception) {
                return failure("BATTLE_ABORTED", "The official battle completion was invalid");
            }
            if (!execution.cleanupComplete()) return failure("CLEANUP_FAILED", "Owned battle processes did not stop");
            if (request.record() && execution.recordingPath().isEmpty()) {
                return failure("RECORDING_FAILED", "The required official recording was not verified");
            }
            if (!request.record() && execution.recordingPath().isPresent()) {
                return failure("INTERNAL_ERROR", "The battle boundary returned an inconsistent recording state");
            }
            return new BattleSuccess(execution.completion().roundsPlayed(), results, execution.recordingPath(),
                    execution.websocketUrl(), execution.completion().provenance(), execution.processes(),
                    execution.cleanupComplete(), execution.diagnostics(), request.showBattle(),
                    execution.viewerConnected());
        } catch (TimeoutException exception) {
            return failure("BATTLE_TIMEOUT", "The official battle exceeded its finite deadline");
        } catch (BattleEngineException exception) {
            logSafeFailureFingerprint(exception);
            return failure(exception.safeCode(), exception.safeMessage());
        } catch (RuntimeException exception) {
            logSafeFailureFingerprint(exception);
            return failure("INTERNAL_ERROR", "The battle could not complete; inspect local diagnostics");
        } catch (Exception exception) {
            logSafeFailureFingerprint(exception);
            return failure("BATTLE_ABORTED", "The official battle did not complete successfully");
        }
    }

    private static void logSafeFailureFingerprint(Exception exception) {
        StackTraceElement origin = exception.getStackTrace().length == 0 ? null : exception.getStackTrace()[0];
        String location = origin == null ? "unknown" : origin.getClassName() + "." + origin.getMethodName()
                + ":" + origin.getLineNumber();
        Throwable cause = exception.getCause();
        String causeType = cause == null ? "none" : cause.getClass().getName();
        System.err.println("Battle failure fingerprint: type=" + exception.getClass().getName()
                + " origin=" + location + " causeType=" + causeType);
    }

    private static BattleFailure failure(String code, String message) {
        return new BattleFailure(code, message, List.of());
    }

    @Override public void close() { engine.close(); }
}
