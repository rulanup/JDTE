package com.jdte.common.capabilities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Wraps a machine's internal item handler for external automation so items can be
 * inserted (e.g. feeding a pickaxe into a Block Breaker, or materials into a Dropper)
 * but can never be extracted. Adjacent hoppers, pipes, ME import/storage buses and
 * other inventory systems therefore cannot pull out the machine's tool or consumable
 * item — the classic "the pickaxe got swallowed" bug.
 */
public class InsertOnlyItemHandler implements IItemHandler {
    private final ItemStackHandler delegate;

    public InsertOnlyItemHandler(ItemStackHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // External automation may never pull items out of the machine's consumable slots.
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}