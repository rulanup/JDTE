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

public abstract class PoweredFluidReceiverBE extends FluidReceiverBE implements PoweredMachineBE {
    protected final MachineEnergySupport energySupport;
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    protected PoweredFluidReceiverBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
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
    protected void receiveFluid() {
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (!hasEnoughPower(getStandardEnergyCost())) return;
        }
        super.receiveFluid();
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
