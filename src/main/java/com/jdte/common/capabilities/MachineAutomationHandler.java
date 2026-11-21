package com.jdte.common.capabilities;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.IntPredicate;

/** Bulk transfers use the same slot rules as indexed transfers. */
public class MachineAutomationHandler<T extends Resource> implements ResourceHandler<T> {
    private final ResourceHandler<T> inventory;
    private final IntPredicate inputSlot;
    private final IntPredicate outputSlot;

    public MachineAutomationHandler(ResourceHandler<T> inventory,
                                    IntPredicate inputSlot, IntPredicate outputSlot) {
        this.inventory = inventory;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }

    @Override public int size() { return inventory.size(); }
    protected int storageSlot(int index) { return java.util.Objects.checkIndex(index, size()); }

    @Override public T getResource(int index) { return inventory.getResource(storageSlot(index)); }
    @Override public long getAmountAsLong(int index) { return inventory.getAmountAsLong(storageSlot(index)); }
    @Override public long getCapacityAsLong(int index, T resource) {
        return inventory.getCapacityAsLong(storageSlot(index), resource);
    }

    @Override public boolean isValid(int index, T resource) {
        return resource.isEmpty() || inputSlot.test(index) && inventory.isValid(storageSlot(index), resource);
    }

    @Override public int insert(int index, T resource, int amount, TransactionContext transaction) {
        return isValid(index, resource) ? inventory.insert(storageSlot(index), resource, amount, transaction) : 0;
    }

    @Override public int extract(int index, T resource, int amount, TransactionContext transaction) {
        return outputSlot.test(index) ? inventory.extract(storageSlot(index), resource, amount, transaction) : 0;
    }
}
