package com.jdte.common.containers;

import com.jdte.test.WorldRegistryTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedMachineSlotTest implements WorldRegistryTest {
    @Test void pickupHonorsTheItemsCurrentMaximumAndCursorRoom() {
        var items = new ItemStacksResourceHandler(1);
        var seed = new ItemStack(Items.WHEAT_SEEDS);
        var slot = new PagedMachineSlot(items, 0, 0, 0) {
            @Override protected boolean isActiveSlot() { return true; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        };
        items.set(0, ItemResource.of(seed), 7267);
        assertEquals(64, slot.safeTake(7267, Integer.MAX_VALUE, null).getCount());
        assertEquals(7203, items.getAmountAsInt(0));
        seed.set(DataComponents.MAX_STACK_SIZE, 16);
        items.set(0, ItemResource.of(seed), 7203);
        assertEquals(16, slot.safeTake(7203, Integer.MAX_VALUE, null).getCount());
        assertEquals(3, slot.safeTake(7203, 3, null).getCount());
        assertEquals(7184, items.getAmountAsInt(0));
    }
}
