package com.jdte.common.greenhouse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixAccelerationClockTest {
    @Test
    void drainsOnlyCompleteSettlementIntervals() {
        GreenhouseMatrixAccelerationClock clock = new GreenhouseMatrixAccelerationClock();
        clock.add(512);
        clock.add(512);

        assertEquals(1_020L, clock.takeCompleteTicks(20));
        assertEquals(4L, clock.pendingTicks());
    }

    @Test
    void carriesIncompleteTicksIntoTheNextFlush() {
        GreenhouseMatrixAccelerationClock clock = new GreenhouseMatrixAccelerationClock();
        clock.add(10);

        assertEquals(0L, clock.takeCompleteTicks(20));
        assertEquals(10L, clock.pendingTicks());

        clock.add(10);
        assertEquals(20L, clock.takeCompleteTicks(20));
        assertEquals(0L, clock.pendingTicks());
    }

    @Test
    void restoredTicksAreClampedAndAccumulationSaturates() {
        GreenhouseMatrixAccelerationClock clock = new GreenhouseMatrixAccelerationClock();
        clock.restore(-10L);
        assertEquals(0L, clock.pendingTicks());

        clock.restore(Long.MAX_VALUE - 2L);
        clock.add(10);
        assertEquals(Long.MAX_VALUE, clock.pendingTicks());
    }

    @Test
    void clearDiscardsUnsettledTicksWhenTheControllerStops() {
        GreenhouseMatrixAccelerationClock clock = new GreenhouseMatrixAccelerationClock();
        clock.add(19);

        clock.clear();

        assertEquals(0L, clock.pendingTicks());
    }

    @Test
    void accumulationReportsWhetherPersistentStateChanged() {
        GreenhouseMatrixAccelerationClock clock = new GreenhouseMatrixAccelerationClock();

        assertFalse(clock.add(0));
        assertTrue(clock.add(19));

        clock.restore(Long.MAX_VALUE);
        assertFalse(clock.add(1));
    }
}
