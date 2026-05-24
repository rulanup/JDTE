package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicTimeAcceleratorBE extends TimeAcceleratorBE {
    public BasicTimeAcceleratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_TIME_ACCELERATOR.get(), pos, state);
    }

    @Override
    public int getEffectiveMultiplier() {
        return UpgradeHelper.hasOverclock(this) ? 16 : 4;
    }
}
