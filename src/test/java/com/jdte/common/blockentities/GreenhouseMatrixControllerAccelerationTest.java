package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixControllerAccelerationTest {
    @Test
    void controllerUsesCoalescedAccelerationPath() {
        assertTrue(CoalescedAcceleratedMachine.class.isAssignableFrom(GreenhouseMatrixControllerBE.class));
    }
}
