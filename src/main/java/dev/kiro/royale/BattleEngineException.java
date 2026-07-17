package dev.kiro.royale;

/** Typed engine-boundary failure with fixed client-safe code and message. */
public final class BattleEngineException extends Exception {
    public enum Kind {
        SERVER_CONNECTION_FAILED("The local official server could not become ready"),
        BOT_START_FAILED("A selected bundled bot could not start"),
        BATTLE_ABORTED("The official battle did not complete successfully"),
        RECORDING_FAILED("The required official recording was not verified"),
        CLEANUP_FAILED("Owned battle processes did not stop");

        private final String safeMessage;
        Kind(String safeMessage) { this.safeMessage = safeMessage; }
        String safeMessage() { return safeMessage; }
    }

    private final Kind kind;

    public BattleEngineException(Kind kind, Throwable cause) {
        super(kind.name(), cause);
        this.kind = java.util.Objects.requireNonNull(kind, "kind");
    }

    public Kind kind() { return kind; }
    public String safeCode() { return kind.name(); }
    public String safeMessage() { return kind.safeMessage(); }
}
