package com.jdte.client.jei.lifesynthesis;

import com.jdte.common.recipes.LifeSynthesisRecipe;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 生命合成舱 JEI 展示对象：培养基输入 + 养分流体 -> 生命流体。
 * 按 tier（plant/protein/enriched）分组排序。
 */
public record LifeSynthesisJeiRecipe(Identifier id, String tier, List<JeiInput> inputs,
                                     FluidStack nutrient, FluidStack output,
                                     int processTicks, int energy) {

    public static List<LifeSynthesisJeiRecipe> getRecipes() {
        List<LifeSynthesisJeiRecipe> result = new ArrayList<>();
        for (var holder : com.jdte.client.ClientRecipeEvents.recipes().byType(JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get())) {
            LifeSynthesisRecipe recipe = holder.value();
            List<JeiInput> inputs = recipe.inputs().stream()
                    .map(input -> new JeiInput(input.ingredient().items().map(ItemStack::new).toList(), input.count())).toList();
            result.add(new LifeSynthesisJeiRecipe(holder.id().identifier(), recipe.tier(), inputs,
                    recipe.nutrient(), recipe.output(), recipe.processTicks(), recipe.energy()));
        }
        result.sort(Comparator.comparing((LifeSynthesisJeiRecipe recipe) -> recipe.tier())
                .thenComparing(recipe -> recipe.id().toString()));
        return result;
    }

    public record JeiInput(List<ItemStack> stacks, int count) { }
}
