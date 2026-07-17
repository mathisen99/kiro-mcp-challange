package dev.kiro.royale;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Finite official MCP Java client proof against the repository-root launcher. */
public final class Stage2McpProof {
    private Stage2McpProof() {}

    public static void main(String[] args) throws Exception {
        boolean showBattle = List.of(args).equals(List.of("--show-battle"));
        if (args.length != 0 && !showBattle) {
            throw new IllegalArgumentException("The MCP proof accepts only optional --show-battle");
        }
        long proofStarted = System.nanoTime();
        RepositoryPaths paths = RepositoryPaths.locate();
        var stderr = new BoundedDiagnostics(80);
        var parameters = ServerParameters.builder("./scripts/kiro-royale-mcp.sh").build();
        var transport = new StdioClientTransport(parameters, McpJsonDefaults.getMapper());
        transport.setStdErrorHandler(stderr::add);
        McpSyncClient client = McpClient.sync(transport)
                .clientInfo(new io.modelcontextprotocol.spec.McpSchema.Implementation("kiro-royale-stage2-proof", "0.1.0"))
                .initializationTimeout(Duration.ofSeconds(20))
                .requestTimeout(Duration.ofSeconds(180))
                .build();
        try (client) {
            var initialized = client.initialize();
            require(initialized != null && client.isInitialized(), "MCP initialization failed");
            System.out.println("MCP_HANDSHAKE_OK: server=" + initialized.serverInfo().name()
                    + " version=" + initialized.serverInfo().version());

            var tools = client.listTools().tools();
            List<String> names = tools.stream().map(tool -> tool.name()).sorted().toList();
            List<String> expected = McpToolAdapter.TOOL_NAMES.stream().sorted().toList();
            require(names.equals(expected), "Expected exactly the four Stage 2 tools, got " + names);
            for (var tool : tools) {
                require(Boolean.FALSE.equals(tool.inputSchema().get("additionalProperties")),
                        tool.name() + " schema must reject additional properties");
            }
            System.out.println("MCP_TOOLS_OK: " + names);

            Map<String, Object> status = successful(client.callTool(call("get_arena_status", Map.of())), "get_arena_status");
            require(Boolean.TRUE.equals(status.get("ready")), "Arena was not ready: " + status.get("blockingPrerequisites"));
            require(((Number) status.get("botCount")).intValue() == 2, "Arena status bot count was not two");
            require(status.containsKey("websocketUrl"), "Arena status omitted websocketUrl");
            System.out.println("MCP_ARENA_STATUS_OK: active=" + status.get("battleActive")
                    + " websocketUrl=" + status.get("websocketUrl"));

            Map<String, Object> listed = successful(client.callTool(call("list_bots", Map.of())), "list_bots");
            List<Map<String, Object>> bots = maps(listed.get("bots"), "bots");
            require(bots.size() == 2, "list_bots did not return exactly two bots");
            Set<String> listedIds = new HashSet<>();
            for (Map<String, Object> bot : bots) {
                listedIds.add(string(bot, "id"));
                require("VALID".equals(string(bot, "validationStatus")), "A bundled bot was invalid");
                require(!Path.of(string(bot, "directory")).isAbsolute(), "A bot directory was absolute");
            }
            require(listedIds.equals(Set.of("kiro-bot", "sample-opponent")), "Unexpected registered identities");
            System.out.println("MCP_LIST_BOTS_OK: " + listedIds.stream().sorted().toList());

            Map<String, Object> inspected = successful(client.callTool(call("inspect_bot", Map.of("botId", "kiro-bot"))), "inspect_bot");
            require("bots/kiro-bot/src/main/java/dev/kiro/royale/bots/KiroBot.java".equals(
                    string(inspected, "primaryEditableSource")), "inspect_bot primary source mismatch");
            require(!Path.of(string(inspected, "primaryEditableSource")).isAbsolute(), "inspect_bot exposed an absolute source path");
            System.out.println("MCP_INSPECT_BOT_OK: primaryEditableSource=" + inspected.get("primaryEditableSource"));

            long battleStarted = System.nanoTime();
            Map<String, Object> battleArguments = new LinkedHashMap<>();
            battleArguments.put("botIds", List.of("kiro-bot", "sample-opponent"));
            if (showBattle) battleArguments.put("showBattle", true);
            Map<String, Object> battle = successful(client.callTool(call("run_battle", battleArguments)), "run_battle");
            long battleMillis = Duration.ofNanos(System.nanoTime() - battleStarted).toMillis();
            require(Boolean.TRUE.equals(battle.get("success")), "Battle success flag was not true");
            require("OFFICIAL_BATTLE_RUNNER_COMPLETION".equals(string(battle, "provenance")), "Battle provenance was not official");
            require(((Number) battle.get("roundsPlayed")).intValue() == 1, "Battle did not play one round");
            List<Map<String, Object>> results = maps(battle.get("results"), "results");
            require(results.size() == 2, "Battle did not return exactly two results");
            require(((Number) results.get(0).get("rank")).intValue() == 1
                    && ((Number) results.get(1).get("rank")).intValue() == 2, "Results were not ordered by rank");
            Set<String> identities = new HashSet<>();
            for (Map<String, Object> result : results) {
                identities.add(string(result, "name") + "\u0000" + string(result, "version"));
                for (String field : List.of("totalScore", "survivalScore", "bulletDamage", "ramDamage", "firstPlaces", "roundsPlayed")) {
                    require(result.get(field) instanceof Number, "Result field is missing or non-numeric: " + field);
                }
            }
            require(identities.equals(Set.of("Kiro Bot\u00001.0", "Sample Opponent\u00001.0")), "Official result identities mismatch");
            require(Boolean.TRUE.equals(battle.get("cleanupComplete")), "Battle cleanup did not complete");
            require(Boolean.valueOf(showBattle).equals(battle.get("viewerRequested")),
                    "Battle did not report the requested viewer state");
            require(battle.get("sourceHashes") instanceof Map<?, ?>,
                    "Battle sourceHashes was not an object");
            Map<String, Object> sourceHashes = stringObjectMap(
                    (Map<?, ?>) battle.get("sourceHashes"), "sourceHashes");
            require(sourceHashes.keySet().equals(Set.of("kiro-bot", "sample-opponent")),
                    "Battle source hash identities were incomplete");
            require(sourceHashes.values().stream().allMatch(value -> value instanceof String hash
                            && hash.matches("[0-9a-f]{64}")),
                    "Battle source hashes were not SHA-256 values");
            require(battle.get("viewerConnected") instanceof Boolean,
                    "Battle did not report the viewer connection observation state");
            if (!showBattle) require(Boolean.FALSE.equals(battle.get("viewerConnected")),
                    "Headless battle incorrectly reported a viewer connection");
            for (Map<String, Object> process : maps(battle.get("processes"), "processes")) {
                require(Boolean.FALSE.equals(process.get("aliveAfterCleanup")), "An owned battle process survived cleanup");
            }
            String recordingPath = string(battle, "recordingPath");
            Path recording = paths.repositoryRoot().resolve(recordingPath).normalize();
            require(recording.startsWith(paths.runtimeRoot()) && Files.isRegularFile(recording) && Files.size(recording) > 0,
                    "Requested recording was missing, empty, or outside runtime");
            System.out.println("MCP_BATTLE_OK: durationMs=" + battleMillis + " rounds=1 provenance=" + battle.get("provenance"));
            for (Map<String, Object> result : results) {
                System.out.printf("MCP_RESULT: rank=%s name=%s version=%s totalScore=%s survivalScore=%s bulletDamage=%s ramDamage=%s firstPlaces=%s roundsPlayed=%s%n",
                        result.get("rank"), result.get("name"), result.get("version"), result.get("totalScore"),
                        result.get("survivalScore"), result.get("bulletDamage"), result.get("ramDamage"),
                        result.get("firstPlaces"), result.get("roundsPlayed"));
            }
            System.out.println("MCP_RECORDING_OK: path=" + recordingPath + " bytes=" + Files.size(recording));
            System.out.println("MCP_CLEANUP_OK: ownedProcesses=" + maps(battle.get("processes"), "processes").size());
            System.out.println("MCP_VIEWER_OK: requested=" + battle.get("viewerRequested")
                    + " connected=" + battle.get("viewerConnected"));
            System.out.println("MCP_SOURCE_HASHES_OK: " + sourceHashes);
            System.out.println("MCP_PROTOCOL_STDOUT_VALID: official client parsed handshake, discovery, and four tool responses");
        }
        long proofMillis = Duration.ofNanos(System.nanoTime() - proofStarted).toMillis();
        List<String> diagnostics = stderr.snapshot();
        require(diagnostics.stream().noneMatch(line -> line.contains("Exception") || line.contains("ERROR")),
                "Server stderr contained an error: " + diagnostics);
        System.out.println("MCP_SERVER_STDERR: " + (diagnostics.isEmpty() ? "none" : String.join(" | ", diagnostics)));
        System.out.println("MCP_PROOF_DURATION_MS: " + proofMillis);
        System.out.println("STAGE2_MCP_PROOF_OK");
    }

    private static CallToolRequest call(String name, Map<String, Object> arguments) {
        return CallToolRequest.builder(name).arguments(arguments).build();
    }

    private static Map<String, Object> successful(CallToolResult result, String tool) {
        require(result != null && !Boolean.TRUE.equals(result.isError()), tool + " returned an MCP tool error: "
                + (result == null ? "null result" : result.structuredContent()));
        boolean hasSummary = result.content() != null && result.content().stream()
                .filter(TextContent.class::isInstance).map(TextContent.class::cast)
                .anyMatch(content -> content.text() != null && !content.text().isBlank());
        require(hasSummary, tool + " did not return concise text content");
        require(result.structuredContent() instanceof Map<?, ?>, tool + " did not return structured content");
        return stringObjectMap((Map<?, ?>) result.structuredContent(), tool);
    }

    private static List<Map<String, Object>> maps(Object value, String label) {
        require(value instanceof List<?>, label + " was not a list");
        List<Map<String, Object>> converted = new ArrayList<>();
        for (Object item : (List<?>) value) {
            require(item instanceof Map<?, ?>, label + " contained a non-object");
            converted.add(stringObjectMap((Map<?, ?>) item, label));
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> stringObjectMap(Map<?, ?> value, String label) {
        Map<String, Object> converted = new LinkedHashMap<>();
        for (var entry : value.entrySet()) {
            require(entry.getKey() instanceof String, label + " contained a non-string key");
            converted.put((String) entry.getKey(), entry.getValue());
        }
        return converted;
    }

    private static String string(Map<String, Object> map, String key) {
        require(map.get(key) instanceof String, "Missing string field: " + key);
        return (String) map.get(key);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
