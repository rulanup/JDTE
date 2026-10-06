package com.jdte.common.upgrades;

public enum UpgradeType {
    CAPACITY("capacity", 3),
    OVERCLOCK("overclock", 1),
    UNDERCLOCK("underclock", 1),
    FLUID("fluid", 3),
    FLUID_STORAGE("fluid_storage", 1),
    GENERATOR("generator", 1),
    RANGE("range", 2),
    FILTER("filter", 2),
    CREATIVE("creative", 1),
    FORTUNE("fortune", 8),
    PRECISION("precision", 1),
    AE_ACCELERATION("ae_acceleration", 1),
    AE_CRAFTING_READ("ae_crafting_read", 1),
    AE_OUTPUT("ae_output", 1),
    ADVANCED_AE_OUTPUT("advanced_ae_output", 1),
    ESSENCE_CONVERSION("essence_conversion", 1),
    SEED_CONVERSION("seed_conversion", 1),
    ULTIMATE_OVERCLOCK("ultimate_overclock", 1),
    ULTIMATE_CAPACITY("ultimate_capacity", 1);

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
        return this == OVERCLOCK || this == UNDERCLOCK || this == ULTIMATE_OVERCLOCK;
    }

    public boolean isCreativeUpgrade() {
        return this == CREATIVE;
    }

    public boolean isAeOutputUpgrade() {
        return this == AE_OUTPUT || this == ADVANCED_AE_OUTPUT;
    }
}
