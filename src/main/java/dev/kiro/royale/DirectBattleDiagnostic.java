package dev.kiro.royale;

import java.util.List;
import static dev.kiro.royale.Models.*;

/** Minimal non-MCP diagnostic entry for the fixed two-bot direct battle. */
public final class DirectBattleDiagnostic {
    private DirectBattleDiagnostic() {}

    public static void main(String[] args) {
        int exit = run(args);
        if (exit != 0) System.exit(exit);
    }

    static int run(String[] args) {
        if (!List.of(args).equals(List.of("direct-battle", "--record"))) {
            System.err.println("Usage: direct-battle --record");
            return 64;
        }
        try {
            RepositoryPaths paths = RepositoryPaths.locate();
            try (BattleService service = new BattleService(paths)) {
                System.out.println("DIRECT_BATTLE_MODE: official Battle Runner, no MCP");
                for (var bot : service.registry().list()) {
                    System.out.printf("BOT_VALIDATION: id=%s name=%s version=%s status=%s source=%s%n",
                            bot.id().value(), bot.name(), bot.version(), bot.validationStatus(), bot.sourceLabel());
                }
                var request = new BattleRequest(List.of(new BotId("kiro-bot"), new BotId("sample-opponent")), 1, true);
                BattleOutcome outcome = service.run(request);
                if (outcome instanceof BattleFailure failure) {
                    System.err.printf("DIRECT_BATTLE_FAILED: code=%s message=%s%n", failure.code(), failure.message());
                    failure.diagnostics().forEach(line -> System.err.println("DIAGNOSTIC: " + line));
                    return 1;
                }
                BattleSuccess success = (BattleSuccess) outcome;
                System.out.println("OFFICIAL_COMPLETION_EVENT: observed");
                System.out.println("WEBSOCKET_ENDPOINT: " + success.websocketUrl());
                success.diagnostics().forEach(line -> System.out.println("LIFECYCLE: " + line));
                for (var process : success.processes()) {
                    System.out.printf("OWNED_PROCESS: pid=%d role=%s aliveAfterCleanup=%s command=%s%n",
                            process.pid(), process.role(), process.aliveAfterCleanup(), process.command());
                }
                for (var result : success.results()) {
                    System.out.printf("RESULT: rank=%d name=%s version=%s totalScore=%d survivalScore=%d bulletDamage=%d ramDamage=%d firstPlaces=%d roundsPlayed=%d%n",
                            result.rank(), result.name(), result.version(), result.totalScore(), result.survivalScore(),
                            result.bulletDamage(), result.ramDamage(), result.firstPlaces(), result.roundsPlayed());
                }
                System.out.println("RECORDING: " + success.recordingPath().orElse("none"));
                System.out.println("CLEANUP_COMPLETE: " + success.cleanupComplete());
                System.out.println("DIRECT_BATTLE_OK");
                return 0;
            }
        } catch (Exception exception) {
            System.err.println("DIRECT_BATTLE_FAILED: startup or repository prerequisite failed");
            return 1;
        }
    }
}
