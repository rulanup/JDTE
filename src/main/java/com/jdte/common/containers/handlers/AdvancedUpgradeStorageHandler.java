package com.jdte.common.containers.handlers;

import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class AdvancedUpgradeStorageHandler extends UpgradeStorageHandler {
    public AdvancedUpgradeStorageHandler(ItemStack storage) {
        super(ItemAccess.forStack(storage), AdvancedUpgradeStorageContainer.SLOTS);
    }
}
