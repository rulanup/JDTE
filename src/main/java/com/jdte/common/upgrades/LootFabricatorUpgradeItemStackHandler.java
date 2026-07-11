package com.jdte.common.upgrades;

import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.items.LootingUpgradeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class LootFabricatorUpgradeItemStackHandler extends UpgradeItemStackHandler {
    public static final int SLOT_COUNT = 8;
    public static final int MAX_LOOTING = 3;

    public LootFabricatorUpgradeItemStackHandler(LootFabricatorBE machine) {
        super(machine, SLOT_COUNT);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (stack.getItem() instanceof LootingUpgradeItem) {
            return countLooting(slot) < MAX_LOOTING;
        }
        return super.isItemValid(slot, stack);
    }

    public int getLootingCount() {
        return countLooting(-1);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        if (getSlots() == SLOT_COUNT) return;

        List<ItemStack> oldStacks = new ArrayList<>();
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) oldStacks.add(getStackInSlot(i).copy());
        }
        setSize(SLOT_COUNT);
        for (ItemStack stack : oldStacks) {
            for (int slot = 0; slot < SLOT_COUNT && !stack.isEmpty(); slot++) {
                stack = insertItem(slot, stack, false);
            }
        }
    }

    private int countLooting(int ignoredSlot) {
        int count = 0;
        for (int i = 0; i < getSlots(); i++) {
            if (i != ignoredSlot && getStackInSlot(i).getItem() instanceof LootingUpgradeItem) count++;
        }
        return count;
    }
}
