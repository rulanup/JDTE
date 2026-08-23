package com.jdte.common.containers.handlers;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.datagen.JustDireItemTags;
import com.jdte.common.items.LargeFuelCanisterItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.ItemStackHandler;

public class LargeFuelCanisterHandler extends ItemStackHandler {
    private final ItemStack canisterStack;

    public LargeFuelCanisterHandler(int size, ItemStack canisterStack) {
        super(size);
        this.canisterStack = canisterStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        ItemStack fuelStack = getStackInSlot(slot);
        if (!canisterStack.isEmpty() && !fuelStack.isEmpty()) {
            LargeFuelCanisterItem.incrementFuel(canisterStack, fuelStack);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof FuelCanister)
                && stack.getBurnTime(RecipeType.SMELTING) > 0
                && !stack.hasCraftingRemainingItem()
                && !stack.is(JustDireItemTags.FUEL_CANISTER_DENY);
    }
}
