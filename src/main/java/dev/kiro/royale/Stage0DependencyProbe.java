package dev.kiro.royale;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Stage 0-only dependency probe. It verifies documented types and member names without
 * creating a server, launching a bot, or running a battle.
 */
public final class Stage0DependencyProbe {
    private Stage0DependencyProbe() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        verify("MCP stdio transport", "io.modelcontextprotocol.server.transport.StdioServerTransportProvider");
        verify("MCP synchronous server", "io.modelcontextprotocol.server.McpSyncServer", "addTool", "close");
        verify("MCP server builder", "io.modelcontextprotocol.server.McpServer", "sync");
        verify("MCP sync tool specification", "io.modelcontextprotocol.server.McpServerFeatures$SyncToolSpecification");

        verify("Battle Runner", "dev.robocode.tankroyale.runner.BattleRunner",
                "create", "runBattle", "startBattleAsync", "close");
        verify("Battle setup", "dev.robocode.tankroyale.runner.BattleSetup", "classic");
        verify("Bot entry", "dev.robocode.tankroyale.runner.BotEntry", "of");
        verify("Battle results", "dev.robocode.tankroyale.runner.BattleResults",
                "getResults", "getNumberOfRounds");
        verify("Java Bot API", "dev.robocode.tankroyale.botapi.Bot", "start");

        verify("jqwik property API", "net.jqwik.api.Property");

        System.out.println("STAGE0_PROBE_OK: MCP, Battle Runner, Java Bot API, and jqwik APIs are available");
    }

    private static void verify(String label, String className, String... methodNames)
            throws ReflectiveOperationException {
        Class<?> type = Class.forName(className);
        for (String methodName : methodNames) {
            boolean present = Arrays.stream(type.getMethods())
                    .map(Method::getName)
                    .anyMatch(methodName::equals);
            if (!present) {
                throw new NoSuchMethodException(className + "." + methodName);
            }
        }
        Package typePackage = type.getPackage();
        String version = typePackage == null ? null : typePackage.getImplementationVersion();
        System.out.printf("verified: %s -> %s%s%n", label, className,
                version == null ? "" : " (implementation " + version + ")");
    }
}
