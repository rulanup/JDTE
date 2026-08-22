package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorTimingTest {

    @Test
    void durationSecondsBecomeMinecraftTicks() {
        assertEquals(20, TimeAcceleratorTiming.durationTicks(1));
        assertEquals(1200, TimeAcceleratorTiming.durationTicks(60));
    }

    @Test
    void effectiveMultiplierScalesOneSubmission() {
        assertEquals(960, TimeAcceleratorTiming.workTicks(16, 3));
    }
}
