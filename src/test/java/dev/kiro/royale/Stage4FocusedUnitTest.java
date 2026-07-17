package dev.kiro.royale;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static dev.kiro.royale.Models.*;
import static org.junit.jupiter.api.Assertions.*;

class Stage4FocusedUnitTest {
    private static final List<BotId> VALID_IDS = List.of(new BotId("kiro-bot"), new BotId("sample-opponent"));

    @Test
    void canonicalBotAndRuntimeSymlinkEscapesAreRejected(@TempDir Path temporary) throws Exception {
        Path root = temporary.resolve("repository");
        Path outside = temporary.resolve("outside");
        Files.createDirectories(root.resolve("bots"));
        Files.createDirectories(root.resolve("runtime"));
        Files.createDirectories(outside);
        Files.createSymbolicLink(root.resolve("bots/kiro-bot"), outside);
        Files.createDirectories(root.resolve("bots/sample-opponent"));
        RepositoryPaths paths = RepositoryPaths.fromRoot(root);

        var registry = new BotRegistry(paths);
        assertEquals(ValidationStatus.INVALID, registry.inspect(new BotId("kiro-bot")).descriptor().validationStatus());
        assertThrows(IllegalStateException.class, () -> registry.resolveValidated(new BotId("kiro-bot")));

        Files.createSymbolicLink(root.resolve("runtime/escape"), outside);
        assertThrows(IOException.class, () -> paths.runtimePath("escape", "generated"));
        assertFalse(Files.exists(outside.resolve("generated")));
    }

    @Test
    void unknownBotAndInvalidBotStopBeforeEngineInvocation(@TempDir Path temporary) throws Exception {
        FakeEngine engine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.locate(), engine)) {
            assertFailure(service.run(new BattleRequest(List.of(new BotId("unknown"), VALID_IDS.get(1)), 1, false)), "UNKNOWN_BOT");
            assertEquals(0, engine.invocations.get());
        }

        Path root = temporary.resolve("invalid-repository");
        Files.createDirectories(root.resolve("bots/kiro-bot"));
        Files.createDirectories(root.resolve("bots/sample-opponent"));
        Files.createDirectories(root.resolve("runtime"));
        FakeEngine invalidEngine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.fromRoot(root), invalidEngine)) {
            assertFailure(service.run(new BattleRequest(VALID_IDS, 1, false)), "BOT_INVALID");
            assertEquals(0, invalidEngine.invocations.get());
        }
    }

    @Test
    void roundBoundariesAndRecordFalseAreEnforcedWithoutFalseRecordingClaims() throws Exception {
        FakeEngine engine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.locate(), engine)) {
            assertFailure(service.run(new BattleRequest(VALID_IDS, 0, false)), "INVALID_REQUEST");
            BattleSuccess one = assertInstanceOf(BattleSuccess.class, service.run(new BattleRequest(VALID_IDS, 1, false)));
            BattleSuccess five = assertInstanceOf(BattleSuccess.class, service.run(new BattleRequest(VALID_IDS, 5, false)));
            assertFailure(service.run(new BattleRequest(VALID_IDS, 6, false)), "INVALID_REQUEST");
            assertEquals(List.of(1, 5), engine.rounds);
            assertTrue(one.recordingPath().isEmpty());
            assertTrue(five.recordingPath().isEmpty());
            assertEquals(List.of(false, false), engine.recordFlags);
        }
    }

    @Test
    void duplicateNonIntegerUnexpectedAndUnsafeInputsAreStrictlyRejected() throws Exception {
        FakeEngine engine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.locate(), engine)) {
            McpToolAdapter adapter = new McpToolAdapter(RepositoryPaths.locate(), service);
            var tools = adapter.specifications();
            assertError(call(tools.get(3), "run_battle", Map.of("botIds", List.of("kiro-bot", "kiro-bot"))));
            assertError(call(tools.get(3), "run_battle", Map.of("botIds", ids(), "rounds", 1.5)));
            assertError(call(tools.get(3), "run_battle", Map.of("botIds", ids(), "showBattle", "yes")));
            assertError(call(tools.get(3), "run_battle", Map.of("botIds", ids(), "command", "sh")));
            assertError(call(tools.get(3), "run_battle", Map.of("botIds", ids(), "host", "0.0.0.0")));
            assertError(call(tools.get(0), "get_arena_status", Map.of("extra", true)));
            assertError(call(tools.get(1), "list_bots", Map.of("extra", true)));
            assertError(call(tools.get(2), "inspect_bot", Map.of("botId", "kiro-bot", "extra", true)));
            assertEquals(0, engine.invocations.get());
        }
    }

    @Test
    void typedAndUnexpectedFailuresAreSanitizedAndResultFree() throws Exception {
        for (BattleEngineException.Kind kind : BattleEngineException.Kind.values()) {
            FakeEngine engine = new FakeEngine();
            engine.failure = new BattleEngineException(kind,
                    new IllegalStateException("secret /home/user command=rm environment=TOKEN stack trace"));
            try (BattleService service = service(RepositoryPaths.locate(), engine)) {
                assertFailure(service.run(new BattleRequest(VALID_IDS, 1, false)), kind.name());
            }
        }

        FakeEngine unexpected = new FakeEngine();
        unexpected.runtimeFailure = new IllegalStateException("secret /home/user command=rm environment=TOKEN stack trace");
        try (BattleService service = service(RepositoryPaths.locate(), unexpected)) {
            var result = call(new McpToolAdapter(RepositoryPaths.locate(), service).specifications().get(3),
                    "run_battle", Map.of("botIds", ids(), "record", false));
            assertError(result);
            String boundary = result.content() + " " + result.structuredContent();
            assertTrue(boundary.contains("INTERNAL_ERROR"));
            assertFalse(boundary.contains("/home/user"));
            assertFalse(boundary.contains("TOKEN"));
            assertFalse(boundary.contains("results"));
            assertFalse(boundary.contains("recordingPath"));
        }
    }

    @Test
    void timeoutIsTypedAndShutdownClosesTheInjectedBoundary() throws Exception {
        FakeEngine engine = new FakeEngine();
        engine.timeout = true;
        BattleService service = service(RepositoryPaths.locate(), engine);
        assertFailure(service.run(new BattleRequest(VALID_IDS, 1, false)), "BATTLE_TIMEOUT");
        assertFalse(service.battleActive());
        service.close();
        service.close();
        assertEquals(2, engine.closeCalls.get());
    }

    @Test
    void statusDoesNotInventAnEndpointAndSuccessHasTextAndStructuredContent() throws Exception {
        FakeEngine engine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.locate(), engine)) {
            var tools = new McpToolAdapter(RepositoryPaths.locate(), service).specifications();
            CallToolResult status = call(tools.get(0), "get_arena_status", Map.of());
            assertFalse(Boolean.TRUE.equals(status.isError()));
            assertTrue(status.content().toString().contains("Arena status"));
            assertNull(asMap(status).get("websocketUrl"));
        }
    }

    @Test
    void explicitShowBattleIsPropagatedAndReportedWithoutChangingTheDefault() throws Exception {
        FakeEngine engine = new FakeEngine();
        try (BattleService service = service(RepositoryPaths.locate(), engine)) {
            var battleTool = new McpToolAdapter(RepositoryPaths.locate(), service).specifications().get(3);
            CallToolResult defaultResult = call(battleTool, "run_battle",
                    Map.of("botIds", ids(), "record", false));
            CallToolResult viewerResult = call(battleTool, "run_battle",
                    Map.of("botIds", ids(), "record", false, "showBattle", true));

            assertFalse(Boolean.TRUE.equals(defaultResult.isError()));
            assertFalse(Boolean.TRUE.equals(viewerResult.isError()));
            assertEquals(List.of(false, true), engine.showBattleFlags);
            assertEquals(false, asMap(defaultResult).get("viewerRequested"));
            assertEquals(false, asMap(defaultResult).get("viewerConnected"));
            assertEquals(true, asMap(viewerResult).get("viewerRequested"));
            assertEquals(true, asMap(viewerResult).get("viewerConnected"));
        }
    }

    @Test
    void finiteTimeoutsBoundedRedactedDiagnosticsAndModeStdoutArchitectureAreEnforced() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        assertThrows(IllegalArgumentException.class, () -> new OfficialBattleRunnerAdapter(paths,
                Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new OfficialBattleRunnerAdapter(paths,
                Duration.ofSeconds(1), Duration.ofDays(2), Duration.ofSeconds(1)));

        var diagnostics = new BoundedDiagnostics(2, 8, Set.of("top-secret", "secret"));
        diagnostics.add("top-secret-abcdefghijkl");
        diagnostics.add("safe");
        diagnostics.add("secret");
        String retained = diagnostics.snapshot().toString();
        assertFalse(retained.contains("top-secret"));
        assertFalse(retained.contains("secret"));
        assertTrue(retained.contains("truncated"));

        String mcpBootstrap = Files.readString(paths.repositoryRoot().resolve(
                "src/main/java/dev/kiro/royale/KiroRoyaleApplication.java"));
        String direct = Files.readString(paths.repositoryRoot().resolve(
                "src/main/java/dev/kiro/royale/DirectBattleDiagnostic.java"));
        String runner = Files.readString(paths.repositoryRoot().resolve(
                "src/main/java/dev/kiro/royale/OfficialBattleRunnerAdapter.java"));
        String launcher = Files.readString(paths.repositoryRoot().resolve("scripts/kiro-royale-mcp.sh"));
        assertFalse(mcpBootstrap.contains("System.out.print"));
        assertTrue(direct.contains("System.out.println"));
        assertTrue(runner.contains("new ProcessBuilder(command)"));
        assertFalse(runner.contains(".environment("));
        assertTrue(runner.contains("showBattle ? \"[::1]\" : \"127.0.0.1\""));
        assertTrue(runner.contains("\"--listen=\" + listenAddress + \":\" + serverPort"));
        assertFalse(runner.contains("builder.embeddedServer"));
        assertEquals(Duration.ZERO, LiveViewerLauncher.resultDisplayHold(false));
        assertEquals(Duration.ofSeconds(5), LiveViewerLauncher.resultDisplayHold(true));
        assertTrue(launcher.contains("/proc/$PPID/environ"));
        assertFalse(launcher.contains("env >"));
        assertFalse(launcher.contains("export $("));
    }

    private static BattleService service(RepositoryPaths paths, BattleEngine engine) {
        return new BattleService(new BotRegistry(paths), new BattleCoordinator(), engine, new GenuineResultMapper());
    }

    private static List<String> ids() { return List.of("kiro-bot", "sample-opponent"); }

    private static CallToolResult call(io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification specification,
                                       String name, Map<String, Object> arguments) {
        return specification.callHandler().apply(null, CallToolRequest.builder(name).arguments(arguments).build());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(CallToolResult result) {
        return (Map<String, Object>) result.structuredContent();
    }

    private static void assertError(CallToolResult result) {
        assertTrue(Boolean.TRUE.equals(result.isError()), () -> "Expected tool error but got " + result.structuredContent());
    }

    private static void assertFailure(BattleOutcome outcome, String code) {
        BattleFailure failure = assertInstanceOf(BattleFailure.class, outcome);
        assertEquals(code, failure.code());
        assertTrue(failure.diagnostics().isEmpty());
    }

    private static final class FakeEngine implements BattleEngine {
        private final AtomicInteger invocations = new AtomicInteger();
        private final AtomicInteger closeCalls = new AtomicInteger();
        private final java.util.ArrayList<Integer> rounds = new java.util.ArrayList<>();
        private final java.util.ArrayList<Boolean> recordFlags = new java.util.ArrayList<>();
        private final java.util.ArrayList<Boolean> showBattleFlags = new java.util.ArrayList<>();
        private BattleEngineException failure;
        private RuntimeException runtimeFailure;
        private boolean timeout;

        @Override public Optional<String> readyEndpoint() { return Optional.empty(); }

        @Override public EngineExecution run(List<ValidatedBot> bots, int requestedRounds, boolean record,
                                             boolean showBattle) throws Exception {
            invocations.incrementAndGet();
            rounds.add(requestedRounds);
            recordFlags.add(record);
            showBattleFlags.add(showBattle);
            if (timeout) throw new TimeoutException("hostile timeout detail");
            if (failure != null) throw failure;
            if (runtimeFailure != null) throw runtimeFailure;
            List<EngineResult> results = List.of(
                    result(1, bots.get(0)), result(2, bots.get(1)));
            return new EngineExecution(new EngineCompletion(true, true, requestedRounds, results,
                    CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION), "ws://127.0.0.1:1",
                    record ? Optional.of("runtime/recordings/fake.battle.gz") : Optional.empty(),
                    List.of(), true, List.of(), showBattle);
        }

        private static EngineResult result(int rank, ValidatedBot bot) {
            return new EngineResult(rank, bot.descriptor().name(), bot.descriptor().version(),
                    10 * rank, 5 * rank, 3 * rank, 2 * rank, rank == 1 ? 1 : 0);
        }

        @Override public void close() { closeCalls.incrementAndGet(); }
    }
}
