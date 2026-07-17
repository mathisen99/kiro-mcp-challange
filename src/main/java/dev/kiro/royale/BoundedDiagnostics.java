package dev.kiro.royale;

import java.util.ArrayDeque;
import java.util.List;

/** Thread-safe bounded diagnostic tail that never writes to stdout. */
public final class BoundedDiagnostics {
    private static final String TRUNCATED = "[earlier diagnostics truncated]";
    private final int maxLines;
    private final ArrayDeque<String> lines = new ArrayDeque<>();
    private boolean truncated;

    public BoundedDiagnostics(int maxLines) {
        if (maxLines < 1) throw new IllegalArgumentException("Diagnostic limit must be positive");
        this.maxLines = maxLines;
    }

    public synchronized void add(String line) {
        String safe = line == null ? "" : line.replaceAll("[\\r\\n]+", " ");
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
