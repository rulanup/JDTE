package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedLifeExtractorBE extends PoweredLifeExtractorBE implements ExtendedUpgradeMachine {
    public static final int BASE_ENERGY_CAPACITY = 200000;
    public static final int NORMAL_BATCH_SIZE = 8;
    public static final int OVERCLOCK_BATCH_SIZE = 16;

    public ExtendedLifeExtractorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_LIFE_EXTRACTOR.get(), pos, state,
                BASE_ENERGY_CAPACITY, NORMAL_BATCH_SIZE, OVERCLOCK_BATCH_SIZE);
    }
}
