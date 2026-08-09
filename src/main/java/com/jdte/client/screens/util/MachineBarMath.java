package com.jdte.client.screens.util;

public final class MachineBarMath {
    private MachineBarMath() {
    }

    public static int scaleClamped(long value, long capacity, int pixels) {
        if (value <= 0L || capacity <= 0L || pixels <= 0) return 0;
        long clampedValue = Math.min(value, capacity);
        return (int) Math.clamp(clampedValue * pixels / capacity, 0L, pixels);
    }
}