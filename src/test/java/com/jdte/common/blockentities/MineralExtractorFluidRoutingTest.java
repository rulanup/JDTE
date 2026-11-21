package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.jdte.common.minerals.MineralExtractorFluidRoles;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorFluidRoutingTest implements com.jdte.test.WorldRegistryTest {
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");
    private static final Identifier LAVA = Identifier.withDefaultNamespace("lava");
    private static final Identifier FLOWING_WATER = Identifier.withDefaultNamespace("flowing_water");

    @Test
    void usesDefaultFluidRolesBeforeAWorldIsAttached() {
        MineralExtractorBE extractor = new MineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
        var fluids = extractor.getCombinedFluidHandler();

        assertEquals(1_000, insert(fluids, 
                stack(JDTRegistration.XP_FLUID_SOURCE.get(), 1_000)));
        assertEquals(1_000, insert(fluids, 
                stack(JDTRegistration.TIME_FLUID_SOURCE.get(), 1_000)));
    }

    @Test
    void routesCurrentFortuneAndAccelerationFluidsToTheirDedicatedTanks() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        var fluids = extractor.getCombinedFluidHandler();

        assertTrue(valid(fluids, 0, stack(Fluids.LAVA, 1_000)));
        assertFalse(valid(fluids, 1, stack(Fluids.LAVA, 1_000)));
        assertEquals(1_000, insert(fluids, stack(Fluids.LAVA, 1_000)));
        assertEquals(Fluids.LAVA, contents(fluids, 0).getFluid());
        assertTrue(valid(fluids, 1, stack(Fluids.WATER, 1_000)));
        assertFalse(valid(fluids, 0, stack(Fluids.WATER, 1_000)));
        assertEquals(1_000, insert(fluids, stack(Fluids.WATER, 1_000)));
        assertEquals(Fluids.WATER, contents(fluids, 1).getFluid());

        assertEquals(0, insert(fluids, stack(Fluids.FLOWING_WATER, 1_000)));
    }

    @Test
    void reloadMakesStoredFormerRolesUnusableWithoutPreventingTheirRemoval() {
        AtomicReference<MineralExtractorFluidRoles> roles = new AtomicReference<>(
                new MineralExtractorFluidRoles(LAVA, WATER));
        MineralExtractorBE extractor = extractor(roles);
        var fluids = extractor.getCombinedFluidHandler();
        FluidStack namedLava = named(Fluids.LAVA, 1_000, "former fortune");
        FluidStack namedWater = named(Fluids.WATER, 1_000, "former acceleration");
        insert(fluids, namedLava);
        insert(fluids, namedWater);

        roles.set(new MineralExtractorFluidRoles(WATER, LAVA));

        assertEquals(0, extractor.usableFortuneFluid());
        assertEquals(0, extractor.usableAccelerationFluid());

        FluidStack drainedLava = extract(fluids, 
                contents(fluids, 0).copyWithAmount(1_000), true);
        FluidStack drainedWater = extract(fluids, 1_000, true);
        assertTrue(FluidStack.isSameFluidSameComponents(namedLava, drainedLava));
        assertTrue(FluidStack.isSameFluidSameComponents(namedWater, drainedWater));
        assertEquals(1_000, drainedLava.getAmount());
        assertEquals(1_000, drainedWater.getAmount());

        assertEquals(1_000, insert(fluids, stack(Fluids.WATER, 1_000)));
        assertEquals(Fluids.WATER, contents(fluids, 0).getFluid());
        assertEquals(1_000, insert(fluids, stack(Fluids.LAVA, 1_000)));
        assertEquals(Fluids.LAVA, contents(fluids, 1).getFluid());
    }

    @Test
    void specifiedDrainRejectsSameFluidWithDifferentComponents() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        var fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "component-bearing fortune");
        insert(fluids, stored);

        FluidStack drained = extract(fluids, stack(Fluids.LAVA, 250), true);

        assertTrue(drained.isEmpty());
        assertEquals(1_000, contents(fluids, 0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, contents(fluids, 0)));
    }

    @Test
    void specifiedDrainSimulationReturnsMatchingVariantWithoutMutation() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        var fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "simulated fortune");
        insert(fluids, stored);

        FluidStack drained = extract(fluids, 
                stored.copyWithAmount(275), false);

        assertEquals(275, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(1_000, contents(fluids, 0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, contents(fluids, 0)));
    }

    @Test
    void specifiedDrainExecuteRemovesTheExactRequestedAmount() {
        MineralExtractorBE extractor = extractor(new MineralExtractorFluidRoles(LAVA, WATER));
        var fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "executed fortune");
        insert(fluids, stored);

        FluidStack drained = extract(fluids, 
                stored.copyWithAmount(275), true);

        assertEquals(275, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(725, contents(fluids, 0).getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, contents(fluids, 0)));
    }

    @Test
    void untypedDrainCanRemoveAResidualComponentVariant() {
        AtomicReference<MineralExtractorFluidRoles> roles = new AtomicReference<>(
                new MineralExtractorFluidRoles(LAVA, WATER));
        MineralExtractorBE extractor = extractor(roles);
        var fluids = extractor.getCombinedFluidHandler();
        FluidStack stored = named(Fluids.LAVA, 1_000, "former configured fluid");
        insert(fluids, stored);
        roles.set(new MineralExtractorFluidRoles(WATER, LAVA));

        FluidStack drained = extract(fluids, 400, true);

        assertEquals(400, drained.getAmount());
        assertTrue(FluidStack.isSameFluidSameComponents(stored, drained));
        assertEquals(600, contents(fluids, 0).getAmount());
    }

    @Test
    void largeExtractorInheritsTheSameDynamicTankMapping() {
        LargeMineralExtractorBE extractor = new LargeMineralExtractorBE(
                BlockPos.ZERO, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
        extractor.setFluidRolesResolver(ignored -> new MineralExtractorFluidRoles(LAVA, WATER));
        var fluids = extractor.getCombinedFluidHandler();

        assertEquals(1_000, insert(fluids, stack(Fluids.LAVA, 1_000)));
        assertEquals(Fluids.LAVA, contents(fluids, 0).getFluid());
        assertEquals(1_000, insert(fluids, stack(Fluids.WATER, 1_000)));
        assertEquals(Fluids.WATER, contents(fluids, 1).getFluid());
    }

    private static boolean valid(ResourceHandler<FluidResource> handler, int slot, FluidStack stack) {
        return handler.isValid(slot, FluidResource.of(stack));
    }

    private static FluidStack contents(ResourceHandler<FluidResource> handler, int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    private static int insert(ResourceHandler<FluidResource> handler, FluidStack stack) {
        try (var transaction = Transaction.openRoot()) {
            int accepted = handler.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            transaction.commit();
            return accepted;
        }
    }

    private static FluidStack extract(ResourceHandler<FluidResource> handler, FluidStack requested, boolean commit) {
        try (var transaction = Transaction.openRoot()) {
            var resource = FluidResource.of(requested);
            int amount = handler.extract(resource, requested.getAmount(), transaction);
            if (commit) transaction.commit();
            return resource.toStack(amount);
        }
    }

    private static FluidStack extract(ResourceHandler<FluidResource> handler, int limit, boolean commit) {
        try (var transaction = Transaction.openRoot()) {
            var drained = ResourceHandlerUtil.extractFirst(handler, fluid -> true, limit, transaction);
            if (commit) transaction.commit();
            return drained == null ? FluidStack.EMPTY : drained.resource().toStack(drained.amount());
        }
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
