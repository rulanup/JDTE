package com.jdte.common.items;

import com.jdte.common.blockentities.ExtendedGeneratorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorUpgradeRuntimeTest {
    @Test
    void installedUpgradeExecutesTheTransformedGeneratorBurnPath() {
        ExtendedGeneratorBE generator = new ConfigIndependentGenerator();
        UpgradeHelper.getUpgradeHandler(generator).setStackInSlot(
                0, new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()));
        // The unit harness has no furnace fuel data map; supply that external
        // value through the public event while retaining the real burn path.
        java.util.function.Consumer<FurnaceFuelBurnTimeEvent> fuelValues = event -> {
            if (event.getItemStack().is(Items.COAL)) event.setBurnTime(1600);
        };
        NeoForge.EVENT_BUS.addListener(FurnaceFuelBurnTimeEvent.class, fuelValues);
        try {
            generator.getMachineHandler().setStackInSlot(0, new ItemStack(Items.COAL, 2));
            assertDoesNotThrow(generator::doBurn);
            assertTrue(generator.getMachineHandler().getStackInSlot(0).isEmpty(),
                    "The generator upgrade must consume two ordinary fuel items");
            assertEquals(72_000, generator.feRemaining);
            assertTrue(generator.maxBurn > 0);
            assertTrue(generator.burnRemaining > 0);
        } finally {
            NeoForge.EVENT_BUS.unregister(fuelValues);
        }
    }

    @Test
    void helperConsumesTwoOrdinaryFuelItemsFromTheRealHandler() {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, new ItemStack(Items.COAL, 3));

        GeneratorUpgradeHelper.consumeFuel(handler, handler.getStackInSlot(0));

        assertEquals(1, handler.getStackInSlot(0).getCount());
        assertEquals(Items.COAL, handler.getStackInSlot(0).getItem());
    }

    private static final class ConfigIndependentGenerator extends ExtendedGeneratorBE {
        private ConfigIndependentGenerator() {
            super(BlockPos.ZERO, JDTEBlocks.EXTENDED_GENERATOR.get().defaultBlockState());
        }

        @Override
        public int getMaxEnergy() {
            return 1_000_000;
        }

        @Override
        public int getFEPerTick() {
            return 1_000;
        }

        @Override
        public int getFePerFuelTick() {
            return 15;
        }

        @Override
        public int getBurnSpeedMultiplier() {
            return 4;
        }
    }
}
