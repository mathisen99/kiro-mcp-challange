package dev.kiro.royale;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Stage2McpContractTest {
    @Test
    void battleTextSummaryCarriesTheGenuineFieldsNeededByKiro() {
        var completed = new Models.BattleSuccess(1, List.of(
                new Models.BattleResult(1, "Kiro Bot", "1.0", 42, 30, 8, 4, 1, 1),
                new Models.BattleResult(2, "Sample Opponent", "1.0", 10, 0, 10, 0, 0, 1)),
                Optional.of("runtime/recordings/example.battle.gz"), "ws://127.0.0.1:12345",
                Models.CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION, List.of(), true, List.of());

        String summary = McpToolAdapter.battleSummary(completed);

        assertAll(
                () -> assertTrue(summary.contains("provenance=OFFICIAL_BATTLE_RUNNER_COMPLETION")),
                () -> assertTrue(summary.contains("recordingPath=runtime/recordings/example.battle.gz")),
                () -> assertTrue(summary.contains("cleanupComplete=true")),
                () -> assertTrue(summary.contains("sourceHashes={}")),
                () -> assertTrue(summary.contains("#1 Kiro Bot 1.0 totalScore=42 survivalScore=30 bulletDamage=8 ramDamage=4 firstPlaces=1 roundsPlayed=1")),
                () -> assertTrue(summary.contains("#2 Sample Opponent 1.0 totalScore=10")));
    }

    @Test
    void exposesExactlyFourStrictToolsWithOnlyReadToolsAnnotatedReadOnly() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        try (BattleService service = new BattleService(paths)) {
            var tools = new McpToolAdapter(paths, service).specifications();
            assertEquals(McpToolAdapter.TOOL_NAMES,
                    tools.stream().map(specification -> specification.tool().name()).toList());
            for (var specification : tools) {
                assertEquals(Boolean.FALSE, specification.tool().inputSchema().get("additionalProperties"));
                boolean expectedReadOnly = !"run_battle".equals(specification.tool().name());
                assertEquals(expectedReadOnly, specification.tool().annotations().readOnlyHint());
            }
        }
    }

    @Test
    void readOnlyToolsReturnTextAndStructuredContentWithoutAbsolutePaths() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        try (BattleService service = new BattleService(paths)) {
            var tools = new McpToolAdapter(paths, service).specifications();
            var list = tools.get(1).callHandler().apply(null,
                    CallToolRequest.builder("list_bots").arguments(Map.of()).build());
            var inspect = tools.get(2).callHandler().apply(null,
                    CallToolRequest.builder("inspect_bot").arguments(Map.of("botId", "kiro-bot")).build());
            assertFalse(Boolean.TRUE.equals(list.isError()));
            assertFalse(Boolean.TRUE.equals(inspect.isError()));
            assertNotNull(list.structuredContent());
            assertNotNull(inspect.structuredContent());
            assertFalse(inspect.structuredContent().toString().contains(paths.repositoryRoot().toString()));
        }
    }

    @Test
    void strictHandlersRejectAdditionalPropertiesAndUnsafeBattleShapesWithoutLaunching() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        try (BattleService service = new BattleService(paths)) {
            var tools = new McpToolAdapter(paths, service).specifications();
            var statusExtra = tools.get(0).callHandler().apply(null,
                    CallToolRequest.builder("get_arena_status").arguments(Map.of("extra", true)).build());
            var inspectPath = tools.get(2).callHandler().apply(null,
                    CallToolRequest.builder("inspect_bot").arguments(Map.of("botId", "/tmp/bot")).build());
            var battleCommand = tools.get(3).callHandler().apply(null,
                    CallToolRequest.builder("run_battle").arguments(Map.of(
                            "botIds", List.of("kiro-bot", "sample-opponent"), "command", "sh")).build());
            assertTrue(Boolean.TRUE.equals(statusExtra.isError()));
            assertTrue(Boolean.TRUE.equals(inspectPath.isError()));
            assertTrue(Boolean.TRUE.equals(battleCommand.isError()));
            assertFalse(service.battleActive());
        }
    }
}
