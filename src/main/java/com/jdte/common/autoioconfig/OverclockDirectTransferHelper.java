package com.jdte.common.autoioconfig;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.FluidReceiverBE;
import com.jdte.common.blockentities.FluidSenderBE;
import com.jdte.common.blockentities.ItemReceiverBE;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class OverclockDirectTransferHelper {
    private static final int TARGET_REFRESH_INTERVAL_TICKS = 40;
    private static final int MAX_ITEM_BATCH_OPERATIONS_PER_TICK = 256;
    private static final Map<BaseMachineBE, RangeCache> RANGE_CACHES = new WeakHashMap<>();

    private OverclockDirectTransferHelper() {
    }

    public static boolean isEnabled(BaseMachineBE machine) {
        if (machine == null || !UpgradeHelper.hasOverclock(machine)) return false;
        if (machine instanceof ItemSenderBE || machine instanceof FluidSenderBE) {
            return AutoIoConfigHelper.getInputMask(machine) != 0;
        }
        if (machine instanceof ItemReceiverBE || machine instanceof FluidReceiverBE) {
            return AutoIoConfigHelper.getOutputMask(machine) != 0;
        }
        return false;
    }

    public static int transfer(BaseMachineBE machine) {
        if (!isEnabled(machine) || !(machine.getLevel() instanceof ServerLevel level)
                || !(machine instanceof AreaAffectingBE areaMachine)) {
            return 0;
        }
        if (machine instanceof ItemSenderBE sender) {
            return transferItemSender(level, sender, areaMachine, new ItemTransferBudget());
        }
        if (machine instanceof ItemReceiverBE receiver) {
            return transferItemReceiver(level, receiver, areaMachine, new ItemTransferBudget());
        }
        if (machine instanceof FluidSenderBE sender) return transferFluidSender(level, sender, areaMachine);
        if (machine instanceof FluidReceiverBE receiver) return transferFluidReceiver(level, receiver, areaMachine);
        return 0;
    }

    public static void invalidate(BaseMachineBE machine) {
        RANGE_CACHES.remove(machine);
    }

    public static List<BlockPos> getCachedItemPositions(ServerLevel level, BaseMachineBE machine,
                                                         AreaAffectingBE areaMachine) {
        return getRangeCache(level, machine, areaMachine, false).positions;
    }

    private static int transferItemSender(ServerLevel level, ItemSenderBE machine, AreaAffectingBE areaMachine,
                                          ItemTransferBudget budget) {
        int remaining = JDTEConfig.COMMON.senderReceiverOverclockItemTransferRate.get();
        RangeCache cache = getRangeCache(level, machine, areaMachine, false);
        List<ItemEndpoint> sources = getAdjacentItemEndpoints(level, machine, AutoIoConfigHelper.getInputMask(machine));
        Set<BlockPos> excludedTargets = endpointPositions(sources);

        if (hasVisibleItems(machine.getItemHandler())) {
            remaining -= moveItemsToRange(level, machine, machine.getItemHandler(), null, cache, excludedTargets,
                    remaining, budget);
        }
        for (ItemEndpoint source : sources) {
            if (remaining <= 0 || !budget.canContinue()) break;
            remaining -= moveItemsToRange(level, machine, source.handler(), source.pos(), cache, excludedTargets,
                    remaining, budget);
        }
        return JDTEConfig.COMMON.senderReceiverOverclockItemTransferRate.get() - remaining;
    }

    private static int transferItemReceiver(ServerLevel level, ItemReceiverBE machine, AreaAffectingBE areaMachine,
                                            ItemTransferBudget budget) {
        int limit = JDTEConfig.COMMON.senderReceiverOverclockItemTransferRate.get();
        int remaining = limit;
        RangeCache cache = getRangeCache(level, machine, areaMachine, false);
        List<ItemEndpoint> targets = getAdjacentItemEndpoints(level, machine, AutoIoConfigHelper.getOutputMask(machine));
        if (targets.isEmpty()) return 0;
        Set<BlockPos> excludedSources = endpointPositions(targets);

        if (hasVisibleItems(machine.getItemHandler())) {
            remaining -= moveItemsToAdjacent(machine.getItemHandler(), null, targets, remaining, budget);
        }
        int count = cache.positions.size();
        int start = count == 0 ? 0 : Math.floorMod(cache.cursor, count);
        int attempted = 0;
        while (attempted < count && remaining > 0 && budget.canContinue()) {
            int index = (start + attempted) % count;
            BlockPos sourcePos = cache.positions.get(index);
            if (!excludedSources.contains(sourcePos)) {
                remaining -= moveItemsFromRange(level, machine, sourcePos, cache, targets, remaining, budget);
            }
            attempted++;
        }
        if (count > 0) cache.cursor = (start + Math.max(1, attempted)) % count;
        return limit - remaining;
    }

    private static int transferFluidSender(ServerLevel level, FluidSenderBE machine, AreaAffectingBE areaMachine) {
        int limit = fluidLimit();
        int remaining = limit;
        RangeCache cache = getRangeCache(level, machine, areaMachine, true);
        List<FluidEndpoint> sources = getAdjacentFluidEndpoints(level, machine, AutoIoConfigHelper.getInputMask(machine));
        Set<BlockPos> excludedTargets = endpointPositionsFluid(sources);

        remaining -= moveFluidToRange(level, machine, machine.getFluidTank(), null, cache, excludedTargets, remaining);
        for (FluidEndpoint source : sources) {
            if (remaining <= 0) break;
            remaining -= moveFluidToRange(level, machine, source.handler(), source.pos(), cache, excludedTargets, remaining);
        }
        return limit - remaining;
    }

    private static int transferFluidReceiver(ServerLevel level, FluidReceiverBE machine, AreaAffectingBE areaMachine) {
        int limit = fluidLimit();
        int remaining = limit;
        RangeCache cache = getRangeCache(level, machine, areaMachine, true);
        List<FluidEndpoint> targets = getAdjacentFluidEndpoints(level, machine, AutoIoConfigHelper.getOutputMask(machine));
        if (targets.isEmpty()) return 0;
        Set<BlockPos> excludedSources = endpointPositionsFluid(targets);

        remaining -= moveFluidToAdjacent(machine.getFluidTank(), null, targets, remaining);
        int count = cache.positions.size();
        int start = count == 0 ? 0 : Math.floorMod(cache.cursor, count);
        int attempted = 0;
        while (attempted < count && remaining > 0) {
            int index = (start + attempted) % count;
            BlockPos sourcePos = cache.positions.get(index);
            if (!excludedSources.contains(sourcePos)) {
                remaining -= moveFluidFromRange(level, machine, sourcePos, cache, targets, remaining);
            }
            attempted++;
        }
        if (count > 0) cache.cursor = (start + Math.max(1, attempted)) % count;
        return limit - remaining;
    }

    private static int moveItemsToRange(ServerLevel level, BaseMachineBE machine, IItemHandler source,
                                        BlockPos sourcePos, RangeCache cache, Set<BlockPos> excluded, int limit,
                                        ItemTransferBudget budget) {
        int moved = 0;
        int count = cache.positions.size();
        int start = count == 0 ? 0 : Math.floorMod(cache.cursor, count);
        int attempted = 0;
        while (attempted < count && moved < limit && budget.canContinue()) {
            int index = (start + attempted) % count;
            BlockPos targetPos = cache.positions.get(index);
            if (!excluded.contains(targetPos) && !targetPos.equals(sourcePos)) {
                int remaining = limit - moved;
                moved += withRangeItemHandler(level, machine, targetPos, cache,
                        handler -> moveItems(source, handler, remaining, budget));
            }
            attempted++;
        }
        if (count > 0) cache.cursor = (start + Math.max(1, attempted)) % count;
        return moved;
    }

    private static int moveItemsFromRange(ServerLevel level, BaseMachineBE machine, BlockPos sourcePos,
                                          RangeCache cache, List<ItemEndpoint> targets, int limit,
                                          ItemTransferBudget budget) {
        return withRangeItemHandler(level, machine, sourcePos, cache,
                source -> moveItemsToAdjacent(source, sourcePos, targets, limit, budget));
    }

    private static int moveItemsToAdjacent(IItemHandler source, BlockPos sourcePos,
                                           List<ItemEndpoint> targets, int limit, ItemTransferBudget budget) {
        int moved = 0;
        for (ItemEndpoint target : targets) {
            if (moved >= limit || !budget.canContinue()) break;
            if (target.pos().equals(sourcePos)) continue;
            moved += moveItems(source, target.handler(), limit - moved, budget);
        }
        return moved;
    }

    private static int moveFluidToRange(ServerLevel level, BaseMachineBE machine, IFluidHandler source,
                                        BlockPos sourcePos, RangeCache cache, Set<BlockPos> excluded, int limit) {
        int moved = 0;
        int count = cache.positions.size();
        int start = count == 0 ? 0 : Math.floorMod(cache.cursor, count);
        int attempted = 0;
        while (attempted < count && moved < limit) {
            int index = (start + attempted) % count;
            BlockPos targetPos = cache.positions.get(index);
            if (!excluded.contains(targetPos) && !targetPos.equals(sourcePos)) {
                int remaining = limit - moved;
                moved += withRangeFluidHandler(level, machine, targetPos, cache,
                        handler -> moveFluid(source, handler, remaining));
            }
            attempted++;
        }
        if (count > 0) cache.cursor = (start + Math.max(1, attempted)) % count;
        return moved;
    }

    private static int moveFluidFromRange(ServerLevel level, BaseMachineBE machine, BlockPos sourcePos,
                                          RangeCache cache, List<FluidEndpoint> targets, int limit) {
        return withRangeFluidHandler(level, machine, sourcePos, cache,
                source -> moveFluidToAdjacent(source, sourcePos, targets, limit));
    }

    private static int moveFluidToAdjacent(IFluidHandler source, BlockPos sourcePos,
                                           List<FluidEndpoint> targets, int limit) {
        int moved = 0;
        for (FluidEndpoint target : targets) {
            if (moved >= limit) break;
            if (target.pos().equals(sourcePos)) continue;
            moved += moveFluid(source, target.handler(), limit - moved);
        }
        return moved;
    }

    private static int moveItems(IItemHandler source, IItemHandler target, int limit, ItemTransferBudget budget) {
        int moved = 0;
        for (int slot = 0; slot < source.getSlots() && moved < limit; slot++) {
            while (moved < limit && !source.getStackInSlot(slot).isEmpty() && budget.beginOperation()) {
                ItemStack simulated = source.extractItem(slot, limit - moved, true);
                if (simulated.isEmpty()) break;
                ItemStack simulatedRemainder = ItemHandlerHelper.insertItemStacked(target, simulated, true);
                int movable = simulated.getCount() - simulatedRemainder.getCount();
                if (movable <= 0) break;
                ItemStack extracted = source.extractItem(slot, movable, false);
                if (extracted.isEmpty()) break;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, extracted, false);
                int inserted = extracted.getCount() - remainder.getCount();
                if (!remainder.isEmpty()) source.insertItem(slot, remainder, false);
                if (inserted <= 0) break;
                moved += inserted;
            }
            if (!budget.canContinue()) break;
        }
        return moved;
    }

    private static boolean hasVisibleItems(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private static int moveFluid(IFluidHandler source, IFluidHandler target, int limit) {
        FluidStack simulated = source.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty()) return 0;
        int fillable = target.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return 0;
        FluidStack request = simulated.copy();
        request.setAmount(Math.min(fillable, simulated.getAmount()));
        FluidStack drained = source.drain(request, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return 0;
        int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - Math.max(0, filled));
            source.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        return Math.max(0, filled);
    }

    private static RangeCache getRangeCache(ServerLevel level, BaseMachineBE machine,
                                            AreaAffectingBE areaMachine, boolean fluid) {
        RangeCache cache = RANGE_CACHES.computeIfAbsent(machine, ignored -> new RangeCache());
        AABB area = areaMachine.getAABB(machine.getBlockPos());
        long gameTime = level.getGameTime();
        if (cache.fluid == fluid && sameArea(cache.area, area) && gameTime < cache.nextRefreshTick) return cache;

        int minX = Mth.floor(area.minX);
        int minY = Mth.floor(area.minY);
        int minZ = Mth.floor(area.minZ);
        int maxX = Mth.ceil(area.maxX) - 1;
        int maxY = Mth.ceil(area.maxY) - 1;
        int maxZ = Mth.ceil(area.maxZ) - 1;
        List<BlockPos> positions = new ArrayList<>();
        for (int chunkX = SectionPos.blockToSectionCoord(minX);
             chunkX <= SectionPos.blockToSectionCoord(maxX); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(minZ);
                 chunkZ <= SectionPos.blockToSectionCoord(maxZ); chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    if (pos.equals(machine.getBlockPos())
                            || pos.getX() < minX || pos.getX() > maxX
                            || pos.getY() < minY || pos.getY() > maxY
                            || pos.getZ() < minZ || pos.getZ() > maxZ) {
                        continue;
                    }
                    if (fluid ? hasFluidCapability(level, machine, pos) : hasItemCapability(level, machine, pos)) {
                        positions.add(pos.immutable());
                    }
                }
            }
        }
        cache.positions = List.copyOf(positions);
        cache.successfulSides.keySet().retainAll(cache.positions);
        cache.area = area;
        cache.fluid = fluid;
        cache.nextRefreshTick = gameTime + TARGET_REFRESH_INTERVAL_TICKS;
        cache.cursor = cache.positions.isEmpty() ? 0 : Math.floorMod(cache.cursor, cache.positions.size());
        return cache;
    }

    private static int withRangeItemHandler(ServerLevel level, BaseMachineBE machine, BlockPos pos,
                                            RangeCache cache, ItemHandlerOperation operation) {
        Direction cached = cache.successfulSides.get(pos);
        int moved = 0;
        if (cached != null) {
            moved = tryItemSide(level, pos, cached, operation);
            if (moved > 0) return moved;
            cache.successfulSides.remove(pos);
        }
        Direction preferred = sideFacingMachine(machine, pos);
        if (preferred != cached && (moved = tryItemSide(level, pos, preferred, operation)) > 0) {
            cache.successfulSides.put(pos, preferred);
            return moved;
        }
        for (Direction side : Direction.values()) {
            if (side == cached || side == preferred) continue;
            moved = tryItemSide(level, pos, side, operation);
            if (moved > 0) {
                cache.successfulSides.put(pos, side);
                return moved;
            }
        }
        moved = tryItemSide(level, pos, null, operation);
        if (moved > 0) cache.successfulSides.remove(pos);
        return moved;
    }

    private static int withRangeFluidHandler(ServerLevel level, BaseMachineBE machine, BlockPos pos,
                                             RangeCache cache, FluidHandlerOperation operation) {
        Direction cached = cache.successfulSides.get(pos);
        int moved = 0;
        if (cached != null) {
            moved = tryFluidSide(level, pos, cached, operation);
            if (moved > 0) return moved;
            cache.successfulSides.remove(pos);
        }
        Direction preferred = sideFacingMachine(machine, pos);
        if (preferred != cached && (moved = tryFluidSide(level, pos, preferred, operation)) > 0) {
            cache.successfulSides.put(pos, preferred);
            return moved;
        }
        for (Direction side : Direction.values()) {
            if (side == cached || side == preferred) continue;
            moved = tryFluidSide(level, pos, side, operation);
            if (moved > 0) {
                cache.successfulSides.put(pos, side);
                return moved;
            }
        }
        moved = tryFluidSide(level, pos, null, operation);
        if (moved > 0) cache.successfulSides.remove(pos);
        return moved;
    }

    private static int tryItemSide(ServerLevel level, BlockPos pos, Direction side, ItemHandlerOperation operation) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return handler == null ? 0 : operation.apply(handler);
    }

    private static int tryFluidSide(ServerLevel level, BlockPos pos, Direction side, FluidHandlerOperation operation) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
        return handler == null ? 0 : operation.apply(handler);
    }

    private static boolean hasItemCapability(ServerLevel level, BaseMachineBE machine, BlockPos pos) {
        Direction preferred = sideFacingMachine(machine, pos);
        if (preferred != null && level.getCapability(Capabilities.ItemHandler.BLOCK, pos, preferred) != null) return true;
        for (Direction side : Direction.values()) {
            if (side != preferred && level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null) return true;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) != null;
    }

    private static boolean hasFluidCapability(ServerLevel level, BaseMachineBE machine, BlockPos pos) {
        Direction preferred = sideFacingMachine(machine, pos);
        if (preferred != null && level.getCapability(Capabilities.FluidHandler.BLOCK, pos, preferred) != null) return true;
        for (Direction side : Direction.values()) {
            if (side != preferred && level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side) != null) return true;
        }
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null) != null;
    }

    private static List<ItemEndpoint> getAdjacentItemEndpoints(ServerLevel level, BaseMachineBE machine, int mask) {
        List<ItemEndpoint> endpoints = new ArrayList<>();
        for (int sideIndex = 0; sideIndex < AutoIoConfigData.SIDE_COUNT; sideIndex++) {
            if ((mask & (1 << sideIndex)) == 0) continue;
            Direction direction = directionForUiSide(sideIndex);
            BlockPos pos = machine.getBlockPos().relative(direction);
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
            if (handler != null) endpoints.add(new ItemEndpoint(pos, handler));
        }
        return endpoints;
    }

    private static List<FluidEndpoint> getAdjacentFluidEndpoints(ServerLevel level, BaseMachineBE machine, int mask) {
        List<FluidEndpoint> endpoints = new ArrayList<>();
        for (int sideIndex = 0; sideIndex < AutoIoConfigData.SIDE_COUNT; sideIndex++) {
            if ((mask & (1 << sideIndex)) == 0) continue;
            Direction direction = directionForUiSide(sideIndex);
            BlockPos pos = machine.getBlockPos().relative(direction);
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction.getOpposite());
            if (handler != null) endpoints.add(new FluidEndpoint(pos, handler));
        }
        return endpoints;
    }

    private static Set<BlockPos> endpointPositions(List<ItemEndpoint> endpoints) {
        Set<BlockPos> positions = new HashSet<>();
        for (ItemEndpoint endpoint : endpoints) positions.add(endpoint.pos());
        return positions;
    }

    private static Set<BlockPos> endpointPositionsFluid(List<FluidEndpoint> endpoints) {
        Set<BlockPos> positions = new HashSet<>();
        for (FluidEndpoint endpoint : endpoints) positions.add(endpoint.pos());
        return positions;
    }

    private static int fluidLimit() {
        return JDTEConfig.COMMON.fluidSenderUnlimitedTransfer.get()
                ? Integer.MAX_VALUE
                : JDTEConfig.COMMON.senderReceiverOverclockFluidTransferRate.get();
    }

    private static Direction directionForUiSide(int side) {
        return switch (side) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            case 3 -> Direction.EAST;
            case 4 -> Direction.UP;
            case 5 -> Direction.DOWN;
            default -> Direction.NORTH;
        };
    }

    private static Direction sideFacingMachine(BaseMachineBE machine, BlockPos targetPos) {
        int dx = machine.getBlockPos().getX() - targetPos.getX();
        int dy = machine.getBlockPos().getY() - targetPos.getY();
        int dz = machine.getBlockPos().getZ() - targetPos.getZ();
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);
        int absZ = Math.abs(dz);
        if (absX == 0 && absY == 0 && absZ == 0) return null;
        if (absY >= absX && absY >= absZ) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (absX >= absZ) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static boolean sameArea(AABB first, AABB second) {
        return first != null
                && Double.compare(first.minX, second.minX) == 0
                && Double.compare(first.minY, second.minY) == 0
                && Double.compare(first.minZ, second.minZ) == 0
                && Double.compare(first.maxX, second.maxX) == 0
                && Double.compare(first.maxY, second.maxY) == 0
                && Double.compare(first.maxZ, second.maxZ) == 0;
    }

    private record ItemEndpoint(BlockPos pos, IItemHandler handler) {
    }

    private record FluidEndpoint(BlockPos pos, IFluidHandler handler) {
    }

    @FunctionalInterface
    private interface ItemHandlerOperation {
        int apply(IItemHandler handler);
    }

    @FunctionalInterface
    private interface FluidHandlerOperation {
        int apply(IFluidHandler handler);
    }

    private static final class RangeCache {
        private AABB area;
        private boolean fluid;
        private long nextRefreshTick;
        private List<BlockPos> positions = List.of();
        private final Map<BlockPos, Direction> successfulSides = new HashMap<>();
        private int cursor;
    }

    private static final class ItemTransferBudget {
        private int operations;

        private boolean canContinue() {
            return operations < MAX_ITEM_BATCH_OPERATIONS_PER_TICK;
        }

        private boolean beginOperation() {
            if (!canContinue()) return false;
            operations++;
            return true;
        }
    }
}
