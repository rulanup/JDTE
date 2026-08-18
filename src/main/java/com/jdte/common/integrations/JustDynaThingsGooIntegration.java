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
            case "charged_primogel_goo" -> JustDynaThingsConfig.getInt("GOO_T1_TIER", 1);
            case "charged_blazebloom_goo" -> JustDynaThingsConfig.getInt("GOO_T2_TIER", 2);
            case "charged_voidshimmer_goo" -> JustDynaThingsConfig.getInt("GOO_T3_TIER", 3);
            case "charged_shadowpulse_goo" -> JustDynaThingsConfig.getInt("GOO_T4_TIER", 4);
            case "energized_goo" -> JustDynaThingsConfig.getInt("GOO_ENERGY_TIER", 4);
            case "creative_goo" -> JustDynaThingsConfig.getInt("GOO_CREATIVE_TIER", 4);
            default -> 0;
        };
    }

    public static int getEnergyCostPerTick(ItemStack stack) {
        if (getType(stack) != GooType.ENERGY) {
            return 0;
        }
        long multiplier = JustDynaThingsConfig.getBoolean("GOO_FEGOO_FE_RATE_MULTIPLY", true)
                ? Math.max(1, getTier(stack))
                : 1L;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(0L, (long) JustDynaThingsConfig.getInt("GOO_FEGOO_FE_RATE", 100)) * multiplier);
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
