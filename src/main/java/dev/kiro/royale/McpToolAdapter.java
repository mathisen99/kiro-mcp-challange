package dev.kiro.royale;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static dev.kiro.royale.Models.*;

/** Strict MCP projection over the shared Stage 1 services. */
public final class McpToolAdapter {
    public static final String APPLICATION_VERSION = "0.1.0-SNAPSHOT";
    public static final String ROBOCODE_VERSION = "1.0.2";
    public static final List<String> TOOL_NAMES = List.of(
            "get_arena_status", "list_bots", "inspect_bot", "run_battle");

    private final RepositoryPaths paths;
    private final BattleService battleService;

    public McpToolAdapter(RepositoryPaths paths, BattleService battleService) {
        this.paths = paths;
        this.battleService = battleService;
    }

    public List<SyncToolSpecification> specifications() {
        return List.of(
                specification("get_arena_status", "Report local arena readiness without starting a battle",
                        emptyObjectSchema(), true, this::arenaStatus),
                specification("list_bots", "List the two reviewed bundled bots",
                        emptyObjectSchema(), true, this::listBots),
                specification("inspect_bot", "Inspect one registered bot by stable ID",
                        inspectSchema(), true, this::inspectBot),
                specification("run_battle", "Synchronously run two registered bots, optionally opening a verified live viewer before battle",
                        battleSchema(), false, this::runBattle));
    }

    private SyncToolSpecification specification(String name, String description, Map<String, Object> schema,
                                                  boolean readOnly,
                                                  java.util.function.Function<CallToolRequest, CallToolResult> handler) {
        Tool tool = Tool.builder(name)
                .description(description)
                .inputSchema(schema)
                .annotations(ToolAnnotations.builder().title(name).readOnlyHint(readOnly)
                        .destructiveHint(false).idempotentHint(readOnly).openWorldHint(false).build())
                .build();
        return SyncToolSpecification.builder().tool(tool)
                .callHandler((exchange, request) -> {
                    try {
                        return handler.apply(request);
                    } catch (IllegalArgumentException exception) {
                        return failure("INVALID_REQUEST", "The tool request did not match the strict contract", false);
                    } catch (Exception exception) {
                        System.err.println("MCP tool failure was sanitized: " + name);
                        return failure("INTERNAL_ERROR", "The tool could not complete; inspect local diagnostics", false);
                    }
                }).build();
    }

    private CallToolResult arenaStatus(CallToolRequest request) {
        requireExactKeys(arguments(request), Set.of());
        List<String> blockers = new ArrayList<>();
        for (BotDescriptor bot : battleService.registry().list()) {
            if (bot.validationStatus() != ValidationStatus.VALID) blockers.add(bot.id().value() + " is not valid");
        }
        if (!Files.isExecutable(Path.of("/usr/bin/systemd-socket-activate"))) {
            blockers.add("loopback socket activation is unavailable");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ready", blockers.isEmpty());
        data.put("applicationVersion", APPLICATION_VERSION);
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("robocodeVersion", ROBOCODE_VERSION);
        data.put("botRoot", "bots");
        data.put("botCount", battleService.registry().list().size());
        data.put("battleActive", battleService.battleActive());
        data.put("websocketUrl", battleService.readyWebsocketUrl().orElse(null));
        data.put("viewerUrl", LiveViewerLauncher.VIEWER_URL);
        data.put("viewerInstructions", "Set showBattle=true to open the trusted passive viewer on ws://localhost:7654 before the battle; use the recording fallback if desktop viewing is unavailable.");
        data.put("recordingDirectory", "runtime/recordings");
        data.put("blockingPrerequisites", List.copyOf(blockers));
        return success("Arena status: " + (blockers.isEmpty() ? "ready" : "blocked"), data);
    }

    private CallToolResult listBots(CallToolRequest request) {
        requireExactKeys(arguments(request), Set.of());
        List<Map<String, Object>> bots = battleService.registry().list().stream().map(this::botProjection).toList();
        return success("Found " + bots.size() + " registered bundled bots", Map.of("bots", bots));
    }

    private CallToolResult inspectBot(CallToolRequest request) {
        Map<String, Object> arguments = arguments(request);
        requireExactKeys(arguments, Set.of("botId"));
        Object rawId = arguments.get("botId");
        if (!(rawId instanceof String id) || id.isBlank()) {
            return failure("INVALID_REQUEST", "botId must be a registered bot ID string", false);
        }
        try {
            BotInspection inspection = battleService.registry().inspect(new BotId(id));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("bot", botProjection(inspection.descriptor()));
            data.put("sourceFiles", inspection.sourceFiles());
            data.put("primaryEditableSource", inspection.primaryEditableSource());
            data.put("build", Map.of("arguments", inspection.buildArguments()));
            data.put("run", Map.of("arguments", inspection.runArguments()));
            data.put("validationIssues", inspection.validationIssues());
            return success("Inspection for " + id + ": " + inspection.descriptor().validationStatus(), data);
        } catch (IllegalArgumentException exception) {
            return failure("UNKNOWN_BOT", "The requested bot ID is not registered", false);
        }
    }

    private CallToolResult runBattle(CallToolRequest request) {
        Map<String, Object> arguments = arguments(request);
        if (!Set.of("botIds", "rounds", "record", "showBattle").containsAll(arguments.keySet())) {
            return failure("INVALID_REQUEST", "Only botIds, rounds, record, and showBattle are accepted", false);
        }
        Object rawBotIds = arguments.get("botIds");
        if (!(rawBotIds instanceof List<?> values) || values.size() != 2
                || values.stream().anyMatch(value -> !(value instanceof String))) {
            return failure("INVALID_REQUEST", "Exactly two distinct registered bot IDs are required", false);
        }
        List<String> ids = values.stream().map(String.class::cast).toList();
        if (ids.get(0).equals(ids.get(1))) {
            return failure("INVALID_REQUEST", "Exactly two distinct registered bot IDs are required", false);
        }
        int rounds = 1;
        if (arguments.containsKey("rounds")) {
            Object rawRounds = arguments.get("rounds");
            if (!(rawRounds instanceof Byte || rawRounds instanceof Short || rawRounds instanceof Integer || rawRounds instanceof Long)) {
                return failure("INVALID_REQUEST", "Rounds must be an integer from 1 through 5", false);
            }
            long requested = ((Number) rawRounds).longValue();
            if (requested < 1 || requested > 5) {
                return failure("INVALID_REQUEST", "Rounds must be an integer from 1 through 5", false);
            }
            rounds = (int) requested;
        }
        boolean record = true;
        if (arguments.containsKey("record")) {
            if (!(arguments.get("record") instanceof Boolean requested)) {
                return failure("INVALID_REQUEST", "record must be a Boolean", false);
            }
            record = requested;
        }
        boolean showBattle = false;
        if (arguments.containsKey("showBattle")) {
            if (!(arguments.get("showBattle") instanceof Boolean requested)) {
                return failure("INVALID_REQUEST", "showBattle must be a Boolean", false);
            }
            showBattle = requested;
        }
        BattleOutcome outcome = battleService.run(new BattleRequest(
                ids.stream().map(BotId::new).toList(), rounds, record, showBattle));
        if (outcome instanceof BattleFailure failed) {
            return failure(failed.code(), failed.message(), "BATTLE_ACTIVE".equals(failed.code()));
        }
        BattleSuccess completed = (BattleSuccess) outcome;
        List<Map<String, Object>> results = completed.results().stream().map(result -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", result.rank());
            row.put("name", result.name());
            row.put("version", result.version());
            row.put("totalScore", result.totalScore());
            row.put("survivalScore", result.survivalScore());
            row.put("bulletDamage", result.bulletDamage());
            row.put("ramDamage", result.ramDamage());
            row.put("firstPlaces", result.firstPlaces());
            row.put("roundsPlayed", result.roundsPlayed());
            return row;
        }).toList();
        List<Map<String, Object>> processes = completed.processes().stream().map(process -> Map.<String, Object>of(
                "pid", process.pid(), "role", process.role(), "aliveAfterCleanup", process.aliveAfterCleanup())).toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("roundsPlayed", completed.roundsPlayed());
        data.put("results", results);
        data.put("recordingPath", completed.recordingPath().orElse(null));
        data.put("websocketUrl", completed.websocketUrl());
        data.put("provenance", completed.provenance().name());
        data.put("cleanupComplete", completed.cleanupComplete());
        data.put("viewerRequested", completed.viewerRequested());
        data.put("viewerConnected", completed.viewerConnected());
        data.put("processes", processes);
        return success(battleSummary(completed), data);
    }

    static String battleSummary(BattleSuccess completed) {
        StringBuilder summary = new StringBuilder()
                .append("Official battle completed with ").append(completed.results().size())
                .append(" ranked results\n")
                .append("provenance=").append(completed.provenance())
                .append(" roundsPlayed=").append(completed.roundsPlayed()).append('\n')
                .append("websocketUrl=").append(completed.websocketUrl()).append('\n')
                .append("recordingPath=").append(completed.recordingPath().orElse("none")).append('\n')
                .append("cleanupComplete=").append(completed.cleanupComplete()).append('\n')
                .append("viewerRequested=").append(completed.viewerRequested()).append('\n')
                .append("viewerConnected=").append(completed.viewerConnected()).append('\n')
                .append("results:");
        for (BattleResult result : completed.results()) {
            summary.append("\n#").append(result.rank()).append(' ')
                    .append(result.name()).append(' ').append(result.version())
                    .append(" totalScore=").append(result.totalScore())
                    .append(" survivalScore=").append(result.survivalScore())
                    .append(" bulletDamage=").append(result.bulletDamage())
                    .append(" ramDamage=").append(result.ramDamage())
                    .append(" firstPlaces=").append(result.firstPlaces())
                    .append(" roundsPlayed=").append(result.roundsPlayed());
        }
        return summary.toString();
    }

    private Map<String, Object> botProjection(BotDescriptor bot) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", bot.id().value());
        data.put("name", bot.name());
        data.put("version", bot.version());
        data.put("directory", bot.directory());
        data.put("language", bot.language());
        data.put("validationStatus", bot.validationStatus().name());
        data.put("sourceLabel", bot.sourceLabel());
        return data;
    }

    private static CallToolResult success(String summary, Map<String, Object> data) {
        return CallToolResult.builder().addTextContent(summary).structuredContent(data).isError(false).build();
    }

    private static CallToolResult failure(String code, String message, boolean retryable) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", false);
        data.put("code", code);
        data.put("message", message);
        data.put("retryable", retryable);
        return CallToolResult.builder().addTextContent(code + ": " + message)
                .structuredContent(data).isError(true).build();
    }

    private static Map<String, Object> arguments(CallToolRequest request) {
        return request.arguments() == null ? Map.of() : request.arguments();
    }

    private static void requireExactKeys(Map<String, Object> arguments, Set<String> expected) {
        if (!arguments.keySet().equals(expected)) throw new IllegalArgumentException("Unexpected tool arguments");
    }

    private static Map<String, Object> emptyObjectSchema() {
        return objectSchema(Map.of(), List.of());
    }

    private static Map<String, Object> inspectSchema() {
        return objectSchema(Map.of("botId", Map.of("type", "string", "enum", List.of("kiro-bot", "sample-opponent"))),
                List.of("botId"));
    }

    private static Map<String, Object> battleSchema() {
        Map<String, Object> botIds = Map.of("type", "array", "minItems", 2, "maxItems", 2,
                "uniqueItems", true, "items", Map.of("type", "string", "enum", List.of("kiro-bot", "sample-opponent")));
        return objectSchema(Map.of(
                "botIds", botIds,
                "rounds", Map.of("type", "integer", "minimum", 1, "maximum", 5, "default", 1),
                "record", Map.of("type", "boolean", "default", true),
                "showBattle", Map.of("type", "boolean", "default", false,
                        "description", "Open the trusted passive web viewer and verify it connects before starting")),
                List.of("botIds"));
    }

    private static Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }
}
