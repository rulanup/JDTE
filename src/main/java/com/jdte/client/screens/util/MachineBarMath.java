package com.jdte.client.screens.util;

/** Pure bar-height arithmetic shared by machine screens; no client classes so it stays unit-testable. */
public final class MachineBarMath {
    private MachineBarMath() {
    }

    /** Scales a stored amount to pixels, clamping to the capacity and to the pixel height. */
    public static int scaleClamped(long value, long capacity, int pixels) {
        if (value <= 0L || capacity <= 0L || pixels <= 0) return 0;
        long clampedValue = Math.min(value, capacity);
        long scaled = clampedValue * (long) pixels / capacity;
        return (int) Math.max(0L, Math.min((long) pixels, scaled));
    }

    /**
     * Same scale, but any non-empty amount keeps at least one pixel visible. A 4,000 mB fill in a
     * 400,000 mB tank truncates to zero pixels with plain integer arithmetic, which made filled
     * fluid senders and mixers look empty.
     */
    public static int scaleWithMinimum(long value, long capacity, int pixels) {
        int scaled = scaleClamped(value, capacity, pixels);
        if (scaled == 0 && value > 0L && capacity > 0L && pixels > 0) return 1;
        return scaled;
    }
}
