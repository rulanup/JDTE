package com.jdte.common.containers.handlers;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.util.ModTags;
import com.jdte.common.items.LargeFuelCanisterItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class LargeFuelCanisterHandler extends ItemStacksResourceHandler {
    private final ItemStack canisterStack;
    private final FuelValues fuels;

    public LargeFuelCanisterHandler(int size, ItemStack canisterStack, FuelValues fuels) {
        super(size);
        this.canisterStack = canisterStack;
        this.fuels = fuels;
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previous) {
        ItemStack fuelStack = stacks.get(slot);
        if (!canisterStack.isEmpty() && !fuelStack.isEmpty()) {
            fuelStack.shrink(LargeFuelCanisterItem.incrementFuel(canisterStack, fuelStack, fuels));
        }
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        ItemStack stack = resource.toStack(1);
        return !(stack.getItem() instanceof FuelCanister)
                && stack.getBurnTime(RecipeType.SMELTING, fuels) > 0
                && stack.getCraftingRemainder() == null
                && !stack.is(ModTags.Items.FUEL_CANISTER_DENY);
    }
}
