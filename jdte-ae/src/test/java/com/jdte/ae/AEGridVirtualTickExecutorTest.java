package com.jdte.ae;

import appeng.hooks.ticking.TickHandler;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AEGridVirtualTickExecutorTest {
    private final AEGridVirtualTickExecutor executor = new AEGridVirtualTickExecutor();

    @BeforeEach
    void resetClock() {
        AEVirtualTickContext.reset();
    }

    @Test
    void gridsAdvanceInSynchronizedRoundsWithCompleteLifecycleOrdering() throws Exception {
        List<String> events = new ArrayList<>();
        ServerLevel overworld = AEGridTestFixture.level("overworld");
        ServerLevel nether = AEGridTestFixture.level("the_nether");
        Map<ServerLevel, String> levelNames = Map.of(
                overworld, "overworld",
                nether, "nether");
        RecordingTarget gridA = new RecordingTarget(
                "A", 1, List.of(nether, overworld), levelNames, events, true);
        RecordingTarget gridB = new RecordingTarget(
                "B", 2, List.of(overworld), levelNames, events, false);

        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(gridB, 1),
                new AEGridVirtualTickExecutor.GridWork(gridA, 2)));

        assertEquals(List.of(
                "tick=101", "A:server-start", "B:server-start",
                "A:overworld-start", "B:overworld-start", "A:nether-start",
                "A:overworld-end", "B:overworld-end", "A:nether-end",
                "A:server-end", "B:server-end",
                "tick=102", "A:server-start", "A:overworld-start", "A:nether-start",
                "A:overworld-end", "A:nether-end", "A:server-end"
        ), events);
    }

    @Test
    void nominal1024RunsExactly1023AdditionalLifecycles() throws Exception {
        ServerLevel level = AEGridTestFixture.level("overworld");
        RecordingTarget grid = new RecordingTarget(
                "A", 7, List.of(level), Map.of(level, "overworld"),
                new ArrayList<>(), false);

        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(grid, 1023)));

        assertEquals(1023, grid.serverStarts);
        assertEquals(1023, grid.levelStarts);
        assertEquals(1023, grid.levelEnds);
        assertEquals(1023, grid.serverEnds);
    }

    @Test
    void workDispatchedAtServerEndIsProcessedByTheNextRound() throws Exception {
        ServerLevel level = AEGridTestFixture.level("overworld");
        DispatchProbe grid = new DispatchProbe(level);

        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(grid, 2)));

        assertEquals(1, grid.jobsProcessedAtLevelEnd);
    }

    @Test
    void invalidGridStopsWithoutPreventingOtherGridWork() throws Exception {
        ServerLevel level = AEGridTestFixture.level("overworld");
        RecordingTarget invalidated = new RecordingTarget(
                "A", 1, List.of(level), Map.of(level, "overworld"),
                new ArrayList<>(), false);
        invalidated.validForRounds = 1;
        RecordingTarget healthy = new RecordingTarget(
                "B", 2, List.of(level), Map.of(level, "overworld"),
                new ArrayList<>(), false);

        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(invalidated, 3),
                new AEGridVirtualTickExecutor.GridWork(healthy, 3)));

        assertEquals(1, invalidated.serverStarts);
        assertEquals(3, healthy.serverStarts);
    }

    @Test
    void lifecycleFailureHasContextAndAlwaysRestoresExecutorState() throws Exception {
        ServerLevel level = AEGridTestFixture.level("overworld");
        TestFailure failure = new TestFailure();
        ThrowingTarget broken = new ThrowingTarget(level, failure);

        ReportedException reported = assertThrows(ReportedException.class,
                () -> executor.execute(100L, List.of(
                        new AEGridVirtualTickExecutor.GridWork(broken, 3))));

        assertSame(failure, reported.getCause());
        String report = reported.getReport().getFriendlyReport(ReportType.TEST);
        assertTrue(report.contains("Grid serial"));
        assertTrue(report.contains("7"));
        assertTrue(report.contains("Grid anchors"));
        assertTrue(report.contains("jdte_ae_test:overworld@(1,2,3)"));
        assertTrue(report.contains("Virtual round"));
        assertTrue(report.contains("2"));
        assertTrue(report.contains("Requested additional cycles"));
        assertTrue(report.contains("3"));
        assertFalse(AEVirtualTickContext.isActive());

        RecordingTarget healthy = new RecordingTarget(
                "healthy", 8, List.of(level), Map.of(level, "overworld"),
                new ArrayList<>(), false);
        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(healthy, 1)));
        assertEquals(1, healthy.serverStarts);
    }

    @Test
    void recursiveExecutionIsRejectedAndOuterExecutionCanFinish() throws Exception {
        ServerLevel level = AEGridTestFixture.level("overworld");
        ReentrantTarget target = new ReentrantTarget(level, executor);

        executor.execute(100L, List.of(
                new AEGridVirtualTickExecutor.GridWork(target, 1)));

        assertTrue(target.recursionRejected);
        assertFalse(AEVirtualTickContext.isActive());
    }

    @Test
    void gridWorkRequiresATargetAndPositiveCycles() throws Exception {
        RecordingTarget target = new RecordingTarget(
                "A", 1, List.of(), Map.of(), new ArrayList<>(), false);

        assertThrows(NullPointerException.class,
                () -> new AEGridVirtualTickExecutor.GridWork(null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new AEGridVirtualTickExecutor.GridWork(target, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new AEGridVirtualTickExecutor.GridWork(target, -1));
    }

    private static class RecordingTarget
            implements AEGridVirtualTickExecutor.GridTickTarget {
        private final String name;
        private final int serialNumber;
        private final List<ServerLevel> levels;
        private final Map<ServerLevel, String> levelNames;
        private final List<String> events;
        private final boolean recordTick;
        private int validityChecks;
        int validForRounds = Integer.MAX_VALUE;
        int serverStarts;
        int levelStarts;
        int levelEnds;
        int serverEnds;

        private RecordingTarget(String name, int serialNumber, List<ServerLevel> levels,
                                Map<ServerLevel, String> levelNames, List<String> events,
                                boolean recordTick) {
            this.name = name;
            this.serialNumber = serialNumber;
            this.levels = List.copyOf(levels);
            this.levelNames = Map.copyOf(levelNames);
            this.events = events;
            this.recordTick = recordTick;
        }

        @Override
        public int serialNumber() {
            return serialNumber;
        }

        @Override
        public boolean isValid() {
            return validityChecks++ < validForRounds;
        }

        @Override
        public List<ServerLevel> loadedLevels() {
            return levels;
        }

        @Override
        public String anchorSummary() {
            return "jdte_ae_test:overworld@(1,2,3)";
        }

        @Override
        public void onServerStartTick() {
            serverStarts++;
            if (recordTick) {
                events.add("tick=" + TickHandler.instance().getCurrentTick());
            }
            events.add(name + ":server-start");
        }

        @Override
        public void onLevelStartTick(ServerLevel level) {
            levelStarts++;
            events.add(name + ":" + levelNames.get(level) + "-start");
        }

        @Override
        public void onLevelEndTick(ServerLevel level) {
            levelEnds++;
            events.add(name + ":" + levelNames.get(level) + "-end");
        }

        @Override
        public void onServerEndTick() {
            serverEnds++;
            events.add(name + ":server-end");
        }
    }

    private static final class DispatchProbe extends RecordingTarget {
        private boolean pendingJob;
        private int jobsProcessedAtLevelEnd;

        private DispatchProbe(ServerLevel level) {
            super("dispatch", 3, List.of(level), Map.of(level, "overworld"),
                    new ArrayList<>(), false);
        }

        @Override
        public void onLevelEndTick(ServerLevel level) {
            super.onLevelEndTick(level);
            if (pendingJob) {
                jobsProcessedAtLevelEnd++;
                pendingJob = false;
            }
        }

        @Override
        public void onServerEndTick() {
            super.onServerEndTick();
            pendingJob = true;
        }
    }

    private static final class ThrowingTarget extends RecordingTarget {
        private final TestFailure failure;

        private ThrowingTarget(ServerLevel level, TestFailure failure) {
            super("broken", 7, List.of(level), Map.of(level, "overworld"),
                    new ArrayList<>(), false);
            this.failure = failure;
        }

        @Override
        public void onLevelEndTick(ServerLevel level) {
            super.onLevelEndTick(level);
            if (levelEnds == 2) {
                throw failure;
            }
        }
    }

    private static final class ReentrantTarget extends RecordingTarget {
        private final AEGridVirtualTickExecutor executor;
        private boolean recursionRejected;

        private ReentrantTarget(ServerLevel level, AEGridVirtualTickExecutor executor) {
            super("reentrant", 9, List.of(level), Map.of(level, "overworld"),
                    new ArrayList<>(), false);
            this.executor = executor;
        }

        @Override
        public void onServerStartTick() {
            super.onServerStartTick();
            assertThrows(IllegalStateException.class,
                    () -> executor.execute(100L, List.of()));
            recursionRejected = true;
        }
    }

    private static final class TestFailure extends RuntimeException {
    }
}
