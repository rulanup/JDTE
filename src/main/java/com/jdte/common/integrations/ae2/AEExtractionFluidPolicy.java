package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Chooses a safe one-millibucket prototype for an item fluid tank. */
public final class AEExtractionFluidPolicy {
    private static final Map<Item, Map<Integer, FluidStack>> UNIQUE_FLUID_CACHE = new IdentityHashMap<>();

    private AEExtractionFluidPolicy() {
    }

    public static FluidStack select(ItemStack container, ResourceHandler<FluidResource> handler, int tank, List<net.minecraft.world.level.material.Fluid> candidates) {
        return select(container, handler, tank, candidates, true);
    }

    public static FluidStack select(ItemStack container, ResourceHandler<FluidResource> handler, int tank,
                                    List<net.minecraft.world.level.material.Fluid> candidates, boolean useCache) {
        if (handler == null || tank < 0 || tank >= handler.size()) return null;
        FluidResource stored = handler.getResource(tank);
        if (!stored.isEmpty()) return stored.toStack(1);

        FluidStack cached = null;
        if (useCache && !container.isEmpty()) {
            Item item = container.getItem();
            cached = UNIQUE_FLUID_CACHE.computeIfAbsent(item, ignored -> new HashMap<>()).get(tank);
            if (cached != null) return cached.copy();
        }

        FluidStack unique = null;
        Map<FluidType, FluidStack> byType = new IdentityHashMap<>();
        for (net.minecraft.world.level.material.Fluid fluid : candidates) {
            if (fluid == null) continue;
            FluidStack probe = new FluidStack(fluid, 1);
            if (!handler.isValid(tank, FluidResource.of(probe))) continue;
            FluidType type = fluid.getFluidType();
            FluidStack prior = byType.get(type);
            if (prior == null || (fluid.defaultFluidState().isSource()
                    && !prior.getFluid().defaultFluidState().isSource())) {
                byType.put(type, probe);
            }
        }
        if (byType.size() == 1) unique = byType.values().iterator().next();
        if (unique != null && !container.isEmpty()) {
            UNIQUE_FLUID_CACHE.computeIfAbsent(container.getItem(), ignored -> new java.util.HashMap<>())
                    .put(tank, unique.copy());
        }
        return unique;
    }

    public static boolean hasCached(ItemStack stack, int tank) {
        if (stack == null || stack.isEmpty() || tank < 0) return false;
        Map<Integer, FluidStack> byTank = UNIQUE_FLUID_CACHE.get(stack.getItem());
        return byTank != null && byTank.containsKey(tank);
    }

    public static void invalidate(ItemStack stack, int tank) {
        if (stack == null || stack.isEmpty() || tank < 0) return;
        Map<Integer, FluidStack> byTank = UNIQUE_FLUID_CACHE.get(stack.getItem());
        if (byTank != null) byTank.remove(tank);
    }

    public static void clearCache() {
        UNIQUE_FLUID_CACHE.clear();
    }
}
