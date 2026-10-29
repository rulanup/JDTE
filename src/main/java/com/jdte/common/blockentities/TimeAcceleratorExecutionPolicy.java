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

    public static boolean canAdmitFullWork(int requestedWorkTicks, long maxPendingTicks,
                                           long contributorPendingTicks) {
        if (requestedWorkTicks <= 0 || maxPendingTicks <= 0L) {
            return false;
        }
        long pending = Math.max(0L, contributorPendingTicks);
        return pending <= maxPendingTicks - Math.min(maxPendingTicks, (long) requestedWorkTicks)
                && (long) requestedWorkTicks <= maxPendingTicks;
    }

    public static int admittedWandWorkTicks(int requestedWorkTicks, long maxPendingTicks,
                                            long targetPendingTicks) {
        if (requestedWorkTicks <= 0 || maxPendingTicks <= 0L) {
            return 0;
        }
        long pending = Math.max(0L, targetPendingTicks);
        if (pending >= maxPendingTicks) {
            return 0;
        }
        long available = maxPendingTicks - pending;
        return (int) Math.min((long) requestedWorkTicks, available);
    }
}
