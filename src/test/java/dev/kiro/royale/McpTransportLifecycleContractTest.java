package dev.kiro.royale;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class McpTransportLifecycleContractTest {
    @Test
    void realStdioTransportHasExactStrictDualRepresentationContractAndRuntimeOnlyEffects() throws Exception {
        RepositoryPaths paths = RepositoryPaths.locate();
        Set<String> before = runtimeEntries(paths.runtimeRoot());
        var diagnostics = new BoundedDiagnostics(40);
        var transport = new StdioClientTransport(
                ServerParameters.builder("./scripts/kiro-royale-mcp.sh").build(), McpJsonDefaults.getMapper());
        transport.setStdErrorHandler(diagnostics::add);
        var client = McpClient.sync(transport)
                .initializationTimeout(Duration.ofSeconds(20)).requestTimeout(Duration.ofSeconds(20)).build();
        try (client) {
            client.initialize();
            var tools = client.listTools().tools();
            assertEquals(McpToolAdapter.TOOL_NAMES.stream().sorted().toList(),
                    tools.stream().map(tool -> tool.name()).sorted().toList());
            tools.forEach(tool -> assertEquals(Boolean.FALSE, tool.inputSchema().get("additionalProperties")));

            for (String name : List.of("get_arena_status", "list_bots")) {
                var result = client.callTool(CallToolRequest.builder(name).arguments(Map.of()).build());
                assertFalse(Boolean.TRUE.equals(result.isError()));
                assertTrue(result.structuredContent() instanceof Map<?, ?>);
                assertTrue(result.content().stream().filter(TextContent.class::isInstance)
                        .map(TextContent.class::cast).anyMatch(text -> !text.text().isBlank()));
            }
            var inspect = client.callTool(CallToolRequest.builder("inspect_bot")
                    .arguments(Map.of("botId", "kiro-bot")).build());
            assertFalse(Boolean.TRUE.equals(inspect.isError()));
            var rejected = client.callTool(CallToolRequest.builder("get_arena_status")
                    .arguments(Map.of("diagnostic", "deliberate")).build());
            assertTrue(Boolean.TRUE.equals(rejected.isError()));
        }
        assertTrue(diagnostics.snapshot().stream().anyMatch(line -> line.contains("stdout is protocol-only")),
                () -> "Expected deliberate startup diagnostic on stderr: " + diagnostics.snapshot());
        assertEquals(before, runtimeEntries(paths.runtimeRoot()), "Read-only transport calls created runtime side effects");
    }

    @Test
    void controlledOwnedProcessesAreBoundedlyCleanedAfterSuccessFailureTimeoutAndShutdown() throws Exception {
        Process success = new ProcessBuilder(List.of("/usr/bin/true")).start();
        assertEquals(0, success.waitFor());
        assertTrue(OwnedProcessCleanup.terminate(List.of(success.toHandle()), Duration.ofMillis(100)));

        Process failure = new ProcessBuilder(List.of("/usr/bin/false")).start();
        assertNotEquals(0, failure.waitFor());
        assertTrue(OwnedProcessCleanup.terminate(List.of(failure.toHandle()), Duration.ofMillis(100)));

        Process timeout = new ProcessBuilder(List.of("/usr/bin/sleep", "30")).start();
        assertTrue(OwnedProcessCleanup.terminate(List.of(timeout.toHandle()), Duration.ofMillis(100)));
        assertFalse(timeout.isAlive());

        Process shutdown = new ProcessBuilder(List.of("/usr/bin/sleep", "30")).start();
        assertTrue(OwnedProcessCleanup.terminate(List.of(shutdown.toHandle()), Duration.ofMillis(100)));
        assertTrue(OwnedProcessCleanup.terminate(List.of(shutdown.toHandle()), Duration.ofMillis(100)));
        assertFalse(shutdown.isAlive());
    }

    private static Set<String> runtimeEntries(Path runtimeRoot) throws Exception {
        try (var entries = Files.walk(runtimeRoot)) {
            return entries.map(runtimeRoot::relativize).map(Path::toString).collect(Collectors.toUnmodifiableSet());
        }
    }
}
