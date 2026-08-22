package com.jdte.common.blockentities;

public final class TimeAcceleratorCostMath {
    private static final double SETTLEMENT_EPSILON = 1.0E-9D;

    private TimeAcceleratorCostMath() {
    }

    public static double fluidCost(int virtualTicks, int timeWandFluidCost, double configMultiplier, double tierMultiplier) {
        double cost = virtualTicks * (double) timeWandFluidCost * configMultiplier * tierMultiplier / 600.0D;
        return Math.max(0.0D, cost);
    }

    public static int energyCost(int multiplier, int rfCostPerTick) {
        long product = (long) Math.max(1, multiplier) * Math.max(0, rfCostPerTick);
        return product >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) product);
    }

    public static Settlement settleFluid(double pendingCost, double newCost) {
        double totalCost = Math.max(0.0D, pendingCost) + Math.max(0.0D, newCost);
        int drainMb = (int) Math.floor(totalCost + SETTLEMENT_EPSILON);
        double remainingCost = totalCost - drainMb;
        if (remainingCost < 0.0D) {
            remainingCost = 0.0D;
        }
        return new Settlement(drainMb, remainingCost);
    }

    public record Settlement(int drainMb, double remainingCost) {
    }
}
