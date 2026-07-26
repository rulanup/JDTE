package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedFluidSenderBE extends PoweredFluidSenderBE implements ExtendedUpgradeMachine {
    public static final int BASE_ENERGY_CAPACITY = 100000;
    public static final int BASE_ENERGY_COST = 500;

    public ExtendedFluidSenderBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_FLUID_SENDER.get(), pos, state, BASE_ENERGY_CAPACITY, BASE_ENERGY_COST);
    }
}
