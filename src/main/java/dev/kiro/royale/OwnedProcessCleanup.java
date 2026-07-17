package dev.kiro.royale;

import java.time.Duration;
import java.util.Collection;

/** Bounded, idempotent cleanup support for application-owned process handles only. */
final class OwnedProcessCleanup {
    private OwnedProcessCleanup() {}

    static boolean terminate(Collection<ProcessHandle> owned, Duration grace) {
        if (grace == null || grace.isZero() || grace.isNegative()) {
            throw new IllegalArgumentException("Cleanup grace must be positive");
        }
        waitUntilStopped(owned, grace);
        owned.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroy);
        waitUntilStopped(owned, grace);
        owned.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        waitUntilStopped(owned, grace);
        return owned.stream().noneMatch(ProcessHandle::isAlive);
    }

    private static void waitUntilStopped(Collection<ProcessHandle> owned, Duration grace) {
        long deadline = System.nanoTime() + grace.toNanos();
        while (System.nanoTime() < deadline && owned.stream().anyMatch(ProcessHandle::isAlive)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
