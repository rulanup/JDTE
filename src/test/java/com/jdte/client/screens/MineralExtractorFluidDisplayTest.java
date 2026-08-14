package com.jdte.client.screens;

import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class MineralExtractorFluidDisplayTest {
    @Test
    void usesActualStoredFluidBeforeConfiguredFluid() {
        assertSame(Fluids.LAVA, MineralExtractorScreen.displayFluid(Fluids.LAVA, Fluids.WATER));
    }

    @Test
    void usesConfiguredFluidWhenTankIsEmpty() {
        assertSame(Fluids.WATER, MineralExtractorScreen.displayFluid(Fluids.EMPTY, Fluids.WATER));
    }
}
