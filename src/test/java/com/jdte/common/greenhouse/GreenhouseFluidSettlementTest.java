package com.jdte.common.greenhouse;

import com.jdte.common.upgrades.JDTEFluidTank;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseFluidSettlementTest implements com.jdte.test.WorldRegistryTest {
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");
    private static final Identifier LAVA = Identifier.withDefaultNamespace("lava");
    private static final Identifier UNKNOWN =
            Identifier.fromNamespaceAndPath("jdte", "missing_fluid");

    @Test
    void normalPathBudgetsMatchingFluidAndDrainsOnlyPaidHarvests() {
        JDTEFluidTank tank = tank(new FluidStack(Fluids.WATER, 1_000));

        assertEquals(10, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, WATER, 100, 20, false));
        assertTrue(GreenhouseFluidSettlement.normalTryPay(
                tank, WATER, 100, 4, false));
        assertEquals(600, tank.getFluidAmount());
    }

    @Test
    void normalPathDrainsTheActualComponentBearingVariant() {
        FluidStack namedWater = new FluidStack(Fluids.WATER, 250);
        namedWater.set(DataComponents.CUSTOM_NAME, Component.literal("irrigation batch"));
        JDTEFluidTank tank = tank(namedWater);

        assertEquals(2, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, WATER, 100, 3, false));
        assertTrue(GreenhouseFluidSettlement.normalTryPay(
                tank, WATER, 100, 2, false));
        assertEquals(50, tank.getFluidAmount());
        assertEquals(Component.literal("irrigation batch"),
                tank.getFluid().get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void normalPathDoesNotBudgetOrDrainWrongFluid() {
        JDTEFluidTank tank = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(0, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, WATER, 100, 20, false));
        assertFalse(GreenhouseFluidSettlement.normalTryPay(
                tank, WATER, 100, 4, false));
        assertEquals(1_000, tank.getFluidAmount());
        assertEquals(Fluids.LAVA, tank.getFluid().getFluid());
    }

    @Test
    void normalPathOnlySupportsTheSlotMatchingTheSingleTank() {
        JDTEFluidTank tank = tank(new FluidStack(Fluids.WATER, 200));

        assertEquals(2, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, WATER, 100, 10, false));
        assertEquals(0, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, LAVA, 100, 10, false));
    }

    @Test
    void normalPathTreatsEmptyTankAndUnknownFluidIdAsUnavailable() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);
        JDTEFluidTank water = tank(new FluidStack(Fluids.WATER, 1_000));

        assertEquals(0, GreenhouseFluidSettlement.normalSupportedHarvests(
                empty, WATER, 100, 10, false));
        assertEquals(0, GreenhouseFluidSettlement.normalSupportedHarvests(
                water, UNKNOWN, 100, 10, false));
        assertFalse(GreenhouseFluidSettlement.normalTryPay(
                water, UNKNOWN, 100, 4, false));
        assertEquals(1_000, water.getFluidAmount());
    }

    @Test
    void normalPathZeroCostNeedsNoStoredFluidAndDoesNotDrain() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);

        assertEquals(7, GreenhouseFluidSettlement.normalSupportedHarvests(
                empty, WATER, 0, 7, false));
        assertTrue(GreenhouseFluidSettlement.normalTryPay(
                empty, WATER, 0, 7, false));
    }

    @Test
    void normalCreativePathIgnoresIdentityAndDoesNotDrain() {
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(7, GreenhouseFluidSettlement.normalSupportedHarvests(
                lava, WATER, 100, 7, true));
        assertTrue(GreenhouseFluidSettlement.normalTryPay(
                lava, WATER, 100, 7, true));
        assertEquals(1_000, lava.getFluidAmount());
    }

    @Test
    void normalPaymentFailsWhenExecutionUnderDrainsTheBudgetedVariant() {
        PerCallLimitedHandler limited = new PerCallLimitedHandler(
                new FluidStack(Fluids.WATER, 100), 40);

        assertEquals(1, GreenhouseFluidSettlement.normalSupportedHarvests(
                limited, WATER, 100, 1, false));
        assertFalse(GreenhouseFluidSettlement.normalTryPay(
                limited, WATER, 100, 1, false));
        assertEquals(100, limited.getAmountAsInt(0));
    }

    @Test
    void largePathAggregatesAndDrainsOnlyMatchingMembers() {
        JDTEFluidTank firstWater = tank(new FluidStack(Fluids.WATER, 30));
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));
        JDTEFluidTank secondWater = tank(new FluidStack(Fluids.WATER, 50));
        var members = List.of(firstWater, lava, secondWater);

        assertEquals(8, GreenhouseFluidSettlement.largeSupportedHarvests(
                members, WATER, 90, 20, false));
        assertTrue(GreenhouseFluidSettlement.largeTryPay(
                members, WATER, 90, 7, false));
        assertEquals(0, firstWater.getFluidAmount());
        assertEquals(1_000, lava.getFluidAmount());
        assertEquals(10, secondWater.getFluidAmount());
    }

    @Test
    void largePathDrainsEveryStoredVariantAcrossHandlersAndTanks() {
        FluidStack firstVariant = new FluidStack(Fluids.WATER, 30);
        firstVariant.set(DataComponents.CUSTOM_NAME, Component.literal("first"));
        FluidStack secondVariant = new FluidStack(Fluids.WATER, 50);
        secondVariant.set(DataComponents.CUSTOM_NAME, Component.literal("second"));
        var firstMember = multiTank(firstVariant, new FluidStack(Fluids.LAVA, 1_000));
        var secondMember = multiTank(new FluidStack(Fluids.WATER, 40), secondVariant);

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(firstMember, secondMember), WATER, 90, 20, false));
        assertTrue(GreenhouseFluidSettlement.largeTryPay(
                List.of(firstMember, secondMember), WATER, 90, 10, false));
        assertEquals(0, firstMember.getAmountAsInt(0));
        assertEquals(1_000, firstMember.getAmountAsInt(1));
        assertEquals(0, secondMember.getAmountAsInt(0));
        assertEquals(20, secondMember.getAmountAsInt(1));
        assertEquals(Component.literal("second"),
                secondMember.getResource(1).toStack(1).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void largePathTreatsEmptyWrongAndUnknownFluidsAsUnavailable() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));
        var members = List.of(empty, lava);

        assertEquals(0, GreenhouseFluidSettlement.largeSupportedHarvests(
                members, WATER, 90, 12, false));
        assertEquals(0, GreenhouseFluidSettlement.largeSupportedHarvests(
                members, UNKNOWN, 90, 12, false));
        assertFalse(GreenhouseFluidSettlement.largeTryPay(
                members, WATER, 90, 12, false));
        assertEquals(1_000, lava.getFluidAmount());
    }

    @Test
    void largePathZeroCostNeedsNoStoredFluidAndDoesNotDrain() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(empty), WATER, 0, 12, false));
        assertTrue(GreenhouseFluidSettlement.largeTryPay(
                List.of(empty), WATER, 0, 12, false));
    }

    @Test
    void largeCreativePathIgnoresIdentityAndDoesNotDrainMembers() {
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(lava), WATER, 90, 12, true));
        assertTrue(GreenhouseFluidSettlement.largeTryPay(
                List.of(lava), WATER, 90, 12, true));
        assertEquals(1_000, lava.getFluidAmount());
    }

    @Test
    void largePaymentFailsWhenAnyMemberUnderDrainsTheBudgetedAmount() {
        JDTEFluidTank first = tank(new FluidStack(Fluids.WATER, 30));
        PerCallLimitedHandler limited = new PerCallLimitedHandler(
                new FluidStack(Fluids.WATER, 70), 20);

        assertEquals(10, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(first, limited), WATER, 90, 10, false));
        assertFalse(GreenhouseFluidSettlement.largeTryPay(
                List.of(first, limited), WATER, 90, 10, false));
        assertEquals(30, first.getFluidAmount());
        assertEquals(70, limited.getAmountAsInt(0));
    }

    private static JDTEFluidTank tank(FluidStack initial) {
        JDTEFluidTank tank = new JDTEFluidTank(2_000, stack -> !stack.isEmpty());
        tank.setFluid(initial);
        return tank;
    }

    private static FluidStacksResourceHandler multiTank(FluidStack... initial) {
        var tanks = new FluidStacksResourceHandler(initial.length, 2_000);
        for (int i = 0; i < initial.length; i++) {
            tanks.set(i, FluidResource.of(initial[i]), initial[i].getAmount());
        }
        return tanks;
    }

    private static final class PerCallLimitedHandler extends FluidStacksResourceHandler {
        private final int limit;

        private PerCallLimitedHandler(FluidStack initial, int limit) {
            super(1, 2_000);
            set(0, FluidResource.of(initial), initial.getAmount());
            this.limit = limit;
        }

        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return super.extract(index, resource, Math.min(limit, amount), transaction);
        }
    }
}
