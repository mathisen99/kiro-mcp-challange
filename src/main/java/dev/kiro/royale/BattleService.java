package dev.kiro.royale;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import static dev.kiro.royale.Models.*;

/** Shared Stage 1 use case; the later MCP adapter must delegate to this same service. */
public final class BattleService implements AutoCloseable {
    private final BotRegistry registry;
    private final BattleCoordinator coordinator;
    private final OfficialBattleRunnerAdapter engine;
    private final GenuineResultMapper resultMapper;

    public BattleService(RepositoryPaths paths) {
        this.registry = new BotRegistry(paths);
        this.coordinator = new BattleCoordinator();
        this.engine = new OfficialBattleRunnerAdapter(paths, Duration.ofSeconds(30), Duration.ofSeconds(120), Duration.ofSeconds(5));
        this.resultMapper = new GenuineResultMapper();
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
            EngineExecution execution = engine.run(bots, request.rounds(), request.record());
            var results = resultMapper.map(execution.completion(), bots);
            if (!execution.cleanupComplete()) return new BattleFailure("CLEANUP_FAILED", "Owned battle processes did not stop", execution.diagnostics());
            if (request.record() && execution.recordingPath().isEmpty()) {
                return new BattleFailure("RECORDING_FAILED", "The required official recording was not verified", execution.diagnostics());
            }
            return new BattleSuccess(execution.completion().roundsPlayed(), results, execution.recordingPath(),
                    execution.websocketUrl(), execution.completion().provenance(), execution.processes(),
                    execution.cleanupComplete(), execution.diagnostics());
        } catch (java.util.concurrent.TimeoutException exception) {
            return failure("BATTLE_TIMEOUT", "The official battle exceeded its finite deadline");
        } catch (Exception exception) {
            return failure("BATTLE_ABORTED", "The official battle did not complete successfully");
        }
    }

    private static BattleFailure failure(String code, String message) {
        return new BattleFailure(code, message, List.of());
    }

    @Override public void close() { engine.close(); }
}
