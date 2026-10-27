package com.jdte.common.blockentities;

import com.jdte.common.recipes.FluidMixerParallelLimit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidMixerParallelLimitTest {
    @Test
    void capsRegularMixingUpgradeAtEightBatches() {
        assertEquals(8, FluidMixerParallelLimit.apply(64, true, false));
        assertEquals(5, FluidMixerParallelLimit.apply(5, true, false));
    }

    @Test
    void ultimateMixingUpgradeAllowsTheIntegerMaximum() {
        assertEquals(Integer.MAX_VALUE,
                FluidMixerParallelLimit.apply(Integer.MAX_VALUE, true, true));
    }

    @Test
    void machinesWithoutMixingUpgradeStillProcessOneBatch() {
        assertEquals(1, FluidMixerParallelLimit.apply(64, false, false));
    }

    @Test
    void neverReturnsNegativeParallelism() {
        assertEquals(0, FluidMixerParallelLimit.apply(-1, true, false));
    }
}
