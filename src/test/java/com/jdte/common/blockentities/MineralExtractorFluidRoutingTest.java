package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.minerals.MineralExtractorFluidRoles;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorFluidRoutingTest {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation LAVA = ResourceLocation.withDefaultNamespace("lava");
    private static final ResourceLocation FLOWING_WATER = ResourceLocation.withDefaultNamespace("flowing_water");

    @Test
    void usesDefaultFluidRolesBeforeAWorldIsAttached() {
        MineralExtractorBE extractor = new MineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
        IFluidHandler fluids = extractor.getCombinedFluidHandler();

        assertEquals(1_000, fluids.fill(
                stack(Registration.XP_FLUID_SOURCE.get(), 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(1_000, fluids.fill(
                stack(Registration.TIME_FLUID_SOURCE.get(), 1_000), IFluidHandler.FluidAction.EXECUTE));
    }

    @Test
    void routesCurrentFortuneAndAccelerationFluidsToTheirDedicatedTanks() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        IFluidHandler fluids = extractor.getCombinedFluidHandler();

        assertTrue(fluids.isFluidValid(0, stack(Fluids.LAVA, 1_000)));
        assertFalse(fluids.isFluidValid(1, stack(Fluids.LAVA, 1_000)));
        assertEquals(1_000, fluids.fill(stack(Fluids.LAVA, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.LAVA, fluids.getFluidInTank(0).getFluid());
        assertTrue(fluids.isFluidValid(1, stack(Fluids.WATER, 1_000)));
        assertFalse(fluids.isFluidValid(0, stack(Fluids.WATER, 1_000)));
        assertEquals(1_000, fluids.fill(stack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.WATER, fluids.getFluidInTank(1).getFluid());

        assertEquals(0, fluids.fill(stack(Fluids.FLOWING_WATER, 1_000), IFluidHandler.FluidAction.EXECUTE));
    }

    @Test
    void reloadMakesStoredFormerRolesUnusableWithoutPreventingTheirRemoval() {
        AtomicReference<MineralExtractorFluidRoles> roles = new AtomicReference<>(
                new MineralExtractorFluidRoles(LAVA, WATER));
        MineralExtractorBE extractor = extractor(roles);
        IFluidHandler fluids = extractor.getCombinedFluidHandler();
        FluidStack namedLava = named(Fluids.LAVA, 1_000, "former fortune");
        FluidStack namedWater = named(Fluids.WATER, 1_000, "former acceleration");
        fluids.fill(namedLava, IFluidHandler.FluidAction.EXECUTE);
        fluids.fill(namedWater, IFluidHandler.FluidAction.EXECUTE);

        roles.set(new MineralExtractorFluidRoles(WATER, LAVA));

        assertEquals(0, extractor.usableFortuneFluid());
        assertEquals(0, extractor.usableAccelerationFluid());

        FluidStack drainedLava = fluids.drain(
                fluids.getFluidInTank(0).copyWithAmount(1_000), IFluidHandler.FluidAction.EXECUTE);
        FluidStack drainedWater = fluids.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
        assertTrue(FluidStack.isSameFluidSameComponents(namedLava, drainedLava));
        assertTrue(FluidStack.isSameFluidSameComponents(namedWater, drainedWater));
        assertEquals(1_000, drainedLava.getAmount());
        assertEquals(1_000, drainedWater.getAmount());

        assertEquals(1_000, fluids.fill(stack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.WATER, fluids.getFluidInTank(0).getFluid());
        assertEquals(1_000, fluids.fill(stack(Fluids.LAVA, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.LAVA, fluids.getFluidInTank(1).getFluid());
    }

    @Test
    void specifiedDrainRejectsSameFluidWithDifferentComponents() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        IFluidHandler fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "component-bearing fortune");
        fluids.fill(stored, IFluidHandler.FluidAction.EXECUTE);

        FluidStack drained = fluids.drain(stack(Fluids.LAVA, 250), IFluidHandler.FluidAction.EXECUTE);

        assertTrue(drained.isEmpty());
        assertEquals(1_000, fluids.getFluidInTank(0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, fluids.getFluidInTank(0)));
    }

    @Test
    void specifiedDrainSimulationReturnsMatchingVariantWithoutMutation() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        IFluidHandler fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "simulated fortune");
        fluids.fill(stored, IFluidHandler.FluidAction.EXECUTE);

        FluidStack drained = fluids.drain(
                stored.copyWithAmount(275), IFluidHandler.FluidAction.SIMULATE);

        assertEquals(275, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(1_000, fluids.getFluidInTank(0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, fluids.getFluidInTank(0)));
    }

    @Test
    void specifiedDrainExecuteRemovesTheExactRequestedAmount() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        IFluidHandler fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "executed fortune");
        fluids.fill(stored, IFluidHandler.FluidAction.EXECUTE);

        FluidStack drained = fluids.drain(
                stored.copyWithAmount(275), IFluidHandler.FluidAction.EXECUTE);

        assertEquals(275, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(725, fluids.getFluidInTank(0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, fluids.getFluidInTank(0)));
    }

    @Test
    void untypedDrainCanRemoveAResidualComponentVariant() {
        AtomicReference<MineralExtractorFluidRoles> roles = new AtomicReference<>(
                new MineralExtractorFluidRoles(LAVA, WATER));
        MineralExtractorBE extractor = extractor(roles);
        IFluidHandler fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "former configured fluid");
        fluids.fill(stored, IFluidHandler.FluidAction.EXECUTE);
        roles.set(new MineralExtractorFluidRoles(WATER, LAVA));

        FluidStack drained = fluids.drain(400, IFluidHandler.FluidAction.EXECUTE);

        assertEquals(400, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(600, fluids.getFluidInTank(0).getAmount());
    }

    @Test
    void largeExtractorInheritsTheSameDynamicTankMapping() {
        LargeMineralExtractorBE extractor = new LargeMineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
        extractor.setFluidRolesResolver(ignored -> new MineralExtractorFluidRoles(LAVA, WATER));
        IFluidHandler fluids = extractor.getCombinedFluidHandler();

        assertEquals(1_000, fluids.fill(stack(Fluids.LAVA, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.LAVA, fluids.getFluidInTank(0).getFluid());
        assertEquals(1_000, fluids.fill(stack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(Fluids.WATER, fluids.getFluidInTank(1).getFluid());
    }

    private static MineralExtractorBE extractor(MineralExtractorFluidRoles roles) {
        return extractor(new AtomicReference<>(roles));
    }

    private static MineralExtractorBE extractor(AtomicReference<MineralExtractorFluidRoles> roles) {
        MineralExtractorBE extractor = new MineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
        extractor.setFluidRolesResolver(ignored -> roles.get());
        return extractor;
    }

    private static FluidStack stack(net.minecraft.world.level.material.Fluid fluid, int amount) {
        return new FluidStack(fluid, amount);
    }

    private static FluidStack named(net.minecraft.world.level.material.Fluid fluid, int amount, String name) {
        FluidStack stack = stack(fluid, amount);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }
}
