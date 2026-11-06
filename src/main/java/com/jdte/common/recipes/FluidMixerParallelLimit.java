package com.jdte.common.recipes;

public final class FluidMixerParallelLimit {
    public static final int REGULAR_UPGRADE_LIMIT = 8;
    public static final int ULTIMATE_UPGRADE_LIMIT = Integer.MAX_VALUE;

    private FluidMixerParallelLimit() {
    }

    public static int apply(int availableParallel, boolean hasRegularUpgrade, boolean hasUltimateUpgrade) {
        if (availableParallel <= 0) {
            return 0;
        }
        int upgradeLimit = hasUltimateUpgrade
                ? ULTIMATE_UPGRADE_LIMIT
                : hasRegularUpgrade ? REGULAR_UPGRADE_LIMIT : 1;
        return Math.min(availableParallel, upgradeLimit);
    }
}
