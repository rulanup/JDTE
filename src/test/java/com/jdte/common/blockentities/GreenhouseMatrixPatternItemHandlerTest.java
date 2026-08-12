package com.jdte.common.blockentities;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixPatternItemHandlerTest {
    @Test
    void exposesExactlySixteenSingleItemPatternSlots() {
        AtomicInteger changes = new AtomicInteger();
        GreenhouseMatrixPatternItemHandler handler = new GreenhouseMatrixPatternItemHandler(
                stack -> stack.is(Items.PAPER), changes::incrementAndGet);

        assertEquals(16, handler.getSlots());
        assertEquals(1, handler.getSlotLimit(0));
        assertTrue(handler.insertItem(0, new ItemStack(Items.STONE), false).is(Items.STONE));
        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.PAPER, 3), false);
        assertTrue(remainder.is(Items.PAPER));
        assertEquals(2, remainder.getCount());
        assertEquals(1, handler.getStackInSlot(0).getCount());
        assertEquals(1, changes.get());
    }

    @Test
    void persistsSlotsWithoutTreatingDeserializationAsAUserEdit() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixPatternItemHandler original = new GreenhouseMatrixPatternItemHandler(
                stack -> stack.is(Items.PAPER), () -> { });
        original.insertItem(15, new ItemStack(Items.PAPER), false);
        CompoundTag saved = original.serializeNBT(registries);
        AtomicInteger restoredChanges = new AtomicInteger();
        GreenhouseMatrixPatternItemHandler restored = new GreenhouseMatrixPatternItemHandler(
                stack -> stack.is(Items.PAPER), restoredChanges::incrementAndGet);

        restored.load(saved, registries);

        assertTrue(restored.getStackInSlot(15).is(Items.PAPER));
        assertEquals(0, restoredChanges.get());
    }
}
