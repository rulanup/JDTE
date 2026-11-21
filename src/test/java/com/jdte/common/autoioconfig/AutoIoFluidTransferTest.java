package com.jdte.common.autoioconfig;

import com.jdte.test.WorldRegistryTest;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoIoFluidTransferTest implements WorldRegistryTest {
    @Test void bothFluidsMoveWithinOneBudgetInEitherDirection() {
        var source = new FluidStacksResourceHandler(2, 2000);
        source.set(0, FluidResource.of(Fluids.WATER), 1500);
        source.set(1, FluidResource.of(Fluids.LAVA), 1500);
        var target = new FluidStacksResourceHandler(2, 2000);
        assertEquals(1000, AutoIoTransferHelper.moveFluids(source, target, 1000, 0));
        assertEquals(500, target.getAmountAsInt(0));
        assertEquals(500, target.getAmountAsInt(1));
        assertEquals(FluidResource.of(Fluids.WATER), target.getResource(0));
        assertEquals(FluidResource.of(Fluids.LAVA), target.getResource(1));
        assertEquals(1000, AutoIoTransferHelper.moveFluids(target, source, 1000, 1));
        assertEquals(1500, source.getAmountAsInt(0));
        assertEquals(1500, source.getAmountAsInt(1));
        assertEquals(0, target.getAmountAsInt(0) + target.getAmountAsInt(1));
    }

    @Test void rejectedFluidLeavesItsShareForAnAcceptedFluid() {
        var source = new FluidStacksResourceHandler(2, 2000);
        source.set(0, FluidResource.of(Fluids.WATER), 1500);
        source.set(1, FluidResource.of(Fluids.LAVA), 1500);
        var target = new FluidStacksResourceHandler(1, 2000);
        target.set(0, FluidResource.of(Fluids.WATER), 1);
        assertEquals(1000, AutoIoTransferHelper.moveFluids(source, target, 1000, 0));
        assertEquals(1001, target.getAmountAsInt(0));
        assertEquals(1500, source.getAmountAsInt(1));
    }
}
