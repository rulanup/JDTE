package com.jdte.common.utils;

/** Short labels for stacks that cannot fit a full decimal count inside a 16px slot. */
public final class StackCountLabels {
    private StackCountLabels() { }
    public static String compact(int count) {
        int unit = count >= 1_000_000 ? 1_000_000 : count >= 1_000 ? 1_000 : 1;
        if (unit == 1) return Integer.toString(count);
        long tenths = (long) count * 10 / unit;
        return (tenths % 10 == 0 ? Long.toString(tenths / 10) : tenths / 10 + "." + tenths % 10)
                + (unit == 1_000_000 ? "M" : "K");
    }
}
