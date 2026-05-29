package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedTimeAcceleratorBE extends TimeAcceleratorBE implements PoweredMachineBE {
    public static final int BASE_ENERGY_CAPACITY = 200000;
    public static final int MAX_MULTIPLIER = 128;
    public static final int OVERCLOCK_MULTIPLIER = 256;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    private int multiplier = 4;

    public AdvancedTimeAcceleratorBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), pos, state);
    }

    protected AdvancedTimeAcceleratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = Math.max(1, Math.min(multiplier, MAX_MULTIPLIER));
        markDirtyClient();
    }

    @Override
    public int getEffectiveMultiplier() {
        return (UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)) ? OVERCLOCK_MULTIPLIER : multiplier;
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, BASE_ENERGY_CAPACITY);
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
        return getEnergyCost(getEffectiveMultiplier());
    }

    @Override
    protected boolean hasResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return true;
        }
        return super.hasResources(fluidCost, energyCost) && energyStorage.extractEnergy(energyCost, true) == energyCost;
    }

    @Override
    protected void consumeResources(int fluidCost, int energyCost) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return;
        }
        super.consumeResources(fluidCost, energyCost);
        energyStorage.extractEnergy(energyCost, false);
    }

    @Override
    protected int getEnergyCost(int multiplier) {
        return Math.max(1, multiplier * Config.TIMEWAND_RF_COST.get());
    }

    @Override
    public boolean isDefaultSettings() {
        return super.isDefaultSettings() && energyStorage.getEnergyStored() == 0 && multiplier == 4;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("multiplier", multiplier);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        if (tag.contains("multiplier")) {
            setMultiplier(tag.getInt("multiplier"));
        }
    }
}
