package com.jdte.client.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Final render and tooltip data for one mineral extractor fluid role. */
record MineralExtractorFluidDisplay(
        FluidStack renderedFluid,
        int amount,
        int capacity,
        Component tooltipName
) {
    static MineralExtractorFluidDisplay create(Fluid actual, Fluid configured, int amount, int capacity) {
        Fluid safeActual = actual == null ? Fluids.EMPTY : actual;
        Fluid safeConfigured = configured == null ? Fluids.EMPTY : configured;
        boolean hasActualFluid = safeActual != Fluids.EMPTY;
        Fluid selected = hasActualFluid ? safeActual : safeConfigured;
        int displayedAmount = hasActualFluid ? Math.max(0, amount) : 0;
        FluidStack rendered = displayedAmount > 0 ? new FluidStack(safeActual, displayedAmount) : FluidStack.EMPTY;
        return new MineralExtractorFluidDisplay(
                rendered, displayedAmount, Math.max(1, capacity), new FluidStack(selected, 1).getHoverName());
    }
}
