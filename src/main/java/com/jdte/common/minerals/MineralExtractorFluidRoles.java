package com.jdte.common.minerals;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

    private final Identifier fortuneFluid;
    private final Identifier accelerationFluid;

    public MineralExtractorFluidRoles(Identifier fortuneFluid, Identifier accelerationFluid) {
        this.fortuneFluid = Objects.requireNonNull(fortuneFluid, "fortuneFluid");
        this.accelerationFluid = Objects.requireNonNull(accelerationFluid, "accelerationFluid");
    }

    public Identifier fortuneFluid() {
        return fortuneFluid;
    }

    public Identifier accelerationFluid() {
        return accelerationFluid;
    }

    public Role roleOf(FluidStack fluid) {
        return fluid == null ? Role.NONE : roleOf(fluid.getFluid());
    }

    public Role roleOf(Fluid fluid) {
        if (fluid == null) {
            return Role.NONE;
        }
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
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
