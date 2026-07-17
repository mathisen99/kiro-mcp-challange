package dev.kiro.royale;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static dev.kiro.royale.Models.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("real-smoke")
class RealBattleSmokeTest {
    @Test
    void officialOneRoundBattleUsesBothRealBundledBotsAndCleansUp() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        try (BattleService service = new BattleService(paths)) {
            var descriptors = service.registry().list();
            assertEquals(2, descriptors.size());
            assertTrue(descriptors.stream().allMatch(bot -> bot.validationStatus() == ValidationStatus.VALID));
            var outcome = service.run(new BattleRequest(
                    List.of(new BotId("kiro-bot"), new BotId("sample-opponent")), 1, true));
            BattleSuccess success = assertInstanceOf(BattleSuccess.class, outcome,
                    () -> outcome instanceof BattleFailure failure ? failure.code() + ": " + failure.message() : "unexpected outcome");

            assertEquals(CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION, success.provenance());
            assertEquals(1, success.roundsPlayed());
            assertEquals(List.of(1, 2), success.results().stream().map(BattleResult::rank).toList());
            assertEquals(Set.of("Kiro Bot\u00001.0", "Sample Opponent\u00001.0"), success.results().stream()
                    .map(result -> result.name() + "\u0000" + result.version()).collect(java.util.stream.Collectors.toSet()));
            success.results().forEach(result -> assertAll(
                    () -> assertEquals(1, result.roundsPlayed()),
                    () -> assertTrue(result.totalScore() >= 0),
                    () -> assertTrue(result.survivalScore() >= 0),
                    () -> assertTrue(result.bulletDamage() >= 0),
                    () -> assertTrue(result.ramDamage() >= 0),
                    () -> assertTrue(result.firstPlaces() >= 0)));
            assertTrue(success.cleanupComplete());
            assertFalse(success.processes().isEmpty());
            assertTrue(success.processes().stream().noneMatch(ProcessEvidence::aliveAfterCleanup));
            assertTrue(success.websocketUrl().startsWith("ws://127.0.0.1:"));

            String recordingDisplay = success.recordingPath().orElseThrow();
            var recording = paths.repositoryRoot().resolve(recordingDisplay).normalize();
            assertTrue(recording.startsWith(paths.runtimeRoot()));
            assertTrue(Files.isRegularFile(recording));
            assertTrue(Files.size(recording) > 0);

            System.out.println("REAL_SMOKE_OFFICIAL_COMPLETION: provenance=" + success.provenance()
                    + " rounds=" + success.roundsPlayed() + " endpoint=" + success.websocketUrl());
            success.results().forEach(result -> System.out.printf(
                    "REAL_SMOKE_RESULT: rank=%d name=%s version=%s totalScore=%d survivalScore=%d bulletDamage=%d ramDamage=%d firstPlaces=%d roundsPlayed=%d%n",
                    result.rank(), result.name(), result.version(), result.totalScore(), result.survivalScore(),
                    result.bulletDamage(), result.ramDamage(), result.firstPlaces(), result.roundsPlayed()));
            System.out.println("REAL_SMOKE_RECORDING: path=" + recordingDisplay + " bytes=" + Files.size(recording));
            System.out.println("REAL_SMOKE_CLEANUP: ownedProcesses=" + success.processes().size() + " complete=true");
        }
    }
}
