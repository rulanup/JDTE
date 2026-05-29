package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicGlueActivatorBE extends GlueActivatorBE {
    public BasicGlueActivatorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_GLUE_ACTIVATOR.get(), pos, state);
        tickSpeed = BASE_DELAY_TICKS; // 6 seconds delay
    }

    @Override
    public boolean canRun() {
        return operationTicks == 0;
    }
}
