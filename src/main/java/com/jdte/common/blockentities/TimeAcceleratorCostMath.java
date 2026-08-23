package com.jdte.common.blockentities;

public final class TimeAcceleratorCostMath {
    private TimeAcceleratorCostMath() {
    }

    public static double fluidCost(int virtualTicks, double timeWandFluidCost, double configMultiplier, double tierMultiplier) {
        double cost = virtualTicks * (double) timeWandFluidCost * configMultiplier * tierMultiplier / 600.0D;
        return Math.max(0.0D, cost);
    }

    public static int energyCost(int multiplier, int rfCostPerTick) {
        long product = Math.max(0L, (long) multiplier) * Math.max(0L, (long) rfCostPerTick);
        return product >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }

    public static Settlement settleFluid(double pendingCost, double newCost) {
        double totalCost = Math.max(0.0D, pendingCost) + Math.max(0.0D, newCost);
        int drainMb = (int) Math.floor(totalCost);
        double remainingCost = totalCost - drainMb;
        return new Settlement(drainMb, remainingCost);
    }

    public record Settlement(int drainMb, double remainingCost) {
    }
}
