package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedGeneratorBE extends GeneratorT1BE implements ExtendedUpgradeMachine {
    public ExtendedGeneratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_GENERATOR.get(), pos, state);
    }
}
