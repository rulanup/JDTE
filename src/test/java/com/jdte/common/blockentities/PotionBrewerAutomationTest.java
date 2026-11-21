package com.jdte.common.blockentities;

import static com.jdte.test.TestItemHandlers.stackAt;

import static com.jdte.test.TestItemHandlers.setStack;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PotionBrewerAutomationTest implements com.jdte.test.WorldRegistryTest {
    private static AdvancedPotionBrewerBE brewer() {
        return new AdvancedPotionBrewerBE(BlockPos.ZERO,
                JDTEBlocks.ADVANCED_POTION_BREWER.get().defaultBlockState());
    }

    @Test
    void fuelCanBeInsertedThroughAllSidesAndUnspecifiedSideWithoutRenumberingSlots() {
        for (Direction side : new Direction[]{null, Direction.DOWN, Direction.UP, Direction.NORTH,
                Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            AdvancedPotionBrewerBE brewer = brewer();
            var handler = brewer.getAutomationItemHandler(side);
            ItemStack powder = new ItemStack(Items.BLAZE_POWDER, 16);
            assertTrue(handler.isValid(4, ItemResource.of(powder)), "Fuel must be available from " + side);
            try (var simulation = Transaction.openRoot()) {
                assertEquals(16, handler.insert(4, ItemResource.of(powder), 16, simulation));
            }
            assertTrue(stackAt(brewer.getMachineHandler(), 4).isEmpty());
            assertEquals(16, insertPowder(handler, 4, 16));
            assertEquals(16, stackAt(brewer.getMachineHandler(), 4).getCount());
            try (var transaction = Transaction.openRoot()) {
                assertEquals(0, handler.extract(4, ItemResource.of(powder), 16, transaction));
                transaction.commit();
            }
        }
    }

    @Test
    void disablingInputPreservesFuelVisibilityButRejectsInsertion() {
        AdvancedPotionBrewerBE brewer = brewer();
        setStack(brewer.getMachineHandler(), 4, new ItemStack(Items.BLAZE_POWDER, 12));
        brewer.setFuelInputEnabled(false);
        var handler = brewer.getAutomationItemHandler(Direction.NORTH);
        assertEquals(12, handler.getAmountAsInt(4));
        assertFalse(handler.isValid(4, ItemResource.of(Items.BLAZE_POWDER)));
        assertEquals(0, insertPowder(handler, 4, 7));
        assertEquals(12, stackAt(brewer.getMachineHandler(), 4).getCount());
    }

    @Test
    void energyBrewingUpgradeDisablesFuelInputEvenWhenToggleIsEnabled() {
        AdvancedPotionBrewerBE brewer = brewer();
        brewer.getUpgradeHandler().setStackInSlot(0, new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()));
        assertFalse(brewer.getAutomationItemHandler().isValid(4, ItemResource.of(Items.BLAZE_POWDER)));
        assertEquals(0, insertPowder(brewer.getAutomationItemHandler(), 4, 2));
    }

    @Test
    void savedDisabledSettingAndLegacySideSettingsRemainAuthoritative() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        AdvancedPotionBrewerBE brewer = brewer();
        CompoundTag tag = new CompoundTag();
        brewer.loadAdditional(TagValueInput.create(new ProblemReporter.Collector(), registries, tag));
        assertTrue(brewer.isFuelInputEnabled());
        tag.putInt("fuelInputSide", -1);
        brewer.loadAdditional(TagValueInput.create(new ProblemReporter.Collector(), registries, tag));
        assertFalse(brewer.isFuelInputEnabled());
        tag.putInt("fuelInputSide", 2);
        brewer.loadAdditional(TagValueInput.create(new ProblemReporter.Collector(), registries, tag));
        assertTrue(brewer.isFuelInputEnabled());
        tag.putBoolean("fuelInputEnabled", false);
        brewer.loadAdditional(TagValueInput.create(new ProblemReporter.Collector(), registries, tag));
        assertFalse(brewer.isFuelInputEnabled());
    }

    @Test
    void bulkPowderInputFillsFuelWithoutPollutingIngredientSteps() throws Exception {
        AdvancedPotionBrewerBE brewer = brewerWithRecipes();
        var handler = brewer.getAutomationItemHandler();
        try (var transaction = Transaction.openRoot()) {
            assertEquals(64, handler.insert(ItemResource.of(Items.BLAZE_POWDER), 80, transaction));
            transaction.commit();
        }
        assertEquals(64, stackAt(brewer.getMachineHandler(), 4).getCount());
        for (int slot : new int[]{3, 5, 6, 7, 8, 9}) {
            assertTrue(stackAt(brewer.getMachineHandler(), slot).isEmpty());
        }
    }

    @Test
    void explicitlyLockedStrengthIngredientStillAcceptsPowder() throws Exception {
        AdvancedPotionBrewerBE brewer = brewerWithRecipes();
        setStack(brewer.getMachineHandler(), 3, new ItemStack(Items.BLAZE_POWDER));
        brewer.setRecipeLocked(true);
        setStack(brewer.getMachineHandler(), 3, ItemStack.EMPTY);
        assertEquals(5, insertPowder(brewer.getAutomationItemHandler(), 3, 5));
        assertEquals(5, stackAt(brewer.getMachineHandler(), 3).getCount());
        assertEquals(0, insertPowder(brewer.getAutomationItemHandler(), 5, 5));
    }

    private static int insertPowder(ResourceHandler<ItemResource> handler, int slot, int amount) {
        try (var transaction = Transaction.openRoot()) {
            int inserted = handler.insert(slot, ItemResource.of(Items.BLAZE_POWDER), amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static AdvancedPotionBrewerBE brewerWithRecipes() throws Exception {
        var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var unsafe = (sun.misc.Unsafe) field.get(null);
        AdvancedPotionBrewerBE brewer = brewer();
        brewer.setLevel((BrewingLevel) unsafe.allocateInstance(BrewingLevel.class));
        return brewer;
    }

    /** Uses real brewing recipes without creating chunks or starting a server. */
    private static final class BrewingLevel extends ServerLevel {
        private static final PotionBrewing BREWING = vanillaBrewing();
        private BrewingLevel() {
            super(null, null, null, null, null, null, false, 0, java.util.List.of(), false);
        }
        private static PotionBrewing vanillaBrewing() {
            var builder = new PotionBrewing.Builder(FeatureFlags.VANILLA_SET);
            PotionBrewing.addVanillaMixes(builder);
            return builder.build();
        }
        @Override public PotionBrewing potionBrewing() { return BREWING; }
        @Override public void blockEntityChanged(BlockPos pos) { }
        @Override public void updateNeighbourForOutputSignal(BlockPos pos, Block block) { }
        @Override public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) { }
    }
}
