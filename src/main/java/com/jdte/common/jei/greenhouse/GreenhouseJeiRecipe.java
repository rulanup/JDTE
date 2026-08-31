package com.jdte.common.jei.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.integrations.BotanyPotsGreenhouseIntegration;
import com.jdte.common.integrations.MysticalAgricultureGreenhouseIntegration;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTERecipes;
import com.jdte.common.content.JDTEContentControl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record GreenhouseJeiRecipe(ResourceLocation id, ItemStack seed, List<ItemStack> outputs,
                                  ResourceLocation fluid, int timeFluid, int energy, int growthWork) {
    public static List<GreenhouseJeiRecipe> getRecipes() {
        JDTEContentControl control = JDTEContentControl.current();
        if (!control.isDynamicRecipeGenerationEnabled(JDTEContentControl.DynamicRecipeFamily.GREENHOUSE)
                || (!control.isBlockEnabled(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse"))
                && !control.isBlockEnabled(ResourceLocation.fromNamespaceAndPath("jdte", "large_greenhouse")))) {
            return List.of();
        }
        Minecraft minecraft = Minecraft.getInstance();
        RecipeManager manager = minecraft.level != null ? minecraft.level.getRecipeManager()
                : minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
        if (manager == null) return List.of();
        List<GreenhouseJeiRecipe> result = new ArrayList<>();
        Set<Item> seen = new HashSet<>();

        for (var holder : manager.getAllRecipesFor(JDTERecipes.GREENHOUSE_RECIPE_TYPE.get())) {
            if (!control.isRecipeEnabled(holder.id())) continue;
            GreenhouseRecipe recipe = holder.value();
            for (ItemStack stack : recipe.seed().getItems()) {
                if (!stack.isEmpty() && seen.add(stack.getItem())) {
                    result.add(create(holder.id(), stack, recipe.outputs(), recipe.fluid(), recipe.timeFluid(),
                            recipe.growthWork()));
                }
            }
        }

        if (ModList.get().isLoaded("mysticalagriculture")) {
            MysticalAgricultureGreenhouseIntegration.getCrops().forEach((item, definition) -> {
                if (!seen.add(item) || definition.outputs().isEmpty()) return;
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("jdte",
                        "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath());
                if (control.isRecipeEnabled(recipeId)) {
                    result.add(create(recipeId, new ItemStack(item), definition.outputs(), definition.fluid(),
                            definition.timeFluid(), definition.growthWork()));
                }
            });
        }

        if (ModList.get().isLoaded("botanypots") && minecraft.level != null) {
            for (var crop : BotanyPotsGreenhouseIntegration.getCrops(minecraft.level)) {
                if (!seen.add(crop.seed().getItem()) || crop.definition().outputs().isEmpty()) continue;
                ResourceLocation seedId = BuiltInRegistries.ITEM.getKey(crop.seed().getItem());
                ResourceLocation recipeId = crop.recipeId();
                ResourceLocation displayId = ResourceLocation.fromNamespaceAndPath("jdte",
                        "jei/greenhouse/botanypots/" + recipeId.getNamespace() + "/"
                                + recipeId.getPath() + "/" + seedId.getNamespace() + "/" + seedId.getPath());
                if (control.isRecipeEnabled(displayId)) {
                    result.add(create(displayId, crop.seed(), crop.definition().outputs(), crop.definition().fluid(),
                            crop.definition().timeFluid(), crop.definition().growthWork()));
                }
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (seen.contains(item) || !(item instanceof BlockItem)) continue;
            ItemStack seed = new ItemStack(item);
            if (!control.isItemEnabled(seed)) continue;
            GreenhouseCropDefinition definition = GreenhouseCropResolver.findGeneric(seed);
            if (definition == null || definition.outputs().isEmpty()) continue;
            seen.add(item);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("jdte",
                    "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath());
            if (control.isRecipeEnabled(recipeId)) {
                result.add(create(recipeId, seed, definition.outputs(), definition.fluid(), definition.timeFluid(),
                        definition.growthWork()));
            }
        }

        result.sort(Comparator.comparing(recipe -> BuiltInRegistries.ITEM.getKey(recipe.seed().getItem()).toString()));
        return result;
    }

    private static GreenhouseJeiRecipe create(ResourceLocation id, ItemStack seed, List<ItemStack> outputs,
                                               ResourceLocation fluidType, int rawFluid, int growthWork) {
        int divisor = JDTEConfig.COMMON.greenhouseFluidCostDivisor.get();
        int fluid = Math.max(1, (rawFluid + divisor - 1) / divisor);
        return new GreenhouseJeiRecipe(id, seed.copyWithCount(1), outputs.stream().map(ItemStack::copy).toList(),
                fluidType, fluid, JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get(), growthWork);
    }

}
