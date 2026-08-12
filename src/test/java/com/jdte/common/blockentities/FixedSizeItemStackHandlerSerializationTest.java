package com.jdte.common.blockentities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedSizeItemStackHandlerSerializationTest {
    @Test
    void legacyInventorySizeCannotShrinkCurrentHandler() {
        ItemStackHandler handler = new ItemStackHandler(8);
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("Size", 1);
        legacy.put("Items", new ListTag());

        FixedSizeItemStackHandlerSerialization.deserialize(handler, null, legacy, 8);

        assertEquals(8, handler.getSlots());
        assertEquals(1, legacy.getInt("Size"), "loading must not mutate the saved tag");
    }
}
