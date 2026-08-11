package com.jdte.common.items;

import net.minecraft.core.BlockPos;

/**
 * Captures JDT's authoritative block-removal result only while the Time
 * Multitool is executing one helper call on the current server thread.
 */
public final class TimeMultitoolBreakTracker {
    private static final ThreadLocal<TrackedRemoval> ACTIVE = new ThreadLocal<>();

    private TimeMultitoolBreakTracker() {
    }

    public static void begin(BlockPos target) {
        ACTIVE.set(new TrackedRemoval(target.immutable(), false));
    }

    public static void recordRemoval(BlockPos target, boolean removed) {
        TrackedRemoval active = ACTIVE.get();
        if (removed && active != null && active.target().equals(target)) {
            ACTIVE.set(new TrackedRemoval(active.target(), true));
        }
    }

    public static boolean finish() {
        TrackedRemoval active = ACTIVE.get();
        ACTIVE.remove();
        return active != null && active.removed();
    }

    private record TrackedRemoval(BlockPos target, boolean removed) {
    }
}
