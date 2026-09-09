package com.jdte.common.blockentities;

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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PotionBrewerAutomationTest {
    private static AdvancedPotionBrewerBE brewer() {
        return new AdvancedPotionBrewerBE(BlockPos.ZERO,
                JDTEBlocks.ADVANCED_POTION_BREWER.get().defaultBlockState());
    }

    @Test
    void fuelCanBeInsertedThroughAllSidesAndUnspecifiedSideWithoutRenumberingSlots() {
        for (Direction side : new Direction[]{null, Direction.DOWN, Direction.UP, Direction.NORTH,
                Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            AdvancedPotionBrewerBE brewer = brewer();
            IItemHandler handler = brewer.getAutomationItemHandler(side);
            ItemStack powder = new ItemStack(Items.BLAZE_POWDER, 16);
            assertTrue(handler.isItemValid(4, powder), "Fuel must be available from " + side);
            assertTrue(handler.insertItem(4, powder, true).isEmpty());
            assertTrue(brewer.getMachineHandler().getStackInSlot(4).isEmpty());
            assertTrue(handler.insertItem(4, powder, false).isEmpty());
            assertEquals(16, brewer.getMachineHandler().getStackInSlot(4).getCount());
            assertTrue(handler.extractItem(4, 16, false).isEmpty());
        }
    }

    @Test
    void disablingInputPreservesFuelVisibilityButRejectsInsertion() {
        AdvancedPotionBrewerBE brewer = brewer();
        brewer.getMachineHandler().setStackInSlot(4, new ItemStack(Items.BLAZE_POWDER, 12));
        brewer.setFuelInputEnabled(false);
        IItemHandler handler = brewer.getAutomationItemHandler(Direction.NORTH);
        assertEquals(12, handler.getStackInSlot(4).getCount());
        assertFalse(handler.isItemValid(4, new ItemStack(Items.BLAZE_POWDER)));
        assertEquals(7, handler.insertItem(4, new ItemStack(Items.BLAZE_POWDER, 7), false).getCount());
        assertEquals(12, brewer.getMachineHandler().getStackInSlot(4).getCount());
    }

    @Test
    void energyBrewingUpgradeDisablesFuelInputEvenWhenToggleIsEnabled() {
        AdvancedPotionBrewerBE brewer = brewer();
        brewer.getUpgradeHandler().setStackInSlot(0, new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()));
        assertFalse(brewer.getAutomationItemHandler().isItemValid(4, new ItemStack(Items.BLAZE_POWDER)));
        assertEquals(2, brewer.getAutomationItemHandler().insertItem(4,
                new ItemStack(Items.BLAZE_POWDER, 2), false).getCount());
    }

    @Test
    void savedDisabledSettingAndLegacySideSettingsRemainAuthoritative() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        AdvancedPotionBrewerBE brewer = brewer();
        CompoundTag tag = new CompoundTag();
        brewer.loadAdditional(tag, registries);
        assertTrue(brewer.isFuelInputEnabled());
        tag.putInt("fuelInputSide", -1);
        brewer.loadAdditional(tag, registries);
        assertFalse(brewer.isFuelInputEnabled());
        tag.putInt("fuelInputSide", 2);
        brewer.loadAdditional(tag, registries);
        assertTrue(brewer.isFuelInputEnabled());
        tag.putBoolean("fuelInputEnabled", false);
        brewer.loadAdditional(tag, registries);
        assertFalse(brewer.isFuelInputEnabled());
    }

    @Test
    void bulkPowderInputFillsFuelWithoutPollutingIngredientSteps() throws Exception {
        AdvancedPotionBrewerBE brewer = brewerWithRecipes();
        IItemHandler handler = brewer.getAutomationItemHandler();
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler,
                new ItemStack(Items.BLAZE_POWDER, 80), false);
        assertEquals(16, remainder.getCount());
        assertEquals(64, brewer.getMachineHandler().getStackInSlot(4).getCount());
        for (int slot : new int[]{3, 5, 6, 7, 8, 9}) {
            assertTrue(brewer.getMachineHandler().getStackInSlot(slot).isEmpty());
        }
    }

    @Test
    void explicitlyLockedStrengthIngredientStillAcceptsPowder() throws Exception {
        AdvancedPotionBrewerBE brewer = brewerWithRecipes();
        brewer.getMachineHandler().setStackInSlot(3, new ItemStack(Items.BLAZE_POWDER));
        brewer.setRecipeLocked(true);
        brewer.getMachineHandler().setStackInSlot(3, ItemStack.EMPTY);
        assertTrue(brewer.getAutomationItemHandler().insertItem(3,
                new ItemStack(Items.BLAZE_POWDER, 5), false).isEmpty());
        assertEquals(5, brewer.getMachineHandler().getStackInSlot(3).getCount());
        assertEquals(5, brewer.getAutomationItemHandler().insertItem(5,
                new ItemStack(Items.BLAZE_POWDER, 5), false).getCount());
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
            super(null, null, null, null, null, null, null, false, 0, java.util.List.of(), false, null);
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
