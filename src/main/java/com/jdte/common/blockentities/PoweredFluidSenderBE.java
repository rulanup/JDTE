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

public abstract class PoweredFluidSenderBE extends FluidSenderBE implements PoweredMachineBE {
    protected final MachineEnergySupport energySupport;
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    protected PoweredFluidSenderBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                   int baseEnergyCapacity, int baseEnergyCost) {
        super(type, pos, state);
        tickSpeed = 1;
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
        return energySupport.standardCost();
    }

    @Override
    protected int sendFluid() {
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (!hasEnoughPower(getStandardEnergyCost())) return 0;
        }
        int transferred = super.sendFluid();
        if (transferred > 0 && !UpgradeHelper.hasCreativeUpgrade(this)) {
            extractEnergy(getStandardEnergyCost(), false);
        }
        return transferred;
    }

    @Override
    protected boolean canRunDirectTransfer() {
        return UpgradeHelper.hasCreativeUpgrade(this) || hasEnoughPower(getStandardEnergyCost());
    }

    @Override
    protected void onDirectTransferSuccess() {
        if (!UpgradeHelper.hasCreativeUpgrade(this)) extractEnergy(getStandardEnergyCost(), false);
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
