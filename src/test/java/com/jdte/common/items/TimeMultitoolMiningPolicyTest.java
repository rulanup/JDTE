package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeMultitoolMiningPolicyTest {
    @Test
    void noEnergyDisablesTheTool() {
        TimeMultitoolMiningPolicy.Decision decision = TimeMultitoolMiningPolicy.decide(
                TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR, 10_000, 49, 50);

        assertFalse(decision.powered());
        assertEquals(1, decision.speedMultiplier());
        assertEquals(0, decision.timeFluidCost());
    }

    @Test
    void enoughResourcesEnableTheSelectedSpeedAndExactFluidCost() {
        TimeMultitoolMiningPolicy.Decision decision = TimeMultitoolMiningPolicy.decide(
                TimeMultitoolSpeedMode.TWO_FIFTY_SIX, 256, 50, 50);

        assertTrue(decision.powered());
        assertEquals(256, decision.speedMultiplier());
        assertEquals(256, decision.timeFluidCost());
    }

    @Test
    void insufficientFluidKeepsTheToolPoweredButFallsBackToOneX() {
        TimeMultitoolMiningPolicy.Decision decision = TimeMultitoolMiningPolicy.decide(
                TimeMultitoolSpeedMode.SIXTEEN, 15, 50, 50);

        assertTrue(decision.powered());
        assertEquals(1, decision.speedMultiplier());
        assertEquals(0, decision.timeFluidCost());
    }

    @Test
    void aMultiBlockOperationNeverPartiallyPaysForTheSelectedSpeed() {
        TimeMultitoolMiningPolicy.Decision insufficient = TimeMultitoolMiningPolicy.decideBatch(
                TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR, 1_024, 50, 50, 2);
        assertTrue(insufficient.powered());
        assertEquals(1, insufficient.speedMultiplier());
        assertEquals(0, insufficient.timeFluidCost());

        TimeMultitoolMiningPolicy.Decision exact = TimeMultitoolMiningPolicy.decideBatch(
                TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR, 2_048, 50, 50, 2);
        assertEquals(1_024, exact.speedMultiplier());
        assertEquals(1_024, exact.timeFluidCost());
    }
    @Test void overflowCannotCreateCheapAccelerationOrPartialBatchPayment() {
        assertEquals(Integer.MAX_VALUE, TimeMultitoolMiningPolicy.instantCost(500, Float.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, TimeMultitoolMiningPolicy.instantCost(500, Float.NaN, 0));
        assertEquals(500, TimeMultitoolMiningPolicy.instantCost(500, 1, 0));
        assertEquals(250, TimeMultitoolMiningPolicy.instantCost(500, 1, 5));
        assertEquals(1, TimeMultitoolMiningPolicy.decideBatch(TimeMultitoolSpeedMode.ONE_THOUSAND_TWENTY_FOUR, Integer.MAX_VALUE, Integer.MAX_VALUE, 50, Integer.MAX_VALUE).speedMultiplier());
    }
}
