package com.jdte.common.containers.handlers;

import com.jdte.common.items.LargePotionCanisterItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ComponentItemHandler;

public class LargePotionCanisterHandler extends ComponentItemHandler {
    private final ItemStack canisterStack;

    public LargePotionCanisterHandler(ItemStack canisterStack,
                                      DataComponentType<ItemContainerContents> componentType, int size) {
        super(canisterStack, componentType, size);
        this.canisterStack = canisterStack;
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {
        if (!newStack.isEmpty()
                && newStack.getItem() instanceof PotionItem
                && LargePotionCanisterItem.tryFillBatch(canisterStack, newStack)) {
            if (newStack.isEmpty()) {
                setStackInSlot(slot, new ItemStack(Items.GLASS_BOTTLE, 4));
            }
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.getItem() instanceof PotionItem
                || stack.is(Items.GLASS_BOTTLE)
                || stack.isEmpty();
    }
}
