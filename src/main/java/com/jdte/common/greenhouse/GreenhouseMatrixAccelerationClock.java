package com.jdte.common.greenhouse;

/**
 * Accumulates virtual production ticks for the Greenhouse Matrix without
 * repeatedly ticking the complete controller block entity.
 */
public final class GreenhouseMatrixAccelerationClock {
    private long pendingTicks;

    public boolean add(int ticks) {
        if (ticks <= 0 || pendingTicks == Long.MAX_VALUE) {
            return false;
        }

        long addedTicks = ticks;
        pendingTicks = pendingTicks > Long.MAX_VALUE - addedTicks
                ? Long.MAX_VALUE
                : pendingTicks + addedTicks;
        return true;
    }

    public long takeCompleteTicks(int interval) {
        int safeInterval = Math.max(1, interval);
        long completeTicks = pendingTicks - pendingTicks % safeInterval;
        pendingTicks -= completeTicks;
        return completeTicks;
    }

    public long pendingTicks() {
        return pendingTicks;
    }

    public void restore(long ticks) {
        pendingTicks = Math.max(0L, ticks);
    }

    public void clear() {
        pendingTicks = 0L;
    }
}
