package com.jdte.common.integrations.ae2;

import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

class ItemStackBatchSource<K> implements AEItemBatchTransfer.Source<K> {
    private final ItemStacksResourceHandler handler;
    private final int slot;
    private final K key;
    private final ItemResource template;

    ItemStackBatchSource(ItemStacksResourceHandler handler, int slot, K key) {
        this.handler = handler;
        this.slot = slot;
        this.key = key;
        this.template = handler.getResource(slot);
    }

    @Override public K key() {
        return key;
    }

    @Override public long available() {
        return template.equals(handler.getResource(slot)) ? handler.getAmountAsLong(slot) : 0L;
    }

    @Override public long extract(long amount, boolean simulate) {
        if (amount <= 0L || template.isEmpty()) return 0L;
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(slot, template, (int) Math.min(amount, Integer.MAX_VALUE), tx);
            if (!simulate) tx.commit();
            return extracted;
        }
    }

    @Override public long restore(long amount) {
        if (amount <= 0L) return 0L;
        ItemResource visible = handler.getResource(slot);
        if (!visible.isEmpty() && !template.equals(visible)) return 0L;
        int current = handler.getAmountAsInt(slot);
        int restored = (int) Math.min(Math.min(amount, Integer.MAX_VALUE), Integer.MAX_VALUE - (long) current);
        if (restored <= 0) return 0L;
        handler.set(slot, template, current + restored);
        return restored;
    }
}
