package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Immutable, flattened capability view of every greenhouse owned by one matrix.
 * Building the snapshot is O(members); all item-slot lookups afterwards are O(log members).
 */
final class GreenhouseMatrixCapabilitySnapshot {
    static final GreenhouseMatrixCapabilitySnapshot EMPTY =
            new GreenhouseMatrixCapabilitySnapshot(List.of(), false);

    record MachineTarget(IItemHandler itemHandler,
                         int inputStart, int inputEnd,
                         int outputStart, int outputEnd,
                         IFluidHandler fluidHandler,
                         IEnergyStorage energyStorage) {
    }

    record ItemTarget(IItemHandler handler, int slot) {
    }

    private record ItemSegment(IItemHandler handler, int start, int size, long endExclusive) {
    }

    private final List<ItemSegment> inputItems;
    private final List<ItemSegment> outputItems;
    private final int inputSlots;
    private final int outputSlots;
    private final List<IFluidHandler> fluids;
    private final List<IEnergyStorage> energies;
    private final boolean virtualFluidTank;

    static GreenhouseMatrixCapabilitySnapshot create(List<BlockPos> members,
                                                      Function<BlockPos, MachineTarget> resolver) {
        if (members.isEmpty()) return EMPTY;
        List<MachineTarget> targets = new ArrayList<>(members.size());
        for (BlockPos member : members) {
            MachineTarget target = resolver.apply(member);
            if (target != null) targets.add(target);
        }
        return new GreenhouseMatrixCapabilitySnapshot(targets, true);
    }

    private GreenhouseMatrixCapabilitySnapshot(List<MachineTarget> targets, boolean virtualFluidTank) {
        List<ItemSegment> inputs = new ArrayList<>(targets.size());
        List<ItemSegment> outputs = new ArrayList<>(targets.size());
        List<IFluidHandler> fluidTargets = new ArrayList<>(targets.size());
        List<IEnergyStorage> energyTargets = new ArrayList<>(targets.size());
        long inputTotal = 0;
        long outputTotal = 0;
        for (MachineTarget target : targets) {
            int inputSize = Math.max(0, target.inputEnd() - target.inputStart());
            if (inputSize > 0 && inputTotal < Integer.MAX_VALUE) {
                int indexedSize = (int) Math.min(inputSize, Integer.MAX_VALUE - inputTotal);
                inputTotal += indexedSize;
                inputs.add(new ItemSegment(target.itemHandler(), target.inputStart(), indexedSize, inputTotal));
            }
            int outputSize = Math.max(0, target.outputEnd() - target.outputStart());
            if (outputSize > 0 && outputTotal < Integer.MAX_VALUE) {
                int indexedSize = (int) Math.min(outputSize, Integer.MAX_VALUE - outputTotal);
                outputTotal += indexedSize;
                outputs.add(new ItemSegment(target.itemHandler(), target.outputStart(), indexedSize, outputTotal));
            }
            fluidTargets.add(target.fluidHandler());
            energyTargets.add(target.energyStorage());
        }
        inputItems = List.copyOf(inputs);
        outputItems = List.copyOf(outputs);
        inputSlots = (int) inputTotal;
        outputSlots = (int) outputTotal;
        fluids = List.copyOf(fluidTargets);
        energies = List.copyOf(energyTargets);
        this.virtualFluidTank = virtualFluidTank;
    }

    int itemSlots(boolean input) {
        return input ? inputSlots : outputSlots;
    }

    @Nullable
    ItemTarget itemTarget(boolean input, int slot) {
        List<ItemSegment> segments = input ? inputItems : outputItems;
        int total = input ? inputSlots : outputSlots;
        if (slot < 0 || slot >= total) return null;
        int low = 0;
        int high = segments.size() - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            ItemSegment segment = segments.get(middle);
            long segmentStart = segment.endExclusive() - segment.size();
            if (slot < segmentStart) {
                high = middle - 1;
            } else if (slot >= segment.endExclusive()) {
                low = middle + 1;
            } else {
                return new ItemTarget(segment.handler(), segment.start() + (int) (slot - segmentStart));
            }
        }
        return null;
    }

    int fluidTanks() {
        return virtualFluidTank ? 1 : 0;
    }

    FluidStack fluidInTank() {
        FluidStack aggregate = FluidStack.EMPTY;
        long amount = 0;
        for (IFluidHandler target : fluids) {
            FluidStack stored = target.getFluidInTank(0);
            if (stored.isEmpty()) continue;
            if (aggregate.isEmpty()) {
                aggregate = stored.copyWithAmount(1);
            } else if (!FluidStack.isSameFluidSameComponents(aggregate, stored)) {
                continue;
            }
            amount += stored.getAmount();
        }
        return aggregate.isEmpty() ? FluidStack.EMPTY : aggregate.copyWithAmount(saturatingInt(amount));
    }

    int fluidCapacity() {
        long capacity = 0;
        for (IFluidHandler target : fluids) {
            capacity += target.getTankCapacity(0);
            if (capacity >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) capacity;
    }

    boolean isFluidValid(FluidStack stack) {
        for (IFluidHandler target : fluids) {
            if (target.isFluidValid(0, stack)) return true;
        }
        return false;
    }

    int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        int filled = 0;
        for (IFluidHandler target : fluids) {
            if (filled >= resource.getAmount()) break;
            filled += target.fill(resource.copyWithAmount(resource.getAmount() - filled), action);
        }
        return filled;
    }

    int receiveEnergy(int maxReceive, boolean simulate) {
        int received = 0;
        for (IEnergyStorage target : energies) {
            if (received >= maxReceive) break;
            received += target.receiveEnergy(maxReceive - received, simulate);
        }
        return received;
    }

    long fluidStoredLong() {
        long total = 0L;
        for (IFluidHandler target : fluids) {
            for (int tank = 0; tank < target.getTanks(); tank++) {
                total = saturatingAdd(total, target.getFluidInTank(tank).getAmount());
            }
        }
        return total;
    }

    long energyStoredLong() {
        long total = 0L;
        for (IEnergyStorage target : energies) total = saturatingAdd(total, target.getEnergyStored());
        return total;
    }

    long drainFluid(long amount) {
        long remaining = Math.max(0L, amount);
        for (IFluidHandler target : fluids) {
            while (remaining > 0L) {
                int request = (int) Math.min(Integer.MAX_VALUE, remaining);
                FluidStack drained = target.drain(request, IFluidHandler.FluidAction.EXECUTE);
                if (drained.isEmpty()) break;
                remaining -= drained.getAmount();
                if (drained.getAmount() < request) break;
            }
            if (remaining == 0L) break;
        }
        return amount - remaining;
    }

    long extractEnergy(long amount) {
        long remaining = Math.max(0L, amount);
        for (IEnergyStorage target : energies) {
            while (remaining > 0L) {
                int request = (int) Math.min(Integer.MAX_VALUE, remaining);
                int extracted = target.extractEnergy(request, false);
                if (extracted <= 0) break;
                remaining -= extracted;
                if (extracted < request) break;
            }
            if (remaining == 0L) break;
        }
        return amount - remaining;
    }

    int energyStored() {
        long total = 0;
        for (IEnergyStorage target : energies) {
            total += target.getEnergyStored();
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    int maxEnergyStored() {
        long total = 0;
        for (IEnergyStorage target : energies) {
            total += target.getMaxEnergyStored();
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    private static int saturatingInt(long value) {
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}
