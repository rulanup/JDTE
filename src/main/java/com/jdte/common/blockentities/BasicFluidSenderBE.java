package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicFluidSenderBE extends FluidSenderBE {
    public static final int BASE_DELAY_TICKS = 1;

    public BasicFluidSenderBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_FLUID_SENDER.get(), pos, state);
        tickSpeed = BASE_DELAY_TICKS;
    }
}
