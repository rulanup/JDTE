package com.jdte.common.items;

import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeStorageTest {

    @Test
    void slotCountAndDimensionsAreExactlyTwentyAndFourByFive() {
        assertEquals(4, UpgradeStorageContainer.ROWS);
        assertEquals(5, UpgradeStorageContainer.COLUMNS);
        assertEquals(20, UpgradeStorageContainer.SLOTS);

        ItemStack storage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        UpgradeStorageHandler handler = new UpgradeStorageHandler(storage);
        assertEquals(20, handler.getSlots());
    }

    @Test
    void allowsOnlyUpgradeItemsAndRejectsOthersAndSelf() {
        ItemStack storage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());

        // Self should be rejected to prevent recursive nesting
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(storage));

        // Non-upgrades rejected
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(Items.DIRT)));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(Items.DIAMOND)));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(Items.NETHERITE_SWORD)));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(ItemStack.EMPTY));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(null));

        // Standard JDTE upgrade cards allowed
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.CAPACITY_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.FLUID_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.FLUID_STORAGE_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.GENERATOR_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.RANGE_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.FILTER_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.CREATIVE_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.FORTUNE_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.PRECISION_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.AE_ACCELERATION_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.AE_OUTPUT_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.SEED_CONVERSION_UPGRADE.get())));

        // Dedicated upgrade items allowed
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.LOOTING_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.AE_EXTRACTION_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.EXTENDED_UPGRADE.get())));
    }

    @Test
    void handlerAllowsStackingUpToSixtyFour() {
        ItemStack storage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        UpgradeStorageHandler handler = new UpgradeStorageHandler(storage);

        for (int i = 0; i < 20; i++) {
            assertEquals(64, handler.getSlotLimit(i));
        }

        ItemStack card = new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), 1);
        assertEquals(1, card.getMaxStackSize(), "Energy overload upgrade ordinarily stacks to 1");

        // Insert 1 card
        ItemStack remainder1 = handler.insertItem(0, card.copy(), false);
        assertTrue(remainder1.isEmpty());
        assertEquals(1, handler.getStackInSlot(0).getCount());

        // Insert another 63 cards into the same slot
        ItemStack stack63 = new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), 63);
        ItemStack remainder2 = handler.insertItem(0, stack63, false);
        assertTrue(remainder2.isEmpty());
        assertEquals(64, handler.getStackInSlot(0).getCount());

        // Inserting further into slot 0 should be rejected as slot limit is reached
        ItemStack extra = new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), 1);
        ItemStack remainder3 = handler.insertItem(0, extra, false);
        assertEquals(1, remainder3.getCount());
        assertEquals(64, handler.getStackInSlot(0).getCount());

        // Non-upgrades cannot be inserted
        ItemStack dirt = new ItemStack(Items.DIRT, 10);
        ItemStack dirtRemainder = handler.insertItem(1, dirt, false);
        assertEquals(10, dirtRemainder.getCount());
        assertTrue(handler.getStackInSlot(1).isEmpty());
    }

    @Test
    void persistsContentsToDataComponentAndRestoresCorrectly() {
        ItemStack storage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        UpgradeStorageHandler handler = new UpgradeStorageHandler(storage);

        handler.insertItem(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 64), false);
        handler.insertItem(1, new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), 16), false);
        handler.insertItem(19, new ItemStack(JDTEItems.EXTENDED_UPGRADE.get(), 32), false);

        // Verify the parent itemstack now holds the component
        ItemContainerContents contents = storage.get(JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get());
        assertNotNull(contents);
        assertEquals(JDTEItems.CAPACITY_UPGRADE.get(), contents.getStackInSlot(0).getItem());
        assertEquals(64, contents.getStackInSlot(0).getCount());
        assertEquals(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), contents.getStackInSlot(1).getItem());
        assertEquals(16, contents.getStackInSlot(1).getCount());
        assertEquals(JDTEItems.EXTENDED_UPGRADE.get(), contents.getStackInSlot(19).getItem());
        assertEquals(32, contents.getStackInSlot(19).getCount());

        // Restoring in a new handler instance from the same stack
        UpgradeStorageHandler restoredHandler = new UpgradeStorageHandler(storage);
        assertEquals(64, restoredHandler.getStackInSlot(0).getCount());
        assertEquals(16, restoredHandler.getStackInSlot(1).getCount());
        assertEquals(32, restoredHandler.getStackInSlot(19).getCount());

        // Extracting items from slot 0
        ItemStack extracted = restoredHandler.extractItem(0, 20, false);
        assertEquals(20, extracted.getCount());
        assertEquals(44, restoredHandler.getStackInSlot(0).getCount());
    }

    @Test
    void slotClassReportsMaxStackSizeSixtyFour() {
        ItemStack storage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());
        UpgradeStorageHandler handler = new UpgradeStorageHandler(storage);
        UpgradeStorageContainer.UpgradeStorageSlot slot =
                new UpgradeStorageContainer.UpgradeStorageSlot(handler, 0, 44, 18);

        assertEquals(64, slot.getMaxStackSize());
        ItemStack singleUpgrade = new ItemStack(JDTEItems.CAPACITY_UPGRADE.get());
        assertEquals(64, slot.getMaxStackSize(singleUpgrade));
        assertTrue(slot.mayPlace(singleUpgrade));
        assertFalse(slot.mayPlace(new ItemStack(Items.STONE)));
    }
}
