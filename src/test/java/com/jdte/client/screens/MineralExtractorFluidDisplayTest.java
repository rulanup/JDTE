package com.jdte.client.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
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

    @Test
    void bothTankTooltipsExplainOptionalBonusesAndPowerOnlyBaseProduction() {
        MineralExtractorFluidDisplay empty = MineralExtractorFluidDisplay.create(
                Fluids.EMPTY, Fluids.WATER, 0, 1_000);
        var fortune = MineralExtractorScreen.experienceTooltip(empty, 0);
        var acceleration = MineralExtractorScreen.accelerationTooltip(empty);

        assertEquals(4, fortune.size());
        assertEquals(4, acceleration.size());
        assertEquals("jdte.screen.mineral_extractor.fortune_optional", translationKey(fortune.get(2)));
        assertEquals("jdte.screen.mineral_extractor.acceleration_optional", translationKey(acceleration.get(2)));
        assertEquals("jdte.screen.mineral_extractor.base_power_only", translationKey(fortune.get(3)));
        assertEquals("jdte.screen.mineral_extractor.base_power_only", translationKey(acceleration.get(3)));
    }

    private static String translationKey(FormattedText text) {
        return ((TranslatableContents) ((Component) text).getContents()).getKey();
    }
}
