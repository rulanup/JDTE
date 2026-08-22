package com.jdte.common.blockentities;

public final class TimeAcceleratorExecutionPolicy {
    private TimeAcceleratorExecutionPolicy() {
    }

    public static long globalBudget(boolean accelerateAllMachines, int configuredBudget) {
        return accelerateAllMachines ? Long.MAX_VALUE : Math.max(0L, configuredBudget);
    }

    public static int requestedTicks(long pendingTicks, int batchSize, long remainingBudget) {
        long request = Math.min(pendingTicks, Math.min((long) Math.max(1, batchSize), remainingBudget));
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, request));
    }
}
