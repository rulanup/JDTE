package com.jdte.common.containers.handlers;

import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class UpgradeStorageHandler extends BatchComponentItemHandler {
    public UpgradeStorageHandler(ItemStack storage) {
        this(ItemAccess.forStack(storage), UpgradeStorageContainer.SLOTS);
    }

    public UpgradeStorageHandler(ItemAccess access, int slots) {
        super(access, JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get(), slots);
    }

    public int store(ItemStack stack) {
        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            int stored = net.neoforged.neoforge.transfer.ResourceHandlerUtil.insertStacking(
                    this, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return stored;
        }
    }

    @Override public boolean isValid(int slot, ItemResource resource) {
        return super.isValid(slot, resource) && !resource.isEmpty()
                && UpgradeStorageItem.isAllowedUpgrade(resource.toStack());
    }

    @Override protected int getCapacity(int slot, ItemResource resource) {
        return 64;
    }
}
