package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TimeAccelerationWorkQueueTest {

    @Test
    void sameFinalTargetFromDirectAndProxySourcesSharesOneQueueEntry() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey target = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        queue.enqueue(target, new Object(), 3, 2, 64);
        queue.enqueue(target, new Object(), 5, 4, 64);

        AtomicInteger calls = new AtomicInteger();
        long used = queue.execute(64, 64, (key, requested, remaining) -> {
            calls.incrementAndGet();
            assertEquals(8, requested);
            return new TimeAccelerationWorkQueue.ExecutionResult(8, true, false);
        }, (key, result, multiplier) -> assertEquals(6, multiplier));

        assertEquals(8, used);
        assertEquals(1, calls.get());
    }

    @Test
    void ordinaryKindChangeMergesPendingAndMultiplierIntoOneQueueEntry() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey blockEntity = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey randomTick = new ExtendedTimeAccelerationManager.TargetKey(
                level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.RANDOM_TICK);
        queue.enqueue(blockEntity, new Object(), 3, 2, 64);
        queue.enqueue(randomTick, new Object(), 5, 4, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = queue.execute(64, 64, (target, requested, remaining) -> {
            calls.incrementAndGet();
            assertEquals(8, requested);
            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
        }, (target, result, multiplier) -> assertEquals(6, multiplier));

        assertEquals(8, used);
        assertEquals(1, calls.get());
    }

    @Test
    void ae2AndOrdinaryRemainSeparateQueueEntries() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ServerLevel level = serverLevelFixture();
        queue.enqueue(new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY),
                new Object(), 3, 2, 64);
        queue.enqueue(new ExtendedTimeAccelerationManager.TargetKey(
                        level, BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.AE2_GRID),
                new Object(), 5, 4, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = queue.execute(64, 64, (target, requested, remaining) -> {
            calls.incrementAndGet();
            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
        }, (target, result, multiplier) -> { });

        assertEquals(8, used);
        assertEquals(2, calls.get());
    }

    @Test
    void samePositionInDifferentLevelsUsesIndependentQueueEntries() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ExtendedTimeAccelerationManager.TargetKey levelA = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey levelB = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        queue.enqueue(levelA, new Object(), 3, 2, 64);
        queue.enqueue(levelB, new Object(), 5, 4, 64);

        AtomicInteger calls = new AtomicInteger();
        long used = queue.execute(64, 64, (key, requested, remaining) -> {
            calls.incrementAndGet();
            return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
        }, (key, result, multiplier) -> { });

        assertEquals(8, used);
        assertEquals(2, calls.get());
    }

    @Test
    void invalidResultRemovesOnlyItsFinalTargetKey() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ExtendedTimeAccelerationManager.TargetKey invalid = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey valid = new ExtendedTimeAccelerationManager.TargetKey(
                serverLevelFixture(), BlockPos.ZERO, ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        Object source = new Object();
        queue.enqueue(invalid, source, 3, 2, 64);
        queue.enqueue(valid, source, 5, 4, 64);

        long used = queue.execute(64, 64, (key, requested, remaining) ->
                        key == invalid
                                ? new TimeAccelerationWorkQueue.ExecutionResult(0, false, true)
                                : new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false),
                (key, result, multiplier) -> { });

        assertEquals(5, used);
        assertEquals(0, queue.pendingTicks(invalid));
        assertEquals(0, queue.pendingTicks(valid));
    }

    @Test
    void executionFilterRemovesOnlyInvalidContributor() {
        TimeAccelerationWorkQueue<Object, String> queue = new TimeAccelerationWorkQueue<>();
        Object rejected = new Object();
        Object accepted = new Object();
        queue.enqueue("target", rejected, 3, 2, 64);
        queue.enqueue("target", accepted, 5, 4, 64);
        AtomicInteger calls = new AtomicInteger();

        long used = queue.execute(64, 64,
                (target, source) -> source == accepted,
                (target, requested, remaining) -> {
                    calls.incrementAndGet();
                    assertEquals(5, requested);
                    return new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false);
                },
                (target, result, multiplier) -> assertEquals(4, multiplier));

        assertEquals(5, used);
        assertEquals(1, calls.get());
        assertEquals(0, queue.pendingTicks("target"));
    }

    @Test
    void reconciliationRemovesOnlySourceTargetsOutsideCurrentSet() throws Exception {
        TimeAccelerationWorkQueue<Object, ExtendedTimeAccelerationManager.TargetKey> queue = new TimeAccelerationWorkQueue<>();
        ServerLevel level = serverLevelFixture();
        ExtendedTimeAccelerationManager.TargetKey stale = new ExtendedTimeAccelerationManager.TargetKey(
                level, new BlockPos(1, 0, 0), ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        ExtendedTimeAccelerationManager.TargetKey current = new ExtendedTimeAccelerationManager.TargetKey(
                level, new BlockPos(2, 0, 0), ExtendedTimeAccelerationManager.TargetKind.BLOCK_ENTITY);
        Object source = new Object();
        Object otherSource = new Object();
        queue.enqueue(stale, source, 3, 2, 64);
        queue.enqueue(current, source, 5, 4, 64);
        queue.enqueue(stale, otherSource, 7, 8, 64);

        queue.reconcileContributor(source, Set.of(current));

        assertEquals(7, queue.pendingTicks(stale));
        assertEquals(5, queue.pendingTicks(current));

        long used = queue.execute(64, 64,
                (target, requested, remaining) ->
                        new TimeAccelerationWorkQueue.ExecutionResult(requested, true, false),
                (target, result, multiplier) -> {
                    if (target.equals(stale)) {
                        assertEquals(8, multiplier);
                        assertEquals(7, result.executed());
                    } else {
                        assertEquals(4, multiplier);
                        assertEquals(5, result.executed());
                    }
                });

        assertEquals(12, used);
    }

    @Test
    void wandRemainderSurvivesABatchWhileSharingTheGlobalBudget() {
        TimeAccelerationWorkQueue<String, String> queue = new TimeAccelerationWorkQueue<>();
        queue.enqueue("wand-target", "wand", 1024, 0, 2048);
        queue.enqueue("machine-target", "machine", 64, 16, 2048);
        Map<String, Integer> executed = new LinkedHashMap<>();

        long usedBudget = queue.execute(128, 64,
                (target, requestedTicks, remainingBudget) ->
                        new TimeAccelerationWorkQueue.ExecutionResult(requestedTicks, true, false),
                (target, result, displayMultiplier) ->
                        executed.merge(target, result.executed(), Integer::sum));

        assertEquals(128, usedBudget);
        assertEquals(64, executed.get("wand-target"));
        assertEquals(64, executed.get("machine-target"));
        assertEquals(960, queue.pendingTicks("wand-target"));
        assertEquals(0, queue.pendingTicks("machine-target"));
    }

    private static ServerLevel serverLevelFixture() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (ServerLevel) ((Unsafe) field.get(null)).allocateInstance(ServerLevel.class);
    }
}
