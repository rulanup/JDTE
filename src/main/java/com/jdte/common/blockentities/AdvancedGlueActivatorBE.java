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

public class AdvancedGlueActivatorBE extends GlueActivatorBE implements PoweredMachineBE {
    public static final int BASE_ENERGY_CAPACITY = 50000;
    public static final int BASE_ENERGY_COST = 500;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    public AdvancedGlueActivatorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_GLUE_ACTIVATOR.get(), pos, state);
        tickSpeed = 1; // No delay
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
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
        return UpgradeHelper.adjustEnergyCost(this, BASE_ENERGY_COST);
    }

    @Override
    protected boolean tryReviveGoo(net.minecraft.server.level.ServerLevel serverLevel, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (!hasEnoughPower(getStandardEnergyCost())) return false;
        }
        boolean result = super.tryReviveGoo(serverLevel, pos, state);
        if (result && !UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(getStandardEnergyCost(), false);
        }
        return result;
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
