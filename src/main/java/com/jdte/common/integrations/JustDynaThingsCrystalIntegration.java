package com.jdte.common.integrations;

import com.devdyna.justdynathings.registry.builders.echoing_buddings.BuddingBE;
import com.devdyna.justdynathings.config.CommonConfig;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class JustDynaThingsCrystalIntegration {
    private static final Direction[] DIRECTIONS = Direction.values();

    private JustDynaThingsCrystalIntegration() {
    }

    public static boolean isBudding(BlockEntity blockEntity) {
        return blockEntity instanceof BuddingBE;
    }

    public static boolean isMatureCrystal(BlockEntity budding, BlockState state) {
        return budding instanceof BuddingBE dynaBudding && state.is(dynaBudding.finalCluster);
    }

    public static int grow(BlockEntity blockEntity, RandomSource random, int attempts,
                           MachineEnergyStorage sourceEnergy, FluidTank sourceFluid,
                           int reservedEnergy, int reservedFluid, boolean creative) {
        if (!(blockEntity instanceof BuddingBE budding)) {
            return 0;
        }
        int grown = 0;
        for (int i = 0; i < attempts; i++) {
            if (!supplyActivationResources(budding, sourceEnergy, sourceFluid,
                    reservedEnergy, reservedFluid, creative)) {
                break;
            }
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            if (budding.growCluster(direction)) {
                grown++;
                if (CommonConfig.BUDDING_GENERAL_SOUND.get()) {
                    budding.applySound(direction);
                }
                if (!CommonConfig.BUDDING_GENERAL_FE_CHANCE.get() || random.nextBoolean()) {
                    budding.extractFEWhenPossible();
                }
                if (!CommonConfig.BUDDING_GENERAL_MB_CHANCE.get() || random.nextBoolean()) {
                    budding.extractMBWhenPossible();
                }
            }
        }
        return grown;
    }

    private static boolean supplyActivationResources(BuddingBE budding,
                                                     MachineEnergyStorage sourceEnergy,
                                                     FluidTank sourceFluid,
                                                     int reservedEnergy,
                                                     int reservedFluid,
                                                     boolean creative) {
        int energyNeeded = Math.max(0,
                budding.getStandardEnergyCost() - budding.getEnergyStorage().getEnergyStored());
        int fluidNeeded = Math.max(0,
                budding.getStandardFluidCost() - budding.getFluidTank().getFluidAmount());

        if (!creative) {
            if (sourceEnergy.getEnergyStored() - reservedEnergy < energyNeeded
                    || sourceFluid.getFluidAmount() - reservedFluid < fluidNeeded) {
                return false;
            }
        }

        if (energyNeeded > 0 && budding.getEnergyStorage().receiveEnergy(energyNeeded, true) < energyNeeded) {
            return false;
        }
        FluidStack offeredFluid = fluidNeeded <= 0
                ? FluidStack.EMPTY
                : new FluidStack(Registration.TIME_FLUID_SOURCE.get(), fluidNeeded);
        if (fluidNeeded > 0
                && budding.getFluidTank().fill(offeredFluid, IFluidHandler.FluidAction.SIMULATE) < fluidNeeded) {
            return false;
        }

        if (energyNeeded > 0) {
            budding.getEnergyStorage().receiveEnergy(energyNeeded, false);
            if (!creative) {
                sourceEnergy.extractEnergy(energyNeeded, false);
            }
        }
        if (fluidNeeded > 0) {
            budding.getFluidTank().fill(offeredFluid, IFluidHandler.FluidAction.EXECUTE);
            if (!creative) {
                sourceFluid.drain(fluidNeeded, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return budding.canExtractFE() && budding.canExtractMB();
    }
}
