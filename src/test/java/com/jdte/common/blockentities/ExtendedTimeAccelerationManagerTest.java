package com.jdte.common.blockentities;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.config.IConfigSpec;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

        assertFalse(ExtendedTimeAccelerationManager.executeIfCurrentTarget(
                expected, Optional.of(new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK)),
                4, 4, executor).valid());
        assertFalse(ExtendedTimeAccelerationManager.executeIfCurrentTarget(
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
    void managerPreparesResourcesFromConfiguredWorkTicks() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(4, prepared.displayMultiplier());
            assertEquals(400, prepared.workTicks());
            assertEquals(400, accelerator.fluidWorkTicks);
            assertEquals(400, accelerator.energyWorkTicks);
            assertEquals(7, prepared.fluidCost());
            assertEquals(11, prepared.energyCost());
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerUsesUpdatedDurationForEachSubmission() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(2));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(160, prepared.workTicks());
            assertEquals(160, accelerator.fluidWorkTicks);
            assertEquals(160, accelerator.energyWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerPaymentSeamChecksCostsAndConsumesConfiguredWorkTicks() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, prepared));
            assertEquals(7, accelerator.checkedFluidCost);
            assertEquals(11, accelerator.checkedEnergyCost);
            assertEquals(400, accelerator.consumedWorkTicks);
            assertEquals(11, accelerator.consumedEnergyCost);
            assertNotEquals(prepared.fluidCost(), accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void requestLargerThanMaxPendingChargesAndEnqueuesOnlyAcceptedWork() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            ExtendedTimeAccelerationManager.PreparedAcceleration accepted =
                    ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                            accelerator, request, 100L, 0L).orElseThrow();

            assertEquals(400, request.workTicks());
            assertEquals(100, accepted.workTicks());
            assertEquals(100, accelerator.fluidWorkTicks);
            assertEquals(100, accelerator.energyWorkTicks);
            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, accepted));
            assertEquals(100, accelerator.consumedWorkTicks);
            assertEquals(accepted.workTicks(), accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void nearlyFullTargetChargesAndEnqueuesOnlyItsRemainingCapacity() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            ExtendedTimeAccelerationManager.PreparedAcceleration accepted =
                    ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                            accelerator, request, 100L, 95L).orElseThrow();

            assertEquals(5, accepted.workTicks());
            assertEquals(5, accelerator.fluidWorkTicks);
            assertEquals(5, accelerator.energyWorkTicks);
            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, accepted));
            assertEquals(5, accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void fullTargetRejectsSubmissionBeforeAnyCostIsCalculatedOrPaid() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            assertFalse(ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                    accelerator, request, 100L, 100L).isPresent());
            assertEquals(0, accelerator.fluidWorkTicks);
            assertEquals(0, accelerator.energyWorkTicks);
            assertEquals(0, accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
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
