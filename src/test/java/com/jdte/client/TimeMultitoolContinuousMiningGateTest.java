package com.jdte.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeMultitoolContinuousMiningGateTest {
    @Test
    void shortPressContinuesFirstBlockButCannotSwitchToAnother() {
        var gate = new TimeMultitoolContinuousMiningGate();
        assertTrue(gate.allowAttack(1, 1000, 250));
        assertTrue(gate.allowAttack(1, 1100, 250));
        assertFalse(gate.allowAttack(2, 1100, 250));
        assertFalse(gate.allowAttack(3, 1249, 250));
        assertTrue(gate.allowAttack(3, 1250, 250));
    }

    @Test
    void releaseStartsANewSingleBlockPressEvenAfterContinuousMining() {
        var gate = new TimeMultitoolContinuousMiningGate();
        assertTrue(gate.allowAttack(1, 0, 250));
        assertTrue(gate.allowAttack(2, 300, 250));
        gate.reset();
        assertTrue(gate.allowAttack(3, 350, 250));
        assertFalse(gate.allowAttack(4, 400, 250));
    }

    @Test
    void configurableThresholdAndZeroDelayApplyToHeldInput() {
        var gate = new TimeMultitoolContinuousMiningGate();
        assertTrue(gate.allowAttack(1, 0, 200));
        assertFalse(gate.allowAttack(2, 199, 200));
        assertTrue(gate.allowAttack(2, 200, 200));
        gate.reset();
        assertTrue(gate.allowAttack(1, 0, 0));
        assertTrue(gate.allowAttack(2, 0, 0));
    }

    @Test
    void physicalPressStartsTimerBeforeAcquiringABlockTarget() {
        var gate = new TimeMultitoolContinuousMiningGate();
        gate.press(1000);
        assertTrue(gate.allowAttack(1, 1240, 250));
        assertTrue(gate.allowAttack(2, 1250, 250));
    }
}
