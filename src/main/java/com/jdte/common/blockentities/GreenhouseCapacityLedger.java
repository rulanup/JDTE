package com.jdte.common.blockentities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

/** Lightweight, mutation-free capacity model shared by every planting lane in one production settlement. */
final class GreenhouseCapacityLedger {
    private final ItemStack[] types;
    private final int[] counts;
    private final int[] limits;

    private GreenhouseCapacityLedger(ItemStack[] types, int[] counts, int[] limits) {
        this.types = types;
        this.counts = counts;
        this.limits = limits;
    }

    static GreenhouseCapacityLedger capture(IItemHandler handler) {
        int slots = handler.getSlots();
        ItemStack[] types = new ItemStack[slots];
        int[] counts = new int[slots];
        int[] limits = new int[slots];
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            types[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
            counts[slot] = stack.getCount();
            limits[slot] = handler.getSlotLimit(slot);
        }
        return new GreenhouseCapacityLedger(types, counts, limits);
    }

    boolean canFit(List<ItemStack> drops, int repetitions) {
        return insert(drops, repetitions, types.clone(), counts.clone());
    }

    void reserve(List<ItemStack> drops, int repetitions) {
        if (!insert(drops, repetitions, types, counts)) {
            throw new IllegalStateException("Committed Greenhouse output exceeded its reserved capacity");
        }
    }

    private boolean insert(List<ItemStack> drops, int repetitions, ItemStack[] workingTypes, int[] workingCounts) {
        for (ItemStack drop : drops) {
            long remaining = (long) drop.getCount() * repetitions;
            for (int slot = 0; slot < workingCounts.length && remaining > 0; slot++) {
                if (!workingTypes[slot].isEmpty()
                        && ItemStack.isSameItemSameComponents(workingTypes[slot], drop)) {
                    remaining = fill(slot, drop, remaining, workingTypes, workingCounts);
                }
            }
            for (int slot = 0; slot < workingCounts.length && remaining > 0; slot++) {
                if (workingCounts[slot] == 0) {
                    workingTypes[slot] = drop.copyWithCount(1);
                    remaining = fill(slot, drop, remaining, workingTypes, workingCounts);
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private long fill(int slot, ItemStack stack, long amount,
                      ItemStack[] workingTypes, int[] workingCounts) {
        int accepted = (int) Math.min(amount, Math.max(0, limits[slot] - workingCounts[slot]));
        if (accepted > 0 && workingTypes[slot].isEmpty()) workingTypes[slot] = stack.copyWithCount(1);
        workingCounts[slot] += accepted;
        return amount - accepted;
    }
}
