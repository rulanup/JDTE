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
}