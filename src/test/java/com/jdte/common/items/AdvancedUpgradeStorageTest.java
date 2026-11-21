package com.jdte.common.items;

import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.network.handler.AdvancedUpgradeStorageActionPacket;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.transfer.item.ItemResource;
import static com.jdte.test.TestItemHandlers.insertAt;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedUpgradeStorageTest implements com.jdte.test.WorldRegistryTest {

    @Test
    void slotCountAndDimensionsAreExactlyThirtySixAndSixBySix() {
        assertEquals(6, AdvancedUpgradeStorageContainer.ROWS);
        assertEquals(6, AdvancedUpgradeStorageContainer.COLUMNS);
        assertEquals(36, AdvancedUpgradeStorageContainer.SLOTS);

        ItemStack storage = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        AdvancedUpgradeStorageHandler handler = new AdvancedUpgradeStorageHandler(storage);
        assertEquals(36, handler.size());
    }

    @Test
    void allowsOnlyUpgradeItemsAndRejectsSelfAndNormalStorage() {
        ItemStack advStorage = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        ItemStack normStorage = new ItemStack(JDTEItems.UPGRADE_STORAGE.get());

        // Anti-nesting: both self and normal storage rejected
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(advStorage));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(normStorage));

        // Non-upgrades rejected
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(Items.DIRT)));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(Items.DIAMOND)));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(ItemStack.EMPTY));
        assertFalse(UpgradeStorageItem.isAllowedUpgrade(null));

        // Upgrades allowed
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.CAPACITY_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get())));
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(new ItemStack(JDTEItems.LOOTING_UPGRADE.get())));
    }

    @Test
    void handlerAllowsStackingUpToSixtyFourInThirtySixSlots() {
        ItemStack storage = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        AdvancedUpgradeStorageHandler handler = new AdvancedUpgradeStorageHandler(storage);

        for (int i = 0; i < 36; i++) {
            assertEquals(64, handler.getCapacityAsInt(i, ItemResource.EMPTY));
        }

        ItemStack card = new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 1);
        ItemStack remainder1 = insertAt(handler, 0, card.copy());
        assertTrue(remainder1.isEmpty());
        assertEquals(1, handler.copyStack(0).getCount());

        ItemStack stack63 = new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 63);
        ItemStack remainder2 = insertAt(handler, 0, stack63);
        assertTrue(remainder2.isEmpty());
        assertEquals(64, handler.copyStack(0).getCount());

        // Over 64 rejected
        ItemStack extra = new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 1);
        ItemStack remainder3 = insertAt(handler, 0, extra);
        assertEquals(1, remainder3.getCount());
        assertEquals(64, handler.copyStack(0).getCount());
    }

    @Test
    void persistenceAcrossItemContainerContents() {
        ItemStack storage = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        AdvancedUpgradeStorageHandler handler1 = new AdvancedUpgradeStorageHandler(storage);

        insertAt(handler1, 0, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get(), 10));
        insertAt(handler1, 35, new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), 5));

        ItemContainerContents contents = storage.get(JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get());
        assertNotNull(contents);

        AdvancedUpgradeStorageHandler handler2 = new AdvancedUpgradeStorageHandler(storage);
        assertEquals(10, handler2.copyStack(0).getCount());
        assertEquals(JDTEItems.OVERCLOCK_UPGRADE.get(), handler2.copyStack(0).getItem());

        assertEquals(5, handler2.copyStack(35).getCount());
        assertEquals(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(), handler2.copyStack(35).getItem());
    }

    @Test
    void insertIntoMachineDirectTransferWorksAndRespectsLimits() {
        AdvancedEnergyTransmitterBE transmitter = new AdvancedEnergyTransmitterBE(
                BlockPos.ZERO, com.jdte.setup.JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());
        UpgradeItemStackHandler machineHandler = UpgradeHelper.getUpgradeHandler(transmitter);
        assertNotNull(machineHandler);

        ItemStack storage = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        AdvancedUpgradeStorageHandler storageHandler = new AdvancedUpgradeStorageHandler(storage);

        // Put 10 Capacity upgrades in storage slot 0 (capacity limit is 3)
        insertAt(storageHandler, 0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(), 10));
        assertEquals(10, storageHandler.copyStack(0).getCount());

        // Insert 1 into machine
        int insertedOne = AdvancedUpgradeStorageActionPacket.insertIntoMachine(transmitter, storageHandler, 0, 1);
        assertEquals(1, insertedOne);
        assertEquals(9, storageHandler.copyStack(0).getCount());
        assertEquals(1, UpgradeHelper.countUpgrades(transmitter, UpgradeType.CAPACITY));

        // Insert max (attempting 9, but machine limit is 3, so only 2 more can fit)
        int insertedMax = AdvancedUpgradeStorageActionPacket.insertIntoMachine(transmitter, storageHandler, 0, 9);
        assertEquals(2, insertedMax);
        assertEquals(7, storageHandler.copyStack(0).getCount());
        assertEquals(3, UpgradeHelper.countUpgrades(transmitter, UpgradeType.CAPACITY));

        // Further insertion rejected due to limit
        int insertedExcess = AdvancedUpgradeStorageActionPacket.insertIntoMachine(transmitter, storageHandler, 0, 7);
        assertEquals(0, insertedExcess);
        assertEquals(7, storageHandler.copyStack(0).getCount());
    }
}
