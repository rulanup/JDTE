package com.jdte.common.integrations;

import com.devdyna.justdynathings.registry.builders.echoing_buddings.BuddingBE;
import com.devdyna.justdynathings.config.CommonConfig;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.slf4j.Logger;

public final class JustDynaThingsCrystalIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    /** Time Fluid registry id; resolved dynamically so growth does not depend on JDT internals. */
    private static final ResourceLocation TIME_FLUID_ID = ResourceLocation.parse("justdirethings:time_fluid_source");

    private JustDynaThingsCrystalIntegration() {
    }

    public static boolean isBudding(BlockEntity blockEntity) {
        return blockEntity instanceof BuddingBE;
    }

    public static boolean isMatureCrystal(BlockEntity budding, BlockState state) {
        if (!(budding instanceof BuddingBE dynaBudding)) {
            return false;
        }
        try {
            return state.is(dynaBudding.finalCluster);
        } catch (LinkageError | RuntimeException e) {
            // A Just Dyna Things update may have reshaped BuddingBE; never crash the server over it.
            LOGGER.warn("[JDTE] Could not check Just Dyna Things mature crystal at {}: {}", budding.getBlockPos(), e.toString());
            return false;
        }
    }

    public static int grow(BlockEntity blockEntity, RandomSource random, int attempts,
                           MachineEnergyStorage sourceEnergy, FluidTank sourceFluid,
                           int reservedEnergy, int reservedFluid, boolean creative) {
        if (!(blockEntity instanceof BuddingBE budding)) {
            return 0;
        }
        int grown = 0;
        for (int i = 0; i < attempts; i++) {
            try {
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
            } catch (LinkageError | RuntimeException e) {
                // Never let a Just Dyna Things version mismatch or runtime error take down the server.
                LOGGER.warn("[JDTE] Crystal Incubator could not grow Just Dyna Things budding at {}: {}",
                        budding.getBlockPos(), e.toString());
                break;
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
                : new FluidStack(resolveTimeFluid(), fluidNeeded);
        if (offeredFluid.isEmpty() && fluidNeeded > 0) {
            return false;
        }
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

    private static Fluid resolveTimeFluid() {
        Fluid fluid = BuiltInRegistries.FLUID.get(TIME_FLUID_ID);
        return fluid == null || fluid == Fluids.EMPTY ? Fluids.EMPTY : fluid;
    }
}