package com.jdte.common.items;

public final class TimeMultitoolMiningPolicy {
    private TimeMultitoolMiningPolicy() {
    }

    public static Decision decide(TimeMultitoolSpeedMode selectedMode, int storedFluid,
                                  int storedEnergy, int energyCost) {
        if (storedEnergy < Math.max(0, energyCost)) {
            return new Decision(false, 1, 0);
        }
        TimeMultitoolSpeedMode effectiveMode = selectedMode.effectiveForFluid(Math.max(0, storedFluid));
        return new Decision(true, effectiveMode.multiplier(), effectiveMode.fluidCostPerBlock());
    }

    public static Decision decideBatch(TimeMultitoolSpeedMode selectedMode, int storedFluid,
                                       int storedEnergy, int energyCost, int targetCount) {
        Decision singleBlock = decide(selectedMode, storedFluid, storedEnergy, energyCost);
        if (!singleBlock.powered() || singleBlock.timeFluidCost() == 0) {
            return singleBlock;
        }

        long requiredFluid = (long) singleBlock.timeFluidCost() * Math.max(0, targetCount);
        if (storedFluid < requiredFluid) {
            return new Decision(true, 1, 0);
        }
        return singleBlock;
    }

    public record Decision(boolean powered, int speedMultiplier, int timeFluidCost) {
    }
}
