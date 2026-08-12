package com.jdte.common.blockentities;

import com.jdte.common.greenhouse.GreenhouseMatrixPortType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixAutoIoTest {
    @Test
    void matrixFluidInputDoesNotReuseTheOrdinaryMachineRateCap() {
        FluidTank source = new FluidTank(5_000_000);
        source.setFluid(new FluidStack(Fluids.WATER, 5_000_000));
        FluidTank target = new FluidTank(5_000_000);

        int limit = GreenhouseMatrixAutoIo.transferLimit(
                GreenhouseMatrixPortType.FLUID_INPUT, 10_000, 1_000_000);
        int moved = GreenhouseMatrixAutoIo.pullFluid(source, target, limit);

        assertEquals(5_000_000, moved);
        assertEquals(0, source.getFluidAmount());
        assertEquals(5_000_000, target.getFluidAmount());
    }

    @Test
    void itemPortsStillUseTheConfiguredOrdinaryMachineRate() {
        assertEquals(10_000, GreenhouseMatrixAutoIo.transferLimit(
                GreenhouseMatrixPortType.ITEM_INPUT, 10_000, 1_000_000));
        assertEquals(10_000, GreenhouseMatrixAutoIo.transferLimit(
                GreenhouseMatrixPortType.ITEM_OUTPUT, 10_000, 1_000_000));
    }
}
