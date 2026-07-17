package dev.kiro.royale;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Thread-safe bounded and redacted diagnostic tail that never writes to stdout. */
public final class BoundedDiagnostics {
    private static final String TRUNCATED = "[earlier diagnostics truncated]";
    private static final String REDACTED = "[redacted]";
    private final int maxLines;
    private final int maxCharactersPerLine;
    private final List<String> secrets;
    private final ArrayDeque<String> lines = new ArrayDeque<>();
    private boolean truncated;

    public BoundedDiagnostics(int maxLines) {
        this(maxLines, 4096, Set.of());
    }

    public BoundedDiagnostics(int maxLines, int maxCharactersPerLine, Set<String> secrets) {
        if (maxLines < 1 || maxCharactersPerLine < 1) {
            throw new IllegalArgumentException("Diagnostic limits must be positive");
        }
        this.maxLines = maxLines;
        this.maxCharactersPerLine = maxCharactersPerLine;
        this.secrets = secrets == null ? List.of() : secrets.stream()
                .filter(value -> value != null && !value.isEmpty())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    public synchronized void add(String line) {
        String safe = line == null ? "" : line.replaceAll("[\\r\\n]+", " ");
        for (String secret : secrets) safe = safe.replace(secret, REDACTED);
        if (safe.length() > maxCharactersPerLine) {
            safe = safe.substring(0, maxCharactersPerLine) + "...[truncated]";
            truncated = true;
        }
        if (lines.size() == maxLines) {
            lines.removeFirst();
            truncated = true;
        }
        lines.addLast(safe);
    }

    public synchronized List<String> snapshot() {
        var result = new java.util.ArrayList<String>();
        if (truncated) result.add(TRUNCATED);
        result.addAll(lines);
        return List.copyOf(result);
    }
}
