package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class GeneratorUpgradeHelper {
    private GeneratorUpgradeHelper() {
    }

    public static int burnSpeedMultiplier(ItemStack fuelStack) {
        return com.jdte.common.items.PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }

    /** Consumes double fuel for the generator upgrade's tripled output. */
    public static void consumeFuel(ItemStackHandler machineHandler, ItemStack fuelStack) {
        if (fuelStack.hasCraftingRemainingItem()) {
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            if (remaining.getItem() instanceof com.jdte.common.items.LargeFuelCanisterItem) {
                com.jdte.common.items.LargeFuelCanisterItem.decrementFuel(remaining);
            } else if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            machineHandler.setStackInSlot(0, remaining);
        } else {
            fuelStack.shrink(2);
        }
    }
}
