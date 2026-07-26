package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedLifeExtractorBE extends PoweredLifeExtractorBE {
    public static final int BASE_ENERGY_CAPACITY = 100000;
    public static final int NORMAL_BATCH_SIZE = 4;
    public static final int OVERCLOCK_BATCH_SIZE = 8;

    public AdvancedLifeExtractorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_LIFE_EXTRACTOR.get(), pos, state,
                BASE_ENERGY_CAPACITY, NORMAL_BATCH_SIZE, OVERCLOCK_BATCH_SIZE);
    }
}
