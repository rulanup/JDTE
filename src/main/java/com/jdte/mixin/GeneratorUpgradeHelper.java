package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

final class GeneratorUpgradeHelper {
    private GeneratorUpgradeHelper() {
    }

    static int burnSpeedMultiplier(ItemStack fuelStack) {
        if (fuelStack.getItem() instanceof Coal_T1 direCoal) {
            return direCoal.getBurnSpeedMultiplier();
        }
        if (fuelStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CoalBlock_T1 coalBlock) {
            return coalBlock.getBurnSpeedMultiplier();
        }
        if (fuelStack.getItem() instanceof FuelCanister) {
            return FuelCanister.getBurnSpeedMultiplier(fuelStack);
        }
        return 1;
    }

    /** Consumes double fuel for the generator upgrade's tripled output. */
    static void consumeFuel(ItemStackHandler machineHandler, ItemStack fuelStack) {
        if (fuelStack.hasCraftingRemainingItem()) {
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            machineHandler.setStackInSlot(0, remaining);
        } else {
            fuelStack.shrink(2);
        }
    }
}
