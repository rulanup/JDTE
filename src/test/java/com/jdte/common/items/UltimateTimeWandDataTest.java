package com.jdte.common.items;

import com.jdte.common.entities.UltimateTimeWandEntity.WandState;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import com.jdte.setup.JDTEConfig;

import static com.jdte.common.items.UltimateTimeWandData.Mode;
import static com.jdte.common.items.UltimateTimeWandData.multiplierForExponent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UltimateTimeWandDataTest {
    @Test
    void cyclesNormalX2X4MaxAndBack() {
        assertEquals(Mode.X2, Mode.NORMAL.next());
        assertEquals(Mode.X4, Mode.X2.next());
        assertEquals(Mode.MAX, Mode.X4.next());
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
    void validModeNamesResolveToTheirModes() {
        assertEquals(Mode.NORMAL, Mode.fromName("normal"));
        assertEquals(Mode.X2, Mode.fromName("x2"));
        assertEquals(Mode.X4, Mode.fromName("x4"));
        assertEquals(Mode.MAX, Mode.fromName("max"));
    }

    @Test
    void exponentAndMultiplierAreClampedToTen() {
        assertEquals(1, multiplierForExponent(-1));
        assertEquals(1024, multiplierForExponent(11));
        assertEquals(10, UltimateTimeWandData.addStep(9, Mode.X2));
        assertEquals(10, UltimateTimeWandData.addStep(Integer.MAX_VALUE, Mode.MAX));
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
    void insufficientResourcesDoNotChangeExistingState() {
        WandState before = new WandState(BlockPos.ZERO, 4, 600, 400);
        assertFalse(UltimateTimeWandData.canApply(before, Mode.MAX, 0, 0, 1, 1));
        assertEquals(before, UltimateTimeWandData.applyIfAffordable(before, Mode.MAX, 0, 0, 1, 1).state());
    }

    @Test
    void existingMaxExponentIsNotChargedAgain() {
        UltimateTimeWandData.OperationResult result = UltimateTimeWandData.planOperation(
                new WandState(BlockPos.ZERO, 10, 600, 300), Mode.MAX, 100000, 100000);
        assertFalse(result.success());
        assertEquals(0, result.fluidCost());
        assertEquals(0, result.energyCost());
    }

    @Test
    void commonConfigUsesUltimateTimeWandDefaults() {
        assertEquals(800000, JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get());
        assertEquals(10000000, JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get());
        assertEquals(600, JDTEConfig.COMMON.ultimateTimeWandDuration.get());
        assertEquals(10, UltimateTimeWandData.MAX_EXPONENT);
        assertEquals(1, Mode.NORMAL.step());
        assertEquals(2, Mode.X2.step());
        assertEquals(4, Mode.X4.step());
        assertEquals(10, Mode.MAX.step());
        assertEquals(1.0D, JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get());
        assertEquals(1.0D, JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get());
        assertEquals(true, JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get());
    }
}
