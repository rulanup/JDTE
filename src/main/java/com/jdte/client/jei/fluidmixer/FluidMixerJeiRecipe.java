package com.jdte.client.jei.fluidmixer;

import com.jdte.common.integrations.MekanismFluidMixerIntegration;
import com.jdte.common.recipes.FluidMixerRecipe;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record FluidMixerJeiRecipe(
        FluidStack fluidInputA,
        FluidStack fluidInputB,
        List<ItemStack> itemInputs,
        FluidStack output,
        int processTicks,
        int energy, ResourceLocation id
) {
    public static List<ItemStack> getMachines() {
        return List.of(
                new ItemStack(JDTEBlocks.ADVANCED_FLUID_MIXER.get()),
                new ItemStack(JDTEBlocks.EXTENDED_FLUID_MIXER.get())
        );
    }

    public static List<FluidMixerJeiRecipe> getRecipes() {
        List<FluidMixerJeiRecipe> recipes = new ArrayList<>();
        Level level = Minecraft.getInstance().level;
        if (level == null) return recipes;

        RecipeManager recipeManager = level.getRecipeManager();
        if (recipeManager != null) {
            for (FluidMixerRecipe r : recipeManager.getAllRecipesFor(JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get())) {
                List<ItemStack> items = r.itemInput().map(input -> {
                    ItemStack[] matching = input.ingredient().getItems();
                    return Arrays.stream(matching).map(s -> {
                        ItemStack copy = s.copy();
                        copy.setCount(input.count());
                        return copy;
                    }).toList();
                }).orElse(List.of());

                recipes.add(new FluidMixerJeiRecipe(
                        r.fluidInputA(),
                        r.fluidInputB(),
                        items,
                        r.output(),
                        r.processTicks(),
                        r.energy(),r.getId()
                ));
            }
        }

        if (ModList.get().isLoaded("mekanism")) {
            for (FluidMixerRecipe r : MekanismFluidMixerIntegration.getCachedRecipes(level)) {
                recipes.add(new FluidMixerJeiRecipe(
                        r.fluidInputA(),
                        r.fluidInputB(),
                        List.of(),
                        r.output(),
                        r.processTicks(),
                        r.energy(),r.getId()
                ));
            }
        }

        return recipes;
    }
}
