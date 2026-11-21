package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import com.jdte.test.TestWorldRegistries;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OversizedItemStackHandlerSerializationTest implements com.jdte.test.WorldRegistryTest {
    private static HolderLookup.Provider registries;

    @org.junit.jupiter.api.BeforeAll
    static void bindDefaultComponents() {
        registries = TestWorldRegistries.load();
    }

    @Test
    void roundTripsHandlerStacksAboveVanillaSaveRange() {
        MachineInventoryHandler original = new MachineInventoryHandler(2);
        original.setStack(0, new ItemStack(Items.BEEHIVE, 616));
        original.setStack(1, new ItemStack(Items.BEEHIVE, 6_400_000));

        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        OversizedItemStackHandlerSerialization.serialize(original, output);
        MachineInventoryHandler migrated = new MachineInventoryHandler(2);
        migrated.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertEquals(ItemResource.of(Items.BEEHIVE), migrated.getResource(0));
        assertEquals(616, migrated.getAmountAsInt(0));
        assertEquals(6_400_000, migrated.getAmountAsInt(1));
        var rewritten = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        OversizedItemStackHandlerSerialization.serialize(migrated, rewritten);
        MachineInventoryHandler reloaded = new MachineInventoryHandler(2);
        reloaded.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, rewritten.buildResult()));
        assertEquals(6_400_000, reloaded.getAmountAsInt(1));
    }

    @Test
    void missingItemRetainsRawComponentsCountAndSlotAcrossBothSaveFormats() {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        output.putInt("Size", 2);
        var entry = output.childrenList("Items").addChild();
        entry.putInt("Slot", 1);
        entry.putString("id", "missingmod:ore");
        entry.putInt("count", 99);
        entry.putInt("jdteRealCount", 6762);
        entry.child("components").child("missingmod:quality").putInt("grade", 3);
        var source = output.buildResult();
        var expected = source.getListOrEmpty("Items").getFirst();
        MachineInventoryHandler handler = new MachineInventoryHandler(2);
        com.jdte.common.utils.HandlerTagHelper.read(handler, registries, source);
        handler.setStack(1, new ItemStack(Items.COAL, 5));
        var nativeSave = com.jdte.common.utils.HandlerTagHelper.write(handler, registries);
        var key = com.jdte.common.utils.MachineDropDataHelper.UNRESOLVED_ITEMS;
        assertEquals(expected, nativeSave.getListOrEmpty(key).getFirst());
        var reloaded = new MachineInventoryHandler(2);
        com.jdte.common.utils.HandlerTagHelper.read(reloaded, registries, nativeSave);
        var oversized = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        OversizedItemStackHandlerSerialization.serialize(reloaded, oversized);
        assertEquals(expected, oversized.buildResult().getListOrEmpty(key).getFirst());
        assertEquals(5, reloaded.getAmountAsInt(1));
    }

    @Test
    void restoredRegistryEntryWaitsForItsOriginalSlotToBeFree() {
        var handler = new MachineInventoryHandler(1);
        handler.setStack(0, new ItemStack(Items.COAL));
        var saved = com.jdte.common.utils.HandlerTagHelper.write(handler, registries);
        var entry = new CompoundTag();
        entry.putInt("Slot", 0);
        entry.putString("id", "minecraft:diamond");
        entry.putInt("count", 99);
        entry.putInt("jdteRealCount", 12345);
        var list = new net.minecraft.nbt.ListTag();
        list.add(entry);
        var key = com.jdte.common.utils.MachineDropDataHelper.UNRESOLVED_ITEMS;
        saved.put(key, list);
        com.jdte.common.utils.HandlerTagHelper.read(handler, registries, saved);
        assertEquals(ItemResource.of(Items.COAL), handler.getResource(0));
        handler.setStack(0, ItemStack.EMPTY);
        com.jdte.common.utils.HandlerTagHelper.read(handler, registries,
                com.jdte.common.utils.HandlerTagHelper.write(handler, registries));
        assertEquals(ItemResource.of(Items.DIAMOND), handler.getResource(0));
        assertEquals(12345, handler.getAmountAsInt(0));
        var recovered = com.jdte.common.utils.HandlerTagHelper.write(handler, registries);
        org.junit.jupiter.api.Assertions.assertFalse(recovered.contains(key));
        var reloaded = new MachineInventoryHandler(1);
        com.jdte.common.utils.HandlerTagHelper.read(reloaded, registries, recovered);
        assertEquals(12345, reloaded.getAmountAsInt(0));
    }

    @Test
    void roundTripsPendingOutputsAboveVanillaSaveRange() {
        BioFactoryPendingOutput original = BioFactoryPendingOutput.of(
                List.of(new ItemStack(Items.BEEHIVE, 7620)),
                new FluidStack(Fluids.WATER, 250), 120, 40, 30, 10, false,
                new int[]{1, 2}, new int[]{2, 1});

        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        original.save(output);
        BioFactoryPendingOutput restored = BioFactoryPendingOutput.load(
                TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));

        assertEquals(1, restored.items().size());
        assertEquals(Items.BEEHIVE, restored.items().getFirst().getItem());
        assertEquals(7620, restored.items().getFirst().getCount());
    }

    @Test
    void loadsHandlerStacksWrittenWithoutRealCount() {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        output.putInt("Size", 1);
        var entry = output.childrenList("Items").addChild();
        entry.putInt("Slot", 0);
        entry.putString("id", "minecraft:beehive");
        entry.putInt("count", 3);
        MachineInventoryHandler restored = new MachineInventoryHandler(1);
        restored.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertEquals(3, restored.getAmountAsInt(0));
    }

    @Test
    void greenhouseConversionRestoresRetainedInputsOnShortfall() {
        MachineInventoryHandler storage = new MachineInventoryHandler(3) {
            @Override public int extract(int slot, ItemResource resource, int amount,
                                         net.neoforged.neoforge.transfer.transaction.TransactionContext tx) {
                return 0;
            }
        };
        storage.set(0, ItemResource.of(Items.WHEAT_SEEDS), 4);
        storage.set(1, ItemResource.of(Items.WHEAT), 9);
        storage.set(2, ItemResource.of(Items.BEEHIVE), 64);
        GreenhouseOutputInventory outputs = new GreenhouseOutputInventory(storage, 1, 2);
        var ingredients = List.of(new ItemStack(Items.WHEAT));
        org.junit.jupiter.api.Assertions.assertFalse(outputs.replace(ingredients,
                List.of(new ItemStack(Items.BREAD), new ItemStack(Items.CARROT))));
        assertEquals(ItemResource.of(Items.WHEAT), storage.getResource(1));
        assertEquals(9, storage.getAmountAsInt(1));
        assertTrue(outputs.replace(ingredients, List.of(new ItemStack(Items.BREAD, 3))));
        assertEquals(ItemResource.of(Items.BREAD), storage.getResource(1));
        assertEquals(3, storage.getAmountAsInt(1));
        assertEquals(4, storage.getAmountAsInt(0));
        assertEquals(64, storage.getAmountAsInt(2));
    }

    @Test
    void fitsEntireOutputBatchInsideActiveSlotsOrRestoresOriginalContents() {
        MachineInventoryHandler inventory = new MachineInventoryHandler(4);
        inventory.setStack(0, new ItemStack(Items.BEEHIVE, 1));
        inventory.setStack(1, new ItemStack(Items.BEEHIVE, 63));
        var oversizedBatch = List.of(new ItemStack(Items.BEEHIVE, 2), new ItemStack(Items.CARROT));
        org.junit.jupiter.api.Assertions.assertFalse(inventory.insertBatch(1, 3, oversizedBatch, null));
        assertEquals(63, inventory.getAmountAsInt(1));
        assertTrue(inventory.getResource(2).isEmpty());
        var fittingBatch = List.of(new ItemStack(Items.BEEHIVE), new ItemStack(Items.CARROT));
        assertTrue(inventory.insertBatch(1, 3, fittingBatch, null));
        assertEquals(1, inventory.getAmountAsInt(0));
        assertEquals(64, inventory.getAmountAsInt(1));
        assertEquals(ItemResource.of(Items.CARROT), inventory.getResource(2));
        assertTrue(inventory.getResource(3).isEmpty());
    }

    @Test
    void restrictsBothBulkAndIndexedAutomationAndRollsBack() {
        MachineInventoryHandler inventory = new MachineInventoryHandler(2);
        var automation = new MachineAutomationHandler<>(
                inventory, slot -> slot == 0, slot -> slot == 1);
        ItemResource resource = ItemResource.of(Items.BEEHIVE);
        inventory.set(0, resource, 62);
        inventory.set(1, resource, 5);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, automation.extract(0, resource, 62, tx));
            assertEquals(0, automation.insert(1, resource, 59, tx));
            assertEquals(2, automation.insert(resource, 64, tx));
            assertEquals(5, automation.extract(resource, 64, tx));
        }
        assertEquals(62, inventory.getAmountAsInt(0));
        assertEquals(5, inventory.getAmountAsInt(1));
    }

    @Test
    void restoresLegacySlotsAndMovesOnlyWhatTheTargetAccepts() {
        ItemStack named = new ItemStack(Items.BEEHIVE, 10);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Hive"));
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        output.putInt("Size", 2);
        var entry = output.childrenList("Items").addChild();
        entry.store(ItemStack.MAP_CODEC, named);
        entry.putInt("Slot", 1);
        MachineInventoryHandler source = new MachineInventoryHandler(2);
        source.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertTrue(source.getResource(0).isEmpty());
        assertEquals(ItemResource.of(named), source.getResource(1));

        int[] changes = {0};
        MachineInventoryHandler target = new MachineInventoryHandler(1) {
            @Override protected void onContentsChanged(int index, ItemStack previous) { changes[0]++; }
        };
        target.set(0, ItemResource.of(named), 61);
        try (var outer = Transaction.openRoot()) {
            try (var nested = Transaction.open(outer)) {
                assertEquals(3, ResourceHandlerUtil.moveStacking(source, target, r -> true, 10, nested));
                nested.commit();
            }
        }
        assertEquals(10, source.getAmountAsInt(1));
        assertEquals(61, target.getAmountAsInt(0));
        assertEquals(1, changes[0]);
        try (var tx = Transaction.openRoot()) {
            assertEquals(3, ResourceHandlerUtil.moveStacking(source, target, r -> true, 10, tx));
            tx.commit();
        }
        assertEquals(7, source.getAmountAsInt(1));
        assertEquals(64, target.getAmountAsInt(0));
        assertEquals(2, changes[0]);
    }
}
