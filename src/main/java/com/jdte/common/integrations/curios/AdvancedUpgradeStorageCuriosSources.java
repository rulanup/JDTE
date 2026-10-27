package com.jdte.common.integrations.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.Predicate;

public final class AdvancedUpgradeStorageCuriosSources {
    private static boolean isCuriosLoaded() {
        return ModList.get() != null && ModList.get().isLoaded("curios");
    }

    private AdvancedUpgradeStorageCuriosSources() {
    }

    public static ItemStack find(Player player, Predicate<ItemStack> predicate) {
        if (isCuriosLoaded()) {
            return AdvancedUpgradeStorageCuriosIntegration.find(player, predicate);
        }
        return ItemStack.EMPTY;
    }

    public static java.util.List<ItemStack> findAll(Player player, Predicate<ItemStack> predicate) {
        if (isCuriosLoaded()) {
            return AdvancedUpgradeStorageCuriosIntegration.findAll(player, predicate);
        }
        return java.util.List.of();
    }
}
