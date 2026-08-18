package com.jdte.common.utils;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.Consumer;

public final class DedicatedUpgradeDropHelper {
    private DedicatedUpgradeDropHelper() {
    }

    public static void drop(ItemStackHandler handler, Consumer<ItemStack> dropper) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                dropper.accept(stack.copy());
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
