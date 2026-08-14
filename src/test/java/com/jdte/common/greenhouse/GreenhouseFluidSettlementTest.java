package com.jdte.common.greenhouse;

import com.jdte.common.upgrades.JDTEFluidTank;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseFluidSettlementTest {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation LAVA = ResourceLocation.withDefaultNamespace("lava");
    private static final ResourceLocation UNKNOWN =
            ResourceLocation.fromNamespaceAndPath("jdte", "missing_fluid");

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
        assertEquals(60, limited.getFluidInTank(0).getAmount());
    }

    @Test
    void largePathAggregatesAndDrainsOnlyMatchingMembers() {
        JDTEFluidTank firstWater = tank(new FluidStack(Fluids.WATER, 30));
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));
        JDTEFluidTank secondWater = tank(new FluidStack(Fluids.WATER, 50));
        List<IFluidHandler> members = List.of(firstWater, lava, secondWater);

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
        MultiTankHandler firstMember = multiTank(firstVariant, new FluidStack(Fluids.LAVA, 1_000));
        MultiTankHandler secondMember = multiTank(new FluidStack(Fluids.WATER, 40), secondVariant);

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(firstMember, secondMember), WATER, 90, 20, false));
        assertTrue(GreenhouseFluidSettlement.largeTryPay(
                List.of(firstMember, secondMember), WATER, 90, 10, false));
        assertEquals(0, firstMember.getFluidInTank(0).getAmount());
        assertEquals(1_000, firstMember.getFluidInTank(1).getAmount());
        assertEquals(0, secondMember.getFluidInTank(0).getAmount());
        assertEquals(20, secondMember.getFluidInTank(1).getAmount());
        assertEquals(Component.literal("second"),
                secondMember.getFluidInTank(1).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void largePathTreatsEmptyWrongAndUnknownFluidsAsUnavailable() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));
        List<IFluidHandler> members = List.of(empty, lava);

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
        assertEquals(0, first.getFluidAmount());
        assertEquals(50, limited.getFluidInTank(0).getAmount());
    }

    private static JDTEFluidTank tank(FluidStack initial) {
        JDTEFluidTank tank = new JDTEFluidTank(2_000, stack -> !stack.isEmpty());
        if (!initial.isEmpty()) tank.fill(initial, IFluidHandler.FluidAction.EXECUTE);
        return tank;
    }

    private static MultiTankHandler multiTank(FluidStack... initial) {
        List<FluidTank> tanks = new ArrayList<>(initial.length);
        for (FluidStack stack : initial) {
            FluidTank tank = new FluidTank(2_000);
            tank.setFluid(stack.copy());
            tanks.add(tank);
        }
        return new MultiTankHandler(tanks);
    }

    private record MultiTankHandler(List<FluidTank> tanks) implements IFluidHandler {
        @Override public int getTanks() { return tanks.size(); }
        @Override public FluidStack getFluidInTank(int tank) { return tanks.get(tank).getFluid(); }
        @Override public int getTankCapacity(int tank) { return tanks.get(tank).getCapacity(); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tanks.get(tank).isFluidValid(stack);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            int filled = 0;
            for (FluidTank tank : tanks) {
                if (filled >= resource.getAmount()) break;
                filled += tank.fill(resource.copyWithAmount(resource.getAmount() - filled), action);
            }
            return filled;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack result = FluidStack.EMPTY;
            int remaining = resource.getAmount();
            for (FluidTank tank : tanks) {
                if (remaining == 0) break;
                FluidStack drained = tank.drain(resource.copyWithAmount(remaining), action);
                if (drained.isEmpty()) continue;
                if (result.isEmpty()) result = drained.copy();
                else result.grow(drained.getAmount());
                remaining -= drained.getAmount();
            }
            return result;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank tank : tanks) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) return drained;
            }
            return FluidStack.EMPTY;
        }
    }

    private static final class PerCallLimitedHandler implements IFluidHandler {
        private final FluidTank tank = new FluidTank(2_000);
        private final int limit;

        private PerCallLimitedHandler(FluidStack initial, int limit) {
            tank.setFluid(initial.copy());
            this.limit = limit;
        }

        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tankIndex) { return tank.getFluid(); }
        @Override public int getTankCapacity(int tankIndex) { return tank.getCapacity(); }
        @Override public boolean isFluidValid(int tankIndex, FluidStack stack) { return tank.isFluidValid(stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return tank.fill(resource, action); }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource.copyWithAmount(Math.min(limit, resource.getAmount())), action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(Math.min(limit, maxDrain), action);
        }
    }
}
