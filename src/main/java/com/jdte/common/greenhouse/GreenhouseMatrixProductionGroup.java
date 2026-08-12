package com.jdte.common.greenhouse;

import java.util.Objects;

/** Mutable aggregate state shared by every planting lane with the same profile. */
public final class GreenhouseMatrixProductionGroup {
    private final GreenhouseMatrixProductionProfile profile;
    private long units;
    private long workRemainder;
    private long pendingHarvests;

    public GreenhouseMatrixProductionGroup(GreenhouseMatrixProductionProfile profile) {
        this.profile = Objects.requireNonNull(profile);
    }

    public void addUnit() {
        if (units < Long.MAX_VALUE) units++;
    }

    public GreenhouseMatrixProductionProfile profile() { return profile; }
    public long units() { return units; }
    public long workRemainder() { return workRemainder; }
    public void setWorkRemainder(long workRemainder) { this.workRemainder = Math.max(0L, workRemainder); }
    public long pendingHarvests() { return pendingHarvests; }
    public void setPendingHarvests(long pendingHarvests) { this.pendingHarvests = Math.max(0L, pendingHarvests); }

    public void consumeHarvests(long completed) {
        long safeCompleted = Math.min(Math.max(0L, completed), pendingHarvests);
        long consumedWork = safeCompleted > Long.MAX_VALUE / profile.growthWork()
                ? Long.MAX_VALUE : safeCompleted * profile.growthWork();
        workRemainder = Math.max(0L, workRemainder - Math.min(workRemainder, consumedWork));
        pendingHarvests -= safeCompleted;
    }
}
