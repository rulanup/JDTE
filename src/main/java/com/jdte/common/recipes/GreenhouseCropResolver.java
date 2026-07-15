package com.jdte.common.recipes;

import com.jdte.common.integrations.MysticalAgricultureGreenhouseIntegration;
import com.jdte.setup.JDTERecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import com.jdte.setup.JDTEConfig;
import net.neoforged.fml.ModList;

public final class GreenhouseCropResolver {
    private GreenhouseCropResolver() {
    }

    public static GreenhouseCropDefinition find(Level level, ItemStack seed) {
        if (level == null || seed.isEmpty()) {
            return null;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(JDTERecipes.GREENHOUSE_RECIPE_TYPE.get())) {
            GreenhouseRecipe recipe = holder.value();
            if (recipe.matchesSeed(seed)) {
                return new GreenhouseCropDefinition(recipe.outputs(), recipe.displayBlock(),
                        recipe.harvestBlock().orElse(recipe.displayBlock()), recipe.useLootTable(),
                        recipe.growthWork(), recipe.timeFluid());
            }
        }
        if (ModList.get().isLoaded("mysticalagriculture")) {
            GreenhouseCropDefinition mystical = MysticalAgricultureGreenhouseIntegration.find(seed);
            if (mystical != null) return mystical;
        }
        if (seed.getItem() instanceof BlockItem blockItem
                && (blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof BushBlock
                || blockItem.getBlock().defaultBlockState().getProperties().stream()
                .anyMatch(property -> property instanceof IntegerProperty && "age".equals(property.getName())))) {
            var blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            return new GreenhouseCropDefinition(java.util.List.of(seed.copyWithCount(1)), blockId, blockId, true,
                    JDTEConfig.COMMON.greenhouseDefaultGrowthWork.get(),
                    JDTEConfig.COMMON.greenhouseGenericFluidCost.get());
        }
        return null;
    }
}
