package com.jdte.common.jei.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.integrations.MysticalAgricultureGreenhouseIntegration;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record GreenhouseJeiRecipe(ResourceLocation id, ItemStack seed, List<ItemStack> outputs,
                                  int timeFluid, int energy, int growthWork) {
    public static List<GreenhouseJeiRecipe> getRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        RecipeManager manager = minecraft.level != null ? minecraft.level.getRecipeManager()
                : minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
        if (manager == null) return List.of();
        List<GreenhouseJeiRecipe> result = new ArrayList<>();
        Set<Item> seen = new HashSet<>();

        for (var holder : manager.getAllRecipesFor(JDTERecipes.GREENHOUSE_RECIPE_TYPE.get())) {
            GreenhouseRecipe recipe = holder.value();
            for (ItemStack stack : recipe.seed().getItems()) {
                if (!stack.isEmpty() && seen.add(stack.getItem())) {
                    result.add(create(holder.id(), stack, recipe.outputs(), recipe.timeFluid(), recipe.growthWork()));
                }
            }
        }

        if (ModList.get().isLoaded("mysticalagriculture")) {
            MysticalAgricultureGreenhouseIntegration.getCrops().forEach((item, definition) -> {
                if (!seen.add(item) || definition.outputs().isEmpty()) return;
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                result.add(create(ResourceLocation.fromNamespaceAndPath("jdte",
                                "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath()),
                        new ItemStack(item), definition.outputs(), definition.timeFluid(), definition.growthWork()));
            });
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (seen.contains(item) || !(item instanceof BlockItem blockItem) || !isPlant(blockItem)) continue;
            ItemStack seed = new ItemStack(item);
            GreenhouseCropDefinition definition = minecraft.level != null
                    ? GreenhouseCropResolver.find(minecraft.level, seed) : findWithoutLevel(seed, blockItem);
            if (definition == null || definition.outputs().isEmpty()) continue;
            seen.add(item);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            result.add(create(ResourceLocation.fromNamespaceAndPath("jdte",
                    "jei/greenhouse/" + itemId.getNamespace() + "/" + itemId.getPath()),
                    seed, definition.outputs(), definition.timeFluid(), definition.growthWork()));
        }

        result.sort(Comparator.comparing(recipe -> BuiltInRegistries.ITEM.getKey(recipe.seed().getItem()).toString()));
        return result;
    }

    private static GreenhouseJeiRecipe create(ResourceLocation id, ItemStack seed, List<ItemStack> outputs,
                                               int rawFluid, int growthWork) {
        int divisor = JDTEConfig.COMMON.greenhouseFluidCostDivisor.get();
        int fluid = Math.max(1, (rawFluid + divisor - 1) / divisor);
        return new GreenhouseJeiRecipe(id, seed.copyWithCount(1), outputs.stream().map(ItemStack::copy).toList(),
                fluid, JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get(), growthWork);
    }

    private static boolean isPlant(BlockItem item) {
        var block = item.getBlock();
        return block instanceof BushBlock || block instanceof CropBlock
                || block.defaultBlockState().getProperties().stream()
                .anyMatch(property -> property instanceof IntegerProperty && "age".equals(property.getName()));
    }

    private static GreenhouseCropDefinition findWithoutLevel(ItemStack seed, BlockItem blockItem) {
        if (ModList.get().isLoaded("mysticalagriculture")) {
            GreenhouseCropDefinition mystical = MysticalAgricultureGreenhouseIntegration.find(seed);
            if (mystical != null) return mystical;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        return new GreenhouseCropDefinition(List.of(seed.copyWithCount(1)), blockId, blockId, true,
                JDTEConfig.COMMON.greenhouseDefaultGrowthWork.get(), JDTEConfig.COMMON.greenhouseGenericFluidCost.get());
    }
}
