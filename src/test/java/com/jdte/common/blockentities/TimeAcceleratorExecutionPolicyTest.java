package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorExecutionPolicyTest {

    @Test
    void disabledParallelModeKeepsConfiguredGlobalBudget() {
        assertEquals(4096L, TimeAcceleratorExecutionPolicy.globalBudget(false, 4096));
    }

    @Test
    void enabledParallelModeRemovesOnlyTheGlobalBudget() {
        assertEquals(Long.MAX_VALUE, TimeAcceleratorExecutionPolicy.globalBudget(true, 4096));
    }

    @Test
    void requestedTicksStillRespectPerTargetBatchSizeWhenBudgetIsUnlimited() {
        assertEquals(64, TimeAcceleratorExecutionPolicy.requestedTicks(1000L, 64, Long.MAX_VALUE));
        assertEquals(64, TimeAcceleratorExecutionPolicy.requestedTicks(Long.MAX_VALUE, 64, Long.MAX_VALUE));
    }

    @Test
    void requestedTicksShrinkToFiniteRemainingBudget() {
        assertEquals(12, TimeAcceleratorExecutionPolicy.requestedTicks(1000L, 64, 12L));
        assertEquals(0, TimeAcceleratorExecutionPolicy.requestedTicks(1000L, 64, 0L));
    }

    @Test
    void requestedTicksNeverExceedPendingWork() {
        assertEquals(5, TimeAcceleratorExecutionPolicy.requestedTicks(5L, 64, Long.MAX_VALUE));
        assertEquals(1, TimeAcceleratorExecutionPolicy.requestedTicks(1L, Integer.MAX_VALUE, Long.MAX_VALUE));
    }
}
