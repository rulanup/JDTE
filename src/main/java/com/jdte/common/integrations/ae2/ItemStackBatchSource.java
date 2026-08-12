package com.jdte.common.integrations.ae2;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.Predicate;

class ItemStackBatchSource<K> implements AEItemBatchTransfer.Source<K> {
    private final ItemStackHandler handler;
    private final int slot;
    private final K key;
    private final Predicate<ItemStack> sameKey;
    private final ItemStack template;

    ItemStackBatchSource(ItemStackHandler handler, int slot, K key, Predicate<ItemStack> sameKey) {
        this.handler = handler;
        this.slot = slot;
        this.key = key;
        this.sameKey = sameKey;
        this.template = handler.getStackInSlot(slot).copyWithCount(1);
    }

    @Override public K key() {
        return key;
    }

    @Override public long available() {
        ItemStack visible = handler.getStackInSlot(slot);
        return sameKey.test(visible) ? visible.getCount() : 0L;
    }

    @Override public long extract(long amount, boolean simulate) {
        ItemStack visible = handler.getStackInSlot(slot);
        if (amount <= 0L || !sameKey.test(visible)) return 0L;
        int extracted = (int) Math.min(Math.min(amount, Integer.MAX_VALUE), visible.getCount());
        if (extracted <= 0) return 0L;
        if (!simulate) {
            int remaining = visible.getCount() - extracted;
            handler.setStackInSlot(slot, remaining <= 0 ? ItemStack.EMPTY : visible.copyWithCount(remaining));
        }
        return extracted;
    }

    @Override public long restore(long amount) {
        if (amount <= 0L) return 0L;
        ItemStack visible = handler.getStackInSlot(slot);
        if (!visible.isEmpty() && !sameKey.test(visible)) return 0L;
        int current = visible.isEmpty() ? 0 : visible.getCount();
        int restored = (int) Math.min(Math.min(amount, Integer.MAX_VALUE), Integer.MAX_VALUE - (long) current);
        if (restored <= 0) return 0L;
        handler.setStackInSlot(slot, template.copyWithCount(current + restored));
        return restored;
    }
}
