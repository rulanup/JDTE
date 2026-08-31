package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.entities.TimeAcceleratorEffectEntity;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.content.JDTEContentControl;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.common.upgrades.UpgradeHelper;
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
import net.minecraft.world.level.block.Blocks;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ExtendedTimeAccelerationManager {
    private static final TagKey<Block> JDT_TICK_SPEED_DENY = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath("justdirethings", "tick_speed_deny"));
    private static final boolean AE2_LOADED = ModList.get().isLoaded("ae2");
    private static final Map<ServerLevel, LevelState> LEVELS = new IdentityHashMap<>();
    private static final Map<MinecraftServer, Integer> LEVEL_CURSORS = new IdentityHashMap<>();

    private ExtendedTimeAccelerationManager() {
    }

    public static void submit(TimeAcceleratorBE accelerator) {
        if (accelerator.getLevel() instanceof ServerLevel level && !accelerator.isRemoved()
                && JDTEContentControl.current().isBlockEnabled(accelerator.getBlockState().getBlock())) {
            LEVELS.computeIfAbsent(level, ignored -> new LevelState()).submitted.add(accelerator);
        }
    }

    public static void deactivate(TimeAcceleratorBE accelerator) {
        if (accelerator.getLevel() instanceof ServerLevel level) {
            LevelState state = LEVELS.get(level);
            if (state != null) {
                state.deactivate(accelerator);
            }
        }
    }

    public static boolean submitWand(UltimateTimeWandEntity wand, ServerLevel level,
                                     BlockPos target, int requestedTicks) {
        if (wand.isRemoved() || wand.level() != level || requestedTicks <= 0) {
            return false;
        }
        Optional<TargetKey> resolved = resolveTargetKey(level, target, true);
        if (resolved.isEmpty()) {
            return false;
        }
        LevelState state = LEVELS.computeIfAbsent(level, ignored -> new LevelState());
        state.submittedWands.put(wand,
                new WandSubmission(resolved.get(), requestedTicks));
        return true;
    }

    static boolean isCurrentWandTarget(BlockPos pendingPos, UltimateTimeWandTargetRuntime.Route pendingRoute,
                                       BlockPos submittedPos, UltimateTimeWandTargetRuntime.Route submittedRoute) {
        return pendingPos.equals(submittedPos) && pendingRoute == submittedRoute;
    }

    static boolean isCurrentWandTarget(TargetKey pendingTarget, TargetKey submittedTarget) {
        return pendingTarget.targetLevel() == submittedTarget.targetLevel()
                && pendingTarget.pos().equals(submittedTarget.pos())
                && routeFor(pendingTarget) == routeFor(submittedTarget);
    }

    private static UltimateTimeWandTargetRuntime.Route routeFor(TargetKey target) {
        return target.kind() == TargetKind.AE2_GRID
                ? UltimateTimeWandTargetRuntime.Route.AE2
                : UltimateTimeWandTargetRuntime.Route.ORDINARY;
    }

    static boolean shouldRecheckAe2Target(boolean ae2AccelerationConfigured, boolean wandRoute) {
        return ae2AccelerationConfigured || wandRoute;
    }

    static boolean isCurrentTarget(TargetKey expected, Optional<TargetKey> current) {
        return current.isPresent() && current.get().equals(expected);
    }

    static TimeAccelerationWorkQueue.ExecutionResult executeIfCurrentRoute(
            TargetKey pending, Optional<TargetKey> current, int requested, long remainingBudget,
            TargetExecution executor) {
        if (current.isEmpty() || !isCurrentWandTarget(pending, current.get())) {
            return new TimeAccelerationWorkQueue.ExecutionResult(0, false, true);
        }
        return executor.execute(current.get(), requested, remainingBudget);
    }

    static TimeAccelerationWorkQueue.ExecutionResult executeIfCurrentTarget(
            TargetKey expected, Optional<TargetKey> current, int requested, long remainingBudget,
            TargetExecution executor) {
        if (!isCurrentTarget(expected, current)) {
            return new TimeAccelerationWorkQueue.ExecutionResult(0, false, true);
        }
        return executor.execute(expected, requested, remainingBudget);
    }

    static void flushCoalescedTargets(Set<CoalescedAcceleratedMachine> targets) {
        targets.forEach(CoalescedAcceleratedMachine::flushAcceleratedTicks);
        targets.clear();
    }

    @FunctionalInterface
    interface TargetExecution {
        TimeAccelerationWorkQueue.ExecutionResult execute(
                TargetKey target, int requested, long remainingBudget);
    }

    static Optional<TimeAccelerationTarget> resolveTimeAccelerationTarget(ServerLevel level, BlockPos pos) {
        return resolveTimeAccelerationTarget(level, pos, ExtendedTimeAccelerationManager::getLoadedBlockEntity);
    }

    static Optional<TimeAccelerationTarget> resolveTimeAccelerationTarget(
            ServerLevel level, BlockPos pos, TargetLookup blockEntityLookup) {
        if (level == null || pos == null || blockEntityLookup == null) {
            return Optional.empty();
        }
        IdentityHashMap<ServerLevel, Set<BlockPos>> visited = new IdentityHashMap<>();
        ServerLevel currentLevel = level;
        BlockPos currentPos = pos.immutable();
        for (int depth = 0; depth <= 10; depth++) {
            Set<BlockPos> positions = visited.computeIfAbsent(currentLevel,
                    ignored -> new LinkedHashSet<>());
            if (!positions.add(currentPos)) {
                return Optional.empty();
            }
            BlockEntity blockEntity = blockEntityLookup.get(currentLevel, currentPos);
            if (blockEntity != null && blockEntity.isRemoved()) {
                return Optional.empty();
            }
            if (!(blockEntity instanceof TimeAccelerationTargetProxy proxy)) {
                return Optional.of(new TimeAccelerationTarget(currentLevel, currentPos));
            }
            TimeAccelerationTarget next = proxy.getTimeAccelerationTarget();
            if (next == null) {
                return Optional.empty();
            }
            if (depth == 10) {
                return Optional.empty();
            }
            currentLevel = next.level();
            currentPos = next.pos();
        }
        return Optional.empty();
    }

    private static boolean isAE2Target(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        return !state.is(JDT_TICK_SPEED_DENY)
                && ExtendedTimeAcceleratorAE2Integration.hasTickable(level, pos);
    }

    @SuppressWarnings("unchecked")
    private static boolean isBlockEntityTarget(ServerLevel level, BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        BlockEntityTicker<BlockEntity> ticker = state.getTicker(level,
                (BlockEntityType<BlockEntity>) blockEntity.getType());
        return ticker != null && MiscTools.isValidTickAccelBlock(level, state, blockEntity);
    }

    static TargetKind selectTargetKind(boolean hasAe2Service, boolean ae2AccelerationEnabled,
                                       boolean hasBlockEntityTicker) {
        if (hasAe2Service && ae2AccelerationEnabled) {
            return TargetKind.AE2_GRID;
        }
        return hasBlockEntityTicker ? TargetKind.BLOCK_ENTITY : null;
    }

    static boolean isContributorFilterValid(TargetKey target, Object source,
                                            LoadedStateLookup loadedStateLookup,
                                            ContributorFilter filter) {
        if (!(source instanceof TimeAcceleratorBE)) {
            return true;
        }
        Optional<BlockState> state = loadedStateLookup.get(target.targetLevel(), target.pos());
        return state.isPresent()
                && filter.test(source, target.targetLevel(), target.pos(), state.get());
    }

    @FunctionalInterface
    interface LoadedStateLookup {
        Optional<BlockState> get(ServerLevel level, BlockPos pos);
    }

    @FunctionalInterface
    interface ContributorFilter {
        boolean test(Object source, ServerLevel level, BlockPos pos, BlockState state);
    }

    static Optional<BlockState> getLoadedBlockState(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = getLoadedChunk(level, pos);
        return chunk == null ? Optional.empty() : Optional.of(chunk.getBlockState(pos));
    }

    static <S> void reconcilePreparedTargets(TimeAccelerationWorkQueue<S, TargetKey> queue,
                                             Collection<TargetKey> targets, S source) {
        queue.reconcileContributor(source, Set.copyOf(targets));
    }

    static <S> void enqueuePreparedTargets(TimeAccelerationWorkQueue<S, TargetKey> queue,
                                           Collection<TargetKey> targets, S source,
                                           int workTicks, int displayMultiplier, long maxPending) {
        for (TargetKey target : targets) {
            queue.enqueue(target, source, workTicks, displayMultiplier, maxPending);
        }
    }

    static <S> long executePendingTargets(TimeAccelerationWorkQueue<S, TargetKey> queue,
                                          long maxExecutions, int batchSize,
                                          java.util.function.BiPredicate<TargetKey, S> keepContributor,
                                          TimeAccelerationWorkQueue.Executor<TargetKey> executor,
                                          TimeAccelerationWorkQueue.ExecutionListener<TargetKey> listener) {
        return queue.execute(maxExecutions, batchSize, keepContributor, executor, listener);
    }

    static Set<TargetKey> resolveDistinctTargetKeys(ServerLevel level, Collection<BlockPos> sources,
                                                     TargetLookup lookup, TargetKind kind) {
        Set<TargetKey> targets = new LinkedHashSet<>();
        if (sources == null || lookup == null || kind == null) {
            return targets;
        }
        for (BlockPos source : sources) {
            resolveTimeAccelerationTarget(level, source, lookup)
                    .map(target -> new TargetKey(target.level(), target.pos(), kind))
                    .ifPresent(targets::add);
        }
        return targets;
    }

    static Optional<TargetKey> resolveTargetKey(ServerLevel level, BlockPos pos,
                                                  boolean ae2AccelerationEnabled) {
        Optional<TimeAccelerationTarget> target = resolveTimeAccelerationTarget(level, pos);
        return target.flatMap(resolved -> classifyTarget(resolved, ae2AccelerationEnabled));
    }

    private static Optional<TargetKey> classifyTarget(TimeAccelerationTarget target,
                                                       boolean ae2AccelerationEnabled) {
        ServerLevel level = target.level();
        BlockPos pos = target.pos();
        LevelChunk chunk = getLoadedChunk(level, pos);
        if (chunk == null) {
            return Optional.empty();
        }
        BlockEntity blockEntity = chunk.getBlockEntity(pos);
        BlockState state = chunk.getBlockState(pos);
        if (blockEntity != null) {
            if (blockEntity.isRemoved() || blockEntity instanceof TimeAcceleratorMachine) {
                return Optional.empty();
            }
            TargetKind kind = selectTargetKind(
                    isAE2Target(level, pos, blockEntity), ae2AccelerationEnabled,
                    isBlockEntityTarget(level, blockEntity));
            if (kind == TargetKind.AE2_GRID
                    && !MiscTools.isValidTickAccelBlock(level, state, blockEntity)) {
                return Optional.empty();
            }
            return kind == null ? Optional.empty() : Optional.of(new TargetKey(level, pos, kind));
        }
        if (!state.hasBlockEntity() && state.isRandomlyTicking()
                && MiscTools.isValidTickAccelBlock(level, state, null)) {
            return Optional.of(new TargetKey(level, pos, TargetKind.RANDOM_TICK));
        }
        return Optional.empty();
    }

    private static BlockEntity getLoadedBlockEntity(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = getLoadedChunk(level, pos);
        return chunk == null ? null : chunk.getBlockEntity(pos);
    }

    private static LevelChunk getLoadedChunk(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        return level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    @FunctionalInterface
    interface TargetLookup {
        BlockEntity get(ServerLevel level, BlockPos pos);
    }

    static AccelerationRequest requestAcceleration(TimeAcceleratorBE accelerator) {
        int displayMultiplier = accelerator.getEffectiveMultiplier();
        int workTicks = accelerator.getAccelerationWorkTicks(displayMultiplier);
        return new AccelerationRequest(displayMultiplier, workTicks);
    }

    static PreparedAcceleration prepareAcceleration(TimeAcceleratorBE accelerator) {
        AccelerationRequest request = requestAcceleration(accelerator);
        return prepareAcceleration(accelerator, request.displayMultiplier(), request.workTicks());
    }

    static Optional<PreparedAcceleration> prepareAcceptedAcceleration(
            TimeAcceleratorBE accelerator, AccelerationRequest request,
            long maxPendingTicks, long highestPendingTicks) {
        int acceptedWorkTicks = TimeAcceleratorExecutionPolicy.admittedWorkTicks(
                request.workTicks(), maxPendingTicks, highestPendingTicks);
        if (acceptedWorkTicks <= 0) {
            return Optional.empty();
        }
        acceptedWorkTicks = largestAffordableWorkTicks(
                accelerator, request.displayMultiplier(), acceptedWorkTicks);
        if (acceptedWorkTicks <= 0) {
            return Optional.empty();
        }
        return Optional.of(prepareAcceleration(
                accelerator, request.displayMultiplier(), acceptedWorkTicks));
    }

    private static int largestAffordableWorkTicks(
            TimeAcceleratorBE accelerator, int displayMultiplier, int requestedWorkTicks) {
        PreparedAcceleration requested = prepareAcceleration(
                accelerator, displayMultiplier, requestedWorkTicks);
        if (accelerator.hasResources(requested.fluidCost(), requested.energyCost())) {
            return requestedWorkTicks;
        }

        int low = 1;
        int high = requestedWorkTicks - 1;
        int largestAffordable = 0;
        while (low <= high) {
            int candidateWorkTicks = low + (high - low) / 2;
            PreparedAcceleration candidate = prepareAcceleration(
                    accelerator, displayMultiplier, candidateWorkTicks);
            if (accelerator.hasResources(candidate.fluidCost(), candidate.energyCost())) {
                largestAffordable = candidateWorkTicks;
                low = candidateWorkTicks + 1;
            } else {
                high = candidateWorkTicks - 1;
            }
        }
        return largestAffordable;
    }

    private static PreparedAcceleration prepareAcceleration(
            TimeAcceleratorBE accelerator, int displayMultiplier, int workTicks) {
        int fluidCost = accelerator.getFluidDrainAmount(workTicks);
        int energyCost = accelerator.getEnergyCost(workTicks);
        return new PreparedAcceleration(displayMultiplier, workTicks, fluidCost, energyCost);
    }

    static boolean payForSubmission(TimeAcceleratorBE accelerator, PreparedAcceleration prepared) {
        if (!accelerator.hasResources(prepared.fluidCost(), prepared.energyCost())) {
            return false;
        }
        consumePreparedResources(accelerator, prepared);
        return true;
    }

    static void consumePreparedResources(TimeAcceleratorBE accelerator, PreparedAcceleration prepared) {
        accelerator.consumeResources(prepared.workTicks(), prepared.energyCost());
    }

    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
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
        long executionBudget = TimeAcceleratorExecutionPolicy.globalBudget(
                JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.get(),
                JDTEConfig.COMMON.timeAcceleratorMaxExecutionsPerTick.get());
        int scanBudget = JDTEConfig.COMMON.timeAcceleratorMaxScannedBlocksPerTick.get();
        long executionBase = executionBudget == Long.MAX_VALUE ? Long.MAX_VALUE : executionBudget / states.size();
        long executionExtra = executionBudget == Long.MAX_VALUE ? 0L : executionBudget % states.size();
        int scanBase = scanBudget / states.size();
        int scanExtra = scanBudget % states.size();
        for (int offset = 0; offset < states.size(); offset++) {
            Map.Entry<ServerLevel, LevelState> entry = states.get((startIndex + offset) % states.size());
            int levelScanBudget = scanBase + (offset < scanExtra ? 1 : 0);
            long levelExecutionBudget = executionBudget == Long.MAX_VALUE
                    ? Long.MAX_VALUE
                    : executionBase + (offset < executionExtra ? 1L : 0L);
            entry.getValue().prepare(entry.getKey(), levelScanBudget);
            entry.getValue().execute(entry.getKey(), levelExecutionBudget);
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
        LEVEL_CURSORS.remove(server);
    }

    enum TargetKind {
        BLOCK_ENTITY,
        RANDOM_TICK,
        AE2_GRID
    }

    static record TargetKey(ServerLevel targetLevel, BlockPos pos, TargetKind kind) {
        TargetKey {
            java.util.Objects.requireNonNull(targetLevel, "targetLevel");
            pos = java.util.Objects.requireNonNull(pos, "pos").immutable();
            java.util.Objects.requireNonNull(kind, "kind");
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TargetKey that)) {
                return false;
            }
            return targetLevel == that.targetLevel
                    && pos.equals(that.pos)
                    && routeFor(this) == routeFor(that);
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(targetLevel);
            result = 31 * result + pos.hashCode();
            return 31 * result + routeFor(this).hashCode();
        }
    }

    record PreparedAcceleration(int displayMultiplier, int workTicks, int fluidCost, int energyCost) {
    }

    record AccelerationRequest(int displayMultiplier, int workTicks) {
    }

    private record WandSubmission(TargetKey target, int workTicks) {
    }

    private static final class TickBudget {
        private int remaining;

        private TickBudget(int remaining) {
            this.remaining = Math.max(0, remaining);
        }

        private boolean consumeOne() {
            if (remaining <= 0) {
                return false;
            }
            remaining--;
            return true;
        }
    }

    private static final class AcceleratorContext {
        private final TimeAcceleratorBE accelerator;
        private final AABB area;
        private final AccelerationRequest request;
        private final boolean ae2AccelerationEnabled;
        private final Set<TargetKey> targets = new LinkedHashSet<>();

        private AcceleratorContext(TimeAcceleratorBE accelerator, AABB area,
                                   AccelerationRequest request, boolean ae2AccelerationEnabled) {
            this.accelerator = accelerator;
            this.area = area;
            this.request = request;
            this.ae2AccelerationEnabled = ae2AccelerationEnabled;
        }

        private boolean contains(BlockPos pos) {
            return pos.getX() >= area.minX && pos.getX() < area.maxX
                    && pos.getY() >= area.minY && pos.getY() < area.maxY
                    && pos.getZ() >= area.minZ && pos.getZ() < area.maxZ;
        }
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

    interface LevelPreparationAdapter {
        long gameTime(ServerLevel level);

        boolean isActive(TimeAcceleratorBE accelerator, ServerLevel level);

        AccelerationRequest request(TimeAcceleratorBE accelerator);

        AABB area(TimeAcceleratorBE accelerator);

        boolean ae2Enabled(TimeAcceleratorBE accelerator);

        Map<BlockPos, BlockEntity> blockEntities(ServerLevel level, ChunkPos chunkPos);

        Optional<TargetKey> resolveTarget(ServerLevel level, BlockPos pos, boolean ae2Enabled);

        BlockState state(TargetKey target);

        boolean filter(TimeAcceleratorBE accelerator, TargetKey target, BlockState state);

        Optional<PreparedAcceleration> accept(TimeAcceleratorBE accelerator, AccelerationRequest request,
                                              long maxPending, long highestPending);

        boolean pay(TimeAcceleratorBE accelerator, PreparedAcceleration prepared);

        long maxPending();

        default boolean includeRandomTargets() {
            return true;
        }
    }

    private static final LevelPreparationAdapter SERVER_PREPARATION = new LevelPreparationAdapter() {
        @Override
        public long gameTime(ServerLevel level) {
            return level.getGameTime();
        }

        @Override
        public boolean isActive(TimeAcceleratorBE accelerator, ServerLevel level) {
            return !accelerator.isRemoved() && accelerator.getLevel() == level
                    && JDTEContentControl.current().isBlockEnabled(accelerator.getBlockState().getBlock());
        }

        @Override
        public AccelerationRequest request(TimeAcceleratorBE accelerator) {
            return requestAcceleration(accelerator);
        }

        @Override
        public AABB area(TimeAcceleratorBE accelerator) {
            return accelerator.getAABB(accelerator.getBlockPos());
        }

        @Override
        public boolean ae2Enabled(TimeAcceleratorBE accelerator) {
            return isAE2AccelerationConfigured() && UpgradeHelper.hasAEAccelerationUpgrade(accelerator);
        }

        @Override
        public Map<BlockPos, BlockEntity> blockEntities(ServerLevel level, ChunkPos chunkPos) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            return chunk == null ? Map.of() : chunk.getBlockEntities();
        }

        @Override
        public Optional<TargetKey> resolveTarget(ServerLevel level, BlockPos pos, boolean ae2Enabled) {
            return resolveTargetKey(level, pos, ae2Enabled);
        }

        @Override
        public BlockState state(TargetKey target) {
            return getLoadedBlockState(target.targetLevel(), target.pos()).orElse(Blocks.AIR.defaultBlockState());
        }

        @Override
        public boolean filter(TimeAcceleratorBE accelerator, TargetKey target, BlockState state) {
            return accelerator.isBlockValidFilter(target.targetLevel(), target.pos(), state);
        }

        @Override
        public Optional<PreparedAcceleration> accept(TimeAcceleratorBE accelerator, AccelerationRequest request,
                                                     long maxPending, long highestPending) {
            return prepareAcceptedAcceleration(accelerator, request, maxPending, highestPending);
        }

        @Override
        public boolean pay(TimeAcceleratorBE accelerator, PreparedAcceleration prepared) {
            return payForSubmission(accelerator, prepared);
        }

        @Override
        public long maxPending() {
            return JDTEConfig.COMMON.timeAcceleratorMaxPendingTicks.get();
        }
    };

    @FunctionalInterface
    interface CurrentTargetResolver {
        Optional<TargetKey> resolve(TargetKey pending);
    }

    static final class LevelState {
        private final Set<TimeAcceleratorBE> submitted = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<UltimateTimeWandEntity, WandSubmission> submittedWands = new IdentityHashMap<>();
        private final Set<TargetKey> wandAe2Targets = new LinkedHashSet<>();
        private final TimeAccelerationWorkQueue<Object, TargetKey> workQueue = new TimeAccelerationWorkQueue<>();
        private final Set<CoalescedAcceleratedMachine> coalescedTargets =
                Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<TimeAcceleratorBE, RandomTargetCache> randomTargets = new IdentityHashMap<>();
        private final Map<TargetKey, Long> nextEffectTick = new LinkedHashMap<>();

        void submitForTest(TimeAcceleratorBE accelerator) {
            submitted.add(accelerator);
        }

        long pendingTicksForTest(TargetKey target) {
            return workQueue.pendingTicks(target);
        }

        void recordCoalescedTarget(CoalescedAcceleratedMachine target) {
            if (target != null) {
                coalescedTargets.add(target);
            }
        }

        private boolean hasWork() {
            return !submitted.isEmpty() || !submittedWands.isEmpty() || workQueue.hasWork();
        }

        private void deactivate(TimeAcceleratorBE accelerator) {
            submitted.remove(accelerator);
            randomTargets.remove(accelerator);
            workQueue.retainContributors((target, source) -> source != accelerator);
        }

        private void prepare(ServerLevel level, int maxScannedBlocks) {
            prepare(level, maxScannedBlocks, SERVER_PREPARATION);
        }

        void prepare(ServerLevel level, int maxScannedBlocks, LevelPreparationAdapter adapter) {
            TickBudget scanBudget = new TickBudget(maxScannedBlocks);
            long gameTime = adapter.gameTime(level);
            if (gameTime % 200L == 0L) {
                nextEffectTick.entrySet().removeIf(entry -> entry.getValue() + 200L < gameTime);
            }
            Set<Object> active = Collections.newSetFromMap(new IdentityHashMap<>());
            active.addAll(submitted);
            active.addAll(submittedWands.keySet());
            Set<Object> ae2Active = Collections.newSetFromMap(new IdentityHashMap<>());
            ae2Active.addAll(submittedWands.keySet());
            if (isAE2AccelerationConfigured()) {
                for (TimeAcceleratorBE accelerator : submitted) {
                    if (UpgradeHelper.hasAEAccelerationUpgrade(accelerator)) {
                        ae2Active.add(accelerator);
                    }
                }
            }
            Set<TargetKey> currentWandAe2Targets = new LinkedHashSet<>();
            for (WandSubmission submission : submittedWands.values()) {
                if (submission.target().kind() == TargetKind.AE2_GRID) {
                    currentWandAe2Targets.add(submission.target());
                }
            }
            wandAe2Targets.retainAll(currentWandAe2Targets);
            retainActiveContributors(active, ae2Active);
            randomTargets.keySet().removeIf(accelerator -> !active.contains(accelerator));
            wandAe2Targets.removeIf(target -> workQueue.pendingTicks(target) <= 0L);
            if (submitted.isEmpty() && submittedWands.isEmpty()) {
                return;
            }

            List<AcceleratorContext> contexts = new ArrayList<>();
            Map<Long, List<AcceleratorContext>> byChunk = new LinkedHashMap<>();
            for (TimeAcceleratorBE accelerator : submitted) {
                if (!adapter.isActive(accelerator, level)) {
                    reconcilePreparedTargets(workQueue, List.of(), accelerator);
                    continue;
                }
                AccelerationRequest request = adapter.request(accelerator);
                AABB area = adapter.area(accelerator);
                boolean ae2AccelerationEnabled = adapter.ae2Enabled(accelerator);
                AcceleratorContext context = new AcceleratorContext(
                        accelerator, area, request, ae2AccelerationEnabled);
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
            randomTargets.keySet().removeIf(accelerator -> !adapter.isActive(accelerator, level));
            if (!contexts.isEmpty()) {
                discoverBlockEntities(level, byChunk, adapter);
                if (adapter.includeRandomTargets()) {
                    for (AcceleratorContext context : contexts) {
                        context.targets.addAll(getRandomTargets(level, context, scanBudget));
                    }
                }
            }

            long maxPending = adapter.maxPending();
            for (AcceleratorContext context : contexts) {
                reconcilePreparedTargets(workQueue, context.targets, context.accelerator);
                if (context.targets.isEmpty()) {
                    continue;
                }
                long highestPending = workQueue.highestPendingTicks(context.targets);
                Optional<PreparedAcceleration> accepted = adapter.accept(
                        context.accelerator, context.request, maxPending, highestPending);
                if (accepted.isEmpty() || !adapter.pay(context.accelerator, accepted.get())) {
                    continue;
                }
                PreparedAcceleration prepared = accepted.get();
                enqueuePreparedTargets(workQueue, context.targets, context.accelerator,
                        prepared.workTicks(), prepared.displayMultiplier(), maxPending);
            }

            for (Map.Entry<UltimateTimeWandEntity, WandSubmission> entry : submittedWands.entrySet()) {
                WandSubmission submission = entry.getValue();
                int accepted = TimeAcceleratorExecutionPolicy.admittedWorkTicks(
                        submission.workTicks(), maxPending, workQueue.pendingTicks(submission.target()));
                if (accepted > 0) {
                    workQueue.enqueue(submission.target(), entry.getKey(), accepted, 0, maxPending);
                    if (submission.target().kind() == TargetKind.AE2_GRID) {
                        wandAe2Targets.add(submission.target());
                    }
                }
            }
            submittedWands.clear();
        }

        private void retainActiveContributors(Set<Object> active, Set<Object> ae2Active) {
            workQueue.retainContributors((target, source) -> {
                if (source instanceof UltimateTimeWandEntity wand) {
                    WandSubmission current = submittedWands.get(wand);
                    return current != null && isCurrentWandTarget(target, current.target());
                }
                return (target.kind() == TargetKind.AE2_GRID ? ae2Active : active).contains(source);
            });
        }

        private void discoverBlockEntities(ServerLevel level, Map<Long, List<AcceleratorContext>> byChunk,
                                           LevelPreparationAdapter adapter) {
            for (Map.Entry<Long, List<AcceleratorContext>> entry : byChunk.entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey());
                for (Map.Entry<BlockPos, BlockEntity> blockEntityEntry
                        : adapter.blockEntities(level, chunkPos).entrySet()) {
                    BlockPos pos = blockEntityEntry.getKey();
                    BlockEntity blockEntity = blockEntityEntry.getValue();
                    if (blockEntity instanceof TimeAcceleratorMachine || blockEntity.isRemoved()) {
                        continue;
                    }
                    for (AcceleratorContext context : entry.getValue()) {
                        if (!context.contains(pos)) {
                            continue;
                        }
                        Optional<TargetKey> target = adapter.resolveTarget(
                                level, pos, context.ae2AccelerationEnabled);
                        if (target.isPresent()) {
                            TargetKey key = target.get();
                            BlockState state = adapter.state(key);
                            if (adapter.filter(context.accelerator, key, state)) {
                                context.targets.add(key);
                            }
                        }
                    }
                }
            }
        }

        private List<TargetKey> getRandomTargets(ServerLevel level, AcceleratorContext context, TickBudget scanBudget) {
            RandomTargetCache cache = randomTargets.computeIfAbsent(context.accelerator, ignored -> new RandomTargetCache());
            long gameTime = level.getGameTime();
            boolean areaChanged = cache.area == null || !cache.area.equals(context.area);
            if (!cache.rebuilding && !areaChanged && gameTime < cache.refreshAt) {
                return cache.targets;
            }
            if (!cache.rebuilding) {
                beginRandomTargetRefresh(cache, context.area, areaChanged);
            }

            BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
            while (cache.scanIndex < cache.scanVolume && scanBudget.consumeOne()) {
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
                    cache.rebuildingTargets.add(new TargetKey(level, scanPos, TargetKind.RANDOM_TICK));
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

        private void execute(ServerLevel level, long maxExecutions) {
            execute(level, maxExecutions,
                    target -> {
                        boolean ae2RecheckEnabled = shouldRecheckAe2Target(
                                isAE2AccelerationConfigured(), wandAe2Targets.contains(target));
                        return resolveTargetKey(target.targetLevel(), target.pos(),
                                target.kind() == TargetKind.AE2_GRID && ae2RecheckEnabled);
                    },
                    this::executeTargetRoute,
                    this::isContributorFilterValid,
                    (target, multiplier) -> spawnEffect(target, multiplier));
        }

        void execute(ServerLevel level, long maxExecutions,
                     CurrentTargetResolver resolver,
                     TargetExecution targetExecution,
                     java.util.function.BiPredicate<TargetKey, Object> contributorFilter,
                     java.util.function.ObjIntConsumer<TargetKey> effectSpawner) {
            int batchSize = JDTEConfig.COMMON.timeAcceleratorExecutionBatchSize.get();
            coalescedTargets.clear();
            try {
                executePendingTargets(workQueue, maxExecutions, batchSize,
                        contributorFilter,
                        (target, requested, remainingBudget) -> {
                            TimeAccelerationWorkQueue.ExecutionResult result = executeIfCurrentRoute(
                                    target, resolver.resolve(target), requested, remainingBudget,
                                    targetExecution);
                            if (!result.valid()) {
                                nextEffectTick.remove(target);
                                wandAe2Targets.remove(target);
                            }
                            return result;
                        },
                        (target, result, displayMultiplier) -> {
                            if (displayMultiplier > 0) {
                                effectSpawner.accept(target, displayMultiplier);
                            }
                        });
            } finally {
                flushCoalescedTargets(coalescedTargets);
            }
        }

        private boolean isContributorFilterValid(TargetKey target, Object source) {
            return ExtendedTimeAccelerationManager.isContributorFilterValid(
                    target, source, ExtendedTimeAccelerationManager::getLoadedBlockState,
                    (contributor, level, pos, state) ->
                            ((TimeAcceleratorBE) contributor).isBlockValidFilter(level, pos, state));
        }

        private TimeAccelerationWorkQueue.ExecutionResult executeTargetRoute(
                TargetKey target, int requested, long remainingBudget) {
            return switch (target.kind()) {
                case BLOCK_ENTITY, RANDOM_TICK -> executeOrdinaryTarget(target, requested, remainingBudget);
                case AE2_GRID -> {
                    ExtendedTimeAcceleratorAE2Integration.Result result =
                            ExtendedTimeAcceleratorAE2Integration.accelerate(
                                    target.targetLevel(), target.pos(), requested);
                    yield new TimeAccelerationWorkQueue.ExecutionResult(
                            result.executed(), result.valid(), result.idle());
                }
            };
        }

        private TimeAccelerationWorkQueue.ExecutionResult executeOrdinaryTarget(
                TargetKey target, int requested, long remainingBudget) {
            UltimateTimeWandTargetRuntime.Result result =
                    UltimateTimeWandTargetRuntime.executeOrdinary(
                            new TimeAccelerationTarget(target.targetLevel(), target.pos()),
                            requested, remainingBudget);
            if (result.coalescedTarget() != null) {
                coalescedTargets.add(result.coalescedTarget());
            }
            return new TimeAccelerationWorkQueue.ExecutionResult(
                    result.executed(), result.valid(), result.idle());
        }

        private void spawnEffect(TargetKey target, int multiplier) {
            ServerLevel level = target.targetLevel();
            BlockPos pos = target.pos();
            long gameTime = level.getGameTime();
            if (gameTime < nextEffectTick.getOrDefault(target, Long.MIN_VALUE)) {
                return;
            }
            nextEffectTick.put(target, gameTime + 10L);
            boolean present = !level.getEntitiesOfClass(TimeAcceleratorEffectEntity.class, new AABB(pos), entity -> true).isEmpty();
            if (!present) {
                level.addFreshEntity(new TimeAcceleratorEffectEntity(level, pos, Math.max(1, multiplier)));
            }
        }

    }

    private static boolean isAE2AccelerationConfigured() {
        return AE2_LOADED && JDTEConfig.COMMON.timeAcceleratorAE2Enabled.get();
    }
}
