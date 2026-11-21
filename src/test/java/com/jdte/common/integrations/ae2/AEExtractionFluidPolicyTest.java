package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.jdte.test.TestWorldRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AEExtractionFluidPolicyTest implements com.jdte.test.WorldRegistryTest {
    @BeforeAll
    static void loadRegistries() { TestWorldRegistries.load(); }

    @AfterEach
    void clearCache() { AEExtractionFluidPolicy.clearCache(); }

    @Test
    void existingContentsWinAndEmptyUniversalTankStaysUnselected() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), new FluidStack(Fluids.WATER, 250), null);
        assertEquals(Fluids.WATER, AEExtractionFluidPolicy.select(handler.container, handler, 0,
                List.of(Fluids.WATER, Fluids.LAVA)).getFluid());
        FakeHandler universal = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY, (tank, fluid) -> true);
        assertNull(AEExtractionFluidPolicy.select(universal.container, universal, 0, List.of(Fluids.WATER, Fluids.LAVA)));
    }

    @Test
    void emptyDedicatedTankSelectsOnlyCandidate() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER));
        FluidStack selected = AEExtractionFluidPolicy.select(handler.container, handler, 0,
                List.of(Fluids.WATER, Fluids.LAVA));
        assertNotNull(selected);
        assertEquals(Fluids.WATER, selected.getFluid());
        assertEquals(1, selected.getAmount());
    }

    @Test
    void sourceAndFlowingVariantsCountAsOneFluidTypeAndPreferSource() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.getFluid().getFluidType() == Fluids.WATER.getFluidType());
        FluidStack selected = AEExtractionFluidPolicy.select(handler.container, handler, 0,
                List.of(Fluids.FLOWING_WATER, Fluids.WATER));
        assertNotNull(selected);
        assertEquals(Fluids.WATER, selected.getFluid());
    }

    @Test
    void cachedCandidateIsClearedAndRediscoveredAfterActualRejection() {
        ItemStack container = new ItemStack(Items.BUCKET);
        FakeHandler handler = new FakeHandler(container, FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER));
        assertNotNull(AEExtractionFluidPolicy.select(handler.container, handler, 0, List.of(Fluids.WATER)));
        assertTrue(AEExtractionFluidPolicy.hasCached(container, 0));
        try (var transaction = Transaction.openRoot()) {
            assertEquals(0, handler.insert(0, FluidResource.of(Fluids.WATER), 1, transaction));
        }
        AEExtractionFluidPolicy.invalidate(container, 0);
        assertFalse(AEExtractionFluidPolicy.hasCached(container, 0));
        assertNotNull(AEExtractionFluidPolicy.select(handler.container, handler, 0, List.of(Fluids.WATER), false));
        assertTrue(AEExtractionFluidPolicy.hasCached(container, 0));
    }

    @Test
    void rejectsZeroCandidatesMultipleTanksAndInvalidTank() {
        FakeHandler handler = new FakeHandler(new ItemStack(Items.BUCKET), new FluidStack(Fluids.WATER, 1),
                (tank, fluid) -> fluid.is(Fluids.WATER));
        assertNull(AEExtractionFluidPolicy.select(handler.container, handler, 1, List.of(Fluids.WATER)));
        assertNull(AEExtractionFluidPolicy.select(handler.container, handler, -1, List.of(Fluids.WATER)));
        FakeHandler empty = new FakeHandler(new ItemStack(Items.BUCKET), FluidStack.EMPTY,
                (tank, fluid) -> fluid.is(Fluids.WATER));
        assertNull(AEExtractionFluidPolicy.select(empty.container, empty, 0, List.of()));
    }

    private static final class FakeHandler extends FluidStacksResourceHandler {
        private final ItemStack container;
        private final Validity validity;
        private FakeHandler(ItemStack container, FluidStack fluid, Validity validity) {
            super(1, 1000);
            this.container = container;
            this.validity = validity;
            set(0, FluidResource.of(fluid), fluid.getAmount());
        }

        @Override public boolean isValid(int tank, FluidResource resource) {
            return validity != null && validity.accept(tank, resource.toStack(1));
        }

        @Override public int insert(int tank, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
    @FunctionalInterface private interface Validity { boolean accept(int tank, FluidStack fluid); }
}
