package com.jdte.common.jei.lifesynthesis;

import com.jdte.common.recipes.LifeSynthesisRecipe;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 生命合成舱 JEI 展示对象：培养基输入 + 养分流体 -> 生命流体。
 * 按 tier（plant/protein/enriched）分组排序。
 */
public record LifeSynthesisJeiRecipe(ResourceLocation id, String tier, List<JeiInput> inputs,
                                     FluidStack nutrient, FluidStack output,
                                     int processTicks, int energy) {

    public static List<LifeSynthesisJeiRecipe> getRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        var manager = minecraft.level != null ? minecraft.level.getRecipeManager()
                : minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
        if (manager == null) return List.of();
        List<LifeSynthesisJeiRecipe> result = new ArrayList<>();
        for (LifeSynthesisRecipe recipe : manager.getAllRecipesFor(JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get())) {
            List<JeiInput> inputs = recipe.inputs().stream()
                    .map(input -> new JeiInput(List.of(input.ingredient().getItems()), input.count())).toList();
            result.add(new LifeSynthesisJeiRecipe(recipe.getId(), recipe.tier(), inputs,
                    recipe.nutrient(), recipe.output(), recipe.processTicks(), recipe.energy()));
        }
        result.sort(Comparator.comparing((LifeSynthesisJeiRecipe recipe) -> recipe.tier())
                .thenComparing(recipe -> recipe.id().toString()));
        return result;
    }

    public record JeiInput(List<ItemStack> stacks, int count) { }
}
