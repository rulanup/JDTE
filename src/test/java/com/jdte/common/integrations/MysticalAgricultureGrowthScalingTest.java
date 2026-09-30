package com.jdte.common.integrations;

import com.jdte.setup.JDTEConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MysticalAgricultureGrowthScalingTest {

    @Test
    void defaultConfigHasExpectedTierGrowthPercent() {
        assertEquals(50, JDTEConfig.COMMON.greenhouseMysticalTierGrowthPercent.get());
        assertEquals(4096, JDTEConfig.COMMON.greenhouseDefaultGrowthWork.get());
    }

    @Test
    void tierScalingCalculatesExpectedWorkWindow() {
        int baseWork = 4096;
        int percent = 50;

        assertEquals(4096, calculateWork(baseWork, 1, percent));
        assertEquals(6144, calculateWork(baseWork, 2, percent));
        assertEquals(8192, calculateWork(baseWork, 3, percent));
        assertEquals(10240, calculateWork(baseWork, 4, percent));
        assertEquals(12288, calculateWork(baseWork, 5, percent));
        assertEquals(14336, calculateWork(baseWork, 6, percent));
    }

    @Test
    void zeroPercentKeepsFlatBaselineAcrossAllTiers() {
        int baseWork = 4096;
        int percent = 0;

        for (int tier = 1; tier <= 6; tier++) {
            assertEquals(4096, calculateWork(baseWork, tier, percent));
        }
    }

    @Test
    void twentyFivePercentCalculatesQuarterIncrements() {
        int baseWork = 4096;
        int percent = 25;

        assertEquals(4096, calculateWork(baseWork, 1, percent));
        assertEquals(5120, calculateWork(baseWork, 2, percent));
        assertEquals(6144, calculateWork(baseWork, 3, percent));
        assertEquals(7168, calculateWork(baseWork, 4, percent));
        assertEquals(8192, calculateWork(baseWork, 5, percent));
        assertEquals(9216, calculateWork(baseWork, 6, percent));
    }

    @Test
    void invalidateCachesExecutesSafely() {
        assertDoesNotThrow(MysticalAgricultureGreenhouseIntegration::invalidateCaches);
    }

    private static int calculateWork(int baseWork, int tier, int percent) {
        long scaled = (long) baseWork * (100L + (long) (Math.max(1, tier) - 1) * percent) / 100L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, scaled));
    }
}
