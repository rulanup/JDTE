package com.jdte.common.integrations;

import com.devdyna.justdynathings.config.CommonConfig;
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
            case "charged_primogel_goo" -> CommonConfig.GOO_T1_TIER.get();
            case "charged_blazebloom_goo" -> CommonConfig.GOO_T2_TIER.get();
            case "charged_voidshimmer_goo" -> CommonConfig.GOO_T3_TIER.get();
            case "charged_shadowpulse_goo" -> CommonConfig.GOO_T4_TIER.get();
            case "energized_goo" -> CommonConfig.GOO_ENERGY_TIER.get();
            case "creative_goo" -> CommonConfig.GOO_CREATIVE_TIER.get();
            default -> 0;
        };
    }

    public static int getEnergyCostPerTick(ItemStack stack) {
        if (getType(stack) != GooType.ENERGY) {
            return 0;
        }
        long multiplier = CommonConfig.GOO_FEGOO_FE_RATE_MULTIPLY.get()
                ? Math.max(1, getTier(stack))
                : 1L;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(0L, (long) CommonConfig.GOO_FEGOO_FE_RATE.get()) * multiplier);
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
