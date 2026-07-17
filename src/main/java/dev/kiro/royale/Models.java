package dev.kiro.royale;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Stage 1 internal data contracts; none depend on MCP types. */
public final class Models {
    private Models() {}

    public record BotId(String value) {
        public BotId {
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Bot ID must not be blank");
        }
    }

    public enum ValidationStatus { VALID, INVALID }

    public record BotDescriptor(
            BotId id, String name, String version, String language, String directory,
            ValidationStatus validationStatus, String sourceLabel) {}

    public record BotInspection(
            BotDescriptor descriptor, List<String> sourceFiles, String primaryEditableSource,
            List<String> buildArguments, List<String> runArguments, List<String> validationIssues) {}

    public record ValidatedBot(BotDescriptor descriptor, Path canonicalDirectory, List<String> launchArguments) {}

    public record BattleRequest(List<BotId> botIds, int rounds, boolean record, boolean showBattle) {
        public BattleRequest(List<BotId> botIds, int rounds, boolean record) {
            this(botIds, rounds, record, false);
        }
    }

    public enum CompletionProvenance { OFFICIAL_BATTLE_RUNNER_COMPLETION }

    public record EngineResult(
            int rank, String name, String version, int totalScore, int survivalScore,
            int bulletDamage, int ramDamage, int firstPlaces) {}

    public record EngineCompletion(
            boolean successful, boolean officialCompletionObserved, int roundsPlayed,
            List<EngineResult> results, CompletionProvenance provenance) {}

    public record BattleResult(
            int rank, String name, String version, long totalScore, long survivalScore,
            long bulletDamage, long ramDamage, int firstPlaces, int roundsPlayed) {}

    public record ProcessEvidence(long pid, String role, String command, boolean aliveAfterCleanup) {}

    public record EngineExecution(
            EngineCompletion completion, String websocketUrl, Optional<String> recordingPath,
            List<ProcessEvidence> processes, boolean cleanupComplete, List<String> diagnostics,
            boolean viewerConnected) {
        public EngineExecution(EngineCompletion completion, String websocketUrl, Optional<String> recordingPath,
                               List<ProcessEvidence> processes, boolean cleanupComplete, List<String> diagnostics) {
            this(completion, websocketUrl, recordingPath, processes, cleanupComplete, diagnostics, false);
        }
    }

    public sealed interface BattleOutcome permits BattleSuccess, BattleFailure {}

    public record BattleSuccess(
            int roundsPlayed, List<BattleResult> results, Optional<String> recordingPath,
            String websocketUrl, CompletionProvenance provenance, List<ProcessEvidence> processes,
            boolean cleanupComplete, List<String> diagnostics, boolean viewerRequested,
            boolean viewerConnected) implements BattleOutcome {
        public BattleSuccess(int roundsPlayed, List<BattleResult> results, Optional<String> recordingPath,
                             String websocketUrl, CompletionProvenance provenance, List<ProcessEvidence> processes,
                             boolean cleanupComplete, List<String> diagnostics) {
            this(roundsPlayed, results, recordingPath, websocketUrl, provenance, processes,
                    cleanupComplete, diagnostics, false, false);
        }
    }

    public record BattleFailure(String code, String message, List<String> diagnostics) implements BattleOutcome {}
}
