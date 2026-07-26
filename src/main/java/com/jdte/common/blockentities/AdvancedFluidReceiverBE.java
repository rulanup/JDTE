package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedFluidReceiverBE extends PoweredFluidReceiverBE {
    public static final int BASE_ENERGY_CAPACITY = 50000;
    public static final int BASE_ENERGY_COST = 500;

    public AdvancedFluidReceiverBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_FLUID_RECEIVER.get(), pos, state, BASE_ENERGY_CAPACITY, BASE_ENERGY_COST);
    }
}
