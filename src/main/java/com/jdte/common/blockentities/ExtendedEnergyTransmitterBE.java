package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedEnergyTransmitterBE extends EnergyTransmitterBE implements ExtendedUpgradeMachine {
    public ExtendedEnergyTransmitterBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), pos, state);
    }

    @Override
    public IEnergyStorage getTransmitterHandler(BlockPos blockPos) {
        if (blockPos.equals(worldPosition)) {
            return getEnergyStorage();
        }
        if (level == null || !level.getBlockState(blockPos).is(Registration.EnergyTransmitter.get())) {
            return null;
        }
        return super.getTransmitterHandler(blockPos);
    }
}
