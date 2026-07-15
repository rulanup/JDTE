package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedTimeAcceleratorBE extends AdvancedTimeAcceleratorBE implements ExtendedUpgradeMachine {
    public ExtendedTimeAcceleratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_TIME_ACCELERATOR.get(), pos, state);
    }

    @Override
    protected int getMaxAdjustableMultiplier() {
        return JDTEConfig.COMMON.extendedTimeAcceleratorMaxMultiplier.get();
    }

    @Override
    protected int getOverclockMultiplier() {
        return JDTEConfig.COMMON.extendedTimeAcceleratorOverclockMultiplier.get();
    }

    @Override
    protected double getTierFluidCostMultiplier() {
        return 5.0D;
    }
}
