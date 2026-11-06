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

    public static int instantCost(int base, float hardness, int efficiency) {
        if (!Float.isFinite(hardness)) return Integer.MAX_VALUE;
        double raw=Math.max(Math.max(0,base),(double)Math.max(0,base)*Math.floor(Math.max(0,hardness)));
        double discount=Math.min(1,Math.max(0,efficiency)*0.1);
        return (int)Math.max(0,Math.min(Integer.MAX_VALUE,raw*(1-discount)));
    }

    public record Decision(boolean powered, int speedMultiplier, int timeFluidCost) {
    }
}
