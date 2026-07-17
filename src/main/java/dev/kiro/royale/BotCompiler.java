package dev.kiro.royale;

import java.util.List;
import java.util.Map;
import static dev.kiro.royale.Models.BotId;

/** Compiles only application-registered Bot sources before a battle. */
interface BotCompiler {
    Map<String, String> compile(List<BotId> botIds) throws BotCompilationException;

    static BotCompiler noOp() {
        return botIds -> botIds.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                BotId::value, ignored -> "unavailable-in-test-boundary"));
    }
}

final class BotCompilationException extends Exception {
    private final List<String> diagnostics;

    BotCompilationException(List<String> diagnostics) {
        super("Registered Bot compilation failed");
        this.diagnostics = List.copyOf(diagnostics);
    }

    List<String> diagnostics() { return diagnostics; }
}
