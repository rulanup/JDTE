package com.jdte.common.items;

import java.util.Locale;

/** Pure data and arithmetic contracts for the Ultimate Time Wand. */
public final class UltimateTimeWandData {
    public static final int MAX_EXPONENT = 10;

    private UltimateTimeWandData() {
    }

    public enum Mode {
        NORMAL("normal", 1),
        X2("x2", 2),
        X4("x4", 4),
        MAX("max", 10);

        private final String serializedName;
        private final int step;

        Mode(String serializedName, int step) {
            this.serializedName = serializedName;
            this.step = step;
        }

        public Mode next() {
            Mode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }

        public int step() {
            return step;
        }

        public String serializedName() {
            return serializedName;
        }

        public static Mode fromOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NORMAL;
        }

        public static Mode fromName(String name) {
            if (name == null) {
                return NORMAL;
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            for (Mode mode : values()) {
                if (mode.serializedName.equals(normalized)) {
                    return mode;
                }
            }
            return NORMAL;
        }
    }

    public static int multiplierForExponent(int exponent) {
        return 1 << clampExponent(exponent);
    }

    public static int addStep(int exponent, Mode mode) {
        Mode safeMode = mode == null ? Mode.NORMAL : mode;
        long candidate = (long) Math.max(0, exponent) + safeMode.step();
        return (int) Math.min(MAX_EXPONENT, candidate);
    }

    public static int saturatingEnergyCost(int multiplier, int baseCost) {
        long product = Math.max(0L, (long) multiplier) * Math.max(0L, (long) baseCost);
        return product >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }

    public static FluidSettlement settleFluid(double pendingCost, double newCost) {
        double total = Math.max(0.0D, pendingCost) + Math.max(0.0D, newCost);
        int drainMb = (int) Math.min(Integer.MAX_VALUE, Math.floor(total));
        return new FluidSettlement(drainMb, total - drainMb);
    }

    private static int clampExponent(int exponent) {
        return Math.max(0, Math.min(MAX_EXPONENT, exponent));
    }

    public record FluidSettlement(int drainMb, double remainingCost) {
    }
}
