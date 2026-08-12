package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemStackBatchSourceTest {
    public static void main(String[] args) {
        new ItemStackBatchSourceTest().extractsAndRestoresOversizedSlotWithoutVanillaStackLimit();
    }

    @Test
    void extractsAndRestoresOversizedSlotWithoutVanillaStackLimit() {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, new ItemStack(Items.WHEAT, 2_100_000));
        ItemStackBatchSource<String> source = new ItemStackBatchSource<>(handler, 0, "item",
                stack -> stack.is(Items.WHEAT));

        assertEquals(2_100_000L, source.extract(2_100_000L, true));
        assertEquals(2_100_000, handler.getStackInSlot(0).getCount());
        assertEquals(2_100_000L, source.extract(2_100_000L, false));
        assertEquals(0, handler.getStackInSlot(0).getCount());
        assertEquals(600_000L, source.restore(600_000L));
        assertEquals(600_000, handler.getStackInSlot(0).getCount());
    }
}
