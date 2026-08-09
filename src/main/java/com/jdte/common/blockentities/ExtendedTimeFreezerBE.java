package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedTimeFreezerBE extends TimeFreezerBE implements ExtendedUpgradeMachine {
    public ExtendedTimeFreezerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_TIME_FREEZER.get(), pos, state);
    }
}
