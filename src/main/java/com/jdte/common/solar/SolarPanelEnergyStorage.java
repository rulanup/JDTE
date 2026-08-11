package com.jdte.common.solar;

import net.neoforged.neoforge.energy.IEnergyStorage;

public final class SolarPanelEnergyStorage implements IEnergyStorage {
    private final boolean creative;
    private int capacity;
    private int energy;

    private SolarPanelEnergyStorage(boolean creative, int capacity) {
        this.creative = creative;
        this.capacity = Math.max(0, capacity);
        this.energy = creative ? Integer.MAX_VALUE : 0;
    }

    public static SolarPanelEnergyStorage finite(int capacity) {
        return new SolarPanelEnergyStorage(false, capacity);
    }

    public static SolarPanelEnergyStorage creative() {
        return new SolarPanelEnergyStorage(true, Integer.MAX_VALUE);
    }

    public int addGeneratedEnergy(int amount) {
        if (creative || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, capacity - energy);
        energy += accepted;
        return accepted;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
        if (!creative) {
            energy = Math.min(energy, this.capacity);
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        if (creative) {
            return maxExtract;
        }
        int extracted = Math.min(maxExtract, energy);
        if (!simulate) {
            energy -= extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return creative ? Integer.MAX_VALUE : energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return creative ? Integer.MAX_VALUE : capacity;
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
