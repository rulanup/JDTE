package com.jdte.setup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TimeAcceleratorServerConfigTest {

    @Test
    void serverSettingsUseTheApprovedDefaults() {
        assertFalse(JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.getDefault());
        assertEquals(1, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getDefault());
        assertEquals(1, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getSpec().getRange().getMin());
        assertEquals(60, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getSpec().getRange().getMax());
    }

    @Test
    void localSingleplayerDefaultsMirrorTheServerValueContract() {
        assertFalse(JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerateAllMachines.getDefault());
        assertEquals(1, JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getDefault());
        assertEquals(1, JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getSpec().getRange().getMin());
        assertEquals(60, JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.getSpec().getRange().getMax());
    }
}
