package com.jdte.common.blockentities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixFluidBudgetTest {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation LAVA = ResourceLocation.withDefaultNamespace("lava");

    @Test
    void sameFluidSequentialPaymentsShareOneComponentAgnosticLedgerAndAdvanceTheCursor() {
        TrackingFluidHandler first = handler(namedWater(30, "first"));
        TrackingFluidHandler second = handler(new FluidStack(Fluids.WATER, 40));
        TrackingFluidHandler third = handler(namedWater(50, "third"));
        GreenhouseMatrixFluidBudget budget = GreenhouseMatrixFluidBudget.capture(
                List.of(first, second, third));

        assertEquals(120L, budget.available(WATER));
        assertEquals(120L, budget.available(WATER));
        assertEquals(1, first.reads());
        assertEquals(1, second.reads());
        assertEquals(1, third.reads());

        assertTrue(budget.tryPay(WATER, 60L, false));
        int firstReadsAfterFirstPayment = first.reads();
        assertEquals(60L, budget.available(WATER));

        assertTrue(budget.tryPay(WATER, 50L, false));
        assertEquals(10L, budget.available(WATER));
        assertEquals(firstReadsAfterFirstPayment, first.reads(),
                "the depleted first bucket entry must not be rescanned");
        assertEquals(0, first.amount());
        assertEquals(0, second.amount());
        assertEquals(10, third.amount());
    }

    @Test
    void differentFluidBucketsRemainIsolatedDuringPayment() {
        TrackingFluidHandler water = handler(new FluidStack(Fluids.WATER, 30));
        TrackingFluidHandler lava = handler(new FluidStack(Fluids.LAVA, 50));
        GreenhouseMatrixFluidBudget budget = GreenhouseMatrixFluidBudget.capture(List.of(water, lava));

        assertEquals(30L, budget.available(WATER));
        assertEquals(50L, budget.available(LAVA));
        assertTrue(budget.tryPay(WATER, 20L, false));

        assertEquals(10L, budget.available(WATER));
        assertEquals(50L, budget.available(LAVA));
        assertEquals(1, lava.reads(), "water payment must not inspect the lava bucket again");
        assertEquals(0, lava.drainCalls());
        assertEquals(50, lava.amount());
    }

    @Test
    void creativePaymentDoesNotDrainOrReinspectStoredFluid() {
        TrackingFluidHandler water = handler(namedWater(100, "creative"));
        GreenhouseMatrixFluidBudget budget = GreenhouseMatrixFluidBudget.capture(List.of(water));

        assertTrue(budget.tryPay(WATER, 100L, true));

        assertEquals(100L, budget.available(WATER));
        assertEquals(1, water.reads());
        assertEquals(0, water.drainCalls());
        assertEquals(100, water.amount());
    }

    @Test
    void underDrainFailsPaymentAndDebitsOnlyTheAmountActuallyRemoved() {
        TrackingFluidHandler limited = new TrackingFluidHandler(
                new FluidStack(Fluids.WATER, 100), 20);
        GreenhouseMatrixFluidBudget budget = GreenhouseMatrixFluidBudget.capture(List.of(limited));

        assertFalse(budget.tryPay(WATER, 50L, false));

        assertEquals(80L, budget.available(WATER));
        assertEquals(80, limited.amount());
        assertEquals(1, limited.drainCalls());
    }

    private static TrackingFluidHandler handler(FluidStack initial) {
        return new TrackingFluidHandler(initial, Integer.MAX_VALUE);
    }

    private static FluidStack namedWater(int amount, String name) {
        FluidStack stack = new FluidStack(Fluids.WATER, amount);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static final class TrackingFluidHandler implements IFluidHandler {
        private FluidStack stored;
        private final int perCallLimit;
        private int reads;
        private int drainCalls;

        private TrackingFluidHandler(FluidStack stored, int perCallLimit) {
            this.stored = stored.copy();
            this.perCallLimit = perCallLimit;
        }

        int reads() { return reads; }
        int drainCalls() { return drainCalls; }
        int amount() { return stored.getAmount(); }

        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) {
            reads++;
            return stored;
        }
        @Override public int getTankCapacity(int tank) { return 1_000; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return !stack.isEmpty(); }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            drainCalls++;
            if (stored.isEmpty() || !FluidStack.isSameFluidSameComponents(stored, resource)) {
                return FluidStack.EMPTY;
            }
            int drainedAmount = Math.min(Math.min(stored.getAmount(), resource.getAmount()), perCallLimit);
            FluidStack drained = stored.copyWithAmount(drainedAmount);
            if (action == FluidAction.EXECUTE) {
                int remaining = stored.getAmount() - drainedAmount;
                stored = remaining == 0 ? FluidStack.EMPTY : stored.copyWithAmount(remaining);
            }
            return drained;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return stored.isEmpty() ? FluidStack.EMPTY
                    : drain(stored.copyWithAmount(maxDrain), action);
        }
    }
}
