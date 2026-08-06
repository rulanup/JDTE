package com.jdte.common.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;

public final class InfusionFluidHelper {
    public static final int BOTTLE_FLUID_AMOUNT = 250;

    private static final List<TagKey<Fluid>> HONEY_FLUID_TAGS = List.of(
            fluidTag("c", "honey"),
            fluidTag("c", "honeys"),
            fluidTag("c", "honey_fluid"),
            fluidTag("c", "honey_fluids"),
            fluidTag("forge", "honey"),
            fluidTag("forge", "honeys"),
            fluidTag("forge", "honey_fluid"),
            fluidTag("forge", "honey_fluids")
    );

    private InfusionFluidHelper() {
    }

    public static boolean isHoneyFluid(FluidStack stack) {
        return !stack.isEmpty() && isHoneyFluid(stack.getFluid());
    }

    public static boolean isHoneyFluid(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return false;
        }

        for (TagKey<Fluid> tag : HONEY_FLUID_TAGS) {
            if (fluid.builtInRegistryHolder().is(tag)) {
                return true;
            }
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        if (fluidId != null && containsHoney(fluidId.getPath())) {
            return true;
        }

        return containsHoney(fluid.getFluidType().getDescriptionId());
    }

    public static boolean isFillableSourceFluid(Fluid fluid) {
        return fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource();
    }

    private static TagKey<Fluid> fluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static boolean containsHoney(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains("honey");
    }
}
