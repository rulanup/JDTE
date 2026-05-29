package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedGelGeneratorBE extends GelGeneratorBE {
    public AdvancedGelGeneratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_GEL_GENERATOR.get(), pos, state);
    }
}
