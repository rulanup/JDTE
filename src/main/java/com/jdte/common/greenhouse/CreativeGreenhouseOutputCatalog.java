package com.jdte.common.greenhouse;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Component-aware catalog of the products exposed by a Creative Greenhouse.
 * Each catalog entry represents an inexhaustible logical quantity.
 */
public final class CreativeGreenhouseOutputCatalog {
    private final int maxTypes;
    private final IItemHandler itemView = new OutputView();
    private volatile List<ItemStack> entries = List.of();

    public CreativeGreenhouseOutputCatalog(int maxTypes) {
        if (maxTypes < 1) throw new IllegalArgumentException("maxTypes must be positive");
        this.maxTypes = maxTypes;
    }

    /** Replaces the complete catalog, or leaves the previous snapshot untouched on overflow. */
    public ReplaceResult replaceCatalog(List<ItemStack> products) {
        List<ItemStack> replacement = new ArrayList<>();
        for (ItemStack product : products) {
            if (product.isEmpty() || contains(replacement, product)) continue;
            if (replacement.size() >= maxTypes) return ReplaceResult.DISTINCT_TYPE_LIMIT_EXCEEDED;
            replacement.add(product.copyWithCount(1));
        }
        entries = List.copyOf(replacement);
        return ReplaceResult.REPLACED;
    }

    public int distinctTypes() {
        return entries.size();
    }

    public ItemStack prototypeAt(int entry) {
        List<ItemStack> snapshot = entries;
        return entry >= 0 && entry < snapshot.size() ? snapshot.get(entry).copy() : ItemStack.EMPTY;
    }

    public long amountAt(int entry) {
        return entry >= 0 && entry < entries.size() ? Long.MAX_VALUE : 0L;
    }

    public IItemHandler itemView() {
        return itemView;
    }

    private static boolean contains(List<ItemStack> stacks, ItemStack target) {
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, target)) return true;
        }
        return false;
    }

    public enum ReplaceResult {
        REPLACED,
        DISTINCT_TYPE_LIMIT_EXCEEDED
    }

    private final class OutputView implements IItemHandler {
        @Override
        public int getSlots() {
            return entries.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            List<ItemStack> snapshot = entries;
            if (slot < 0 || slot >= snapshot.size()) return ItemStack.EMPTY;
            ItemStack prototype = snapshot.get(slot);
            return prototype.copyWithCount(prototype.getMaxStackSize());
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            List<ItemStack> snapshot = entries;
            if (slot < 0 || slot >= snapshot.size() || amount <= 0) return ItemStack.EMPTY;
            ItemStack prototype = snapshot.get(slot);
            return prototype.copyWithCount(amount);
        }

        @Override
        public int getSlotLimit(int slot) {
            List<ItemStack> snapshot = entries;
            return slot >= 0 && slot < snapshot.size() ? Integer.MAX_VALUE : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
