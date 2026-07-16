package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.ItemStackKey;
import com.jdte.setup.JDTEConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class AdvancedItemCollectorManager {
    private static final Map<ServerLevel, LevelIndex> LEVELS = new WeakHashMap<>();

    private AdvancedItemCollectorManager() {
    }

    public static void register(AdvancedItemCollectorBE collector) {
        if (!(collector.getLevel() instanceof ServerLevel level)) return;
        LevelIndex index = LEVELS.computeIfAbsent(level, ignored -> new LevelIndex());
        index.remove(collector);
        index.add(collector);
    }

    public static void unregister(AdvancedItemCollectorBE collector) {
        if (!(collector.getLevel() instanceof ServerLevel level)) return;
        LevelIndex index = LEVELS.get(level);
        if (index != null) index.remove(collector);
    }

    public static void refresh(AdvancedItemCollectorBE collector) {
        if (!collector.isRemoved()) register(collector);
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !JDTEConfig.COMMON.advancedItemCollectorPreDrainEnabled.get()
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        LevelIndex index = LEVELS.get(level);
        if (index == null) return;

        BlockPos sourcePos = event.getPos();
        Set<AdvancedItemCollectorBE> collectors = index.at(Vec3.atCenterOf(sourcePos));
        if (collectors.isEmpty()) return;

        IItemHandlerModifiable source = findModifiableItemHandler(level, sourcePos);
        int threshold = JDTEConfig.COMMON.advancedItemCollectorPreDrainThreshold.get();
        if (source == null || !hasThresholdStack(source, threshold)) return;

        if (!transferOversizedSlots(source, sourcePos, collectors, threshold)) {
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(
                    Component.translatable("message.jdte.advanced_item_collector.break_blocked", threshold)
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }

    private static IItemHandlerModifiable findModifiableItemHandler(ServerLevel level, BlockPos pos) {
        IItemHandler unsided = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (unsided instanceof IItemHandlerModifiable modifiable) return modifiable;
        for (Direction direction : Direction.values()) {
            IItemHandler sided = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
            if (sided instanceof IItemHandlerModifiable modifiable) return modifiable;
        }
        return null;
    }

    private static boolean hasThresholdStack(IItemHandler handler, int threshold) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot).getCount() >= threshold) return true;
        }
        return false;
    }

    private static boolean transferOversizedSlots(IItemHandlerModifiable source, BlockPos sourcePos,
                                                   Set<AdvancedItemCollectorBE> collectors, int threshold) {
        // Direct slot replacement avoids IItemHandler's one-normal-stack extraction limit.
        Vec3 sourceCenter = Vec3.atCenterOf(sourcePos);
        boolean fullyDrained = true;
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack remaining = source.getStackInSlot(slot);
            if (remaining.getCount() < threshold) continue;

            for (AdvancedItemCollectorBE collector : collectors) {
                if (remaining.isEmpty()
                        || !collector.getAABB(collector.getBlockPos()).contains(sourceCenter)
                        || collector.isAttachedInventoryAt(sourcePos)
                        || !collector.canCollect(remaining)) {
                    continue;
                }

                ItemStack simulatedRemainder = collector.insertCollectedStack(remaining, true);
                if (!simulatedRemainder.isEmpty()) continue;

                ItemStack toInsert = remaining.copy();
                source.setStackInSlot(slot, ItemStack.EMPTY);

                ItemStack insertionRemainder = collector.insertCollectedStack(toInsert, false);
                if (!insertionRemainder.isEmpty()) {
                    restoreToSourceSlot(source, slot, insertionRemainder, insertionRemainder.getCount());
                }
                remaining = source.getStackInSlot(slot);
            }
            if (!source.getStackInSlot(slot).isEmpty()) fullyDrained = false;
        }
        return fullyDrained;
    }

    private static void restoreToSourceSlot(IItemHandlerModifiable source, int slot, ItemStack template, int amount) {
        ItemStack current = source.getStackInSlot(slot);
        ItemStack restored = current.isEmpty() ? template.copyWithCount(amount) : current.copy();
        if (!current.isEmpty()) restored.grow(amount);
        source.setStackInSlot(slot, restored);
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        LevelIndex index = LEVELS.get(level);
        if (index == null) return;
        if (index.consumeBypass(itemEntity)) return;

        Vec3 position = itemEntity.position();
        Set<AdvancedItemCollectorBE> collectors = index.at(position);
        if (collectors.isEmpty()) return;

        if (collectors.size() == 1) {
            AdvancedItemCollectorBE collector = collectors.iterator().next();
            if (index.enqueueIfCollectible(collector, itemEntity, position)) {
                event.setCanceled(true);
            }
            return;
        }

        int originalCount = itemEntity.getItem().getCount();
        ItemStack remainder = itemEntity.getItem();
        for (AdvancedItemCollectorBE collector : collectors) {
            remainder = collector.collect(remainder, position);
            if (remainder.isEmpty()) {
                event.setCanceled(true);
                return;
            }
        }
        if (remainder.getCount() != originalCount) itemEntity.setItem(remainder);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        for (Map.Entry<ServerLevel, LevelIndex> entry : LEVELS.entrySet()) {
            if (entry.getKey().getServer() == event.getServer()) {
                entry.getValue().flush(entry.getKey());
            }
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) LEVELS.remove(level);
    }

    private static final class LevelIndex {
        private final Map<Long, Set<AdvancedItemCollectorBE>> byChunk = new java.util.HashMap<>();
        private final Map<AdvancedItemCollectorBE, Set<Long>> chunksByCollector = new IdentityHashMap<>();
        private final Map<AdvancedItemCollectorBE, AABB> areasByCollector = new IdentityHashMap<>();
        private Map<AdvancedItemCollectorBE, Map<ItemStackKey, PendingGroup>> pending = new IdentityHashMap<>();
        private final Set<ItemEntity> bypass = Collections.newSetFromMap(new IdentityHashMap<>());
        private final List<AdvancedItemCollectorBE> scanOrder = new ArrayList<>();
        private final Map<AdvancedItemCollectorBE, Long> nextExistingScan = new IdentityHashMap<>();
        private int scanCursor;

        private void add(AdvancedItemCollectorBE collector) {
            AABB area = collector.getAABB(collector.getBlockPos());
            int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.minX));
            int maxChunkX = SectionPos.blockToSectionCoord(Mth.ceil(area.maxX) - 1);
            int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.minZ));
            int maxChunkZ = SectionPos.blockToSectionCoord(Mth.ceil(area.maxZ) - 1);
            areasByCollector.put(collector, area);
            Set<Long> chunks = new LinkedHashSet<>();
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long key = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
                    chunks.add(key);
                    byChunk.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(collector);
                }
            }
            chunksByCollector.put(collector, chunks);
            scanOrder.add(collector);
            nextExistingScan.put(collector, 0L);
        }

        private void remove(AdvancedItemCollectorBE collector) {
            areasByCollector.remove(collector);
            nextExistingScan.remove(collector);
            int scanIndex = scanOrder.indexOf(collector);
            if (scanIndex >= 0) {
                scanOrder.remove(scanIndex);
                if (scanIndex < scanCursor) scanCursor--;
                if (scanCursor >= scanOrder.size()) scanCursor = 0;
            }
            Set<Long> chunks = chunksByCollector.remove(collector);
            if (chunks == null) return;
            for (long chunk : chunks) {
                Set<AdvancedItemCollectorBE> collectors = byChunk.get(chunk);
                if (collectors == null) continue;
                collectors.remove(collector);
                if (collectors.isEmpty()) byChunk.remove(chunk);
            }
        }

        private Set<AdvancedItemCollectorBE> at(Vec3 position) {
            long key = net.minecraft.world.level.ChunkPos.asLong(
                    SectionPos.blockToSectionCoord(Mth.floor(position.x)),
                    SectionPos.blockToSectionCoord(Mth.floor(position.z)));
            return byChunk.getOrDefault(key, Collections.emptySet());
        }

        private boolean consumeBypass(ItemEntity entity) {
            return bypass.remove(entity);
        }

        private boolean enqueueIfCollectible(AdvancedItemCollectorBE collector, ItemEntity entity, Vec3 position) {
            AABB area = areasByCollector.get(collector);
            if (area == null || !area.contains(position)) return false;

            ItemStack stack = entity.getItem();
            ItemStackKey key = new ItemStackKey(stack, true);
            Map<ItemStackKey, PendingGroup> groups = pending.computeIfAbsent(collector, ignored -> new HashMap<>());
            PendingGroup group = groups.get(key);
            if (group == null) {
                if (!collector.canCollect(stack)) {
                    if (groups.isEmpty()) pending.remove(collector);
                    return false;
                }
                group = new PendingGroup();
                groups.put(key, group);
            }
            group.add(entity);
            return true;
        }

        private void flush(ServerLevel level) {
            scanExistingItems(level);
            if (pending.isEmpty()) return;

            Map<AdvancedItemCollectorBE, Map<ItemStackKey, PendingGroup>> current = pending;
            pending = new IdentityHashMap<>();
            for (Map.Entry<AdvancedItemCollectorBE, Map<ItemStackKey, PendingGroup>> collectorEntry : current.entrySet()) {
                AdvancedItemCollectorBE collector = collectorEntry.getKey();
                for (Map.Entry<ItemStackKey, PendingGroup> groupEntry : collectorEntry.getValue().entrySet()) {
                    PendingGroup group = groupEntry.getValue();
                    long accepted = insertBatch(collector, groupEntry.getKey(), group.totalCount);
                    restoreRemainders(level, group.entities, accepted);
                }
            }
        }

        private void scanExistingItems(ServerLevel level) {
            if (!JDTEConfig.COMMON.advancedItemCollectorExistingItemScanEnabled.get()
                    || scanOrder.isEmpty()) {
                return;
            }

            long gameTime = level.getGameTime();
            AdvancedItemCollectorBE collector = null;
            for (int checked = 0; checked < scanOrder.size(); checked++) {
                if (scanCursor >= scanOrder.size()) scanCursor = 0;
                AdvancedItemCollectorBE candidate = scanOrder.get(scanCursor++);
                if (gameTime >= nextExistingScan.getOrDefault(candidate, 0L)) {
                    collector = candidate;
                    break;
                }
            }
            if (collector == null) return;

            int interval = JDTEConfig.COMMON.advancedItemCollectorExistingItemScanInterval.get();
            nextExistingScan.put(collector, gameTime + interval);
            AABB area = areasByCollector.get(collector);
            if (area == null || collector.isRemoved()) return;

            int limit = JDTEConfig.COMMON.advancedItemCollectorExistingItemScanLimit.get();
            int[] matched = {0};
            AdvancedItemCollectorBE selectedCollector = collector;
            List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, area, entity -> {
                if (matched[0] >= limit || !entity.isAlive()
                        || !selectedCollector.canCollect(entity.getItem())) {
                    return false;
                }
                matched[0]++;
                return true;
            });

            for (ItemEntity entity : entities) {
                ItemStack remainder = collector.collect(entity.getItem(), entity.position());
                if (remainder.isEmpty()) {
                    entity.discard();
                } else if (remainder.getCount() != entity.getItem().getCount()) {
                    entity.setItem(remainder);
                }
            }
        }

        private long insertBatch(AdvancedItemCollectorBE collector, ItemStackKey key, long totalCount) {
            long remaining = totalCount;
            while (remaining > 0) {
                int requestCount = (int) Math.min(Integer.MAX_VALUE, remaining);
                ItemStack remainder = collector.insertCollectedStack(key.getStack(requestCount));
                int inserted = requestCount - remainder.getCount();
                if (inserted <= 0) break;
                remaining -= inserted;
                if (inserted < requestCount) break;
            }
            return totalCount - remaining;
        }

        private void restoreRemainders(ServerLevel level, List<ItemEntity> entities, long accepted) {
            for (ItemEntity entity : entities) {
                int count = entity.getItem().getCount();
                if (accepted >= count) {
                    accepted -= count;
                    continue;
                }
                if (accepted > 0) {
                    entity.setItem(entity.getItem().copyWithCount(count - (int) accepted));
                    accepted = 0;
                }
                bypass.add(entity);
                if (!level.addFreshEntity(entity)) bypass.remove(entity);
            }
        }
    }

    private static final class PendingGroup {
        private final List<ItemEntity> entities = new ArrayList<>();
        private long totalCount;

        private void add(ItemEntity entity) {
            entities.add(entity);
            totalCount += entity.getItem().getCount();
        }
    }
}
