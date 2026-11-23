package com.jdte.common.solar;

import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolarPanelLogicTest {
    // ---- SolarGenerationPolicy ----

    @Test
    void adjacencyDoublesOnlyWithSevenOrMoreSameTierNeighbors() {
        for (int n = 0; n <= 6; n++) {
            assertEquals(1, SolarGenerationPolicy.adjacencyMultiplier(n), "neighbors=" + n);
        }
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(7));
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(8));
    }

    @Test
    void adjacencyClampsOutOfRangeNeighborCounts() {
        assertEquals(1, SolarGenerationPolicy.adjacencyMultiplier(-5));
        assertEquals(2, SolarGenerationPolicy.adjacencyMultiplier(100));
    }

    @Test
    void heightIsLowestAtMidpointAndFullAtBuildLimits() {
        assertEquals(0.05F, SolarGenerationPolicy.heightMultiplier(-64, 320, 128), 1e-6F);
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(-64, 320, 320), 1e-6F);
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(-64, 320, -64), 1e-6F);
        float quarter = SolarGenerationPolicy.heightMultiplier(-64, 320, 224);
        assertTrue(quarter > 0.05F && quarter < 1.0F);
    }

    @Test
    void heightIsNeutralForDegenerateRange() {
        assertEquals(1.0F, SolarGenerationPolicy.heightMultiplier(10, 10, 10));
    }

    @Test
    void generatedPerTickCombinesBaseAdjacencyAndHeight() {
        int base = SolarPanelTier.CONCENTRATED.defaultGeneration();
        assertEquals(base, SolarGenerationPolicy.generatedPerTick(base, 0, -64, 320, 320));
        assertEquals(base * 2, SolarGenerationPolicy.generatedPerTick(base, 8, -64, 320, 320));
        assertEquals((int) (base * 0.05F), SolarGenerationPolicy.generatedPerTick(base, 0, -64, 320, 128));
        assertEquals(0, SolarGenerationPolicy.generatedPerTick(0, 8, -64, 320, 320));
        assertEquals(0, SolarGenerationPolicy.generatedPerTick(-1, 8, -64, 320, 320));
    }

    @Test
    void generatedPerTickSaturatesInsteadOfOverflowing() {
        assertEquals(Integer.MAX_VALUE,
                SolarGenerationPolicy.generatedPerTick(Integer.MAX_VALUE, 8, -64, 320, 320));
    }

    @Test
    void tiersScaleByFourAndCapacityIsHundredTicks() {
        SolarPanelTier[] finite = {SolarPanelTier.CONCENTRATED, SolarPanelTier.SINGULARITY,
                SolarPanelTier.STELLAR_FUSION, SolarPanelTier.DIMENSIONAL_COLLAPSE};
        for (int i = 0; i < finite.length; i++) {
            assertFalse(finite[i].creative());
            assertEquals(finite[i].defaultGeneration() * 100, finite[i].defaultCapacity());
            if (i > 0) {
                assertEquals(finite[i - 1].defaultGeneration() * 4, finite[i].defaultGeneration());
            }
        }
        assertTrue(SolarPanelTier.CREATIVE.creative());
    }

    // ---- SolarPanelEnergyStorage ----

    @Test
    void finiteStorageAcceptsGenerationUpToCapacityAndRejectsExternalInput() {
        SolarPanelEnergyStorage s = SolarPanelEnergyStorage.finite(100);
        assertEquals(60, s.addGeneratedEnergy(60));
        assertEquals(40, s.addGeneratedEnergy(60));
        assertEquals(100, s.getEnergyStored());
        assertEquals(0, s.addGeneratedEnergy(1));
        assertEquals(0, s.addGeneratedEnergy(-5));
        assertFalse(s.canReceive());
        assertEquals(0, s.receiveEnergy(50, false));
        assertTrue(s.canExtract());
    }

    @Test
    void finiteExtractRespectsSimulateAndStoredEnergy() {
        SolarPanelEnergyStorage s = SolarPanelEnergyStorage.finite(100);
        s.addGeneratedEnergy(30);
        assertEquals(30, s.extractEnergy(50, true));
        assertEquals(30, s.getEnergyStored());
        assertEquals(20, s.extractEnergy(20, false));
        assertEquals(10, s.getEnergyStored());
        assertEquals(0, s.extractEnergy(0, false));
        assertEquals(0, s.extractEnergy(-1, false));
    }

    @Test
    void shrinkingCapacityClampsStoredEnergy() {
        SolarPanelEnergyStorage s = SolarPanelEnergyStorage.finite(100);
        s.addGeneratedEnergy(100);
        s.setCapacity(40);
        assertEquals(40, s.getEnergyStored());
        assertEquals(40, s.getMaxEnergyStored());
        s.setCapacity(-10);
        assertEquals(0, s.getEnergyStored());
        assertEquals(0, s.getMaxEnergyStored());
    }

    @Test
    void creativeStorageIsAlwaysFullAndNeverDrains() {
        SolarPanelEnergyStorage s = SolarPanelEnergyStorage.creative();
        assertEquals(Integer.MAX_VALUE, s.getEnergyStored());
        assertEquals(Integer.MAX_VALUE, s.getMaxEnergyStored());
        assertEquals(1_000_000, s.extractEnergy(1_000_000, false));
        assertEquals(Integer.MAX_VALUE, s.getEnergyStored());
        assertEquals(0, s.addGeneratedEnergy(10));
    }

    // ---- SolarEnergyTransfer ----

    @Test
    void pushMovesEnergyLimitedByOfferAndReceiverSpace() {
        SolarPanelEnergyStorage src = SolarPanelEnergyStorage.finite(1000);
        src.addGeneratedEnergy(1000);
        EnergyStorage dst = new EnergyStorage(300);

        assertEquals(200, SolarEnergyTransfer.push(src, dst, 200).moved());
        assertEquals(800, src.getEnergyStored());
        assertEquals(200, dst.getEnergyStored());

        assertEquals(100, SolarEnergyTransfer.push(src, dst, 500).moved());
        assertEquals(700, src.getEnergyStored());
        assertEquals(300, dst.getEnergyStored());

        assertEquals(0, SolarEnergyTransfer.push(src, dst, 500).moved());
        assertEquals(700, src.getEnergyStored());
    }

    @Test
    void pushIsNoopForNullNonReceivingOrNonPositiveOffer() {
        SolarPanelEnergyStorage src = SolarPanelEnergyStorage.finite(100);
        src.addGeneratedEnergy(100);
        assertEquals(0, SolarEnergyTransfer.push(src, null, 50).moved());
        assertEquals(0, SolarEnergyTransfer.push(src, new EnergyStorage(100, 0, 0), 50).moved());
        assertEquals(0, SolarEnergyTransfer.push(src, new EnergyStorage(100), 0).moved());
        assertEquals(100, src.getEnergyStored());
    }

    @Test
    void pushRefundsEnergyTheReceiverRejectsAfterSimulating() {
        SolarPanelEnergyStorage src = SolarPanelEnergyStorage.finite(100);
        src.addGeneratedEnergy(100);
        IEnergyStorage liar = new EnergyStorage(1000) {
            @Override
            public int receiveEnergy(int max, boolean simulate) {
                return simulate ? max : Math.min(max, 10);
            }
        };
        assertEquals(10, SolarEnergyTransfer.push(src, liar, 60).moved());
        assertEquals(90, src.getEnergyStored());
    }

    @Test
    void creativeSourcePushesFullOfferWithoutDraining() {
        SolarPanelEnergyStorage src = SolarPanelEnergyStorage.creative();
        EnergyStorage dst = new EnergyStorage(5000);
        assertEquals(5000, SolarEnergyTransfer.push(src, dst, 10_000).moved());
        assertEquals(Integer.MAX_VALUE, src.getEnergyStored());
    }
}
