package com.jdte.common.fluid;

import com.jdte.setup.JDTEFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LifeFluidBlock extends LiquidBlock {
    public LifeFluidBlock() {
        super(JDTEFluids.LIFE_FLUID_SOURCE.get(), BlockBehaviour.Properties.of()
                .noCollission()
                .strength(100.0F)
                .noLootTable()
                .liquid()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                .replaceable()
        );
    }
}
