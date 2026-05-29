package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicFluidReceiverBE extends FluidReceiverBE {
    public static final int BASE_FLUID_TO_RECEIVE = 1000; // 1 bucket
    public static final int BASE_DELAY_TICKS = 10;

    public BasicFluidReceiverBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_FLUID_RECEIVER.get(), pos, state);
        tickSpeed = BASE_DELAY_TICKS;
    }

    @Override
    protected int getBaseFluidToReceive() {
        return BASE_FLUID_TO_RECEIVE;
    }
}
