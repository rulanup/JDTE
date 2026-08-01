package com.jdte.common.blockentities;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Applies the greenhouse-specific, loot-table-independent Fortune output bonus. */
final class GreenhouseFortuneHelper {
    private static final int BONUS_DENOMINATOR = 10;

    private GreenhouseFortuneHelper() {
    }

    /**
     * Returns the aggregate output for a completed harvest batch. Each Fortune Upgrade adds an
     * expected 10% to every output. Probabilistic remainder rounding keeps count-one outputs useful.
     */
    static List<ItemStack> scaleBatch(List<ItemStack> drops, int harvests, int fortuneLevel,
                                      RandomSource random) {
        if (harvests <= 0) return List.of();
        List<ItemStack> scaled = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            long baseAmount = (long) drop.getCount() * harvests;
            long bonusNumerator = baseAmount * Math.max(0, fortuneLevel);
            long bonus = bonusNumerator / BONUS_DENOMINATOR;
            int remainder = (int) (bonusNumerator % BONUS_DENOMINATOR);
            if (remainder > 0 && random.nextInt(BONUS_DENOMINATOR) < remainder) bonus++;
            long total = Math.min(Integer.MAX_VALUE, baseAmount + bonus);
            scaled.add(drop.copyWithCount((int) total));
        }
        return scaled;
    }

    /** Uses the rounded-up maximum bonus so a batch accepted by routing always fits after rolling. */
    static List<ItemStack> capacityBound(List<ItemStack> drops, int harvests, int fortuneLevel) {
        if (harvests <= 0) return List.of();
        List<ItemStack> scaled = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            long baseAmount = (long) drop.getCount() * harvests;
            long bonusNumerator = baseAmount * Math.max(0, fortuneLevel);
            long bonus = (bonusNumerator + BONUS_DENOMINATOR - 1L) / BONUS_DENOMINATOR;
            long total = Math.min(Integer.MAX_VALUE, baseAmount + bonus);
            scaled.add(drop.copyWithCount((int) total));
        }
        return scaled;
    }
}
