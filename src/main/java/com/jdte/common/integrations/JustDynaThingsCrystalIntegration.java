package com.jdte.common.integrations;

import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Deliberately disabled on Forge 1.20.1. Just Dyna Things has no Forge
 * 1.20.1 release, so its budding-block API cannot be linked on this branch.
 */
public final class JustDynaThingsCrystalIntegration {
    private JustDynaThingsCrystalIntegration() {
    }

    public static boolean isBudding(BlockEntity blockEntity) {
        return false;
    }

    public static boolean isMatureCrystal(BlockEntity budding, BlockState state) {
        return false;
    }

    public static int grow(BlockEntity blockEntity, RandomSource random, int attempts,
                           MachineEnergyStorage sourceEnergy, FluidTank sourceFluid,
                           int reservedEnergy, int reservedFluid, boolean creative) {
        return 0;
    }
}
