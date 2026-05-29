package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedGelGeneratorBE extends GelGeneratorBE implements ExtendedUpgradeMachine {
    public ExtendedGelGeneratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_GEL_GENERATOR.get(), pos, state);
    }
}
