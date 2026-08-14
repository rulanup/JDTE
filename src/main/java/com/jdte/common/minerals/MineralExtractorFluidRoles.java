package com.jdte.common.minerals;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

/** Immutable routing policy for the mineral extractor's two production-fluid tanks. */
public final class MineralExtractorFluidRoles {
    public enum Role {
        FORTUNE,
        ACCELERATION,
        NONE
    }

    private final ResourceLocation fortuneFluid;
    private final ResourceLocation accelerationFluid;

    public MineralExtractorFluidRoles(ResourceLocation fortuneFluid, ResourceLocation accelerationFluid) {
        this.fortuneFluid = Objects.requireNonNull(fortuneFluid, "fortuneFluid");
        this.accelerationFluid = Objects.requireNonNull(accelerationFluid, "accelerationFluid");
    }

    public Role roleOf(FluidStack fluid) {
        return fluid == null ? Role.NONE : roleOf(fluid.getFluid());
    }

    public Role roleOf(Fluid fluid) {
        if (fluid == null) {
            return Role.NONE;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (fortuneFluid.equals(id)) {
            return Role.FORTUNE;
        }
        if (accelerationFluid.equals(id)) {
            return Role.ACCELERATION;
        }
        return Role.NONE;
    }

    public boolean matchesFortune(FluidStack fluid) {
        return roleOf(fluid) == Role.FORTUNE;
    }

    public boolean matchesFortune(Fluid fluid) {
        return roleOf(fluid) == Role.FORTUNE;
    }

    public boolean matchesAcceleration(FluidStack fluid) {
        return roleOf(fluid) == Role.ACCELERATION;
    }

    public boolean matchesAcceleration(Fluid fluid) {
        return roleOf(fluid) == Role.ACCELERATION;
    }
}
