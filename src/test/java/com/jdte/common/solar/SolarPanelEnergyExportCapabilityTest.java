package com.jdte.common.solar;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolarPanelEnergyExportCapabilityTest {
    @Test
    void externalFiniteExtractionMarksTheOwnerDirtyExactlyOnce() {
        SolarPanelEnergyStorage storage = SolarPanelEnergyStorage.finite(1_000);
        storage.addGeneratedEnergy(600);
        AtomicInteger dirtyCalls = new AtomicInteger();
        SolarPanelEnergyExportCapability capability = new SolarPanelEnergyExportCapability(
                storage, true, dirtyCalls::incrementAndGet);

        assertEquals(250, capability.extractEnergy(250, false));
        assertEquals(1, dirtyCalls.get());
        assertEquals(350, storage.getEnergyStored());
    }

    @Test
    void simulatedOrCreativeExtractionDoesNotSchedulePersistence() {
        SolarPanelEnergyStorage finite = SolarPanelEnergyStorage.finite(1_000);
        finite.addGeneratedEnergy(100);
        AtomicInteger finiteDirtyCalls = new AtomicInteger();
        SolarPanelEnergyExportCapability finiteCapability = new SolarPanelEnergyExportCapability(
                finite, true, finiteDirtyCalls::incrementAndGet);
        assertEquals(100, finiteCapability.extractEnergy(100, true));
        assertEquals(0, finiteDirtyCalls.get());

        AtomicInteger creativeDirtyCalls = new AtomicInteger();
        SolarPanelEnergyExportCapability creativeCapability = new SolarPanelEnergyExportCapability(
                SolarPanelEnergyStorage.creative(), false, creativeDirtyCalls::incrementAndGet);
        assertEquals(100, creativeCapability.extractEnergy(100, false));
        assertEquals(0, creativeDirtyCalls.get());
    }
}
