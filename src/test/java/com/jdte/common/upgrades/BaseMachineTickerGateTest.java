package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseMachineTickerGateTest {
    @Test
    void aeDenyPausesTheRealTickerWithoutAdvancingExistingWork() {
        AtomicInteger progress = new AtomicInteger(12);

        UpgradeHelper.runServerTicker(true, () -> false, false, progress::incrementAndGet);

        assertEquals(12, progress.get());
    }

    @Test
    void activeCraftingTaskResumesTheRealTicker() {
        AtomicInteger progress = new AtomicInteger(12);

        UpgradeHelper.runServerTicker(true, () -> true, false, progress::incrementAndGet);

        assertEquals(13, progress.get());
    }

    @Test
    void machineWithoutUpgradeKeepsOriginalTickerAndOverclockBehavior() {
        AtomicInteger executions = new AtomicInteger();
        AtomicInteger permissionReads = new AtomicInteger();

        UpgradeHelper.runServerTicker(true, () -> {
            permissionReads.incrementAndGet();
            return true;
        }, true, executions::incrementAndGet);

        assertEquals(2, executions.get());
        assertEquals(1, permissionReads.get());
    }

    @Test
    void redstoneOffStillRunsOriginalTickerResetWithoutReadingAePermission() {
        AtomicInteger resets = new AtomicInteger();
        AtomicInteger permissionReads = new AtomicInteger();

        UpgradeHelper.runServerTicker(false, () -> {
            permissionReads.incrementAndGet();
            return false;
        }, true, resets::incrementAndGet);

        assertEquals(1, resets.get());
        assertEquals(0, permissionReads.get());
    }
}
