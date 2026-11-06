package com.jdte.common.integrations.ae2;

/** An AE batch source whose logical quantity is always {@link Long#MAX_VALUE}. */
final class InfiniteItemBatchSource<K> implements AEItemBatchTransfer.Source<K> {
    private final K key;

    InfiniteItemBatchSource(K key) {
        this.key = key;
    }

    @Override
    public K key() {
        return key;
    }

    @Override
    public long available() {
        return Long.MAX_VALUE;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        return Math.max(0L, amount);
    }

    @Override
    public long restore(long amount) {
        return Math.max(0L, amount);
    }
}
