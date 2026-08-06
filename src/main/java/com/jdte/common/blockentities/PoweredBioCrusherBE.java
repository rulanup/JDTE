package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PoweredBioCrusherBE extends BioCrusherBE implements PoweredMachineBE {
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    protected PoweredBioCrusherBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tickSpeed = 20;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    protected abstract int getBaseEnergyCapacity();

    protected abstract int getMaxEntityBatch();

    protected abstract int getNormalEntityBatch();

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, getBaseEnergyCapacity());
    }

    @Override
    public ContainerData getContainerData() {
        return poweredMachineData;
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public int getStandardEnergyCost() {
        return getEffectiveEnergyCost();
    }

    @Override
    public int getEffectiveEnergyCost() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        int baseCost = JDTEConfig.COMMON.bioCrusherEnergyCost.get();
        long scaledCost = (long) baseCost * getAreaEnergyScale();
        return UpgradeHelper.adjustEnergyCost(this, scaledCost > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaledCost);
    }

    @Override
    public boolean hasEnoughPower(int energyCost) {
        return PoweredMachineBE.super.hasEnoughPower(energyCost);
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return PoweredMachineBE.super.extractEnergy(energy, simulate);
    }

    @Override
    protected int getExtractInterval() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 1;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return 1;
        int baseInterval = JDTEConfig.COMMON.bioCrusherProcessTime.get();
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.UNDERCLOCK) > 0) return baseInterval * 2;
        return baseInterval;
    }

    @Override
    protected int getMaxEntitiesPerTick() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return getMaxEntityBatch();
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return getMaxEntityBatch();
        return getNormalEntityBatch();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
    }
}
