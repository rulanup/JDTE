package com.jdte.common.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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

    public record FilledContainer(ItemStack stack, int fluidAmount) {}

    public static FilledContainer fillContainer(ItemStack input, FluidStack fluid) {
        if (input.isEmpty() || fluid.isEmpty()) return null;
        var slot = new ItemStacksResourceHandler(1);
        slot.set(0, ItemResource.of(input), 1);
        var access = ItemAccess.forHandlerIndexStrict(slot, 0);
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return null;
        try (var transaction = Transaction.openRoot()) {
            int filled = handler.insert(FluidResource.of(fluid), fluid.getAmount(), transaction);
            if (filled <= 0 || filled > fluid.getAmount()) return null;
            transaction.commit();
            ItemStack result = access.getResource().toStack(access.getAmount());
            if (result.getCount() != 1 || ItemStack.isSameItemSameComponents(input, result)) return null;
            return new FilledContainer(result.copy(), filled);
        }
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

        Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        if (fluidId != null && containsHoney(fluidId.getPath())) {
            return true;
        }

        return containsHoney(fluid.getFluidType().getDescriptionId());
    }

    public static boolean isFillableSourceFluid(Fluid fluid) {
        return fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource();
    }

    private static TagKey<Fluid> fluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static boolean containsHoney(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains("honey");
    }
}
