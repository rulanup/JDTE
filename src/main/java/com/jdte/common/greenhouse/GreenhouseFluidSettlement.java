package com.jdte.common.greenhouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public final class GreenhouseFluidSettlement {
    public static final int LARGE_EFFICIENCY_MULTIPLIER = 9;

    private GreenhouseFluidSettlement() {
    }

    public static int normalSupportedHarvests(ResourceHandler<FluidResource> tank, Identifier requiredFluid,
                                               int fluidPerHarvest, int requestedHarvests,
                                               boolean creative) {
        return supportedHarvests(List.of(tank), requiredFluid, fluidPerHarvest,
                requestedHarvests, 1, creative);
    }

    public static boolean normalTryPay(ResourceHandler<FluidResource> tank, Identifier requiredFluid,
                                       int fluidPerHarvest, int paidHarvests,
                                       boolean creative) {
        return tryPay(List.of(tank), requiredFluid, fluidPerHarvest,
                paidHarvests, 1, creative);
    }

    public static int largeSupportedHarvests(List<? extends ResourceHandler<FluidResource>> memberTanks,
                                              Identifier requiredFluid, int fluidPerHarvest,
                                              int requestedHarvests, boolean creative) {
        return supportedHarvests(memberTanks, requiredFluid, fluidPerHarvest,
                requestedHarvests, LARGE_EFFICIENCY_MULTIPLIER, creative);
    }

    public static boolean largeTryPay(List<? extends ResourceHandler<FluidResource>> memberTanks,
                                      Identifier requiredFluid, int fluidPerHarvest,
                                      int paidHarvests, boolean creative) {
        return tryPay(memberTanks, requiredFluid, fluidPerHarvest,
                paidHarvests, LARGE_EFFICIENCY_MULTIPLIER, creative);
    }

    private static int supportedHarvests(List<? extends ResourceHandler<FluidResource>> tanks,
                                         Identifier requiredFluid, int fluidPerHarvest,
                                         int requestedHarvests, int efficiencyMultiplier,
                                         boolean creative) {
        int requested = Math.max(0, requestedHarvests);
        if (requested == 0 || creative || fluidPerHarvest <= 0) return requested;
        long supported = available(tanks, requiredFluid) * Math.max(1, efficiencyMultiplier)
                / fluidPerHarvest;
        return (int) Math.min(requested, supported);
    }

    private static boolean tryPay(List<? extends ResourceHandler<FluidResource>> tanks,
                                  Identifier requiredFluid, int fluidPerHarvest,
                                  int paidHarvests, int efficiencyMultiplier,
                                  boolean creative) {
        if (creative || paidHarvests <= 0 || fluidPerHarvest <= 0) return true;
        int required = requiredAmount(fluidPerHarvest, paidHarvests, efficiencyMultiplier);
        return drain(tanks, requiredFluid, required) == required;
    }

    private static int requiredAmount(int fluidPerHarvest, int paidHarvests,
                                      int efficiencyMultiplier) {
        long baseCost = (long) fluidPerHarvest * paidHarvests;
        int efficiency = Math.max(1, efficiencyMultiplier);
        long cost = baseCost / efficiency + (baseCost % efficiency == 0 ? 0 : 1);
        return (int) Math.min(Integer.MAX_VALUE, cost);
    }

    private static long available(List<? extends ResourceHandler<FluidResource>> tanks, Identifier requiredFluid) {
        long total = 0L;
        for (ResourceHandler<FluidResource> tank : tanks) {
            for (int index = 0; index < tank.size(); index++) {
                FluidStack stored = tank.getResource(index).toStack(tank.getAmountAsInt(index));
                total = Math.min(Integer.MAX_VALUE,
                        total + GreenhouseFluidPolicy.available(stored, requiredFluid));
            }
        }
        return total;
    }

    private static int drain(List<? extends ResourceHandler<FluidResource>> tanks, Identifier requiredFluid, int amount) {
        if (amount <= 0 || !BuiltInRegistries.FLUID.containsKey(requiredFluid)) return 0;
        int remaining = amount;
        try (var transaction = Transaction.openRoot()) {
            for (var handler : tanks) {
                for (int index = 0; index < handler.size() && remaining > 0; index++) {
                    var resource = handler.getResource(index);
                    if (!GreenhouseFluidPolicy.matches(resource.toStack(1), requiredFluid)) continue;
                    remaining -= handler.extract(index, resource, remaining, transaction);
                }
                if (remaining == 0) {
                    transaction.commit();
                    return amount;
                }
            }
            return 0;
        }
    }
}
