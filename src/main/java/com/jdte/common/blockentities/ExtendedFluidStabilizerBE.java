package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedFluidStabilizerBE extends PoweredFluidStabilizerBE implements ExtendedUpgradeMachine {
    public static final int BASE_ENERGY_CAPACITY = 200000;
    public static final int BASE_ENERGY_COST = 500;

    public ExtendedFluidStabilizerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_FLUID_STABILIZER.get(), pos, state, BASE_ENERGY_CAPACITY, BASE_ENERGY_COST);
    }
}
