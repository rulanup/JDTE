package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedItemReceiverBE extends PoweredItemReceiverBE {
    public static final int BASE_ENERGY_CAPACITY = 50000;
    public static final int BASE_ENERGY_COST = 500;

    public AdvancedItemReceiverBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_ITEM_RECEIVER.get(), pos, state, BASE_ENERGY_CAPACITY, BASE_ENERGY_COST);
    }
}
