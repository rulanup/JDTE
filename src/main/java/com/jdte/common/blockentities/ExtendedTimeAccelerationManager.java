package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.entities.TimeAcceleratorEffectEntity;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExtendedTimeAccelerationManager {
    private static final TagKey<Block> JDT_TICK_SPEED_DENY = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath("justdirethings", "tick_speed_deny"));
    private static final boolean AE2_LOADED = ModList.get().isLoaded("ae2");
    private static final Map<ServerLevel, LevelState> LEVELS = new IdentityHashMap<>();
    private static final Map<MinecraftServer, Long> TICK_STARTS = new IdentityHashMap<>();
    private static final Map<MinecraftServer, Integer> LEVEL_CURSORS = new IdentityHashMap<>();

    private ExtendedTimeAccelerationManager() {
    }

    public static void submit(TimeAcceleratorBE accelerator) {
        if (accelerator.getLevel() instanceof ServerLevel level && !accelerator.isRemoved()) {
            LEVELS.computeIfAbsent(level, ignored -> new LevelState()).submitted.add(accelerator);
        }
    }

    public static void onServerTickPre(ServerTickEvent.Pre event) {
        TICK_STARTS.put(event.getServer(), System.nanoTime());
    }

    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long now = System.nanoTime();
        Long recordedStart = TICK_STARTS.remove(server);
        long tickStart = recordedStart == null ? now : recordedStart;
        long targetNanos = (long) (JDTEConfig.COMMON.timeAcceleratorTargetMspt.get() * 1_000_000.0D);
        long deadline = tickStart + targetNanos;

        List<Map.Entry<ServerLevel, LevelState>> states = new ArrayList<>();
        for (Map.Entry<ServerLevel, LevelState> entry : LEVELS.entrySet()) {
            if (entry.getKey().getServer() == server && entry.getValue().hasWork()) {
                states.add(entry);
            }
        }
        if (states.isEmpty()) {
            return;
        }

        int startIndex = Math.floorMod(LEVEL_CURSORS.getOrDefault(server, 0), states.size());
        LEVEL_CURSORS.put(server, startIndex + 1);
        for (int offset = 0; offset < states.size(); offset++) {
            Map.Entry<ServerLevel, LevelState> entry = states.get((startIndex + offset) % states.size());
            int levelsLeft = states.size() - offset;
            now = System.nanoTime();
            long levelDeadline = levelsLeft == 1 ? deadline : now + Math.max(0L, deadline - now) / levelsLeft;
            entry.getValue().prepare(entry.getKey(), levelDeadline);
            entry.getValue().execute(entry.getKey(), levelDeadline);
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LEVELS.remove(level);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        MinecraftServer server = event.getServer();
        LEVELS.keySet().removeIf(level -> level.getServer() == server);
        TICK_STARTS.remove(server);
        LEVEL_CURSORS.remove(server);
    }

    private enum TargetKind {
        BLOCK_ENTITY,
        RANDOM_TICK,
        AE2_GRID
    }

    private record TargetKey(BlockPos pos, TargetKind kind) {
        private TargetKey {
            pos = pos.immutable();
        }
    }

    private record ExecutionResult(int executed, boolean valid, boolean idle) {
        private static ExecutionResult invalid() {
            return new ExecutionResult(0, false, true);
        }
    }

    private static final class AcceleratorContext {
        private final TimeAcceleratorBE accelerator;
        private final AABB area;
        private final int multiplier;
        private final int fluidCost;
        private final int energyCost;
        private final Set<TargetKey> targets = new LinkedHashSet<>();

        private AcceleratorContext(TimeAcceleratorBE accelerator, AABB area, int multiplier,
                                   int fluidCost, int energyCost) {
            this.accelerator = accelerator;
            this.area = area;
            this.multiplier = multiplier;
            this.fluidCost = fluidCost;
            this.energyCost = energyCost;
        }

        private boolean contains(BlockPos pos) {
            return pos.getX() >= area.minX && pos.getX() < area.maxX
                    && pos.getY() >= area.minY && pos.getY() < area.maxY
                    && pos.getZ() >= area.minZ && pos.getZ() < area.maxZ;
        }
    }

    private static final class PendingTarget {
        private long virtualTicks;
        private final Map<TimeAcceleratorBE, Contribution> contributions = new IdentityHashMap<>();

        private void add(TimeAcceleratorBE accelerator, long ticks, int multiplier) {
            Contribution contribution = contributions.computeIfAbsent(accelerator, ignored -> new Contribution());
            contribution.virtualTicks += ticks;
            contribution.multiplier = multiplier;
            virtualTicks += ticks;
        }

        private void retainContributors(Set<TimeAcceleratorBE> active) {
            var iterator = contributions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TimeAcceleratorBE, Contribution> entry = iterator.next();
                if (!active.contains(entry.getKey())) {
                    virtualTicks -= entry.getValue().virtualTicks;
                    iterator.remove();
                }
            }
        }

        private void consume(long ticks) {
            long remaining = ticks;
            var iterator = contributions.entrySet().iterator();
            while (iterator.hasNext() && remaining > 0) {
                Contribution contribution = iterator.next().getValue();
                long consumed = Math.min(remaining, contribution.virtualTicks);
                contribution.virtualTicks -= consumed;
                virtualTicks -= consumed;
                remaining -= consumed;
                if (contribution.virtualTicks <= 0) {
                    iterator.remove();
                }
            }
        }

        private int displayMultiplier() {
            int total = 0;
            for (Contribution contribution : contributions.values()) {
                total = saturatingAdd(total, contribution.multiplier);
            }
            return Math.max(1, total);
        }
    }

    private static final class Contribution {
        private long virtualTicks;
        private int multiplier;
    }

    private static int saturatingAdd(int left, int right) {
        long value = (long) left + right;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static final class RandomTargetCache {
        private AABB area;
        private long refreshAt;
        private List<TargetKey> targets = List.of();
        private List<TargetKey> rebuildingTargets = new ArrayList<>();
        private int minX;
        private int minY;
        private int minZ;
        private int sizeX;
        private int sizeY;
        private int sizeZ;
        private int scanIndex;
        private int scanVolume;
        private boolean rebuilding;
    }

    private static final class LevelState {
        private final Set<TimeAcceleratorBE> submitted = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<TargetKey, PendingTarget> pending = new LinkedHashMap<>();
        private final ArrayDeque<TargetKey> queue = new ArrayDeque<>();
        private final Set<TargetKey> queued = new LinkedHashSet<>();
        private final Map<TimeAcceleratorBE, RandomTargetCache> randomTargets = new IdentityHashMap<>();
        private final Map<BlockPos, Long> nextEffectTick = new LinkedHashMap<>();

        private boolean hasWork() {
            return !submitted.isEmpty() || !pending.isEmpty();
        }

        private void prepare(ServerLevel level, long deadline) {
            if (level.getGameTime() % 200L == 0L) {
                nextEffectTick.entrySet().removeIf(entry -> entry.getValue() + 200L < level.getGameTime());
            }
            Set<TimeAcceleratorBE> active = Collections.newSetFromMap(new IdentityHashMap<>());
            active.addAll(submitted);
            retainActiveContributors(active);
            randomTargets.keySet().removeIf(accelerator -> !active.contains(accelerator));
            if (submitted.isEmpty()) {
                return;
            }

            List<AcceleratorContext> contexts = new ArrayList<>();
            Map<Long, List<AcceleratorContext>> byChunk = new LinkedHashMap<>();
            for (TimeAcceleratorBE accelerator : submitted) {
                if (accelerator.isRemoved() || accelerator.getLevel() != level) {
                    continue;
                }
                int multiplier = accelerator.getEffectiveMultiplier();
                int fluidCost = accelerator.getFluidDrainAmount(multiplier);
                int energyCost = accelerator.getEnergyCost(multiplier);
                if (!accelerator.hasResources(fluidCost, energyCost)) {
                    continue;
                }

                AABB area = accelerator.getAABB(accelerator.getBlockPos());
                AcceleratorContext context = new AcceleratorContext(accelerator, area, multiplier, fluidCost, energyCost);
                contexts.add(context);
                int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.minX));
                int maxChunkX = SectionPos.blockToSectionCoord(Mth.ceil(area.maxX) - 1);
                int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.minZ));
                int maxChunkZ = SectionPos.blockToSectionCoord(Mth.ceil(area.maxZ) - 1);
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        byChunk.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), ignored -> new ArrayList<>()).add(context);
                    }
                }
            }
            submitted.clear();
            randomTargets.keySet().removeIf(accelerator -> accelerator.isRemoved() || accelerator.getLevel() != level);
            if (contexts.isEmpty()) {
                return;
            }

            discoverBlockEntities(level, byChunk);
            for (AcceleratorContext context : contexts) {
                context.targets.addAll(getRandomTargets(level, context, deadline));
            }

            long maxPending = JDTEConfig.COMMON.timeAcceleratorMaxPendingTicks.get();
            for (AcceleratorContext context : contexts) {
                boolean acceptsWork = context.targets.stream().anyMatch(target -> canAccept(target, maxPending));
                if (!acceptsWork || !context.accelerator.hasResources(context.fluidCost, context.energyCost)) {
                    continue;
                }
                context.accelerator.consumeResources(context.fluidCost, context.energyCost);
                for (TargetKey target : context.targets) {
                    enqueue(target, context.accelerator, context.multiplier, maxPending);
                }
            }
        }

        private void retainActiveContributors(Set<TimeAcceleratorBE> active) {
            boolean removed = false;
            var iterator = pending.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TargetKey, PendingTarget> entry = iterator.next();
                entry.getValue().retainContributors(active);
                if (entry.getValue().virtualTicks <= 0) {
                    iterator.remove();
                    removed = true;
                }
            }
            if (removed) {
                queue.removeIf(target -> !pending.containsKey(target));
                queued.retainAll(pending.keySet());
            }
        }

        private void discoverBlockEntities(ServerLevel level, Map<Long, List<AcceleratorContext>> byChunk) {
            for (Map.Entry<Long, List<AcceleratorContext>> entry : byChunk.entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey());
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
                if (chunk == null) {
                    continue;
                }
                for (Map.Entry<BlockPos, BlockEntity> blockEntityEntry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = blockEntityEntry.getKey();
                    BlockEntity blockEntity = blockEntityEntry.getValue();
                    if (blockEntity instanceof TimeAcceleratorMachine || blockEntity.isRemoved()) {
                        continue;
                    }
                    TargetKind kind = getBlockEntityTargetKind(level, pos, blockEntity);
                    if (kind == null) {
                        continue;
                    }
                    BlockState state = blockEntity.getBlockState();
                    for (AcceleratorContext context : entry.getValue()) {
                        if (context.contains(pos) && context.accelerator.isBlockValidFilter(level, pos, state)) {
                            context.targets.add(new TargetKey(pos, kind));
                        }
                    }
                }
            }
        }

        private List<TargetKey> getRandomTargets(ServerLevel level, AcceleratorContext context, long deadline) {
            RandomTargetCache cache = randomTargets.computeIfAbsent(context.accelerator, ignored -> new RandomTargetCache());
            long gameTime = level.getGameTime();
            boolean areaChanged = cache.area == null || !cache.area.equals(context.area);
            if (!cache.rebuilding && !areaChanged && gameTime < cache.refreshAt) {
                return cache.targets;
            }
            if (!cache.rebuilding) {
                beginRandomTargetRefresh(cache, context.area, areaChanged);
            }

            int checked = 0;
            BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
            while (cache.scanIndex < cache.scanVolume && System.nanoTime() < deadline) {
                int index = cache.scanIndex++;
                int xOffset = index % cache.sizeX;
                int remainder = index / cache.sizeX;
                int zOffset = remainder % cache.sizeZ;
                int yOffset = remainder / cache.sizeZ;
                scanPos.set(cache.minX + xOffset, cache.minY + yOffset, cache.minZ + zOffset);
                BlockState state = level.getBlockState(scanPos);
                if (state.hasBlockEntity() || !state.isRandomlyTicking()) {
                    continue;
                }
                if (MiscTools.isValidTickAccelBlock(level, state, null)
                        && context.accelerator.isBlockValidFilter(level, scanPos, state)) {
                    cache.rebuildingTargets.add(new TargetKey(scanPos, TargetKind.RANDOM_TICK));
                }
                if ((++checked & 255) == 0 && System.nanoTime() >= deadline) {
                    break;
                }
            }
            if (cache.scanIndex >= cache.scanVolume) {
                cache.targets = List.copyOf(cache.rebuildingTargets);
                cache.rebuildingTargets = new ArrayList<>();
                cache.rebuilding = false;
                cache.refreshAt = gameTime + JDTEConfig.COMMON.timeAcceleratorRandomRefreshInterval.get();
            }
            return cache.targets;
        }

        private void beginRandomTargetRefresh(RandomTargetCache cache, AABB area, boolean areaChanged) {
            cache.area = area;
            cache.minX = Mth.floor(area.minX);
            cache.minY = Mth.floor(area.minY);
            cache.minZ = Mth.floor(area.minZ);
            cache.sizeX = Math.max(1, Mth.ceil(area.maxX) - cache.minX);
            cache.sizeY = Math.max(1, Mth.ceil(area.maxY) - cache.minY);
            cache.sizeZ = Math.max(1, Mth.ceil(area.maxZ) - cache.minZ);
            long volume = (long) cache.sizeX * cache.sizeY * cache.sizeZ;
            cache.scanVolume = (int) Math.min(Integer.MAX_VALUE, volume);
            cache.scanIndex = 0;
            cache.rebuildingTargets = new ArrayList<>();
            cache.rebuilding = true;
            if (areaChanged) {
                cache.targets = List.of();
            }
        }

        @SuppressWarnings("unchecked")
        private TargetKind getBlockEntityTargetKind(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
            BlockState state = blockEntity.getBlockState();
            if (AE2_LOADED && JDTEConfig.COMMON.timeAcceleratorAE2Enabled.get()
                    && !state.is(JDT_TICK_SPEED_DENY)
                    && ExtendedTimeAcceleratorAE2Integration.hasTickable(level, pos)) {
                return TargetKind.AE2_GRID;
            }
            BlockEntityTicker<BlockEntity> ticker = state.getTicker(level, (BlockEntityType<BlockEntity>) blockEntity.getType());
            if (ticker != null && MiscTools.isValidTickAccelBlock(level, state, blockEntity)) {
                return TargetKind.BLOCK_ENTITY;
            }
            return null;
        }

        private boolean canAccept(TargetKey target, long maxPending) {
            PendingTarget existing = pending.get(target);
            return existing == null || existing.virtualTicks < maxPending;
        }

        private void enqueue(TargetKey target, TimeAcceleratorBE accelerator, int multiplier, long maxPending) {
            PendingTarget work = pending.get(target);
            if (work == null) {
                work = new PendingTarget();
                pending.put(target, work);
                addToQueue(target);
            }
            long accepted = Math.min((long) multiplier, maxPending - work.virtualTicks);
            if (accepted <= 0) {
                return;
            }
            work.add(accelerator, accepted, multiplier);
        }

        private void execute(ServerLevel level, long deadline) {
            int batchSize = JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get();
            while (!queue.isEmpty() && System.nanoTime() < deadline) {
                TargetKey target = queue.removeFirst();
                queued.remove(target);
                PendingTarget work = pending.get(target);
                if (work == null) {
                    continue;
                }
                int requested = (int) Math.min(work.virtualTicks, batchSize);
                ExecutionResult result = executeTarget(level, target, requested, deadline);
                if (!result.valid) {
                    pending.remove(target);
                    nextEffectTick.remove(target.pos());
                    continue;
                }
                if (result.executed <= 0) {
                    addToQueue(target);
                    break;
                }

                int displayMultiplier = work.displayMultiplier();
                work.consume(result.executed);
                spawnEffect(level, target.pos(), displayMultiplier);
                if (result.idle || work.virtualTicks <= 0) {
                    pending.remove(target);
                } else {
                    addToQueue(target);
                }
            }
        }

        private void addToQueue(TargetKey target) {
            if (queued.add(target)) {
                queue.addLast(target);
            }
        }

        private ExecutionResult executeTarget(ServerLevel level, TargetKey target, int requested, long deadline) {
            return switch (target.kind()) {
                case BLOCK_ENTITY -> executeBlockEntity(level, target.pos(), requested, deadline);
                case RANDOM_TICK -> executeRandomTicks(level, target.pos(), requested, deadline);
                case AE2_GRID -> {
                    ExtendedTimeAcceleratorAE2Integration.Result result =
                            ExtendedTimeAcceleratorAE2Integration.accelerate(level, target.pos(), requested, deadline);
                    yield new ExecutionResult(result.executed(), result.valid(), result.idle());
                }
            };
        }

        @SuppressWarnings("unchecked")
        private ExecutionResult executeBlockEntity(ServerLevel level, BlockPos pos, int requested, long deadline) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || blockEntity.isRemoved() || blockEntity instanceof TimeAcceleratorMachine) {
                return ExecutionResult.invalid();
            }
            BlockEntityTicker<BlockEntity> ticker = blockEntity.getBlockState().getTicker(
                    level, (BlockEntityType<BlockEntity>) blockEntity.getType());
            if (ticker == null || !MiscTools.isValidTickAccelBlock(level, blockEntity.getBlockState(), blockEntity)) {
                return ExecutionResult.invalid();
            }
            int executed = 0;
            for (; executed < requested && !blockEntity.isRemoved() && System.nanoTime() < deadline; executed++) {
                ticker.tick(level, pos, blockEntity.getBlockState(), blockEntity);
            }
            return new ExecutionResult(executed, true, false);
        }

        private ExecutionResult executeRandomTicks(ServerLevel level, BlockPos pos, int requested, long deadline) {
            BlockState state = level.getBlockState(pos);
            if (state.hasBlockEntity() || !state.isRandomlyTicking()
                    || !MiscTools.isValidTickAccelBlock(level, state, null)) {
                return ExecutionResult.invalid();
            }
            int executed = 0;
            for (; executed < requested && System.nanoTime() < deadline; executed++) {
                state.randomTick(level, pos, level.random);
            }
            return new ExecutionResult(executed, true, false);
        }

        private void spawnEffect(ServerLevel level, BlockPos pos, int multiplier) {
            long gameTime = level.getGameTime();
            if (gameTime < nextEffectTick.getOrDefault(pos, Long.MIN_VALUE)) {
                return;
            }
            nextEffectTick.put(pos.immutable(), gameTime + 10L);
            boolean present = !level.getEntitiesOfClass(TimeAcceleratorEffectEntity.class, new AABB(pos), entity -> true).isEmpty();
            if (!present) {
                level.addFreshEntity(new TimeAcceleratorEffectEntity(level, pos, Math.max(1, multiplier)));
            }
        }

    }
}
