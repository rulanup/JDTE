package com.jdte.common.items;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeMultitoolBreakTrackerTest {
    @Test
    void reportsOnlyAConfirmedSuccessfulRemoval() {
        TimeMultitoolBreakTracker.begin(BlockPos.ZERO);
        TimeMultitoolBreakTracker.recordRemoval(BlockPos.ZERO, false);
        assertFalse(TimeMultitoolBreakTracker.finish());

        TimeMultitoolBreakTracker.begin(BlockPos.ZERO);
        TimeMultitoolBreakTracker.recordRemoval(BlockPos.ZERO, true);
        assertTrue(TimeMultitoolBreakTracker.finish());
    }

    @Test
    void removalOutsideAnActiveMultitoolBreakIsIgnored() {
        TimeMultitoolBreakTracker.recordRemoval(BlockPos.ZERO, true);

        TimeMultitoolBreakTracker.begin(BlockPos.ZERO);
        assertFalse(TimeMultitoolBreakTracker.finish());
    }

    @Test
    void removalOfAnotherBlockDoesNotSettleTheCurrentTarget() {
        TimeMultitoolBreakTracker.begin(BlockPos.ZERO);
        TimeMultitoolBreakTracker.recordRemoval(BlockPos.ZERO.above(), true);

        assertFalse(TimeMultitoolBreakTracker.finish());
    }
}
