package com.jdte.common.blockentities;

/**
 * Pure bounded-work calculations shared by standalone and multiblock greenhouses.
 * World access, resource payment and drop generation deliberately remain at the block entity boundary.
 */
public final class GreenhouseProductionEngine {
    private GreenhouseProductionEngine() {
    }

    public static WorkWindow accumulate(long storedWork, int growthWork, int harvestBudget,
                                        long addedWork, long maxPendingWork) {
        int safeGrowthWork = Math.max(1, growthWork);
        int safeBudget = Math.max(1, harvestBudget);
        long settlementLimit = saturatingMultiply(safeGrowthWork, safeBudget + 1L);
        long boundedLimit = Math.max(safeGrowthWork, Math.min(settlementLimit, Math.max(1L, maxPendingWork)));
        long available = Math.min(saturatingAdd(Math.max(0L, storedWork), Math.max(0L, addedWork)), boundedLimit);
        int requested = (int) Math.min(available / safeGrowthWork, safeBudget);
        return new WorkWindow(available, requested, safeGrowthWork);
    }

    public static long addedWork(int elapsedTicks, int baseMultiplier, int selectedMultiplier,
                                 int parallelPlants, int structureMultiplier) {
        long result = Math.max(1, elapsedTicks);
        result = saturatingMultiply(result, Math.max(1, baseMultiplier));
        result = saturatingMultiply(result, Math.max(1, selectedMultiplier));
        result = saturatingMultiply(result, Math.max(1, parallelPlants));
        return saturatingMultiply(result, Math.max(1, structureMultiplier));
    }

    public static int budgetForIndex(int totalBudget, int activeSlots, int activeIndex) {
        int safeActiveSlots = Math.max(1, activeSlots);
        int safeBudget = Math.max(1, totalBudget);
        return Math.max(1, safeBudget / safeActiveSlots + (activeIndex < safeBudget % safeActiveSlots ? 1 : 0));
    }

    public static GroupWorkWindow accumulateGroup(long storedWork, long elapsedTicks,
                                                   long workPerTickPerUnit, int units,
                                                   int growthWork, long harvestBudget,
                                                   long maxPendingWork) {
        int safeGrowthWork = Math.max(1, growthWork);
        long addedWork = saturatingMultiply(Math.max(0L, elapsedTicks), Math.max(0L, workPerTickPerUnit));
        addedWork = saturatingMultiply(addedWork, Math.max(0, units));
        long available = saturatingAdd(Math.max(0L, storedWork), addedWork);
        available = Math.min(available, Math.max(0L, maxPendingWork));
        long requested = Math.min(available / safeGrowthWork, Math.max(0L, harvestBudget));
        return new GroupWorkWindow(available, requested, safeGrowthWork);
    }

    private static long saturatingAdd(long left, long right) {
        if (right > Long.MAX_VALUE - left) return Long.MAX_VALUE;
        return left + right;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0L || right == 0L) return 0L;
        if (left > Long.MAX_VALUE / right) return Long.MAX_VALUE;
        return left * right;
    }

    public record WorkWindow(long availableWork, int requestedHarvests, int growthWorkPerHarvest) {
        public long remainingAfter(int completedHarvests) {
            long consumed = (long) Math.max(0, completedHarvests) * growthWorkPerHarvest;
            return Math.max(0L, availableWork - consumed);
        }

        public long stalledWork() {
            return Math.min(availableWork, growthWorkPerHarvest);
        }
    }

    public record GroupWorkWindow(long availableWork, long requestedHarvests, int growthWorkPerHarvest) {
        public long remainingAfter(long completedHarvests) {
            long consumed = saturatingMultiply(Math.max(0L, completedHarvests), growthWorkPerHarvest);
            return Math.max(0L, availableWork - Math.min(availableWork, consumed));
        }
    }
}
