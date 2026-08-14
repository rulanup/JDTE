package com.jdte.common.greenhouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public final class GreenhouseFluidSettlement {
    public static final int LARGE_EFFICIENCY_MULTIPLIER = 9;

    private GreenhouseFluidSettlement() {
    }

    public static int normalSupportedHarvests(IFluidHandler tank, ResourceLocation requiredFluid,
                                               int fluidPerHarvest, int requestedHarvests,
                                               boolean creative) {
        return supportedHarvests(List.of(tank), requiredFluid, fluidPerHarvest,
                requestedHarvests, 1, creative);
    }

    public static int normalDrainPaidHarvests(IFluidHandler tank, ResourceLocation requiredFluid,
                                               int fluidPerHarvest, int paidHarvests,
                                               boolean creative) {
        return drainPaidHarvests(List.of(tank), requiredFluid, fluidPerHarvest,
                paidHarvests, 1, creative);
    }

    public static int largeSupportedHarvests(List<? extends IFluidHandler> memberTanks,
                                              ResourceLocation requiredFluid, int fluidPerHarvest,
                                              int requestedHarvests, boolean creative) {
        return supportedHarvests(memberTanks, requiredFluid, fluidPerHarvest,
                requestedHarvests, LARGE_EFFICIENCY_MULTIPLIER, creative);
    }

    public static int largeDrainPaidHarvests(List<? extends IFluidHandler> memberTanks,
                                              ResourceLocation requiredFluid, int fluidPerHarvest,
                                              int paidHarvests, boolean creative) {
        return drainPaidHarvests(memberTanks, requiredFluid, fluidPerHarvest,
                paidHarvests, LARGE_EFFICIENCY_MULTIPLIER, creative);
    }

    private static int supportedHarvests(List<? extends IFluidHandler> tanks,
                                         ResourceLocation requiredFluid, int fluidPerHarvest,
                                         int requestedHarvests, int efficiencyMultiplier,
                                         boolean creative) {
        int requested = Math.max(0, requestedHarvests);
        if (requested == 0 || creative || fluidPerHarvest <= 0) return requested;
        long supported = available(tanks, requiredFluid) * Math.max(1, efficiencyMultiplier)
                / fluidPerHarvest;
        return (int) Math.min(requested, supported);
    }

    private static int drainPaidHarvests(List<? extends IFluidHandler> tanks,
                                         ResourceLocation requiredFluid, int fluidPerHarvest,
                                         int paidHarvests, int efficiencyMultiplier,
                                         boolean creative) {
        if (creative || paidHarvests <= 0 || fluidPerHarvest <= 0) return 0;
        long baseCost = (long) fluidPerHarvest * paidHarvests;
        int efficiency = Math.max(1, efficiencyMultiplier);
        long cost = baseCost / efficiency + (baseCost % efficiency == 0 ? 0 : 1);
        return drain(tanks, requiredFluid, (int) Math.min(Integer.MAX_VALUE, cost));
    }

    private static long available(List<? extends IFluidHandler> tanks, ResourceLocation requiredFluid) {
        long total = 0L;
        for (IFluidHandler tank : tanks) {
            for (int index = 0; index < tank.getTanks(); index++) {
                FluidStack stored = tank.getFluidInTank(index);
                total = Math.min(Integer.MAX_VALUE,
                        total + GreenhouseFluidPolicy.available(stored, requiredFluid));
            }
        }
        return total;
    }

    private static int drain(List<? extends IFluidHandler> tanks, ResourceLocation requiredFluid, int amount) {
        if (amount <= 0 || !BuiltInRegistries.FLUID.containsKey(requiredFluid)) return 0;
        Fluid fluid = BuiltInRegistries.FLUID.get(requiredFluid);
        int remaining = amount;
        for (IFluidHandler tank : tanks) {
            if (remaining == 0) break;
            FluidStack drained = tank.drain(new FluidStack(fluid, remaining),
                    IFluidHandler.FluidAction.EXECUTE);
            remaining -= drained.getAmount();
        }
        return amount - remaining;
    }
}
