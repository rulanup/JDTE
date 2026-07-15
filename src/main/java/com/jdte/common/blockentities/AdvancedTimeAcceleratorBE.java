package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedTimeAcceleratorBE extends TimeAcceleratorBE implements PoweredMachineBE {
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    private int multiplier;

    public AdvancedTimeAcceleratorBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.ADVANCED_TIME_ACCELERATOR.get(), pos, state);
    }

    protected AdvancedTimeAcceleratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        multiplier = JDTEConfig.COMMON.advancedTimeAcceleratorDefaultMultiplier.get();
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = Math.max(1, Math.min(multiplier, getMaxAdjustableMultiplier()));
        markDirtyClient();
    }

    @Override
    public int getEffectiveMultiplier() {
        return (UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this))
                ? getOverclockMultiplier()
                : multiplier;
    }

    protected int getMaxAdjustableMultiplier() {
        return JDTEConfig.COMMON.advancedTimeAcceleratorMaxMultiplier.get();
    }

    protected int getOverclockMultiplier() {
        return JDTEConfig.COMMON.advancedTimeAcceleratorOverclockMultiplier.get();
    }

    @Override
    protected double getTierFluidCostMultiplier() {
        return 2.0D;
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.advancedTimeAcceleratorEnergyCapacity.get());
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
        return super.isDefaultSettings() && energyStorage.getEnergyStored() == 0 && multiplier == JDTEConfig.COMMON.advancedTimeAcceleratorDefaultMultiplier.get();
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
