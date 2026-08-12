package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.tools.EclipseAlloyPaxel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeMultitoolItemTest {
    @Test
    void reusesTheJdtPoweredPaxelAndItsToolUpgrades() {
        assertTrue(EclipseAlloyPaxel.class.isAssignableFrom(TimeMultitoolItem.class));
    }

    @Test
    void exposesTheApprovedEnergyAndTimeFluidCapacities() {
        assertTrue(FluidContainingItem.class.isAssignableFrom(TimeMultitoolItem.class));
        assertEquals(500_000, TimeMultitoolItem.MAX_ENERGY);
        assertEquals(1_000_000, TimeMultitoolItem.MAX_TIME_FLUID);
    }
}
