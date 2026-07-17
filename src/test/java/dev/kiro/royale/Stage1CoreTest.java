package dev.kiro.royale;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import static dev.kiro.royale.Models.*;
import static org.junit.jupiter.api.Assertions.*;

class Stage1CoreTest {
    @Test
    void registryContainsExactlyTheTwoValidReviewedBots() throws Exception {
        var registry = new BotRegistry(RepositoryPaths.locate());
        assertEquals(List.of("kiro-bot", "sample-opponent"),
                registry.list().stream().map(bot -> bot.id().value()).sorted().toList());
        assertTrue(registry.list().stream().allMatch(bot -> bot.validationStatus() == ValidationStatus.VALID));
        assertThrows(IllegalArgumentException.class, () -> registry.resolveValidated(new BotId("unknown")));
    }

    @Test
    void runtimePathsRemainContainedAndDisplayAsRepositoryRelative() throws Exception {
        var paths = RepositoryPaths.locate();
        var target = paths.runtimePath("results", "stage1-test");
        assertTrue(target.startsWith(paths.runtimeRoot()));
        assertEquals("runtime/results/stage1-test", paths.display(target));
        assertThrows(java.io.IOException.class, () -> paths.runtimePath("..", "outside"));
    }

    @Test
    void genuineMapperOrdersAndPreservesOfficialFields() {
        var first = validated("kiro-bot", "Kiro Bot");
        var second = validated("sample-opponent", "Sample Opponent");
        var completion = new EngineCompletion(true, true, 1, List.of(
                new EngineResult(2, "Sample Opponent", "1.0", 11, 12, 13, 14, 0),
                new EngineResult(1, "Kiro Bot", "1.0", 21, 22, 23, 24, 1)),
                CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION);
        var mapped = new GenuineResultMapper().map(completion, List.of(first, second));
        assertEquals(List.of(1, 2), mapped.stream().map(BattleResult::rank).toList());
        assertEquals(new BattleResult(1, "Kiro Bot", "1.0", 21, 22, 23, 24, 1, 1), mapped.getFirst());
    }

    @Test
    void mapperRejectsCompletionWithoutOfficialSuccessEvent() {
        var bot = validated("kiro-bot", "Kiro Bot");
        var other = validated("sample-opponent", "Sample Opponent");
        var completion = new EngineCompletion(true, false, 1, List.of(), CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION);
        assertThrows(IllegalStateException.class, () -> new GenuineResultMapper().map(completion, List.of(bot, other)));
    }

    @Test
    void coordinatorAllowsOnlyOneIdempotentlyReleasedLease() {
        var coordinator = new BattleCoordinator();
        var lease = coordinator.tryAcquire();
        assertNotNull(lease);
        assertTrue(coordinator.isActive());
        assertNull(coordinator.tryAcquire());
        lease.close();
        lease.close();
        assertFalse(coordinator.isActive());
        assertNotNull(coordinator.tryAcquire());
    }

    @Test
    void diagnosticsKeepOnlyABoundedTail() {
        var diagnostics = new BoundedDiagnostics(2);
        diagnostics.add("first");
        diagnostics.add("second");
        diagnostics.add("third\nline");
        assertEquals(List.of("[earlier diagnostics truncated]", "second", "third line"), diagnostics.snapshot());
    }

    private static ValidatedBot validated(String id, String name) {
        var descriptor = new BotDescriptor(new BotId(id), name, "1.0", "Java 21", "bots/" + id,
                ValidationStatus.VALID, "test");
        return new ValidatedBot(descriptor, Path.of("bots", id), List.of("fixed"));
    }
}
