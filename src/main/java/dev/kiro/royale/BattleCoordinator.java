package dev.kiro.royale;

import java.util.concurrent.atomic.AtomicBoolean;

/** Atomic no-queue ownership gate for the single direct/production battle path. */
public final class BattleCoordinator {
    private final AtomicBoolean active = new AtomicBoolean();

    public Lease tryAcquire() {
        return active.compareAndSet(false, true) ? new Lease() : null;
    }

    public boolean isActive() { return active.get(); }

    public final class Lease implements AutoCloseable {
        private final AtomicBoolean closed = new AtomicBoolean();
        private Lease() {}
        @Override public void close() {
            if (closed.compareAndSet(false, true)) active.set(false);
        }
    }
}
