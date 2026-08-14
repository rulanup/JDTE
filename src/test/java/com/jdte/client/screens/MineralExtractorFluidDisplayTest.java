package com.jdte.client.screens;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorFluidDisplayTest {
    @Test
    void nonEmptyTankUsesActualFluidAndAmountForRenderingAndTooltip() {
        MineralExtractorFluidDisplay display = MineralExtractorFluidDisplay.create(
                Fluids.LAVA, Fluids.WATER, 375, 1_000);

        assertSame(Fluids.LAVA, display.renderedFluid().getFluid());
        assertEquals(375, display.renderedFluid().getAmount());
        assertEquals(375, display.amount());
        assertEquals(1_000, display.capacity());
        assertFalse(display.configuredGhost());
        assertEquals(new FluidStack(Fluids.LAVA, 1).getHoverName(), display.tooltipName());
    }

    @Test
    void emptyTankUsesAConfiguredGhostWithoutInventingStoredAmount() {
        MineralExtractorFluidDisplay display = MineralExtractorFluidDisplay.create(
                Fluids.EMPTY, Fluids.WATER, 0, 1_000);

        assertSame(Fluids.WATER, display.renderedFluid().getFluid());
        assertEquals(1, display.renderedFluid().getAmount());
        assertEquals(0, display.amount());
        assertEquals(1_000, display.capacity());
        assertTrue(display.configuredGhost());
        assertEquals(new FluidStack(Fluids.WATER, 1).getHoverName(), display.tooltipName());
    }

    @Test
    void missingActualFluidIdentityNeverPresentsConfiguredFluidAsStoredQuantity() {
        MineralExtractorFluidDisplay display = MineralExtractorFluidDisplay.create(
                Fluids.EMPTY, Fluids.WATER, 375, 1_000);

        assertEquals(0, display.amount());
        assertTrue(display.configuredGhost());
    }

    @Test
    void bothMineralFluidTooltipsUseTheDisplayModelsSelectedNames() {
        MineralExtractorFluidDisplay actual = MineralExtractorFluidDisplay.create(
                Fluids.LAVA, Fluids.WATER, 375, 1_000);
        MineralExtractorFluidDisplay configured = MineralExtractorFluidDisplay.create(
                Fluids.EMPTY, Fluids.WATER, 0, 1_000);

        FormattedText actualName = MineralExtractorScreen.experienceTooltip(actual, 25).getFirst();
        FormattedText configuredName = MineralExtractorScreen.accelerationTooltip(configured).getFirst();

        assertEquals(new FluidStack(Fluids.LAVA, 1).getHoverName(), actualName);
        assertEquals(new FluidStack(Fluids.WATER, 1).getHoverName(), configuredName);
    }
}
