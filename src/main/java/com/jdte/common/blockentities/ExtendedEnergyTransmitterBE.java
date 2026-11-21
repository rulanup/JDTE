package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedEnergyTransmitterBE extends EnergyTransmitterBE implements ExtendedUpgradeMachine {
    public ExtendedEnergyTransmitterBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), pos, state);
    }

    @Override
    public EnergyHandler getTransmitterHandler(BlockPos blockPos) {
        if (blockPos.equals(worldPosition)) {
            return getEnergyStorage();
        }
        if (level == null || !level.getBlockState(blockPos).is(JDTRegistration.EnergyTransmitter.get())) {
            return null;
        }
        return super.getTransmitterHandler(blockPos);
    }

    @Override
    public int fePerTick() {
        if (com.jdte.common.upgrades.UpgradeHelper.hasEnergyOverloadUpgrade(this)) {
            return Integer.MAX_VALUE;
        }
        return super.fePerTick();
    }
}
