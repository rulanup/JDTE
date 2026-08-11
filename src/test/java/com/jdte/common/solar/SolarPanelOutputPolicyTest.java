package com.jdte.common.solar;

import com.jdte.common.blockentities.SolarPanelBE;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolarPanelOutputPolicyTest {
    @Test
    void storedFiniteEnergyExportsEvenWhenGenerationIsInactive() {
        assertTrue(SolarPanelBE.shouldPushEnergy(false, 1));
        assertFalse(SolarPanelBE.shouldPushEnergy(false, 0));
    }

    @Test
    void creativePanelAlwaysExports() {
        assertTrue(SolarPanelBE.shouldPushEnergy(true, 0));
    }
}
