package com.jdte.common.items;

import static com.jdte.test.TestItemHandlers.stackAt;

import static com.jdte.test.TestItemHandlers.setStack;

import com.jdte.common.blockentities.ExtendedGeneratorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorUpgradeRuntimeTest implements com.jdte.test.WorldRegistryTest {
    @Test
    void installedUpgradeExecutesTheTransformedGeneratorBurnPath() throws ReflectiveOperationException {
        ExtendedGeneratorBE generator = new ConfigIndependentGenerator();
        var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        UpgradeHelper.getUpgradeHandler(generator).setStackInSlot(
                0, new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()));
        // The unit harness has no furnace fuel data map; supply that external
        // value through the public event while retaining the real burn path.
        java.util.function.Consumer<FurnaceFuelBurnTimeEvent> fuelValues = event -> {
            if (event.getItemStack().is(Items.COAL)) event.setBurnTime(1600);
        };
        NeoForge.EVENT_BUS.addListener(FurnaceFuelBurnTimeEvent.class, fuelValues);
        try {
            setStack(generator.getMachineHandler(), 0, new ItemStack(Items.COAL, 2));
            generator.setLevel((FuelLevel) ((sun.misc.Unsafe) field.get(null)).allocateInstance(FuelLevel.class));
            assertDoesNotThrow(generator::doBurn);
            assertTrue(stackAt(generator.getMachineHandler(), 0).isEmpty(),
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
        ItemStacksResourceHandler handler = new ItemStacksResourceHandler(1);
        setStack(handler, 0, new ItemStack(Items.COAL, 3));

        GeneratorUpgradeHelper.consumeFuel(handler, stackAt(handler, 0));

        assertEquals(1, stackAt(handler, 0).getCount());
        assertEquals(Items.COAL, stackAt(handler, 0).getItem());
    }

    private static final class FuelLevel extends net.minecraft.server.level.ServerLevel {
        private FuelLevel() {
            super(null, null, null, null, null, null, false, 0, java.util.List.of(), false);
        }
        @Override public net.minecraft.world.level.block.entity.FuelValues fuelValues() {
            return net.minecraft.world.level.block.entity.FuelValues.vanillaBurnTimes(
                    com.jdte.test.TestWorldRegistries.load(), net.minecraft.world.flag.FeatureFlags.VANILLA_SET);
        }
        @Override public void blockEntityChanged(BlockPos pos) {}
        @Override public void sendBlockUpdated(BlockPos pos, net.minecraft.world.level.block.state.BlockState oldState,
                net.minecraft.world.level.block.state.BlockState newState, int flags) {}
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
