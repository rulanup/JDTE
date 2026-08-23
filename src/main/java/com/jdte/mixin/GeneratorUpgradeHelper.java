package com.jdte.mixin;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

final class GeneratorUpgradeHelper {
    private GeneratorUpgradeHelper() {
    }

    static int burnSpeedMultiplier(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }

    /** Consumes double fuel for the generator upgrade's tripled output. */
    static void consumeFuel(ItemStackHandler machineHandler, ItemStack fuelStack) {
        if (fuelStack.hasCraftingRemainingItem()) {
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            if (remaining.getItem() instanceof LargeFuelCanisterItem) {
                LargeFuelCanisterItem.decrementFuel(remaining);
            } else
            if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            machineHandler.setStackInSlot(0, remaining);
        } else {
            fuelStack.shrink(2);
        }
    }
}
