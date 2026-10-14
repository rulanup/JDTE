package com.jdte.common.blockentities;

public enum ChargedLightningRodMode {
    NORMAL(10_000, 1, 200),
    OVERCLOCK(40_000, 5, 1),
    ULTIMATE(100_000, 50, 1);

    public final int energyPerStrike;
    public final int strikesPerCycle;
    public final int intervalTicks;

    ChargedLightningRodMode(int energyPerStrike, int strikesPerCycle, int intervalTicks) {
        this.energyPerStrike = energyPerStrike;
        this.strikesPerCycle = strikesPerCycle;
        this.intervalTicks = intervalTicks;
    }
}
