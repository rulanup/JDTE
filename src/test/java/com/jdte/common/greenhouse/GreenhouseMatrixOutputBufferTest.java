package com.jdte.common.greenhouse;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixOutputBufferTest {
    @Test
    void storesAndExtractsCountsAboveIntegerRange() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        ItemStack wheat = new ItemStack(Items.WHEAT);

        assertEquals(0L, buffer.insert(wheat, 3_000_000_000L, false));
        assertEquals(3_000_000_000L, buffer.count(wheat));

        IItemHandler view = buffer.itemView();
        assertEquals(1, view.getSlots());
        assertEquals(64, view.extractItem(0, 64, true).getCount());
        assertEquals(3_000_000_000L, buffer.count(wheat), "simulated extraction must not mutate the buffer");
        assertEquals(64, view.extractItem(0, 64, false).getCount());
        assertEquals(2_999_999_936L, buffer.count(wheat));
    }

    @Test
    void keepsDifferentComponentsInDifferentEntries() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, 1_000L);
        ItemStack ordinary = new ItemStack(Items.WHEAT);
        ItemStack named = ordinary.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("matrix"));

        buffer.insert(ordinary, 10L, false);
        buffer.insert(named, 20L, false);

        assertEquals(2, buffer.distinctTypes());
        assertEquals(10L, buffer.count(ordinary));
        assertEquals(20L, buffer.count(named));
    }

    @Test
    void returnsUnacceptedAmountWithoutChangingStateWhenLimitsAreReached() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(1, 100L);
        ItemStack wheat = new ItemStack(Items.WHEAT);

        assertEquals(0L, buffer.insert(wheat, 90L, false));
        assertEquals(20L, buffer.insert(wheat, 30L, true));
        assertEquals(90L, buffer.totalCount());
        assertEquals(20L, buffer.insert(wheat, 30L, false));
        assertEquals(100L, buffer.totalCount());
        assertEquals(5L, buffer.insert(new ItemStack(Items.CARROT), 5L, false));
        assertEquals(1, buffer.distinctTypes());
    }

    @Test
    void removesEmptyEntriesAndRejectsInsertionThroughOutputView() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, 100L);
        buffer.insert(new ItemStack(Items.WHEAT), 2L, false);

        IItemHandler view = buffer.itemView();
        assertTrue(view.insertItem(0, new ItemStack(Items.CARROT), false).is(Items.CARROT));
        assertEquals(2, view.extractItem(0, 64, false).getCount());
        assertEquals(0, view.getSlots());
        assertEquals(0L, buffer.totalCount());
    }

    @Test
    void preservesLongCountsAndComponentsAcrossNbtRoundTrip() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixOutputBuffer original = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        ItemStack named = new ItemStack(Items.WHEAT);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("persistent matrix output"));
        original.insert(named, 4_200_000_000L, false);

        CompoundTag saved = original.save(registries);
        GreenhouseMatrixOutputBuffer restored = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        restored.load(saved, registries, ignored -> { });

        assertEquals(4_200_000_000L, restored.count(named));
        assertEquals(Component.literal("persistent matrix output"),
                restored.itemView().getStackInSlot(0).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void skipsOnlyTheDamagedNbtEntry() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixOutputBuffer original = new GreenhouseMatrixOutputBuffer(8, 1_000L);
        original.insert(new ItemStack(Items.WHEAT), 25L, false);
        CompoundTag saved = original.save(registries);
        ListTag entries = saved.getList("entries", Tag.TAG_COMPOUND);
        CompoundTag damaged = new CompoundTag();
        damaged.putLong("count", 50L);
        entries.add(damaged);
        List<String> warnings = new ArrayList<>();

        GreenhouseMatrixOutputBuffer restored = new GreenhouseMatrixOutputBuffer(8, 1_000L);
        restored.load(saved, registries, warnings::add);

        assertEquals(25L, restored.totalCount());
        assertEquals(1, restored.distinctTypes());
        assertEquals(1, warnings.size());
    }

    @Test
    void rejectsAnEntireProductionBatchWhenOnlyPartWouldFit() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, 10L);
        List<GreenhouseMatrixDropGenerator.Drop> batch = List.of(
                new GreenhouseMatrixDropGenerator.Drop(new ItemStack(Items.WHEAT), 6L),
                new GreenhouseMatrixDropGenerator.Drop(new ItemStack(Items.CARROT), 6L));

        assertTrue(buffer.insertBatch(batch, true) > 0L);
        assertTrue(buffer.insertBatch(batch, false) > 0L);
        assertEquals(0L, buffer.totalCount());
        assertEquals(0, buffer.distinctTypes());
    }

    @Test
    void exposesOneLargeEntryForSingleTickAeTransfer() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 2_100_000L, false);

        assertEquals(2_100_000L, buffer.amountAt(0));
        assertTrue(buffer.prototypeAt(0).is(Items.WHEAT));
        assertEquals(2_100_000L, buffer.removeAmount(0, 2_100_000L));
        assertEquals(0L, buffer.totalCount());
    }

    @Test
    void drainAllReturnsCompactLongEntriesAndClearsBuffer() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 3_000_000_000L, false);
        buffer.insert(new ItemStack(Items.CARROT), 5L, false);

        List<GreenhouseMatrixDropGenerator.Drop> drained = buffer.drainAll();

        assertEquals(2, drained.size());
        assertEquals(3_000_000_000L, drained.getFirst().amount());
        assertEquals(0, buffer.distinctTypes());
        assertEquals(0L, buffer.totalCount());
    }
}
