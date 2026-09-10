package com.jdte.common.blockentities;

public final class TimeAcceleratorTiming {
    private TimeAcceleratorTiming() {
    }

    public static int durationTicks(int seconds) {
        long ticks = Math.max(1L, seconds) * 20L;
        return ticks >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
    }

    public static int additionalCycles(int nominalMultiplier) {
        return Math.max(0, nominalMultiplier - 1);
    }

    public static int batchWorkTicks(int multiplier, int durationSeconds) {
        long work = (long) Math.max(1, multiplier) * durationTicks(durationSeconds);
        return work >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) work;
    }

    public static long pendingWindowCycles(int nominalMultiplier, int durationSeconds,
                                           long configuredMaximum) {
        long cycles = additionalCycles(nominalMultiplier);
        long duration = durationTicks(durationSeconds);
        long window = cycles > Long.MAX_VALUE / duration
                ? Long.MAX_VALUE
                : cycles * duration;
        return Math.min(Math.max(0L, configuredMaximum), window);
    }

}
