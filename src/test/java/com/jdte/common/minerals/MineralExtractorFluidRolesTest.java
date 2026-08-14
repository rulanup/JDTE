package com.jdte.common.minerals;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorFluidRolesTest {
    private static final ResourceLocation LAVA_ID = ResourceLocation.withDefaultNamespace("lava");
    private static final ResourceLocation WATER_ID = ResourceLocation.withDefaultNamespace("water");

    @Test
    void routesConfiguredFluidRegistryIdsToTheirProductionRoles() {
        var roles = new MineralExtractorFluidRoles(LAVA_ID, WATER_ID);
        var lava = new FluidStack(Fluids.LAVA, 1000);
        var water = new FluidStack(Fluids.WATER, 1000);
        var milk = new FluidStack(Fluids.FLOWING_WATER, 1000);

        assertEquals(MineralExtractorFluidRoles.Role.FORTUNE, roles.roleOf(lava));
        assertEquals(MineralExtractorFluidRoles.Role.ACCELERATION, roles.roleOf(water));
        assertEquals(MineralExtractorFluidRoles.Role.NONE, roles.roleOf(milk));
        assertTrue(roles.matchesFortune(lava));
        assertFalse(roles.matchesAcceleration(lava));
    }

    @Test
    void leavesFormerConfiguredFluidUntouchedAfterRolesReload() {
        var xpFluidId = ResourceLocation.fromNamespaceAndPath("justdirethings", "xp_fluid_source");
        var xpFluid = BuiltInRegistries.FLUID.get(xpFluidId);
        var formerFortuneFluid = new FluidStack(xpFluid, 750);
        var reloadedRoles = new MineralExtractorFluidRoles(WATER_ID, LAVA_ID);

        assertEquals(xpFluidId, BuiltInRegistries.FLUID.getKey(xpFluid));
        assertEquals(MineralExtractorFluidRoles.Role.NONE, reloadedRoles.roleOf(formerFortuneFluid));
        assertEquals(750, formerFortuneFluid.getAmount());
        assertEquals(xpFluid, formerFortuneFluid.getFluid());
    }
}
