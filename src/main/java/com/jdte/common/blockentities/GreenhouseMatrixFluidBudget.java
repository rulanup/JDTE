package com.jdte.common.blockentities;

import com.jdte.common.greenhouse.GreenhouseFluidPolicy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One-settlement fluid ledger grouped by registry ID with directed per-fluid drain cursors. */
final class GreenhouseMatrixFluidBudget {
    private final Map<ResourceLocation, Bucket> buckets;

    private GreenhouseMatrixFluidBudget(Map<ResourceLocation, Bucket> buckets) {
        this.buckets = buckets;
    }

    static GreenhouseMatrixFluidBudget capture(List<? extends IFluidHandler> handlers) {
        Map<ResourceLocation, Bucket> buckets = new HashMap<>();
        for (IFluidHandler handler : handlers) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                FluidStack stored = handler.getFluidInTank(tank);
                if (stored.isEmpty()) continue;
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stored.getFluid());
                if (fluidId == null) continue;
                buckets.computeIfAbsent(fluidId, ignored -> new Bucket(fluidId))
                        .add(handler, tank, stored.getAmount());
            }
        }
        return new GreenhouseMatrixFluidBudget(buckets);
    }

    long available(ResourceLocation requiredFluid) {
        Bucket bucket = buckets.get(requiredFluid);
        return bucket == null ? 0L : bucket.available();
    }

    boolean tryPay(ResourceLocation requiredFluid, long amount, boolean creative) {
        long requested = Math.max(0L, amount);
        if (creative || requested == 0L) return true;
        Bucket bucket = buckets.get(requiredFluid);
        return bucket != null && bucket.drain(requested) == requested;
    }

    private static final class Bucket {
        private final ResourceLocation fluidId;
        private final List<TankEntry> entries = new ArrayList<>();
        private long available;
        private int cursor;

        private Bucket(ResourceLocation fluidId) {
            this.fluidId = fluidId;
        }

        private void add(IFluidHandler handler, int tank, int amount) {
            int stored = Math.max(0, amount);
            if (stored == 0) return;
            entries.add(new TankEntry(handler, tank, stored));
            available = saturatingAdd(available, stored);
        }

        private long available() {
            return available;
        }

        private long drain(long amount) {
            long remaining = amount;
            while (remaining > 0L && cursor < entries.size()) {
                TankEntry entry = entries.get(cursor);
                if (entry.remaining == 0L) {
                    cursor++;
                    continue;
                }

                FluidStack stored = entry.handler.getFluidInTank(entry.tank);
                if (!GreenhouseFluidPolicy.matches(stored, fluidId)) {
                    removeUnavailable(entry);
                    cursor++;
                    continue;
                }
                long liveAmount = Math.min(entry.remaining, stored.getAmount());
                if (liveAmount < entry.remaining) {
                    available -= entry.remaining - liveAmount;
                    entry.remaining = liveAmount;
                }
                if (entry.remaining == 0L) {
                    cursor++;
                    continue;
                }

                int request = (int) Math.min(remaining, entry.remaining);
                FluidStack requestedVariant = stored.copyWithAmount(request);
                FluidStack drained = entry.handler.drain(
                        requestedVariant, IFluidHandler.FluidAction.EXECUTE);
                if (drained.isEmpty()
                        || !FluidStack.isSameFluidSameComponents(requestedVariant, drained)) break;

                int paid = Math.min(request, drained.getAmount());
                entry.remaining -= paid;
                available -= paid;
                remaining -= paid;
                if (entry.remaining == 0L) cursor++;
                if (paid < request) break;
            }
            return amount - remaining;
        }

        private void removeUnavailable(TankEntry entry) {
            available -= entry.remaining;
            entry.remaining = 0L;
        }
    }

    private static final class TankEntry {
        private final IFluidHandler handler;
        private final int tank;
        private long remaining;

        private TankEntry(IFluidHandler handler, int tank, long remaining) {
            this.handler = handler;
            this.tank = tank;
            this.remaining = remaining;
        }
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}
