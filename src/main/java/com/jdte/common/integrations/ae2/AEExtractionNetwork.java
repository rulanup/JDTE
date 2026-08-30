package com.jdte.common.integrations.ae2;

import appeng.api.ids.AEComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;

public final class AEExtractionNetwork {
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2");

    private AEExtractionNetwork() {
    }

    public static void registerLinkable() {
        if (AVAILABLE) AEExtractionNetworkIntegration.registerLinkable(JDTEItems.AE_EXTRACTION_UPGRADE.get());
    }

    public static boolean isLinked(ItemStack stack) {
        return AVAILABLE && AEExtractionNetworkIntegration.isLinked(stack);
    }

    public static void copyLink(ItemStack source, ItemStack target) {
        if (AVAILABLE) AEExtractionNetworkIntegration.copyLink(source, target);
    }

    public static void refill(ServerPlayer player, List<ItemStack> stacks) {
        if (AVAILABLE && !stacks.isEmpty()) AEExtractionNetworkIntegration.refill(player, stacks);
    }
}
