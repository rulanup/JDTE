package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UltimatePortalGunCostTest {
    @Test
    void manualSameDimensionCostUsesRealDistanceAndRoundsUp() {
        NBTHelpers.PortalDestination destination = destination(Level.OVERWORLD, new Vec3(3, 68, 0));

        assertEquals(125, UltimatePortalGunItem.calculateManualFluidCost(
                Level.OVERWORLD, new Vec3(0, 64, 0), destination));
        assertEquals(26, UltimatePortalGunItem.calculateManualFluidCost(
                Level.OVERWORLD, Vec3.ZERO, destination(Level.OVERWORLD, new Vec3(1.001, 0, 0))));
    }

    @Test
    void manualSameDimensionCostIsCapped() {
        NBTHelpers.PortalDestination destination = destination(Level.OVERWORLD, new Vec3(2_000, 64, 0));

        assertEquals(25_000, UltimatePortalGunItem.calculateManualFluidCost(
                Level.OVERWORLD, new Vec3(0, 64, 0), destination));
    }

    @Test
    void manualCrossDimensionCostIsFixed() {
        NBTHelpers.PortalDestination destination = destination(Level.NETHER, new Vec3(30_000, -40, 12_000));

        assertEquals(500, UltimatePortalGunItem.calculateManualFluidCost(
                Level.OVERWORLD, Vec3.ZERO, destination));
    }

    @Test
    void manuallyEditedSlotUsesManualCostInsteadOfJdtFavoriteCost() {
        ItemStack gun = gunWithFirstPage();
        NBTHelpers.PortalDestination destination = destination(Level.OVERWORLD, new Vec3(3, 4, 0));
        UltimatePortalGunItem.setDestination(gun, 0, destination);
        PortalGunV2.setFavoritePosition(gun, 0);

        assertEquals(125, UltimatePortalGunItem.calculateDestinationFluidCost(
                gun, false, Level.OVERWORLD, Vec3.ZERO, destination,
                () -> {
                    throw new AssertionError("manual destinations must not use JDT favorite pricing");
                }));
    }

    @Test
    void quickAddedSlotKeepsJdtFavoriteCost() {
        ItemStack gun = gunWithFirstPage();
        NBTHelpers.PortalDestination destination = destination(Level.OVERWORLD, new Vec3(3, 4, 0));
        UltimatePortalGunItem.fillDestination(gun, 0, destination);
        PortalGunV2.setFavoritePosition(gun, 0);

        assertEquals(73, UltimatePortalGunItem.calculateDestinationFluidCost(
                gun, false, Level.OVERWORLD, Vec3.ZERO, destination, () -> 73));
    }

    @Test
    void previousDestinationKeepsJdtCostWhenSelectedSlotIsManual() {
        ItemStack gun = gunWithFirstPage();
        NBTHelpers.PortalDestination selected = destination(Level.OVERWORLD, new Vec3(3, 4, 0));
        NBTHelpers.PortalDestination previous = destination(Level.NETHER, new Vec3(200, 70, -100));
        UltimatePortalGunItem.setDestination(gun, 0, selected);
        PortalGunV2.setFavoritePosition(gun, 0);

        assertEquals(91, UltimatePortalGunItem.calculateDestinationFluidCost(
                gun, true, Level.OVERWORLD, Vec3.ZERO, previous, () -> 91));
    }

    private static ItemStack gunWithFirstPage() {
        ItemStack gun = new ItemStack(JDTEItems.ULTIMATE_PORTAL_GUN.get());
        UltimatePortalGunItem.ensurePage(gun);
        return gun;
    }

    private static NBTHelpers.PortalDestination destination(ResourceKey<Level> dimension, Vec3 position) {
        return new NBTHelpers.PortalDestination(
                new NBTHelpers.GlobalVec3(dimension, position), Direction.NORTH, "test");
    }
}
