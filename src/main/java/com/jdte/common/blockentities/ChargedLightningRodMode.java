package com.jdte.common.blockentities;

public enum ChargedLightningRodMode {
    NORMAL(10_000, 1, 200),
    OVERCLOCK(40_000, 5, 1),
    ULTIMATE(100_000, 50, 1);

    public final int energyPerStrike;
    public final int strikesPerCycle;
    public final int intervalTicks;

    public int affordableStrikes(int energy){return Math.min(strikesPerCycle,Math.max(0,energy)/energyPerStrike);}
    public boolean canBegin(int cooldown){return this!=NORMAL||cooldown<=0;}

    ChargedLightningRodMode(int energyPerStrike, int strikesPerCycle, int intervalTicks) {
        this.energyPerStrike = energyPerStrike;
        this.strikesPerCycle = strikesPerCycle;
        this.intervalTicks = intervalTicks;
    }
}
