package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class GeneratorUpgradeHelper {
    private GeneratorUpgradeHelper() {
    }

    public static int burnSpeedMultiplier(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }

    /** Consumes double fuel for the generator upgrade's tripled output. */
    public static void consumeFuel(ItemStackHandler machineHandler, ItemStack fuelStack) {
        if (fuelStack.hasCraftingRemainingItem()) {
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            if (remaining.getItem() instanceof LargeFuelCanisterItem) {
                LargeFuelCanisterItem.decrementFuel(remaining);
            } else if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            machineHandler.setStackInSlot(0, remaining);
        } else {
            fuelStack.shrink(2);
        }
    }
}
