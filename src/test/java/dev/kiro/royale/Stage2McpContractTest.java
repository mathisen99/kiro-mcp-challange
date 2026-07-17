package dev.kiro.royale;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Stage2McpContractTest {
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
