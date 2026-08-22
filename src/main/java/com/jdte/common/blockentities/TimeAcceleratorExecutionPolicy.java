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

    public static int admittedWorkTicks(int requestedWorkTicks, long maxPendingTicks,
                                        long highestPendingTicks) {
        if (requestedWorkTicks <= 0 || maxPendingTicks <= 0L) {
            return 0;
        }
        long pending = Math.max(0L, highestPendingTicks);
        if (pending >= maxPendingTicks) {
            return 0;
        }
        long available = maxPendingTicks - pending;
        return (int) Math.min((long) requestedWorkTicks, available);
    }
}
