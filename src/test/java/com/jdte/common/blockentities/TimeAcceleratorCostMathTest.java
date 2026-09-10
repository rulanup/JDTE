package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorCostMathTest {

    @Test
    void fluidCostScalesWithVirtualTicks() {
        double oneSecond = TimeAcceleratorCostMath.fluidCost(20, 600, 1.0D, 1.0D);
        double threeSeconds = TimeAcceleratorCostMath.fluidCost(60, 600, 1.0D, 1.0D);
        assertEquals(oneSecond * 3.0D, threeSeconds, 1.0E-9D);
    }

    @Test
    void oneFrameCostUsesOnlyAdditionalCycles() {
        int workTicks = TimeAcceleratorTiming.additionalCycles(1024);
        assertEquals(1023, workTicks);
        assertEquals(102_300, TimeAcceleratorCostMath.energyCost(workTicks, 100));
    }

    @Test
    void fractionalFluidSettlementDoesNotLoseCost() {
        double pending = 0.0D;
        int drained = 0;
        for (int i = 0; i < 5; i++) {
            TimeAcceleratorCostMath.Settlement settlement =
                    TimeAcceleratorCostMath.settleFluid(pending, 0.2D);
            drained += settlement.drainMb();
            pending = settlement.remainingCost();
            assertTrue(pending >= 0.0D);
        }
        assertEquals(1, drained);
        assertEquals(0.0D, pending, 1.0E-9D);
    }

    @Test
    void energyCostUsesSaturatingIntegerMultiplication() {
        assertEquals(0, TimeAcceleratorCostMath.energyCost(0, 123));
        assertEquals(0, TimeAcceleratorCostMath.energyCost(123, 0));
        assertEquals(Integer.MAX_VALUE, TimeAcceleratorCostMath.energyCost(Integer.MAX_VALUE, 2));
    }

    @Test
    void settleFluidUsesPureFloorSemantics() {
        TimeAcceleratorCostMath.Settlement settlement = TimeAcceleratorCostMath.settleFluid(0.9999999995D, 0.0D);
        assertEquals(0, settlement.drainMb());
        assertEquals(0.9999999995D, settlement.remainingCost(), 1.0E-12D);
    }
}
