package com.jdte.common.blockentities;

public final class TimeAcceleratorTiming {
    private TimeAcceleratorTiming() {
    }

    public static int durationTicks(int seconds) {
        long ticks = Math.max(1L, seconds) * 20L;
        return ticks >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
    }

    public static int workTicks(int effectiveMultiplier, int durationSeconds) {
        long work = (long) Math.max(1, effectiveMultiplier) * durationTicks(durationSeconds);
        return work >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) work;
    }
}
