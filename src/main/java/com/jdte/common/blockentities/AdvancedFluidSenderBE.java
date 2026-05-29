package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedFluidSenderBE extends FluidSenderBE implements PoweredMachineBE {
    public static final int BASE_FLUID_TO_SEND = 4000; // 4 buckets
    public static final int BASE_ENERGY_CAPACITY = 50000;
    public static final int BASE_ENERGY_COST = 500;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    public AdvancedFluidSenderBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_FLUID_SENDER.get(), pos, state);
        tickSpeed = 10; // Default delay
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override
    protected int getBaseFluidToSend() {
        return BASE_FLUID_TO_SEND;
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
        int baseCost = BASE_ENERGY_COST;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return baseCost * 3;
        return baseCost;
    }

    @Override
    protected void sendFluid() {
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (!hasEnoughPower(getStandardEnergyCost())) return;
        }
        super.sendFluid();
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
