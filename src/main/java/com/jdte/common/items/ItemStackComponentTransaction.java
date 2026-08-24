package com.jdte.common.items;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Prepares component mutations on a copy so failed resource changes cannot leak to the live stack. */
final class ItemStackComponentTransaction {
    private final DataComponentPatch original;
    private final DataComponentPatch prepared;

    private ItemStackComponentTransaction(DataComponentPatch original, DataComponentPatch prepared) {
        this.original = original;
        this.prepared = prepared;
    }

    static Optional<ItemStackComponentTransaction> prepare(ItemStack live, Mutation mutation) {
        ItemStack staged = live.copy();
        if (!mutation.apply(staged)) {
            return Optional.empty();
        }
        return Optional.of(new ItemStackComponentTransaction(
                live.getComponentsPatch(), staged.getComponentsPatch()));
    }

    boolean commit(ItemStack live) {
        if (!original.equals(live.getComponentsPatch())) {
            return false;
        }
        live.applyComponents(prepared);
        return true;
    }

    @FunctionalInterface
    interface Mutation {
        boolean apply(ItemStack staged);
    }
}
