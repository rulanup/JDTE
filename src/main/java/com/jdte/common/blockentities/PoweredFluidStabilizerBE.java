package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.machines.MachineEnergySupport;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PoweredFluidStabilizerBE extends FluidStabilizerBE implements PoweredMachineBE {
    protected final MachineEnergySupport energySupport;
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    private final int baseEnergyCost;

    protected PoweredFluidStabilizerBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                       int baseEnergyCapacity, int baseEnergyCost) {
        super(type, pos, state);
        tickSpeed = 20;
        this.baseEnergyCost = baseEnergyCost;
        energySupport = new MachineEnergySupport(this, baseEnergyCapacity, baseEnergyCost);
        energyStorage = energySupport.energyStorage;
        poweredMachineData = energySupport.poweredMachineData;
    }

    @Override
    public int getMaxEnergy() {
        return energySupport.maxEnergy();
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
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return 0;
        }
        long scaledCost = (long) baseEnergyCost * getAreaEnergyScale();
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energySupport.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energySupport.load(tag);
    }
}
