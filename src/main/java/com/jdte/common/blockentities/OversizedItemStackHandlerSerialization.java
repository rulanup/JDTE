package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Persists handlers that intentionally allow counts above Minecraft's ItemStack codec limit.
 * The item itself is encoded with a legal count and the real count is stored separately.
 */
final class OversizedItemStackHandlerSerialization {
    private static final int MAX_CODEC_COUNT = 99;
    private static final String REAL_COUNT_TAG = "jdteRealCount";

    private OversizedItemStackHandlerSerialization() {
    }

    static CompoundTag serialize(ItemStackHandler handler, HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        ListTag items = new ListTag();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            int realCount = stack.getCount();
            ItemStack encodedStack = realCount > MAX_CODEC_COUNT
                    ? stack.copyWithCount(MAX_CODEC_COUNT)
                    : stack;
            CompoundTag prefix = new CompoundTag();
            prefix.putInt("Slot", slot);
            Tag encodedTag = encodedStack.save(provider, prefix);
            if (!(encodedTag instanceof CompoundTag itemTag)) continue;
            if (realCount > MAX_CODEC_COUNT) itemTag.putInt(REAL_COUNT_TAG, realCount);
            items.add(itemTag);
        }
        result.put("Items", items);
        result.putInt("Size", handler.getSlots());
        return result;
    }

    /** Writes one stack, preserving a count above {@link #MAX_CODEC_COUNT} in {@code jdteRealCount}. */
    static CompoundTag serializeStack(ItemStack stack, HolderLookup.Provider provider) {
        if (stack.isEmpty()) return new CompoundTag();
        int realCount = stack.getCount();
        ItemStack encodedStack = realCount > MAX_CODEC_COUNT ? stack.copyWithCount(MAX_CODEC_COUNT) : stack;
        CompoundTag tag = encodedStack.save(provider);
        if (realCount > MAX_CODEC_COUNT) tag.putInt(REAL_COUNT_TAG, realCount);
        return tag;
    }

    /** Reads a stack written by {@link #serializeStack(ItemStack, HolderLookup.Provider)}, or by vanilla saving. */
    static ItemStack deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = ItemStack.parse(provider, tag).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (tag.contains(REAL_COUNT_TAG, Tag.TAG_INT)) return stack.copyWithCount(tag.getInt(REAL_COUNT_TAG));
        return stack;
    }

    static void deserialize(ItemStackHandler handler, HolderLookup.Provider provider, CompoundTag tag) {
        handler.deserializeNBT(provider, tag);
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag itemTag = items.getCompound(index);
            if (!itemTag.contains(REAL_COUNT_TAG, Tag.TAG_INT)) continue;

            int slot = itemTag.getInt("Slot");
            int realCount = itemTag.getInt(REAL_COUNT_TAG);
            if (slot < 0 || slot >= handler.getSlots() || realCount <= MAX_CODEC_COUNT) continue;
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) handler.setStackInSlot(slot, stack.copyWithCount(realCount));
        }
    }
}
