package com.jdte.common.integrations.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.Predicate;

public final class AdvancedUpgradeStorageCuriosSources {
    private static final boolean CURIOS_AVAILABLE = ModList.get().isLoaded("curios");

    private AdvancedUpgradeStorageCuriosSources() {
    }

    public static ItemStack find(Player player, Predicate<ItemStack> predicate) {
        if (CURIOS_AVAILABLE) {
            return AdvancedUpgradeStorageCuriosIntegration.find(player, predicate);
        }
        return ItemStack.EMPTY;
    }
}
