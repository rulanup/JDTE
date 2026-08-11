package com.jdte.common.items;

public enum TimeMultitoolSpeedMode {
    ONE(1),
    TWO(2),
    FOUR(4),
    SIXTEEN(16),
    TWO_FIFTY_SIX(256),
    ONE_THOUSAND_TWENTY_FOUR(1_024);

    private static final TimeMultitoolSpeedMode[] VALUES = values();
    private final int multiplier;

    TimeMultitoolSpeedMode(int multiplier) {
        this.multiplier = multiplier;
    }

    public int multiplier() {
        return multiplier;
    }

    public int fluidCostPerBlock() {
        return multiplier == 1 ? 0 : multiplier;
    }

    public TimeMultitoolSpeedMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public TimeMultitoolSpeedMode effectiveForFluid(int storedFluid) {
        return storedFluid >= fluidCostPerBlock() ? this : ONE;
    }

    public static TimeMultitoolSpeedMode fromStoredIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : ONE;
    }
}
