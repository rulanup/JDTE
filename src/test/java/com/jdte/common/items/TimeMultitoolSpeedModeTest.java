package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeMultitoolSpeedModeTest {
    @Test
    void cyclesThroughTheSixConfiguredSpeeds() {
        TimeMultitoolSpeedMode mode = TimeMultitoolSpeedMode.ONE;
        int[] expected = {2, 4, 16, 256, 1_024, 1};

        for (int multiplier : expected) {
            mode = mode.next();
            assertEquals(multiplier, mode.multiplier());
        }
    }

    @Test
    void chargesOneMillibucketPerSelectedMultiplierExceptAtOneX() {
        assertEquals(0, TimeMultitoolSpeedMode.ONE.fluidCostPerBlock());
        assertEquals(2, TimeMultitoolSpeedMode.TWO.fluidCostPerBlock());
        assertEquals(4, TimeMultitoolSpeedMode.FOUR.fluidCostPerBlock());
        assertEquals(16, TimeMultitoolSpeedMode.SIXTEEN.fluidCostPerBlock());
        assertEquals(256, TimeMultitoolSpeedMode.TWO_FIFTY_SIX.fluidCostPerBlock());
        assertEquals(1_024, TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR.fluidCostPerBlock());
    }

    @Test
    void insufficientTimeFluidFallsBackToOneXWithoutPartialPayment() {
        assertEquals(TimeMultitoolSpeedMode.ONE,
                TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR.effectiveForFluid(1_023));
        assertEquals(TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR,
                TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR.effectiveForFluid(1_024));
    }

    @Test
    void invalidStoredModeFallsBackToOneX() {
        assertEquals(TimeMultitoolSpeedMode.ONE, TimeMultitoolSpeedMode.fromStoredIndex(-1));
        assertEquals(TimeMultitoolSpeedMode.ONE, TimeMultitoolSpeedMode.fromStoredIndex(99));
    }
}
