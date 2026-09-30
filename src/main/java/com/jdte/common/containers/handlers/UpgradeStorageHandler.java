package com.jdte.common.containers.handlers;

import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ComponentItemHandler;
import org.jetbrains.annotations.NotNull;

public class UpgradeStorageHandler extends ComponentItemHandler {
    public UpgradeStorageHandler(ItemStack storageStack) {
        super(storageStack, JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get(), UpgradeStorageContainer.SLOTS);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.isEmpty() || UpgradeStorageItem.isAllowedUpgrade(stack);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack toInsert, boolean simulate) {
        this.validateSlotIndex(slot);
        if (toInsert.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!this.isItemValid(slot, toInsert)) {
            return toInsert;
        }

        ItemContainerContents contents = this.getContents();
        ItemStack existing = this.getStackFromContents(contents, slot);
        int insertLimit = this.getSlotLimit(slot);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(toInsert, existing)) {
                return toInsert;
            }
            insertLimit -= existing.getCount();
        }

        if (insertLimit <= 0) {
            return toInsert;
        }

        int inserted = Math.min(insertLimit, toInsert.getCount());
        if (!simulate) {
            this.updateContents(contents, toInsert.copyWithCount(existing.getCount() + inserted), slot);
        }
        return toInsert.copyWithCount(toInsert.getCount() - inserted);
    }
}
