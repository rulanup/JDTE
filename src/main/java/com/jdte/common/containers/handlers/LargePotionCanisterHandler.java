package com.jdte.common.containers.handlers;

import com.jdte.common.items.LargePotionCanisterItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.component.ItemContainerContents;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class LargePotionCanisterHandler extends BatchComponentItemHandler {

    public LargePotionCanisterHandler(ItemStack canisterStack,
                                      DataComponentType<ItemContainerContents> componentType, int size) {
        super(ItemAccess.forStack(canisterStack), componentType, size);
    }

    @Override
    protected ItemResource update(ItemResource parent, int slot, ItemResource resource, int amount) {
        ItemStack input = resource.toStack(amount);
        ItemStack canister = parent.toStack();
        if (LargePotionCanisterItem.tryFillBatch(canister, input) && input.isEmpty()) {
            input = new ItemStack(Items.GLASS_BOTTLE, 4);
        }
        canister.set(component, replace(getContents(parent), slot, input));
        return ItemResource.of(canister);
    }

    @Override public boolean isValid(int slot, ItemResource resource) {
        return super.isValid(slot, resource) && (resource.getItem() instanceof PotionItem || resource.is(Items.GLASS_BOTTLE));
    }

    @Override protected int getCapacity(int slot, ItemResource resource) {
        return 4;
    }
}
