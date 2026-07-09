package com.jdte.client;

import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionBrewerRecipeLockClientCache {
    private static final Map<BlockPos, RecipeLockData> LOCKS = new HashMap<>();

    public static void set(BlockPos blockPos, boolean locked, List<ItemStack> templates) {
        NonNullList<ItemStack> copy = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < copy.size() && i < templates.size(); i++) {
            copy.set(i, templates.get(i).copy());
        }
        LOCKS.put(blockPos.immutable(), new RecipeLockData(locked, copy));
    }

    public static boolean isLocked(BlockPos blockPos) {
        RecipeLockData data = LOCKS.get(blockPos);
        return data != null && data.locked();
    }

    public static ItemStack getTemplate(BlockPos blockPos, int slot) {
        RecipeLockData data = LOCKS.get(blockPos);
        if (data == null || slot < 0 || slot >= data.templates().size()) {
            return ItemStack.EMPTY;
        }
        return data.templates().get(slot);
    }

    private record RecipeLockData(boolean locked, NonNullList<ItemStack> templates) {
    }
}
