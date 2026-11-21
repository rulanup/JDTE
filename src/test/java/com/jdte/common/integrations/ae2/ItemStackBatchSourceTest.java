package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemStackBatchSourceTest {
    public static void main(String[] args) {
        new ItemStackBatchSourceTest().extractsAndRestoresOversizedSlotWithoutVanillaStackLimit();
    }

    @Test
    void extractsAndRestoresOversizedSlotWithoutVanillaStackLimit() {
        ItemStacksResourceHandler handler = new ItemStacksResourceHandler(1);
        handler.set(0, ItemResource.of(Items.WHEAT), 2_100_000);
        ItemStackBatchSource<String> source = new ItemStackBatchSource<>(handler, 0, "item");

        assertEquals(2_100_000L, source.extract(2_100_000L, true));
        assertEquals(2_100_000, handler.getAmountAsInt(0));
        assertEquals(2_100_000L, source.extract(2_100_000L, false));
        assertEquals(0, handler.getAmountAsInt(0));
        assertEquals(600_000L, source.restore(600_000L));
        assertEquals(600_000, handler.getAmountAsInt(0));
    }
}
