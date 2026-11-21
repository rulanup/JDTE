package com.jdte.common.fluid;

import com.jdte.setup.JDTEFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LifeFluidBlock extends LiquidBlock {
    public LifeFluidBlock(BlockBehaviour.Properties properties) {
        super(JDTEFluids.LIFE_FLUID_SOURCE.get(), properties);
    }
}
