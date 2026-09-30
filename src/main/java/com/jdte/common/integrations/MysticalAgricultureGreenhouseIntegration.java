package com.jdte.common.integrations;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

public final class MysticalAgricultureGreenhouseIntegration {
    private static final Map<Item, GreenhouseCropDefinition> CROPS_BY_SEED = new IdentityHashMap<>();
    private static boolean initialized;

    private MysticalAgricultureGreenhouseIntegration() {
    }

    public static GreenhouseCropDefinition find(ItemStack seed) {
        ensureInitialized();
        return CROPS_BY_SEED.get(seed.getItem());
    }

    public static Map<Item, GreenhouseCropDefinition> getCrops() {
        ensureInitialized();
        return Map.copyOf(CROPS_BY_SEED);
    }

    public static synchronized void invalidateCaches() {
        initialized = false;
        CROPS_BY_SEED.clear();
    }

    private static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        int baseFluid = JDTEConfig.COMMON.greenhouseMysticalBaseFluidCost.get();
        int baseGrowthWork = JDTEConfig.COMMON.greenhouseDefaultGrowthWork.get();
        int tierGrowthPercent = JDTEConfig.COMMON.greenhouseMysticalTierGrowthPercent.get();
        for (Crop crop : MysticalAgricultureAPI.getCropRegistry().getCrops()) {
            if (!crop.isEnabled() || crop.getSeedsItem() == null || crop.getEssenceItem() == null
                    || crop.getCropBlock() == null) {
                continue;
            }
            int tier = Math.max(1, crop.getTier().getValue());
            int fluidCost = Math.min(Integer.MAX_VALUE, baseFluid * tier * tier);
            long scaledWork = (long) baseGrowthWork * (100L + (long) (tier - 1) * tierGrowthPercent) / 100L;
            int growthWork = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, scaledWork));
            CROPS_BY_SEED.put(crop.getSeedsItem(), new GreenhouseCropDefinition(
                    java.util.List.of(new ItemStack(crop.getEssenceItem())),
                    BuiltInRegistries.BLOCK.getKey(crop.getCropBlock()),
                    BuiltInRegistries.BLOCK.getKey(crop.getCropBlock()),
                    true,
                    growthWork,
                    GreenhouseRecipe.DEFAULT_FLUID,
                    fluidCost));
        }
    }
}
