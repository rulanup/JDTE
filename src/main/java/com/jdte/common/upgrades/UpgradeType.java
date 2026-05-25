package com.jdte.common.upgrades;

public enum UpgradeType {
    CAPACITY("capacity", 3),
    OVERCLOCK("overclock", 1),
    UNDERCLOCK("underclock", 1),
    FLUID("fluid", 3),
    FLUID_STORAGE("fluid_storage", 1),
    GENERATOR("generator", 1),
    RANGE("range", 2),
    FILTER("filter", 2);

    private final String serializedName;
    private final int maxPerMachine;

    UpgradeType(String serializedName, int maxPerMachine) {
        this.serializedName = serializedName;
        this.maxPerMachine = maxPerMachine;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public int getMaxPerMachine() {
        return maxPerMachine;
    }

    public boolean isSpeedUpgrade() {
        return this == OVERCLOCK || this == UNDERCLOCK;
    }
}
