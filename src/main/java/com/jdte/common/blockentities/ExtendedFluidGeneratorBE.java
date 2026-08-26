package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedFluidGeneratorBE extends GeneratorFluidT1BE implements ExtendedUpgradeMachine {
    public ExtendedFluidGeneratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_FLUID_GENERATOR.get(), pos, state);
    }
}
