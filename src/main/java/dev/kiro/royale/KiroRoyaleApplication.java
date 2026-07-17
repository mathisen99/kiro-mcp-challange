package dev.kiro.royale;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/** Trusted runtime-mode entry point. MCP mode never writes ordinary output to stdout. */
public final class KiroRoyaleApplication {
    private KiroRoyaleApplication() {}

    public static void main(String[] args) {
        if (List.of(args).equals(List.of("direct-battle", "--record"))) {
            int exit = DirectBattleDiagnostic.run(args);
            if (exit != 0) System.exit(exit);
            return;
        }
        if (!List.of(args).equals(List.of("mcp-stdio"))) {
            System.err.println("Usage: kiro-royale mcp-stdio | direct-battle --record");
            System.exit(64);
            return;
        }
        runMcpStdio();
    }

    private static void runMcpStdio() {
        try {
            RepositoryPaths paths = RepositoryPaths.locate();
            BattleService battleService = new BattleService(paths);
            var mapper = McpJsonDefaults.getMapper();
            var transport = new StdioServerTransportProvider(mapper, System.in, System.out);
            McpSyncServer server = McpServer.sync(transport)
                    .serverInfo("kiro-royale", McpToolAdapter.APPLICATION_VERSION)
                    .instructions("Use stable registered bot IDs; run_battle is synchronous and executes local reviewed bot code.")
                    .requestTimeout(Duration.ofSeconds(150))
                    .capabilities(ServerCapabilities.builder().tools(false).build())
                    .strictToolNameValidation(true)
                    .validateToolInputs(true)
                    .tools(new McpToolAdapter(paths, battleService).specifications())
                    .build();
            AtomicBoolean closed = new AtomicBoolean();
            Runnable close = () -> {
                if (closed.compareAndSet(false, true)) {
                    battleService.close();
                    server.close();
                }
            };
            Runtime.getRuntime().addShutdownHook(new Thread(close, "kiro-royale-mcp-shutdown"));
            System.err.println("Kiro Royale MCP stdio server ready; stdout is protocol-only");
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                close.run();
            }
        } catch (Exception exception) {
            System.err.println("Kiro Royale MCP startup failed: repository or local prerequisite unavailable");
            System.exit(1);
        }
    }
}
