package com.jdte.common.upgrades;

/** Whole fluid-fuel batches only: two mB purchase three times the native output. */
public final class GeneratorFuelBatch {
    public static final int FLUID_COST = 2;

    private GeneratorFuelBatch() {}

    public static int energyOutput(int nativeOutput) {
        long output = (long) nativeOutput * 3;
        return output > 0 && output <= Integer.MAX_VALUE ? (int) output : 0;
    }

    public static boolean canCommit(int availableFluid, int output, int acceptedEnergy) {
        return availableFluid >= FLUID_COST && output > 0 && acceptedEnergy == output;
    }
}
