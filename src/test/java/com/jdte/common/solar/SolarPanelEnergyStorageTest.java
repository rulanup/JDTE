package com.jdte.common.solar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolarPanelEnergyStorageTest {
    @Test
    void finiteStorageOnlyAllowsInternalGenerationAndBoundedExtraction() {
        SolarPanelEnergyStorage storage = SolarPanelEnergyStorage.finite(1_000);

        assertFalse(storage.canReceive());
        assertTrue(storage.canExtract());
        assertEquals(0, storage.receiveEnergy(100, false));
        assertEquals(1_000, storage.addGeneratedEnergy(1_200));
        assertEquals(1_000, storage.getEnergyStored());
        assertEquals(250, storage.extractEnergy(250, false));
        assertEquals(750, storage.getEnergyStored());
    }

    @Test
    void finiteStorageCapacityChangesClampStoredEnergy() {
        SolarPanelEnergyStorage storage = SolarPanelEnergyStorage.finite(1_000);
        storage.addGeneratedEnergy(900);

        storage.setCapacity(400);

        assertEquals(400, storage.getMaxEnergyStored());
        assertEquals(400, storage.getEnergyStored());
    }

    @Test
    void creativeStorageIsAnUnconsumedInfiniteSource() {
        SolarPanelEnergyStorage storage = SolarPanelEnergyStorage.creative();

        assertFalse(storage.canReceive());
        assertTrue(storage.canExtract());
        assertEquals(Integer.MAX_VALUE, storage.getEnergyStored());
        assertEquals(Integer.MAX_VALUE, storage.getMaxEnergyStored());
        assertEquals(Integer.MAX_VALUE, storage.extractEnergy(Integer.MAX_VALUE, false));
        assertEquals(Integer.MAX_VALUE, storage.getEnergyStored());
    }
}
