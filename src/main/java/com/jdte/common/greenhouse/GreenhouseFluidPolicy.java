package com.jdte.common.greenhouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public final class GreenhouseFluidPolicy {
    private GreenhouseFluidPolicy() {
    }

    public static boolean matches(FluidStack stored, ResourceLocation requiredFluid) {
        if (stored.isEmpty() || !BuiltInRegistries.FLUID.containsKey(requiredFluid)) return false;
        return requiredFluid.equals(BuiltInRegistries.FLUID.getKey(stored.getFluid()));
    }

    public static int available(FluidStack stored, ResourceLocation requiredFluid) {
        return matches(stored, requiredFluid) ? stored.getAmount() : 0;
    }

    public static boolean canConsume(FluidStack stored, ResourceLocation requiredFluid,
                                     int requiredAmount, boolean creative) {
        return creative || requiredAmount <= 0 || available(stored, requiredFluid) >= requiredAmount;
    }
}
