package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicItemReceiverBE extends ItemReceiverBE {
    public static final int BASE_ITEMS_TO_RECEIVE = 4;
    public static final int BASE_DELAY_TICKS = 10;

    public BasicItemReceiverBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_ITEM_RECEIVER.get(), pos, state);
        tickSpeed = BASE_DELAY_TICKS;
    }

    @Override
    protected int getBaseItemsToReceive() {
        return BASE_ITEMS_TO_RECEIVE;
    }
}
