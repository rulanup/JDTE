package com.jdte.common.solar;

import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Objects;

/**
 * The externally exposed view of a solar panel's stored FE.
 *
 * <p>Generation and automatic output use the backing storage directly so a panel only needs one
 * persistence update for its own tick. External consumers, however, must notify the owning block
 * entity when they drain finite stored FE; otherwise a night-time drain could be lost on save.</p>
 */
public final class SolarPanelEnergyExportCapability implements IEnergyStorage {
    private final SolarPanelEnergyStorage storage;
    private final boolean persistExtraction;
    private final Runnable onExtracted;

    public SolarPanelEnergyExportCapability(SolarPanelEnergyStorage storage, boolean persistExtraction,
                                            Runnable onExtracted) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.persistExtraction = persistExtraction;
        this.onExtracted = Objects.requireNonNull(onExtracted, "onExtracted");
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = storage.extractEnergy(maxExtract, simulate);
        if (!simulate && persistExtraction && extracted > 0) {
            onExtracted.run();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
