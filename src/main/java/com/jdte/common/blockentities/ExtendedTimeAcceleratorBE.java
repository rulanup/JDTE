package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedTimeAcceleratorBE extends AdvancedTimeAcceleratorBE implements ExtendedUpgradeMachine {
    public ExtendedTimeAcceleratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_TIME_ACCELERATOR.get(), pos, state);
    }
}
