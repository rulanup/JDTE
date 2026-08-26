package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedEnergyTransmitterBE extends EnergyTransmitterBE implements ExtendedUpgradeMachine {
    public ExtendedEnergyTransmitterBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_ENERGY_TRANSMITTER.get(), pos, state);
    }
}
