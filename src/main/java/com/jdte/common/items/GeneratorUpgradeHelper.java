package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class GeneratorUpgradeHelper {
    private GeneratorUpgradeHelper() {
    }

    public static int burnSpeedMultiplier(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }

    /** Consumes double fuel for the generator upgrade's tripled output. */
    public static boolean consumeFuel(ItemStacksResourceHandler machineHandler, ItemStack fuelStack) {
        var remainder = fuelStack.getCraftingRemainder();
        if (remainder != null) {
            ItemStack remaining = remainder.create();
            if (remaining.getItem() instanceof LargeFuelCanisterItem) {
                LargeFuelCanisterItem.decrementFuel(remaining);
            } else if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            machineHandler.set(0, ItemResource.of(remaining), remaining.getCount());
            return true;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            if (machineHandler.extract(0, ItemResource.of(fuelStack), 2, transaction) != 2) return false;
            transaction.commit();
            return true;
        }
    }
}
