package com.jdte.ae;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AeGridVirtualTickExecutor {
    private static final ThreadLocal<Boolean> RUNNING = new ThreadLocal<>();

    void execute(long frameTick, List<GridWork> work) {
        Objects.requireNonNull(work, "work");
        if (Boolean.TRUE.equals(RUNNING.get())) {
            throw new IllegalStateException("Recursive AE virtual grid ticking is not allowed");
        }
        RUNNING.set(true);
        try {
            List<GridWork> ordered = work.stream()
                    .map(item -> Objects.requireNonNull(item, "work item"))
                    .sorted(Comparator.comparingInt(item -> item.target().serialNumber()))
                    .toList();
            int maxRounds = ordered.stream()
                    .mapToInt(GridWork::additionalCycles)
                    .max()
                    .orElse(0);
            Set<GridTickTarget> invalidTargets =
                    Collections.newSetFromMap(new IdentityHashMap<>());

            for (int round = 1; round <= maxRounds; round++) {
                List<GridWork> active = validForRound(ordered, invalidTargets, round);
                if (!active.isEmpty()) {
                    executeRound(frameTick, round, active);
                }
                if (round == maxRounds) {
                    break;
                }
            }
        } finally {
            RUNNING.remove();
        }
    }

    private static List<GridWork> validForRound(List<GridWork> ordered,
                                                Set<GridTickTarget> invalidTargets,
                                                int round) {
        List<GridWork> active = new ArrayList<>();
        for (GridWork item : ordered) {
            if (item.additionalCycles() < round || invalidTargets.contains(item.target())) {
                continue;
            }
            boolean valid = call(item, round, "validity check", item.target()::isValid);
            if (valid) {
                active.add(item);
            } else {
                invalidTargets.add(item.target());
            }
        }
        return active;
    }

    private static void executeRound(long frameTick, int round, List<GridWork> active) {
        try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(frameTick, round)) {
            for (GridWork item : active) {
                run(item, round, "server start", item.target()::onServerStartTick);
            }

            Map<GridWork, List<ServerLevel>> levelsByGrid = new LinkedHashMap<>();
            for (GridWork item : active) {
                List<ServerLevel> levels = call(
                        item, round, "loaded level snapshot", item.target()::loadedLevels);
                levelsByGrid.put(item, List.copyOf(levels));
            }

            List<ServerLevel> orderedLevels = orderedLevels(levelsByGrid.values());
            for (ServerLevel level : orderedLevels) {
                for (GridWork item : active) {
                    if (containsIdentity(levelsByGrid.get(item), level)) {
                        run(item, round, "level start " + level.dimension().location(),
                                () -> item.target().onLevelStartTick(level));
                    }
                }
            }
            for (ServerLevel level : orderedLevels) {
                for (GridWork item : active) {
                    if (containsIdentity(levelsByGrid.get(item), level)) {
                        run(item, round, "level end " + level.dimension().location(),
                                () -> item.target().onLevelEndTick(level));
                    }
                }
            }
            for (GridWork item : active) {
                run(item, round, "server end", item.target()::onServerEndTick);
            }
        }
    }

    private static List<ServerLevel> orderedLevels(Iterable<List<ServerLevel>> levelLists) {
        Set<ServerLevel> levels = Collections.newSetFromMap(new IdentityHashMap<>());
        for (List<ServerLevel> levelList : levelLists) {
            for (ServerLevel level : levelList) {
                levels.add(Objects.requireNonNull(level, "loaded level"));
            }
        }
        return levels.stream()
                .sorted(Comparator.comparing(level ->
                        level.dimension().location().toString()))
                .toList();
    }

    private static boolean containsIdentity(List<ServerLevel> levels, ServerLevel expected) {
        return levels.stream().anyMatch(level -> level == expected);
    }

    private static void run(GridWork item, int round, String phase, Runnable action) {
        call(item, round, phase, () -> {
            action.run();
            return null;
        });
    }

    private static <T> T call(GridWork item, int round, String phase,
                              java.util.function.Supplier<T> action) {
        try {
            return action.get();
        } catch (Throwable failure) {
            throw reportFailure(failure, item, round, phase);
        }
    }

    private static ReportedException reportFailure(Throwable failure, GridWork item,
                                                   int round, String phase) {
        CrashReport report = CrashReport.forThrowable(
                failure, "Ticking accelerated AE grid virtual lifecycle");
        CrashReportCategory category = report.addCategory("AE grid virtual acceleration");
        category.setDetail("Grid serial", item.target().serialNumber());
        category.setDetail("Grid anchors", item.target().anchorSummary());
        category.setDetail("Virtual round", round);
        category.setDetail("Requested additional cycles", item.additionalCycles());
        category.setDetail("Requested multiplier",
                item.additionalCycles() == Integer.MAX_VALUE
                        ? Integer.MAX_VALUE
                        : item.additionalCycles() + 1);
        category.setDetail("Lifecycle phase", phase);
        return new ReportedException(report);
    }

    record GridWork(GridTickTarget target, int additionalCycles) {
        GridWork {
            Objects.requireNonNull(target, "target");
            if (additionalCycles <= 0) {
                throw new IllegalArgumentException("additionalCycles must be positive");
            }
        }
    }

    interface GridTickTarget {
        int serialNumber();

        boolean isValid();

        List<ServerLevel> loadedLevels();

        String anchorSummary();

        void onServerStartTick();

        void onLevelStartTick(ServerLevel level);

        void onLevelEndTick(ServerLevel level);

        void onServerEndTick();
    }
}
