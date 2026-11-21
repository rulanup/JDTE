package com.jdte.client.jei.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.integrations.MysticalAgricultureGreenhouseIntegration;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTERecipes;
import com.jdte.common.content.JDTEContentControl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

public record GreenhouseJeiRecipe(Identifier id, ItemStack seed, List<ItemStack> outputs,
                                  Identifier fluid, int timeFluid, int energy, int growthWork) {
    public static List<GreenhouseJeiRecipe> getRecipes() {
        JDTEContentControl control = JDTEContentControl.current();
        if (!control.isBlockEnabled(Identifier.fromNamespaceAndPath("jdte", "greenhouse"))
                && !control.isBlockEnabled(Identifier.fromNamespaceAndPath("jdte", "large_greenhouse"))) {
            return List.of();
        }
        boolean dynamicGeneration = control.isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.GREENHOUSE);
        Minecraft minecraft = Minecraft.getInstance();
        var recipes = com.jdte.client.ClientRecipeEvents.recipes();
        List<GreenhouseJeiRecipe> result = new ArrayList<>();
        Set<Item> seen = new HashSet<>();

        for (var holder : recipes.byType(JDTERecipes.GREENHOUSE_RECIPE_TYPE.get())) {
            if (!control.isRecipeEnabled(holder.id().identifier())) continue;
            GreenhouseRecipe recipe = holder.value();
            for (ItemStack stack : recipe.seed().items().map(ItemStack::new).toList()) {
                if (!stack.isEmpty() && seen.add(stack.getItem())) {
                    result.add(create(holder.id().identifier(), stack, recipe.outputs(), recipe.fluid(), recipe.timeFluid(),
                            recipe.growthWork()));
                }
            }
        }

        if (dynamicGeneration && ModList.get().isLoaded("mysticalagriculture")) {
            MysticalAgricultureGreenhouseIntegration.getCrops().forEach((item, definition) -> {
                if (!seen.add(item) || definition.outputs().isEmpty()) return;
                Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                Identifier recipeId = Identifier.fromNamespaceAndPath("jdte",
                        "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath());
                if (control.isRecipeEnabled(recipeId)) {
                    result.add(create(recipeId, new ItemStack(item), definition.outputs(), definition.fluid(),
                            definition.timeFluid(), definition.growthWork()));
                }
            });
        }

        if (dynamicGeneration) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (seen.contains(item) || !(item instanceof BlockItem)) continue;
                ItemStack seed = new ItemStack(item);
                if (!control.isItemEnabled(seed)) continue;
                GreenhouseCropDefinition definition = GreenhouseCropResolver.findGeneric(seed);
                if (definition == null || definition.outputs().isEmpty()) continue;
                seen.add(item);
                Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                Identifier recipeId = Identifier.fromNamespaceAndPath("jdte",
                        "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath());
                if (control.isRecipeEnabled(recipeId)) {
                    result.add(create(recipeId, seed, definition.outputs(), definition.fluid(), definition.timeFluid(),
                            definition.growthWork()));
                }
            }
        }

        result.sort(Comparator.comparing(recipe -> BuiltInRegistries.ITEM.getKey(recipe.seed().getItem()).toString()));
        return result;
    }

    private static GreenhouseJeiRecipe create(Identifier id, ItemStack seed, List<ItemStack> outputs,
                                               Identifier fluidType, int rawFluid, int growthWork) {
        int divisor = JDTEConfig.COMMON.greenhouseFluidCostDivisor.get();
        int fluid = Math.max(1, (rawFluid + divisor - 1) / divisor);
        return new GreenhouseJeiRecipe(id, seed.copyWithCount(1), outputs.stream().map(ItemStack::copy).toList(),
                fluidType, fluid, JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get(), growthWork);
    }

}
