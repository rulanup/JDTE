package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedBioCrusherBE extends PoweredBioCrusherBE {
    public AdvancedBioCrusherBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_BIO_CRUSHER.get(), pos, state);
    }

    @Override
    protected int getBaseEnergyCapacity() {
        return JDTEConfig.COMMON.advancedBioCrusherEnergyCapacity.get();
    }

    @Override
    protected int getMaxEntityBatch() {
        return JDTEConfig.COMMON.advancedBioCrusherMaxEntities.get();
    }

    @Override
    protected int getNormalEntityBatch() {
        return 1;
    }
}
