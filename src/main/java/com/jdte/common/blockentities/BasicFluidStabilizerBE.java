package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicFluidStabilizerBE extends FluidStabilizerBE {
    public BasicFluidStabilizerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BASIC_FLUID_STABILIZER.get(), pos, state);
        tickSpeed = 40;
    }

    @Override
    public int getStandardEnergyCost() {
        return 0;
    }

    @Override
    public boolean hasEnoughPower(int energyCost) {
        return true;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return 0;
    }
}
