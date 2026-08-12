package com.jdte.common.blockentities;

import com.jdte.common.blocks.GreenhouseMatrixPortBlock;
import com.jdte.common.greenhouse.GreenhouseMatrixPortType;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

final class GreenhouseMatrixAutoIo {
    private static final int PORT_BUDGET_PER_TICK = 16;
    private static final int ENERGY_TRANSFER_RATE = 100_000;

    private GreenhouseMatrixAutoIo() {}

    static int tick(ServerLevel level, GreenhouseMatrixControllerBE controller, List<BlockPos> ports,
                    BlockPos min, BlockPos max, int cursor) {
        if (ports.isEmpty()) return 0;
        int start = Math.floorMod(cursor, ports.size());
        int processed = Math.min(PORT_BUDGET_PER_TICK, ports.size());
        for (int offset = 0; offset < processed; offset++) {
            BlockPos portPos = ports.get((start + offset) % ports.size());
            if (!(level.getBlockState(portPos).getBlock() instanceof GreenhouseMatrixPortBlock port)) continue;
            transferPort(level, controller, portPos, port.portType(), min, max);
        }
        return (start + processed) % ports.size();
    }

    private static void transferPort(ServerLevel level, GreenhouseMatrixControllerBE controller, BlockPos portPos,
                                     GreenhouseMatrixPortType type, BlockPos min, BlockPos max) {
        if (type == GreenhouseMatrixPortType.ITEM_OUTPUT && controller.hasAEOutputUpgrade()) return;
        int remaining = transferLimit(type,
                JDTEConfig.COMMON.autoIoItemTransferRate.get(),
                JDTEConfig.COMMON.autoIoFluidTransferRate.get());
        if (remaining <= 0) return;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = portPos.relative(direction);
            if (!outside(neighborPos, min, max)) continue;
            Direction neighborSide = direction.getOpposite();
            remaining -= switch (type) {
                case ITEM_INPUT -> pullItems(level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, neighborSide),
                        controller.getInputHandler(), remaining);
                case ITEM_OUTPUT -> pushItems(controller.getOutputHandler(),
                        level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, neighborSide), remaining);
                case FLUID_INPUT -> pullFluid(level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, neighborSide),
                        controller.getFluidHandler(), remaining);
                case ENERGY_INPUT -> pullEnergy(level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, neighborSide),
                        controller.getEnergyHandler(), remaining);
            };
            if (remaining <= 0) break;
        }
    }

    private static boolean outside(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() < min.getX() || pos.getX() > max.getX()
                || pos.getY() < min.getY() || pos.getY() > max.getY()
                || pos.getZ() < min.getZ() || pos.getZ() > max.getZ();
    }

    static int transferLimit(GreenhouseMatrixPortType type, int configuredItemRate, int configuredFluidRate) {
        return switch (type) {
            case ITEM_INPUT, ITEM_OUTPUT -> configuredItemRate;
            // A formed matrix can feed thousands of Greenhouses in one settlement.
            // Its virtual tank must therefore pull as much as the source and aggregate
            // member capacity accept instead of reusing the ordinary-machine rate cap.
            case FLUID_INPUT -> Integer.MAX_VALUE;
            case ENERGY_INPUT -> ENERGY_TRANSFER_RATE;
        };
    }

    private static int pullItems(IItemHandler source, IItemHandler target, int limit) {
        if (source == null || target == null || limit <= 0) return 0;
        int moved = 0;
        for (int slot = 0; slot < source.getSlots() && moved < limit; slot++) {
            ItemStack offered = source.extractItem(slot, limit - moved, true);
            if (offered.isEmpty()) continue;
            ItemStack simulatedRemainder = ItemHandlerHelper.insertItemStacked(target, offered, true);
            int movable = offered.getCount() - simulatedRemainder.getCount();
            if (movable <= 0) continue;
            ItemStack extracted = source.extractItem(slot, movable, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, extracted, false);
            int inserted = extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) source.insertItem(slot, remainder, false);
            moved += inserted;
        }
        return moved;
    }

    private static int pushItems(IItemHandler source, IItemHandler target, int limit) {
        if (source == null || target == null || limit <= 0) return 0;
        int moved = 0;
        for (int slot = 0; slot < source.getSlots() && moved < limit; slot++) {
            ItemStack offered = source.extractItem(slot, limit - moved, true);
            if (offered.isEmpty()) continue;
            ItemStack simulatedRemainder = ItemHandlerHelper.insertItemStacked(target, offered, true);
            int movable = offered.getCount() - simulatedRemainder.getCount();
            if (movable <= 0) continue;
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, offered.copyWithCount(movable), false);
            int inserted = movable - remainder.getCount();
            if (inserted > 0) source.extractItem(slot, inserted, false);
            moved += inserted;
        }
        return moved;
    }

    static int pullFluid(IFluidHandler source, IFluidHandler target, int limit) {
        if (source == null || target == null || limit <= 0) return 0;
        FluidStack offered = source.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (offered.isEmpty()) return 0;
        int fillable = target.fill(offered, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return 0;
        FluidStack drained = source.drain(Math.min(fillable, offered.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return 0;
        int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled < drained.getAmount()) {
            source.fill(drained.copyWithAmount(drained.getAmount() - filled), IFluidHandler.FluidAction.EXECUTE);
        }
        return filled;
    }

    private static int pullEnergy(IEnergyStorage source, IEnergyStorage target, int limit) {
        if (source == null || target == null || limit <= 0 || !source.canExtract() || !target.canReceive()) return 0;
        int extractable = source.extractEnergy(limit, true);
        int receivable = target.receiveEnergy(extractable, true);
        if (receivable <= 0) return 0;
        int extracted = source.extractEnergy(receivable, false);
        int received = target.receiveEnergy(extracted, false);
        if (received < extracted && source.canReceive()) source.receiveEnergy(extracted - received, false);
        return received;
    }
}
