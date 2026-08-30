package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AEExtractionFluidPolicyTest {
    @AfterEach
    void clearCache() { AEExtractionFluidPolicy.clearCache(); }

    @Test
    void existingContentsWinAndEmptyUniversalTankStaysUnselected() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), new FluidStack(Fluids.WATER, 250), null);
        assertEquals(Fluids.WATER, AEExtractionFluidPolicy.select(handler, 0,
                List.of(Fluids.WATER, Fluids.LAVA)).getFluid());
        FakeHandler universal = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY, (tank, fluid) -> true);
        assertNull(AEExtractionFluidPolicy.select(universal, 0, List.of(Fluids.WATER, Fluids.LAVA)));
    }

    @Test
    void emptyDedicatedTankSelectsOnlyCandidate() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER));
        FluidStack selected = AEExtractionFluidPolicy.select(handler, 0,
                List.of(Fluids.WATER, Fluids.LAVA));
        assertNotNull(selected);
        assertEquals(Fluids.WATER, selected.getFluid());
        assertEquals(1, selected.getAmount());
    }

    @Test
    void sourceAndFlowingVariantsCountAsOneFluidTypeAndPreferSource() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.getFluid().getFluidType() == Fluids.WATER.getFluidType());
        FluidStack selected = AEExtractionFluidPolicy.select(handler, 0,
                List.of(Fluids.FLOWING_WATER, Fluids.WATER));
        assertNotNull(selected);
        assertEquals(Fluids.WATER, selected.getFluid());
    }

    @Test
    void cachedCandidateIsClearedAndRediscoveredAfterActualRejection() {
        ItemStack container = new ItemStack(Items.BUCKET);
        FakeHandler handler = new FakeHandler(container, FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER));
        assertNotNull(AEExtractionFluidPolicy.select(handler, 0, List.of(Fluids.WATER)));
        assertTrue(AEExtractionFluidPolicy.hasCached(container, 0));
        assertEquals(0, handler.fill(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.SIMULATE));
        AEExtractionFluidPolicy.invalidate(container, 0);
        assertFalse(AEExtractionFluidPolicy.hasCached(container, 0));
        assertNotNull(AEExtractionFluidPolicy.select(handler, 0, List.of(Fluids.WATER), false));
        assertTrue(AEExtractionFluidPolicy.hasCached(container, 0));
    }

    @Test
    void rejectsZeroCandidatesMultipleTanksAndInvalidTank() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), new FluidStack(Fluids.WATER, 1),
                (tank, fluid) -> fluid.is(Fluids.WATER));
        assertNull(AEExtractionFluidPolicy.select(handler, 1, List.of(Fluids.WATER)));
        assertNull(AEExtractionFluidPolicy.select(handler, -1, List.of(Fluids.WATER)));
        assertNull(AEExtractionFluidPolicy.select(new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER)), 0, List.of()));
    }

    private static final class FakeHandler implements IFluidHandlerItem {
        private final ItemStack container;
        private final FluidStack fluid;
        private final Validity validity;
        private FakeHandler(ItemStack container, FluidStack fluid, Validity validity) {
            this.container = container; this.fluid = fluid; this.validity = validity;
        }
        @Override public ItemStack getContainer() { return container; }
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return fluid; }
        @Override public int getTankCapacity(int tank) { return 1000; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return validity != null && validity.accept(tank, stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }
    @FunctionalInterface private interface Validity { boolean accept(int tank, FluidStack fluid); }
}
