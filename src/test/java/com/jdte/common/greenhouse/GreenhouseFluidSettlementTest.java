package com.jdte.common.greenhouse;

import com.jdte.common.upgrades.JDTEFluidTank;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(400, GreenhouseFluidSettlement.normalDrainPaidHarvests(
                tank, WATER, 100, 4, false));
        assertEquals(600, tank.getFluidAmount());
    }

    @Test
    void normalPathDoesNotBudgetOrDrainWrongFluid() {
        JDTEFluidTank tank = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(0, GreenhouseFluidSettlement.normalSupportedHarvests(
                tank, WATER, 100, 20, false));
        assertEquals(0, GreenhouseFluidSettlement.normalDrainPaidHarvests(
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
        assertEquals(0, GreenhouseFluidSettlement.normalDrainPaidHarvests(
                water, UNKNOWN, 100, 4, false));
        assertEquals(1_000, water.getFluidAmount());
    }

    @Test
    void normalPathZeroCostNeedsNoStoredFluidAndDoesNotDrain() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);

        assertEquals(7, GreenhouseFluidSettlement.normalSupportedHarvests(
                empty, WATER, 0, 7, false));
        assertEquals(0, GreenhouseFluidSettlement.normalDrainPaidHarvests(
                empty, WATER, 0, 7, false));
    }

    @Test
    void normalCreativePathIgnoresIdentityAndDoesNotDrain() {
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(7, GreenhouseFluidSettlement.normalSupportedHarvests(
                lava, WATER, 100, 7, true));
        assertEquals(0, GreenhouseFluidSettlement.normalDrainPaidHarvests(
                lava, WATER, 100, 7, true));
        assertEquals(1_000, lava.getFluidAmount());
    }

    @Test
    void largePathAggregatesAndDrainsOnlyMatchingMembers() {
        JDTEFluidTank firstWater = tank(new FluidStack(Fluids.WATER, 30));
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));
        JDTEFluidTank secondWater = tank(new FluidStack(Fluids.WATER, 50));
        List<IFluidHandler> members = List.of(firstWater, lava, secondWater);

        assertEquals(8, GreenhouseFluidSettlement.largeSupportedHarvests(
                members, WATER, 90, 20, false));
        assertEquals(70, GreenhouseFluidSettlement.largeDrainPaidHarvests(
                members, WATER, 90, 7, false));
        assertEquals(0, firstWater.getFluidAmount());
        assertEquals(1_000, lava.getFluidAmount());
        assertEquals(10, secondWater.getFluidAmount());
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
        assertEquals(0, GreenhouseFluidSettlement.largeDrainPaidHarvests(
                members, WATER, 90, 12, false));
        assertEquals(1_000, lava.getFluidAmount());
    }

    @Test
    void largePathZeroCostNeedsNoStoredFluidAndDoesNotDrain() {
        JDTEFluidTank empty = tank(FluidStack.EMPTY);

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(empty), WATER, 0, 12, false));
        assertEquals(0, GreenhouseFluidSettlement.largeDrainPaidHarvests(
                List.of(empty), WATER, 0, 12, false));
    }

    @Test
    void largeCreativePathIgnoresIdentityAndDoesNotDrainMembers() {
        JDTEFluidTank lava = tank(new FluidStack(Fluids.LAVA, 1_000));

        assertEquals(12, GreenhouseFluidSettlement.largeSupportedHarvests(
                List.of(lava), WATER, 90, 12, true));
        assertEquals(0, GreenhouseFluidSettlement.largeDrainPaidHarvests(
                List.of(lava), WATER, 90, 12, true));
        assertEquals(1_000, lava.getFluidAmount());
    }

    private static JDTEFluidTank tank(FluidStack initial) {
        JDTEFluidTank tank = new JDTEFluidTank(2_000, stack -> !stack.isEmpty());
        if (!initial.isEmpty()) tank.fill(initial, IFluidHandler.FluidAction.EXECUTE);
        return tank;
    }
}
