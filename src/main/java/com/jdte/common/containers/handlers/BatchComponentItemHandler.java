package com.jdte.common.containers.handlers;

import com.jdte.mixin.ItemContainerContentsAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.Objects;

/** These slots hold machine batches that can exceed an item's inventory stack limit. */
public class BatchComponentItemHandler extends ItemAccessItemHandler {
    public BatchComponentItemHandler(ItemAccess access, DataComponentType<ItemContainerContents> component, int size) {
        super(access, component, size);
    }

    public static ItemStack readSlot(ItemContainerContents contents, int slot) {
        if (slot >= contents.getSlots()) return ItemStack.EMPTY;
        return ((ItemContainerContentsAccessor) (Object) contents).jdte$getSlots().get(slot)
                .map(BatchComponentItemHandler::stack).orElse(ItemStack.EMPTY);
    }

    public static ItemStack stack(ItemStackTemplate template) {
        return new ItemStack(template.item(), template.count(), template.components());
    }

    static ItemContainerContents replace(ItemContainerContents contents, int slot, ItemStack replacement) {
        var items = NonNullList.withSize(Math.max(contents.getSlots(), slot + 1), ItemStack.EMPTY);
        for (int i = 0; i < contents.getSlots(); i++) items.set(i, readSlot(contents, i));
        items.set(slot, replacement);
        return ItemContainerContents.fromItems(items);
    }

    @Override
    protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
        return readSlot(contents, slot);
    }

    @Override
    protected ItemResource update(ItemResource parent, int slot, ItemResource resource, int amount) {
        return parent.with(component, replace(getContents(parent), slot, resource.toStack(amount)));
    }

    /** Direct slot replacement for menus bound to a single portable item's stack. */
    public void set(int slot, ItemResource resource, int amount) {
        Objects.checkIndex(slot, size());
        try (var tx = Transaction.openRoot()) {
            ItemResource updated = update(itemAccess.getResource(), slot, resource, amount);
            if (itemAccess.exchange(updated, 1, tx) != 1)
                throw new IllegalStateException("Could not update portable item slot " + slot);
            tx.commit();
        }
    }

    public ItemStack copyStack(int slot) {
        return getResource(slot).toStack(getAmountAsInt(slot));
    }
}
