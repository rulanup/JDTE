package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static com.jdte.common.blockentities.UltimateTimeWandTargetRuntime.Route;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UltimateTimeWandTargetRuntimeTest {

    @Test
    void requestedTicksAreCappedBySharedBatchAndBudget() {
        assertEquals(64, UltimateTimeWandTargetRuntime.admit(1024, 64, 64));
        assertEquals(0, UltimateTimeWandTargetRuntime.admit(1024, 64, 0));
    }

    @Test
    void ae2RouteWinsWhenTickableExists() {
        assertEquals(Route.AE2, UltimateTimeWandTargetRuntime.route(true, true, true));
        assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(true, false, true));
        assertEquals(Route.ORDINARY, UltimateTimeWandTargetRuntime.route(false, false, true));
    }

    @Test
    void executeCapsWorkToBothBatchAndCallerBudget() {
        RecordingTarget batchTarget = new RecordingTarget(false, new UltimateTimeWandTargetRuntime.Result(0, false, true, null),
                new UltimateTimeWandTargetRuntime.Result(64, true, false, null));
        RecordingTarget budgetTarget = new RecordingTarget(false, new UltimateTimeWandTargetRuntime.Result(0, false, true, null),
                new UltimateTimeWandTargetRuntime.Result(7, true, false, null));

        UltimateTimeWandTargetRuntime.Result batchResult =
                UltimateTimeWandTargetRuntime.execute(1024, 64, 512, batchTarget);
        UltimateTimeWandTargetRuntime.Result budgetResult =
                UltimateTimeWandTargetRuntime.execute(1024, 64, 7, budgetTarget);

        assertEquals(64, batchResult.executed());
        assertEquals(64, batchTarget.ordinaryRequestedTicks);
        assertEquals(7, budgetResult.executed());
        assertEquals(7, budgetTarget.ordinaryRequestedTicks);
    }

    @Test
    void executePassesResolvedTargetToOrdinaryExecutor() throws Exception {
        ServerLevel targetLevel = serverLevelFixture();
        BlockPos targetPos = new BlockPos(4, 5, 6);
        RecordingTarget target = new RecordingTarget(false, new UltimateTimeWandTargetRuntime.Result(0, false, true, null),
                new UltimateTimeWandTargetRuntime.Result(5, true, false, null));

        UltimateTimeWandTargetRuntime.Result result =
                UltimateTimeWandTargetRuntime.execute(new TimeAccelerationTarget(targetLevel, targetPos),
                        5, 64, 5, target);

        assertEquals(5, result.executed());
        assertSame(targetLevel, target.ordinaryTarget.level());
        assertEquals(targetPos, target.ordinaryTarget.pos());
        assertEquals(5, target.ordinaryRequestedTicks);
    }

    @Test
    void executeDoesNotRunOrdinaryTickerWhenAe2TickableExists() {
        RecordingTarget target = new RecordingTarget(true, new UltimateTimeWandTargetRuntime.Result(5, true, false, null),
                new UltimateTimeWandTargetRuntime.Result(5, true, false, null));

        UltimateTimeWandTargetRuntime.Result result =
                UltimateTimeWandTargetRuntime.execute(5, 64, 5, target);

        assertEquals(5, result.executed());
        assertEquals(5, target.ae2RequestedTicks);
        assertEquals(0, target.ordinaryRequestedTicks);
    }

    @Test
    void executeResolvedTargetDoesNotRunOrdinaryTickerWhenAe2TickableExists() throws Exception {
        RecordingTarget target = new RecordingTarget(true, new UltimateTimeWandTargetRuntime.Result(5, true, false, null),
                new UltimateTimeWandTargetRuntime.Result(5, true, false, null));

        UltimateTimeWandTargetRuntime.Result result =
                UltimateTimeWandTargetRuntime.execute(
                        new TimeAccelerationTarget(serverLevelFixture(), new BlockPos(4, 5, 6)),
                        5, 64, 5, target);

        assertEquals(5, result.executed());
        assertEquals(5, target.ae2RequestedTicks);
        assertEquals(0, target.ordinaryRequestedTicks);
        assertNull(target.ordinaryTarget);
    }

    @Test
    void executeFallsBackToOrdinaryTargetWithoutAe2Service() {
        RecordingTarget target = new RecordingTarget(false, new UltimateTimeWandTargetRuntime.Result(0, false, true, null),
                new UltimateTimeWandTargetRuntime.Result(5, true, false, null));

        UltimateTimeWandTargetRuntime.Result result =
                UltimateTimeWandTargetRuntime.execute(5, 64, 5, target);

        assertEquals(5, result.executed());
        assertEquals(0, target.ae2RequestedTicks);
        assertEquals(5, target.ordinaryRequestedTicks);
    }

    @Test
    void allSleepingAe2EndpointsReturnInvalidIdleWithoutOrdinaryFallback() {
        RecordingTarget target = new RecordingTarget(true, new UltimateTimeWandTargetRuntime.Result(0, false, true, null),
                new UltimateTimeWandTargetRuntime.Result(5, true, false, null));

        UltimateTimeWandTargetRuntime.Result result =
                UltimateTimeWandTargetRuntime.execute(5, 64, 5, target);

        assertEquals(0, result.executed());
        assertFalse(result.valid());
        assertTrue(result.idle());
        assertEquals(5, target.ae2RequestedTicks);
        assertEquals(0, target.ordinaryRequestedTicks);
    }

    private static ServerLevel serverLevelFixture() throws Exception {
        return (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class RecordingTarget implements UltimateTimeWandTargetRuntime.TargetExecutor {
        private final boolean hasTickable;
        private final UltimateTimeWandTargetRuntime.Result ae2Result;
        private final UltimateTimeWandTargetRuntime.Result ordinaryResult;
        private int ae2RequestedTicks;
        private int ordinaryRequestedTicks;
        private TimeAccelerationTarget ordinaryTarget;

        private RecordingTarget(boolean hasTickable, UltimateTimeWandTargetRuntime.Result ae2Result,
                                UltimateTimeWandTargetRuntime.Result ordinaryResult) {
            this.hasTickable = hasTickable;
            this.ae2Result = ae2Result;
            this.ordinaryResult = ordinaryResult;
        }

        @Override
        public boolean hasAe2Tickable() {
            return hasTickable;
        }

        @Override
        public UltimateTimeWandTargetRuntime.Result executeAe2(int requestedTicks) {
            ae2RequestedTicks = requestedTicks;
            return ae2Result;
        }

        @Override
        public UltimateTimeWandTargetRuntime.Result executeOrdinary(int requestedTicks) {
            ordinaryRequestedTicks = requestedTicks;
            return ordinaryResult;
        }

        @Override
        public UltimateTimeWandTargetRuntime.Result executeOrdinary(
                TimeAccelerationTarget target, int requestedTicks) {
            ordinaryTarget = target;
            return executeOrdinary(requestedTicks);
        }
    }
}
