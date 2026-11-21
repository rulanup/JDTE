package com.jdte.common.greenhouse;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseFluidPolicyTest implements com.jdte.test.WorldRegistryTest {
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");
    private static final Identifier LAVA = Identifier.withDefaultNamespace("lava");

    @Test
    void matchesStoredFluidByRegistryId() {
        assertTrue(GreenhouseFluidPolicy.matches(new FluidStack(Fluids.WATER, 1_000), WATER));
        assertFalse(GreenhouseFluidPolicy.matches(new FluidStack(Fluids.LAVA, 1_000), WATER));
        assertFalse(GreenhouseFluidPolicy.matches(FluidStack.EMPTY, WATER));
        assertFalse(GreenhouseFluidPolicy.matches(
                new FluidStack(Fluids.WATER, 1_000), Identifier.fromNamespaceAndPath("jdte", "missing")));
    }

    @Test
    void exposesOnlyMatchingFluidAsAvailable() {
        FluidStack stored = new FluidStack(Fluids.WATER, 1_000);

        assertEquals(1_000, GreenhouseFluidPolicy.available(stored, WATER));
        assertEquals(0, GreenhouseFluidPolicy.available(stored, LAVA));
        assertEquals(0, GreenhouseFluidPolicy.available(FluidStack.EMPTY, WATER));
    }

    @Test
    void zeroConsumptionNeedsNoStoredFluid() {
        assertTrue(GreenhouseFluidPolicy.canConsume(FluidStack.EMPTY, WATER, 0, false));
    }

    @Test
    void creativeBypassesFluidIdentityAndAmount() {
        assertTrue(GreenhouseFluidPolicy.canConsume(
                new FluidStack(Fluids.LAVA, 1), WATER, 1_000, true));
    }

    @Test
    void nonCreativeRequiresEnoughMatchingFluid() {
        assertTrue(GreenhouseFluidPolicy.canConsume(
                new FluidStack(Fluids.WATER, 1_000), WATER, 1_000, false));
        assertFalse(GreenhouseFluidPolicy.canConsume(
                new FluidStack(Fluids.WATER, 999), WATER, 1_000, false));
        assertFalse(GreenhouseFluidPolicy.canConsume(
                new FluidStack(Fluids.LAVA, 1_000), WATER, 1_000, false));
    }
}
