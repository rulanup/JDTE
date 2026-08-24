package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import com.jdte.setup.JDTEConfig;

import static com.jdte.common.items.UltimateTimeWandData.Mode;
import static com.jdte.common.items.UltimateTimeWandData.multiplierForExponent;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UltimateTimeWandDataTest {
    @Test
    void cyclesNormalX2X4MaxAndBack() {
        assertEquals(Mode.X2, Mode.NORMAL.next());
        assertEquals(Mode.X4, Mode.X2.next());
        assertEquals(Mode.MAX.next(), Mode.NORMAL);
    }

    @Test
    void modesUseOneTwoFourAndTenExponentSteps() {
        assertEquals(1, Mode.NORMAL.step());
        assertEquals(2, Mode.X2.step());
        assertEquals(4, Mode.X4.step());
        assertEquals(10, Mode.MAX.step());
        assertEquals(1024, multiplierForExponent(10));
    }

    @Test
    void invalidModeInputsFallBackToNormal() {
        assertEquals(Mode.NORMAL, Mode.fromOrdinal(-1));
        assertEquals(Mode.NORMAL, Mode.fromOrdinal(99));
        assertEquals(Mode.NORMAL, Mode.fromName("unknown"));
        assertEquals(Mode.NORMAL, Mode.fromName(null));
    }

    @Test
    void exponentAndMultiplierAreClampedToTen() {
        assertEquals(1, multiplierForExponent(-1));
        assertEquals(1024, multiplierForExponent(11));
        assertEquals(10, UltimateTimeWandData.addStep(9, Mode.X2));
    }

    @Test
    void energyCostUsesSaturatingLongMultiplication() {
        assertEquals(Integer.MAX_VALUE, UltimateTimeWandData.saturatingEnergyCost(1024, Integer.MAX_VALUE));
        assertEquals(0, UltimateTimeWandData.saturatingEnergyCost(-1, 100));
    }

    @Test
    void fluidSettlementKeepsFractionalRemainder() {
        UltimateTimeWandData.FluidSettlement settlement =
                UltimateTimeWandData.settleFluid(0.75D, 0.5D);
        assertEquals(1, settlement.drainMb());
        assertEquals(0.25D, settlement.remainingCost(), 1.0E-9D);
    }

    @Test
    void commonConfigUsesUltimateTimeWandDefaults() {
        assertEquals(800000, JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get());
        assertEquals(10000000, JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get());
        assertEquals(600, JDTEConfig.COMMON.ultimateTimeWandDuration.get());
        assertEquals(10, JDTEConfig.COMMON.ultimateTimeWandMaxExponent.get());
        assertEquals(1, JDTEConfig.COMMON.ultimateTimeWandNormalStep.get());
        assertEquals(2, JDTEConfig.COMMON.ultimateTimeWandX2Step.get());
        assertEquals(4, JDTEConfig.COMMON.ultimateTimeWandX4Step.get());
        assertEquals(10, JDTEConfig.COMMON.ultimateTimeWandMaxStep.get());
        assertEquals(1.0D, JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get());
        assertEquals(1.0D, JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get());
        assertEquals(true, JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get());
    }
}
