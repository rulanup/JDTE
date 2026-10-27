package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedFluidMixerBE extends FluidMixerBE {
    public static final int BASE_ENERGY_CAPACITY = 100000;

    public AdvancedFluidMixerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_FLUID_MIXER.get(), pos, state, BASE_ENERGY_CAPACITY);
    }
}
