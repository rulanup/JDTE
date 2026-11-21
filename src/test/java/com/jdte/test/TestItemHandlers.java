package com.jdte.test;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class TestItemHandlers {
    private TestItemHandlers() {}

    public static ItemStack insertAt(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public static ItemStack extractAt(ResourceHandler<ItemResource> handler, int slot, int amount) {
        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            ItemResource resource = handler.getResource(slot);
            int extracted = handler.extract(slot, resource, amount, tx);
            tx.commit();
            return resource.toStack(extracted);
        }
    }

    public static ItemStack stackAt(ResourceHandler<ItemResource> handler, int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    public static void setStack(ItemStacksResourceHandler handler, int slot, ItemStack stack) {
        handler.set(slot, ItemResource.of(stack), stack.getCount());
    }
}
