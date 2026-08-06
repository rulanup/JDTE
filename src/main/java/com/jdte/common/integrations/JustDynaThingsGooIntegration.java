package com.jdte.common.integrations;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class JustDynaThingsGooIntegration {
    private static final String MOD_ID = "justdynathings";

    private JustDynaThingsGooIntegration() {
    }

    public static GooType getType(ItemStack stack) {
        String path = getPath(stack);
        if (path == null) {
            return GooType.NONE;
        }
        return switch (path) {
            case "charged_primogel_goo", "charged_blazebloom_goo",
                 "charged_voidshimmer_goo", "charged_shadowpulse_goo", "energized_goo" -> GooType.ENERGY;
            case "creative_goo" -> GooType.CREATIVE;
            default -> GooType.NONE;
        };
    }

    public static int getTier(ItemStack stack) {
        String path = getPath(stack);
        if (path == null) {
            return 0;
        }
        return switch (path) {
            case "charged_primogel_goo" -> 1;
            case "charged_blazebloom_goo" -> 2;
            case "charged_voidshimmer_goo" -> 3;
            case "charged_shadowpulse_goo" -> 4;
            case "energized_goo" -> 5;
            case "creative_goo" -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public static int getEnergyCostPerTick(ItemStack stack) {
        // The Forge 1.20.1 branch has no Just Dyna Things dependency. Keep
        // this method total so callers remain safe if an old item is present.
        if (getType(stack) != GooType.ENERGY) {
            return 0;
        }
        return 0;
    }

    private static String getPath(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return MOD_ID.equals(id.getNamespace()) ? id.getPath() : null;
    }

    public enum GooType {
        NONE,
        ENERGY,
        CREATIVE
    }
}
