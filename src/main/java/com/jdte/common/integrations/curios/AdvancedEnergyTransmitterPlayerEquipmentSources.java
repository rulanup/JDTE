package com.jdte.common.integrations.curios;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.Predicate;

public final class AdvancedEnergyTransmitterPlayerEquipmentSources {
    private static final boolean CURIOS_AVAILABLE = ModList.get().isLoaded("curios");

    private AdvancedEnergyTransmitterPlayerEquipmentSources() {
    }

    public static void collectCurios(ServerPlayer player, Predicate<ItemStack> visitor) {
        if (CURIOS_AVAILABLE) {
            AdvancedEnergyTransmitterCuriosIntegration.collect(player, visitor);
        }
    }
}