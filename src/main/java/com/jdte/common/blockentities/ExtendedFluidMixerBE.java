package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedFluidMixerBE extends FluidMixerBE implements ExtendedUpgradeMachine {
    public static final int BASE_ENERGY_CAPACITY = 200000;

    public ExtendedFluidMixerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_FLUID_MIXER.get(), pos, state, BASE_ENERGY_CAPACITY);
    }
}
