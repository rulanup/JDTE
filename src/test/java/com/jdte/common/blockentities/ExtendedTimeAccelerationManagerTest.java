package com.jdte.common.blockentities;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.config.IConfigSpec;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedTimeAccelerationManagerTest {

    @Test
    void finalTargetKeyIncludesLevelKindAndImmutablePosition() throws Exception {
        ServerLevel firstLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        ServerLevel secondLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(3, 4, 5);
        ExtendedTimeAccelerationManager.TargetKey first = new ExtendedTimeAccelerationManager.TargetKey(
                firstLevel, mutable, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        mutable.set(9, 9, 9);
        ExtendedTimeAccelerationManager.TargetKey second = new ExtendedTimeAccelerationManager.TargetKey(
                secondLevel, new BlockPos(3, 4, 5), ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);

        assertEquals(firstLevel, first.targetLevel());
        assertEquals(new BlockPos(3, 4, 5), first.pos());
        assertNotSame(mutable, first.pos());
        assertNotEquals(first, second);
    }

    @Test
    void finalTargetKeyComparisonSeparatesDimensionsAndRoutes() throws Exception {
        ServerLevel firstLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        ServerLevel secondLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        BlockPos pos = new BlockPos(3, 4, 5);
        ExtendedTimeAccelerationManager.TargetKey first = new ExtendedTimeAccelerationManager.TargetKey(
                firstLevel, pos, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey second = new ExtendedTimeAccelerationManager.TargetKey(
                secondLevel, pos, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey route = new ExtendedTimeAccelerationManager.TargetKey(
                firstLevel, pos, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK);

        assertTrue(ExtendedTimeAccelerationManager.isCurrentWandTarget(first, first));
        assertFalse(ExtendedTimeAccelerationManager.isCurrentWandTarget(first, second));
        assertTrue(ExtendedTimeAccelerationManager.isCurrentWandTarget(first, route));
    }

    @Test
    void finalTargetKeyHashingUsesOwningLevelIdentity() throws Exception {
        ServerLevel firstLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        ServerLevel secondLevel = (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
        BlockPos pos = new BlockPos(3, 4, 5);
        ExtendedTimeAccelerationManager.TargetKey first = new ExtendedTimeAccelerationManager.TargetKey(
                firstLevel, pos, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey same = new ExtendedTimeAccelerationManager.TargetKey(
                firstLevel, pos, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey other = new ExtendedTimeAccelerationManager.TargetKey(
                secondLevel, pos, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, other);
    }

    @Test
    void ordinaryTickerRemainsEligibleWhenAe2ServiceIsDisabled() {
        assertEquals(ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY,
                ExtendedTimeAccelerationManager.selectTargetKind(true, false, true));
        assertEquals(ExtendedTimeAccelerationManager.TargetKind.AE2_GRID,
                ExtendedTimeAccelerationManager.selectTargetKind(true, true, true));
        assertEquals(ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY,
                ExtendedTimeAccelerationManager.selectTargetKind(false, false, true));
    }

    @Test
    void ae2ServiceWithoutOrdinaryTickerIsRejectedWhenDisabled() {
        assertNull(ExtendedTimeAccelerationManager.selectTargetKind(true, false, false));
    }

    @Test
    void inactiveExternalTargetNeverFallsBackToVanillaTicker() throws Exception {
        ExtendedTimeAccelerationManager.TargetKey ordinary = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO,
                ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);

        var result = ExtendedTimeAccelerationManager.routeExternalTarget(
                ExternalTimeAccelerationBackend.TargetResolution.inactiveExternal(),
                () -> Optional.of(ordinary));

        assertTrue(result.isEmpty());
    }

    @Test
    void notExternalContinuesOrdinaryClassification() throws Exception {
        ExtendedTimeAccelerationManager.TargetKey ordinary = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO,
                ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);

        var result = ExtendedTimeAccelerationManager.routeExternalTarget(
                ExternalTimeAccelerationBackend.TargetResolution.notExternal(),
                () -> Optional.of(ordinary));

        assertEquals(ordinary, result.orElseThrow().queuedTarget());
    }

    @Test
    void oneAdmissionAndPaymentFansOutToOrdinaryAndDistinctExternalTargets() throws Exception {
        ServerLevel sourceLevel = serverLevelFixture();
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos ordinarySource = new BlockPos(1, 0, 0);
        BlockPos firstExternalSource = new BlockPos(2, 0, 0);
        BlockPos secondExternalSource = new BlockPos(3, 0, 0);
        BlockPos externalTarget = new BlockPos(12, 0, 0);
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        TestPreparationAdapter adapter = new TestPreparationAdapter(
                sourceLevel, targetLevel,
                Map.of(ordinarySource, fakeBlockEntity(),
                        firstExternalSource, fakeBlockEntity(),
                        secondExternalSource, fakeBlockEntity()),
                firstExternalSource, ordinarySource);
        adapter.bind(firstExternalSource, targetLevel, externalTarget);
        adapter.bind(secondExternalSource, targetLevel, externalTarget);
        adapter.setAe2Enabled(true);
        adapter.setRequest(16, 15);
        RecordingExternalBackend backend = new RecordingExternalBackend();
        TestExternalHandle handle = new TestExternalHandle("grid");
        backend.resolveAs(externalTarget,
                ExternalTimeAccelerationBackend.TargetResolution.active(handle));

        state.submitForTest(accelerator);
        state.prepare(sourceLevel, 64, backend, adapter);

        ExtendedTimeAccelerationManager.TargetKey ordinaryTarget =
                new ExtendedTimeAccelerationManager.TargetKey(
                        targetLevel, ordinarySource,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey externalFallback =
                new ExtendedTimeAccelerationManager.TargetKey(
                        targetLevel, externalTarget,
                        ExtendedTimeAccelerationManager.TargetKind.AE2_GRID);
        assertEquals(1, adapter.acceptCalls);
        assertEquals(1, adapter.payCalls);
        assertEquals(15, state.pendingTicksForTest(ordinaryTarget));
        assertEquals(0, state.pendingTicksForTest(externalFallback));
        assertEquals(1, backend.submissions.size());
        assertSame(handle, backend.submissions.getFirst().handle());
        assertEquals(15, backend.submissions.getFirst().additionalCycles());
        assertFalse(adapter.lastAe2FallbackEnabled);
    }

    @Test
    void rejectedAdmissionSubmitsNeitherOrdinaryNorExternalWork() throws Exception {
        ServerLevel sourceLevel = serverLevelFixture();
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos ordinarySource = new BlockPos(1, 0, 0);
        BlockPos externalSource = new BlockPos(2, 0, 0);
        BlockPos externalTarget = new BlockPos(12, 0, 0);
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        TestPreparationAdapter adapter = new TestPreparationAdapter(
                sourceLevel, targetLevel,
                Map.of(ordinarySource, fakeBlockEntity(), externalSource, fakeBlockEntity()),
                externalSource, ordinarySource);
        adapter.bind(externalSource, targetLevel, externalTarget);
        adapter.setAe2Enabled(true);
        adapter.setRequest(16, 15);
        adapter.setAccepted(false);
        RecordingExternalBackend backend = new RecordingExternalBackend();
        backend.resolveAs(externalTarget, ExternalTimeAccelerationBackend.TargetResolution.active(
                new TestExternalHandle("grid")));

        state.submitForTest(accelerator);
        state.prepare(sourceLevel, 64, backend, adapter);

        ExtendedTimeAccelerationManager.TargetKey ordinaryTarget =
                new ExtendedTimeAccelerationManager.TargetKey(
                        targetLevel, ordinarySource,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        assertEquals(1, adapter.acceptCalls);
        assertEquals(0, adapter.payCalls);
        assertEquals(0, state.pendingTicksForTest(ordinaryTarget));
        assertTrue(backend.submissions.isEmpty());
    }

    @Test
    void filteringHappensBeforeExternalClassification() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos source = new BlockPos(1, 0, 0);
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        TestPreparationAdapter adapter = new TestPreparationAdapter(
                level, level, Map.of(source, fakeBlockEntity()), source, source);
        adapter.setAe2Enabled(true);
        adapter.setFilterResult(false);
        RecordingExternalBackend backend = new RecordingExternalBackend();
        backend.resolveAs(source, ExternalTimeAccelerationBackend.TargetResolution.active(
                new TestExternalHandle("grid")));

        state.submitForTest(accelerator);
        state.prepare(level, 64, backend, adapter);

        assertEquals(0, backend.resolveCalls);
        assertEquals(0, adapter.acceptCalls);
        assertEquals(0, adapter.payCalls);
    }

    @Test
    void framePreparesEveryLevelBeforeOrdinaryAndExternalExecution() throws Exception {
        ServerLevel level = serverLevelFixture();
        RecordingExternalBackend backend = new RecordingExternalBackend();
        TestExternalHandle handle = new TestExternalHandle("grid");
        backend.resolveAs(BlockPos.ZERO,
                ExternalTimeAccelerationBackend.TargetResolution.active(handle));

        ExtendedTimeAccelerationManager.runAccelerationFrame(null, backend, List.of(
                new ExtendedTimeAccelerationManager.FrameWork(
                        () -> {
                            backend.events.add("prepare-first");
                            backend.resolve(level, BlockPos.ZERO);
                            backend.submit(handle, new Object(), 15);
                        },
                        () -> backend.events.add("ordinary-first")),
                new ExtendedTimeAccelerationManager.FrameWork(
                        () -> backend.events.add("prepare-second"),
                        () -> backend.events.add("ordinary-second"))));

        assertEquals(List.of("begin", "prepare-first", "resolve", "submit",
                        "prepare-second", "ordinary-first", "ordinary-second", "execute", "clear"),
                backend.events);
    }

    @Test
    void openedExternalFrameIsClearedWhenOrdinaryExecutionFails() {
        RecordingExternalBackend backend = new RecordingExternalBackend();

        assertThrows(IllegalStateException.class,
                () -> ExtendedTimeAccelerationManager.runAccelerationFrame(null, backend, List.of(
                        new ExtendedTimeAccelerationManager.FrameWork(
                                () -> backend.events.add("prepare"),
                                () -> {
                                    backend.events.add("ordinary");
                                    throw new IllegalStateException("expected");
                                }))));

        assertEquals(List.of("begin", "prepare", "ordinary", "clear"), backend.events);
    }

    @Test
    void wandPendingRouteIsRetainedOnlyWhileItMatchesTheCurrentSubmission() throws Exception {
        ServerLevel level = serverLevelFixture();
        assertTrue(ExtendedTimeAccelerationManager.isCurrentWandTarget(
                new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY),
                new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK)));
        assertFalse(ExtendedTimeAccelerationManager.isCurrentWandTarget(
                new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY),
                new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.AE2_GRID)));
    }

    @Test
    void ae2RecheckAllowsWandRouteWhenGlobalOrdinaryAe2SwitchIsOff() {
        assertTrue(ExtendedTimeAccelerationManager.shouldRecheckAe2Target(false, true));
        assertFalse(ExtendedTimeAccelerationManager.shouldRecheckAe2Target(false, false));
        assertTrue(ExtendedTimeAccelerationManager.shouldRecheckAe2Target(true, false));
    }

    @Test
    void unloadedContributorFilterDoesNotReadBlockState() throws Exception {
        ServerLevel level = serverLevelFixture();
        AtomicInteger stateReads = new AtomicInteger();
        ExtendedTimeAccelerationManager.TargetKey target = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);

        boolean valid = ExtendedTimeAccelerationManager.isContributorFilterValid(
                target, newRecordingAccelerator(), (targetLevel, pos) -> Optional.empty(),
                (source, targetLevel, pos, state) -> {
                    stateReads.incrementAndGet();
                    return true;
                });

        assertFalse(valid);
        assertEquals(0, stateReads.get());
    }

    @Test
    void ordinaryExecutionUsesCurrentKindWithoutDroppingPendingRoute() throws Exception {
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey pending = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey current = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK);
        AtomicInteger calls = new AtomicInteger();

        TimeAccelerationWorkQueue.ExecutionResult result =
                ExtendedTimeAccelerationManager.executeIfCurrentRoute(
                        pending, Optional.of(current), 4, 4,
                        (target, requested, budget) -> {
                            calls.incrementAndGet();
                            assertEquals(current, target);
                            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                        });

        assertTrue(result.valid());
        assertEquals(4, result.executed());
        assertEquals(1, calls.get());
    }

    @Test
    void staleTargetIsRejectedWithoutInvokingExecutor() throws Exception {
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey expected = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        AtomicInteger calls = new AtomicInteger();

        TimeAccelerationWorkQueue.ExecutionResult result =
                ExtendedTimeAccelerationManager.executeIfCurrentTarget(
                        expected, Optional.empty(), 4, 4,
                        (target, requested, budget) -> {
                            calls.incrementAndGet();
                            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                        });

        assertFalse(result.valid());
        assertEquals(0, calls.get());
    }

    @Test
    void ordinaryKindChangeKeepsTargetCurrentButRouteChangeDoesNot() throws Exception {
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey blockEntity = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey randomTick = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK);
        ExtendedTimeAccelerationManager.TargetKey ae2 = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.AE2_GRID);

        assertTrue(ExtendedTimeAccelerationManager.isCurrentWandTarget(blockEntity, randomTick));
        assertFalse(ExtendedTimeAccelerationManager.isCurrentWandTarget(blockEntity, ae2));
    }

    @Test
    void coalescedTargetsFlushExactlyOnceThroughManagerBoundary() {
        CountingCoalesced target = new CountingCoalesced();
        Set<CoalescedAcceleratedMachine> targets =
                Collections.newSetFromMap(new IdentityHashMap<>());
        targets.add(target);
        targets.add(target);

        ExtendedTimeAccelerationManager.flushCoalescedTargets(targets);

        assertEquals(1, target.flushes);
        assertTrue(targets.isEmpty());
    }

    @Test
    void levelStatePrepareAndExecuteRunsRealPipelineWithMergePruningAndFinallyFlush() throws Exception {
        ServerLevel sourceLevel = serverLevelFixture();
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos direct = new BlockPos(3, 0, 0);
        BlockPos proxy = new BlockPos(2, 0, 0);
        RecordingAccelerator rejected = newRecordingAccelerator();
        RecordingAccelerator accepted = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        state.submitForTest(rejected);
        state.submitForTest(accepted);
        Map<BlockPos, BlockEntity> discovered = Map.of(
                direct, fakeBlockEntity(),
                proxy, fakeProxy(new TimeAccelerationTarget(targetLevel, direct)));
        ExtendedTimeAccelerationManager.LevelPreparationAdapter adapter = new TestPreparationAdapter(
                sourceLevel, targetLevel, discovered, proxy, direct);

        state.prepare(sourceLevel, 64, adapter);

        ExtendedTimeAccelerationManager.TargetKey ordinaryKey = new ExtendedTimeAccelerationManager.TargetKey(
                targetLevel, direct, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        assertEquals(8, state.pendingTicksForTest(ordinaryKey));
        AtomicInteger calls = new AtomicInteger();
        CountingCoalesced coalesced = new CountingCoalesced();
        state.execute(sourceLevel, 64,
                pending -> Optional.of(new ExtendedTimeAccelerationManager.TargetKey(
                        targetLevel, direct, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK)),
                (current, requested, budget) -> {
                    calls.incrementAndGet();
                    assertSame(targetLevel, current.targetLevel());
                    assertEquals(ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK, current.kind());
                    assertEquals(4, requested);
                    state.recordCoalescedTarget(coalesced);
                    return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                },
                (target, contributor) -> contributor == accepted,
                (target, multiplier) -> assertEquals(2, multiplier));

        assertEquals(1, calls.get());
        assertEquals(1, coalesced.flushes);
        assertEquals(0, state.pendingTicksForTest(ordinaryKey));
    }

    @Test
    void managerPipelineDiscoversEnqueuesAndExecutesFinalTargetOnce() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos proxy = new BlockPos(2, 0, 0);
        BlockPos finalTarget = new BlockPos(3, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(level, proxy, fakeProxy(new TimeAccelerationTarget(level, finalTarget)));
        graph.put(level, finalTarget, fakeBlockEntity());
        Set<ExtendedTimeAccelerationManager.TargetKey> discovered =
                ExtendedTimeAccelerationManager.resolveDistinctTargetKeys(
                        level, List.of(finalTarget, proxy), graph::get,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue =
                new TimeAccelerationWorkQueue<>();
        Object accelerator = new Object();
        ExtendedTimeAccelerationManager.enqueuePreparedTargets(
                queue, discovered, accelerator, 4, 2, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = ExtendedTimeAccelerationManager.executePendingTargets(
                queue, 64, 64, (target, source) -> true,
                (pending, requested, budget) -> ExtendedTimeAccelerationManager.executeIfCurrentRoute(
                        pending, Optional.of(pending), requested, budget,
                        (current, admitted, remaining) -> {
                            calls.incrementAndGet();
                            assertSame(level, current.targetLevel());
                            assertEquals(finalTarget, current.pos());
                            return new TimeAccelerationWorkQueue.ExecutionResult(admitted, true, false);
                        }),
                (target, result, multiplier) -> assertEquals(2, multiplier));

        assertEquals(4, used);
        assertEquals(1, calls.get());
    }

    @Test
    void managerPipelineUsesCrossLevelTargetAndFlushesCoalescedOnce() throws Exception {
        ServerLevel sourceLevel = serverLevelFixture();
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos proxy = new BlockPos(2, 0, 0);
        BlockPos finalTarget = new BlockPos(8, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(sourceLevel, proxy, fakeProxy(new TimeAccelerationTarget(targetLevel, finalTarget)));
        Set<ExtendedTimeAccelerationManager.TargetKey> discovered =
                ExtendedTimeAccelerationManager.resolveDistinctTargetKeys(
                        sourceLevel, List.of(proxy), graph::get,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue =
                new TimeAccelerationWorkQueue<>();
        ExtendedTimeAccelerationManager.enqueuePreparedTargets(
                queue, discovered, new Object(), 3, 1, 64);
        CountingCoalesced coalesced = new CountingCoalesced();
        Set<CoalescedAcceleratedMachine> coalescedTargets =
                Collections.newSetFromMap(new IdentityHashMap<>());

        try {
            ExtendedTimeAccelerationManager.executePendingTargets(
                    queue, 64, 64, (target, source) -> true,
                    (pending, requested, budget) -> {
                        assertSame(targetLevel, pending.targetLevel());
                        assertEquals(finalTarget, pending.pos());
                        coalescedTargets.add(coalesced);
                        coalescedTargets.add(coalesced);
                        return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                    },
                    (target, result, multiplier) -> { });
        } finally {
            ExtendedTimeAccelerationManager.flushCoalescedTargets(coalescedTargets);
        }

        assertEquals(1, coalesced.flushes);
    }

    @Test
    void managerPipelineDropsCycleBeforeEnqueueAndExecution() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos first = new BlockPos(1, 0, 0);
        BlockPos second = new BlockPos(2, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(level, first, fakeProxy(new TimeAccelerationTarget(level, second)));
        graph.put(level, second, fakeProxy(new TimeAccelerationTarget(level, first)));
        Set<ExtendedTimeAccelerationManager.TargetKey> discovered =
                ExtendedTimeAccelerationManager.resolveDistinctTargetKeys(
                        level, List.of(first), graph::get,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue =
                new TimeAccelerationWorkQueue<>();
        ExtendedTimeAccelerationManager.enqueuePreparedTargets(
                queue, discovered, new Object(), 3, 1, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = ExtendedTimeAccelerationManager.executePendingTargets(
                queue, 64, 64, (target, source) -> true,
                (target, requested, budget) -> {
                    calls.incrementAndGet();
                    return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                },
                (target, result, multiplier) -> { });

        assertEquals(0, used);
        assertEquals(0, calls.get());
    }

    @Test
    void managerPipelineRemovesOnlyContributorWithInvalidDynamicFilter() throws Exception {
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey target = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue =
                new TimeAccelerationWorkQueue<>();
        Object rejected = new Object();
        Object accepted = new Object();
        ExtendedTimeAccelerationManager.enqueuePreparedTargets(queue, Set.of(target), rejected, 3, 2, 64);
        ExtendedTimeAccelerationManager.enqueuePreparedTargets(queue, Set.of(target), accepted, 5, 4, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = ExtendedTimeAccelerationManager.executePendingTargets(
                queue, 64, 64, (pending, source) -> source == accepted,
                (pending, requested, budget) -> {
                    calls.incrementAndGet();
                    assertEquals(5, requested);
                    return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                },
                (pending, result, multiplier) -> assertEquals(4, multiplier));

        assertEquals(5, used);
        assertEquals(1, calls.get());
    }

    @Test
    void sameAcceleratorDirectAndProxySourcesResolveToOneFinalTarget() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos proxy = new BlockPos(2, 0, 0);
        BlockPos finalTarget = new BlockPos(3, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(level, proxy, fakeProxy(new TimeAccelerationTarget(level, finalTarget)));
        graph.put(level, finalTarget, fakeBlockEntity());

        Set<ExtendedTimeAccelerationManager.TargetKey> targets =
                ExtendedTimeAccelerationManager.resolveDistinctTargetKeys(
                        level, List.of(finalTarget, proxy), graph::get,
                        ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        AtomicInteger calls = new AtomicInteger();

        for (ExtendedTimeAccelerationManager.TargetKey target : targets) {
            ExtendedTimeAccelerationManager.executeIfCurrentTarget(
                    target, Optional.of(target), 4, 4,
                    (received, requested, budget) -> {
                        calls.incrementAndGet();
                        return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                    });
        }

        assertEquals(Set.of(new ExtendedTimeAccelerationManager.TargetKey(
                level, finalTarget, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY)), targets);
        assertEquals(1, calls.get());
    }

    @Test
    void prepareReconcilesReboundProxyContributionButRetainsDirectContribution() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos direct = new BlockPos(1, 0, 0);
        BlockPos proxy = new BlockPos(2, 0, 0);
        BlockPos firstTarget = new BlockPos(3, 0, 0);
        BlockPos reboundTarget = new BlockPos(4, 0, 0);
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        Map<BlockPos, BlockEntity> discovered = Map.of(
                direct, fakeBlockEntity(),
                proxy, fakeProxy(new TimeAccelerationTarget(level, firstTarget)));
        TestPreparationAdapter adapter = new TestPreparationAdapter(
                level, level, discovered, proxy, direct, firstTarget);

        state.submitForTest(accelerator);
        state.prepare(level, 64, adapter);

        ExtendedTimeAccelerationManager.TargetKey directKey = new ExtendedTimeAccelerationManager.TargetKey(
                level, direct, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey firstKey = new ExtendedTimeAccelerationManager.TargetKey(
                level, firstTarget, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey reboundKey = new ExtendedTimeAccelerationManager.TargetKey(
                level, reboundTarget, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        assertEquals(4, state.pendingTicksForTest(firstKey));
        assertEquals(4, state.pendingTicksForTest(directKey));

        adapter.rebindProxy(reboundTarget);
        state.submitForTest(accelerator);
        state.prepare(level, 64, adapter);

        assertEquals(0, state.pendingTicksForTest(firstKey));
        assertEquals(8, state.pendingTicksForTest(directKey));
        assertEquals(4, state.pendingTicksForTest(reboundKey));

        Set<BlockPos> executed = new java.util.LinkedHashSet<>();
        state.execute(level, 64,
                pending -> Optional.of(pending),
                (target, requested, budget) -> {
                    executed.add(target.pos());
                    return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                },
                (target, source) -> true,
                (target, multiplier) -> { });

        assertFalse(executed.contains(firstTarget));
        assertTrue(executed.contains(reboundTarget));
        assertTrue(executed.contains(direct));
    }

    @Test
    void proxyTargetCanResolveIntoAnotherServerLevel() throws Exception {
        ServerLevel source = serverLevelFixture();
        ServerLevel target = serverLevelFixture();
        BlockPos sourcePos = new BlockPos(1, 0, 0);
        BlockPos targetPos = new BlockPos(9, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(source, sourcePos, fakeProxy(new TimeAccelerationTarget(target, targetPos)));

        TimeAccelerationTarget resolved = ExtendedTimeAccelerationManager.resolveTimeAccelerationTarget(
                source, sourcePos, graph::get).orElseThrow();

        assertSame(target, resolved.level());
        assertEquals(targetPos, resolved.pos());
    }

    @Test
    void targetReplacementAndRemovalAreRejectedBeforeExecution() throws Exception {
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey expected = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        AtomicInteger calls = new AtomicInteger();
        ExtendedTimeAccelerationManager.TargetExecution executor = (target, requested, budget) -> {
            calls.incrementAndGet();
            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
        };

        assertFalse(ExtendedTimeAccelerationManager.executeIfCurrentRoute(
                expected, Optional.of(new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.AE2_GRID)),
                4, 4, executor).valid());
        assertFalse(ExtendedTimeAccelerationManager.executeIfCurrentRoute(
                expected, Optional.empty(), 4, 4, executor).valid());
        assertEquals(0, calls.get());
    }

    @Test
    void proxyCycleResolvesToNoExecutableTarget() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos first = new BlockPos(1, 0, 0);
        BlockPos second = new BlockPos(2, 0, 0);
        MapTargetLookup graph = new MapTargetLookup();
        graph.put(level, first, fakeProxy(new TimeAccelerationTarget(level, second)));
        graph.put(level, second, fakeProxy(new TimeAccelerationTarget(level, first)));
        AtomicInteger calls = new AtomicInteger();

        Optional<TimeAccelerationTarget> resolved =
                ExtendedTimeAccelerationManager.resolveTimeAccelerationTarget(level, first, graph::get);
        if (resolved.isPresent()) {
            calls.incrementAndGet();
        }

        assertTrue(resolved.isEmpty());
        assertEquals(0, calls.get());
    }

    @Test
    void managerPreparesResourcesFromAdditionalCycles() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(4, prepared.displayMultiplier());
            assertEquals(3, prepared.workTicks());
            assertEquals(3, accelerator.fluidWorkTicks);
            assertEquals(3, accelerator.energyWorkTicks);
            assertEquals(7, prepared.fluidCost());
            assertEquals(11, prepared.energyCost());
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerIgnoresConfiguredDurationForEachSubmission() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(2));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(3, prepared.workTicks());
            assertEquals(3, accelerator.fluidWorkTicks);
            assertEquals(3, accelerator.energyWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerPaymentSeamChecksCostsAndConsumesAdditionalCycles() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, prepared));
            assertEquals(7, accelerator.checkedFluidCost);
            assertEquals(11, accelerator.checkedEnergyCost);
            assertEquals(3, accelerator.consumedWorkTicks);
            assertEquals(11, accelerator.consumedEnergyCost);
            assertNotEquals(prepared.fluidCost(), accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void fullTargetRejectsSubmissionBeforeAnyCostIsCalculatedOrPaid() throws Exception {
        ServerLevel sourceLevel = serverLevelFixture();
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos direct = new BlockPos(3, 0, 0);
        BlockPos proxy = new BlockPos(2, 0, 0);
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.LevelState state = new ExtendedTimeAccelerationManager.LevelState();
        TestPreparationAdapter adapter = new TestPreparationAdapter(
                sourceLevel, targetLevel,
                Map.of(direct, fakeBlockEntity(),
                        proxy, fakeProxy(new TimeAccelerationTarget(targetLevel, direct))),
                proxy, direct);
        adapter.setPendingLimit(4L);

        state.submitForTest(accelerator);
        state.prepare(sourceLevel, 64, adapter);
        assertEquals(4, state.pendingTicksForTest(new ExtendedTimeAccelerationManager.TargetKey(
                targetLevel, direct, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY)));
        assertEquals(1, adapter.acceptCalls);
        assertEquals(1, adapter.payCalls);

        adapter.resetSubmissionCalls();
        state.submitForTest(accelerator);
        state.prepare(sourceLevel, 64, adapter);

        assertEquals(0, adapter.acceptCalls);
        assertEquals(0, adapter.payCalls);
    }

    @Test
    void unaffordableRequestIsRejectedInsteadOfSilentlyShrunk() throws Exception {
        ResourceLimitedAccelerator accelerator = newResourceLimitedAccelerator();
        ExtendedTimeAccelerationManager.AccelerationRequest request =
                new ExtendedTimeAccelerationManager.AccelerationRequest(1024, 1023);

        assertTrue(ExtendedTimeAccelerationManager
                .prepareAcceptedAcceleration(accelerator, request)
                .isEmpty());
        assertEquals(0, accelerator.consumedWorkTicks);
    }

    @Test
    void fullyAffordableRequestKeepsItsExactSize() throws Exception {
        RecordingAccelerator accelerator = newRecordingAccelerator();
        ExtendedTimeAccelerationManager.AccelerationRequest request =
                new ExtendedTimeAccelerationManager.AccelerationRequest(16, 15);

        var prepared = ExtendedTimeAccelerationManager
                .prepareAcceptedAcceleration(accelerator, request)
                .orElseThrow();

        assertEquals(15, prepared.workTicks());
    }

    @Test
    void zeroRoundedFluidCostStillAccumulatesFractionalCostFromWorkTicks() throws Exception {
        FractionalSettlementAccelerator accelerator = newFractionalSettlementAccelerator();
        ExtendedTimeAccelerationManager.PreparedAcceleration subMillibucketWork =
                new ExtendedTimeAccelerationManager.PreparedAcceleration(1, 1, 0, 0);

        for (int i = 0; i < 5; i++) {
            ExtendedTimeAccelerationManager.consumePreparedResources(accelerator, subMillibucketWork);
        }

        assertEquals(1, accelerator.drainedMb);
        assertEquals(0.0D, accelerator.pendingCost, 1.0E-9D);
    }

    private static IConfigSpec.ILoadedConfig loadedServerConfig(int durationSeconds) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set(List.of("jdte", "timeAccelerator", "timeAcceleratorAccelerationDurationSeconds"), durationSeconds);
        JDTEConfig.SERVER_SPEC.correct(config);
        try {
            Class<?> loadedConfigClass = Class.forName("net.neoforged.fml.config.LoadedConfig");
            Constructor<?> constructor = loadedConfigClass.getDeclaredConstructor(
                    CommentedConfig.class,
                    java.nio.file.Path.class,
                    Class.forName("net.neoforged.fml.config.ModConfig"));
            constructor.setAccessible(true);
            return (IConfigSpec.ILoadedConfig) constructor.newInstance(config, null, null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to create LoadedConfig test fixture", e);
        }
    }

    private static RecordingAccelerator newRecordingAccelerator() throws Exception {
        return (RecordingAccelerator) unsafe().allocateInstance(RecordingAccelerator.class);
    }

    private static ResourceLimitedAccelerator newResourceLimitedAccelerator() throws Exception {
        return (ResourceLimitedAccelerator) unsafe().allocateInstance(ResourceLimitedAccelerator.class);
    }

    private static FractionalSettlementAccelerator newFractionalSettlementAccelerator() throws Exception {
        return (FractionalSettlementAccelerator) unsafe().allocateInstance(FractionalSettlementAccelerator.class);
    }

    private static ServerLevel serverLevelFixture() throws Exception {
        return (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class TestPreparationAdapter
            implements ExtendedTimeAccelerationManager.LevelPreparationAdapter {
        private final ServerLevel sourceLevel;
        private final Map<BlockPos, BlockEntity> discovered;
        private final BlockPos proxy;
        private final Map<BlockPos, TimeAccelerationTarget> resolvedTargets = new HashMap<>();
        private ExtendedTimeAccelerationManager.AccelerationRequest accelerationRequest =
                new ExtendedTimeAccelerationManager.AccelerationRequest(2, 4);
        private boolean ae2Enabled;
        private boolean filterResult = true;
        private boolean accepted = true;
        private boolean lastAe2FallbackEnabled;
        private long pendingLimit = 64L;
        private int acceptCalls;
        private int payCalls;

        private TestPreparationAdapter(ServerLevel sourceLevel, ServerLevel targetLevel,
                                       Map<BlockPos, BlockEntity> discovered,
                                       BlockPos proxy, BlockPos direct) {
            this(sourceLevel, targetLevel, discovered, proxy, direct, direct);
        }

        private TestPreparationAdapter(ServerLevel sourceLevel, ServerLevel targetLevel,
                                       Map<BlockPos, BlockEntity> discovered,
                                       BlockPos proxy, BlockPos direct, BlockPos resolvedProxyTarget) {
            this.sourceLevel = sourceLevel;
            this.discovered = discovered;
            this.proxy = proxy;
            bind(proxy, targetLevel, resolvedProxyTarget);
            bind(direct, targetLevel, direct);
        }

        private void rebindProxy(BlockPos target) {
            bind(proxy, resolvedTargets.get(proxy).level(), target);
        }

        private void bind(BlockPos source, ServerLevel level, BlockPos target) {
            resolvedTargets.put(source.immutable(), new TimeAccelerationTarget(level, target));
        }

        private void setAe2Enabled(boolean ae2Enabled) {
            this.ae2Enabled = ae2Enabled;
        }

        private void setRequest(int displayMultiplier, int workTicks) {
            accelerationRequest = new ExtendedTimeAccelerationManager.AccelerationRequest(
                    displayMultiplier, workTicks);
        }

        private void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        private void setFilterResult(boolean filterResult) {
            this.filterResult = filterResult;
        }

        private void setPendingLimit(long pendingLimit) {
            this.pendingLimit = pendingLimit;
        }

        private void resetSubmissionCalls() {
            acceptCalls = 0;
            payCalls = 0;
        }

        @Override
        public long gameTime(ServerLevel level) {
            return 1L;
        }

        @Override
        public boolean isActive(TimeAcceleratorBE accelerator, ServerLevel level) {
            return level == sourceLevel;
        }

        @Override
        public ExtendedTimeAccelerationManager.AccelerationRequest request(TimeAcceleratorBE accelerator) {
            return accelerationRequest;
        }

        @Override
        public net.minecraft.world.phys.AABB area(TimeAcceleratorBE accelerator) {
            return new net.minecraft.world.phys.AABB(0, 0, 0, 16, 1, 1);
        }

        @Override
        public boolean ae2Enabled(TimeAcceleratorBE accelerator) {
            return ae2Enabled;
        }

        @Override
        public Map<BlockPos, BlockEntity> blockEntities(ServerLevel level, net.minecraft.world.level.ChunkPos chunkPos) {
            return chunkPos.x == 0 && chunkPos.z == 0 ? discovered : Map.of();
        }

        @Override
        public Optional<TimeAccelerationTarget> resolveLocation(ServerLevel level, BlockPos pos) {
            return Optional.ofNullable(resolvedTargets.get(pos));
        }

        @Override
        public Optional<net.minecraft.world.level.block.state.BlockState> loadedState(
                ExtendedTimeAccelerationManager.TargetLocation location) {
            return Optional.of(Blocks.STONE.defaultBlockState());
        }

        @Override
        public Optional<ExtendedTimeAccelerationManager.TargetKey> classifyQueued(
                ExtendedTimeAccelerationManager.TargetLocation location,
                boolean ae2FallbackEnabled) {
            lastAe2FallbackEnabled = ae2FallbackEnabled;
            return Optional.of(new ExtendedTimeAccelerationManager.TargetKey(
                    location.level(), location.pos(),
                    ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY));
        }

        @Override
        public boolean filter(TimeAcceleratorBE accelerator,
                              ExtendedTimeAccelerationManager.TargetLocation location,
                              net.minecraft.world.level.block.state.BlockState state) {
            return filterResult;
        }

        @Override
        public Optional<ExtendedTimeAccelerationManager.PreparedAcceleration> accept(
                TimeAcceleratorBE accelerator,
                ExtendedTimeAccelerationManager.AccelerationRequest request) {
            acceptCalls++;
            return accepted
                    ? Optional.of(new ExtendedTimeAccelerationManager.PreparedAcceleration(
                            request.displayMultiplier(), request.workTicks(), 0, 0))
                    : Optional.empty();
        }

        @Override
        public boolean pay(TimeAcceleratorBE accelerator,
                           ExtendedTimeAccelerationManager.PreparedAcceleration prepared) {
            payCalls++;
            return true;
        }

        @Override
        public long pendingLimit(TimeAcceleratorBE accelerator, int displayMultiplier) {
            return pendingLimit;
        }

        @Override
        public boolean includeRandomTargets() {
            return false;
        }
    }

    private static final class RecordingExternalBackend implements ExternalTimeAccelerationBackend {
        private final Map<BlockPos, TargetResolution> resolutions = new HashMap<>();
        private final List<String> events = new ArrayList<>();
        private final List<ExternalSubmission> submissions = new ArrayList<>();
        private int resolveCalls;

        private void resolveAs(BlockPos pos, TargetResolution resolution) {
            resolutions.put(pos.immutable(), resolution);
        }

        @Override
        public void beginFrame(MinecraftServer server) {
            events.add("begin");
        }

        @Override
        public TargetResolution resolve(ServerLevel level, BlockPos pos) {
            resolveCalls++;
            events.add("resolve");
            return resolutions.getOrDefault(pos, TargetResolution.notExternal());
        }

        @Override
        public void submit(TargetHandle target, Object contributor, int additionalCycles) {
            events.add("submit");
            submissions.add(new ExternalSubmission(target, contributor, additionalCycles));
        }

        @Override
        public void executeFrame(MinecraftServer server) {
            events.add("execute");
        }

        @Override
        public void clearFrame(MinecraftServer server) {
            events.add("clear");
        }
    }

    private record ExternalSubmission(ExternalTimeAccelerationBackend.TargetHandle handle,
                                      Object contributor, int additionalCycles) {
    }

    private record TestExternalHandle(String id)
            implements ExternalTimeAccelerationBackend.TargetHandle {
    }

    private static final class MapTargetLookup {
        private final Map<ServerLevel, Map<BlockPos, BlockEntity>> entities = new IdentityHashMap<>();

        private void put(ServerLevel level, BlockPos pos, BlockEntity entity) {
            entities.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), entity);
        }

        private BlockEntity get(ServerLevel level, BlockPos pos) {
            Map<BlockPos, BlockEntity> levelEntities = entities.get(level);
            return levelEntities == null ? null : levelEntities.get(pos);
        }
    }

    private static FakeBlockEntity fakeBlockEntity() throws Exception {
        return (FakeBlockEntity) unsafe().allocateInstance(FakeBlockEntity.class);
    }

    private static FakeProxy fakeProxy(TimeAccelerationTarget target) throws Exception {
        FakeProxy proxy = (FakeProxy) unsafe().allocateInstance(FakeProxy.class);
        proxy.target = target;
        return proxy;
    }

    private static class FakeBlockEntity extends BlockEntity {
        private FakeBlockEntity() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }
    }

    private static final class FakeProxy extends FakeBlockEntity implements TimeAccelerationTargetProxy {
        private TimeAccelerationTarget target;

        private FakeProxy() {
            super();
        }

        @Override
        public TimeAccelerationTarget getTimeAccelerationTarget() {
            return target;
        }
    }

    private static final class CountingCoalesced implements CoalescedAcceleratedMachine {
        private int flushes;

        @Override
        public void accumulateAcceleratedTicks(int ticks) {
        }

        @Override
        public void flushAcceleratedTicks() {
            flushes++;
        }
    }

    private static final class RecordingAccelerator extends TimeAcceleratorBE {
        private int fluidWorkTicks;
        private int energyWorkTicks;
        private int checkedFluidCost;
        private int checkedEnergyCost;
        private int consumedWorkTicks;
        private int consumedEnergyCost;

        private RecordingAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 4;
        }

        @Override
        protected int getFluidDrainAmount(int workTicks) {
            fluidWorkTicks = workTicks;
            return 7;
        }

        @Override
        protected int getEnergyCost(int workTicks) {
            energyWorkTicks = workTicks;
            return 11;
        }

        @Override
        protected boolean hasResources(int fluidCost, int energyCost) {
            checkedFluidCost = fluidCost;
            checkedEnergyCost = energyCost;
            return true;
        }

        @Override
        protected void consumeResources(int workTicks, int energyCost) {
            consumedWorkTicks = workTicks;
            consumedEnergyCost = energyCost;
        }
    }

    private static final class ResourceLimitedAccelerator extends TimeAcceleratorBE {
        private int consumedWorkTicks;

        private ResourceLimitedAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 4;
        }

        @Override
        protected int getFluidDrainAmount(int workTicks) {
            return workTicks;
        }

        @Override
        protected int getEnergyCost(int workTicks) {
            return workTicks;
        }

        @Override
        protected boolean hasResources(int fluidCost, int energyCost) {
            return fluidCost <= 2 && energyCost <= 2;
        }

        @Override
        protected void consumeResources(int workTicks, int energyCost) {
            consumedWorkTicks = workTicks;
        }
    }

    private static final class FractionalSettlementAccelerator extends TimeAcceleratorBE {
        private double pendingCost;
        private int drainedMb;

        private FractionalSettlementAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 1;
        }

        @Override
        protected void consumeResources(int workTicks, int energyCost) {
            TimeAcceleratorCostMath.Settlement settlement = TimeAcceleratorCostMath.settleFluid(
                    pendingCost, TimeAcceleratorCostMath.fluidCost(workTicks, 120.0D, 1.0D, 1.0D));
            drainedMb += settlement.drainMb();
            pendingCost = settlement.remainingCost();
        }
    }
}
