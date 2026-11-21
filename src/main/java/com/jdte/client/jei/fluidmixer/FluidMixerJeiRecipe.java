package com.jdte.client.jei.fluidmixer;

import com.jdte.common.integrations.MekanismFluidMixerIntegration;
import com.jdte.common.recipes.FluidMixerRecipe;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record FluidMixerJeiRecipe(
        FluidStack fluidInputA,
        FluidStack fluidInputB,
        List<ItemStack> itemInputs,
        FluidStack output,
        int processTicks,
        int energy
) {
    public Identifier id() {
        return Identifier.fromNamespaceAndPath("jdte", "jei/fluid_mixer/"
                + Integer.toUnsignedString((fluidInputA.toString() + fluidInputB.toString() + output.toString()).hashCode(), 16));
    }

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

        for (RecipeHolder<FluidMixerRecipe> holder : com.jdte.client.ClientRecipeEvents.recipes().byType(JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get())) {
                FluidMixerRecipe r = holder.value();
                List<ItemStack> items = r.itemInput().map(input -> {
                    return input.ingredient().items().map(item -> new ItemStack(item, input.count())).toList();
                }).orElse(List.of());

                recipes.add(new FluidMixerJeiRecipe(
                        r.fluidInputA(),
                        r.fluidInputB(),
                        items,
                        r.output(),
                        r.processTicks(),
                        r.energy()
                ));
        }

        if (ModList.get().isLoaded("mekanism")) {
            for (FluidMixerRecipe r : MekanismFluidMixerIntegration.getCachedRecipes(level)) {
                recipes.add(new FluidMixerJeiRecipe(
                        r.fluidInputA(),
                        r.fluidInputB(),
                        List.of(),
                        r.output(),
                        r.processTicks(),
                        r.energy()
                ));
            }
        }

        return recipes;
    }
}
