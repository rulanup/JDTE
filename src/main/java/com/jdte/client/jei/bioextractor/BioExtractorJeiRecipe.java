package com.jdte.client.jei.bioextractor;

import com.jdte.common.recipes.BioExtractorRecipe;
import com.jdte.common.recipes.BioFactoryOutput;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record BioExtractorJeiRecipe(ResourceLocation id,
                                   ResourceLocation entityType,
                                   ItemStack entityIcon,
                                   Optional<FluidStack> outputFluid,
                                   List<BioFactoryOutput> outputItems,
                                   int energy,
                                   int cooldown,
                                   float damage) {

    public static List<BioExtractorJeiRecipe> getRecipes() {
        List<BioExtractorJeiRecipe> recipes = new ArrayList<>();
        RecipeManager rm = getRecipeManager();
        if (rm == null) return recipes;

        for (RecipeHolder<BioExtractorRecipe> holder : rm.getAllRecipesFor(JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get())) {
            BioExtractorRecipe recipe = holder.value();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(recipe.entityType());
            SpawnEggItem egg = SpawnEggItem.byId(type);
            ItemStack icon = egg != null ? new ItemStack(egg) : new ItemStack(Items.EGG);
            recipes.add(new BioExtractorJeiRecipe(
                    holder.id(),
                    recipe.entityType(),
                    icon,
                    recipe.outputFluid(),
                    recipe.outputItems(),
                    recipe.energy(),
                    recipe.cooldown(),
                    recipe.damage()
            ));
        }
        return recipes;
    }

    public static List<ItemStack> getMachines() {
        return List.of(
                new ItemStack(JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get()),
                new ItemStack(JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get())
        );
    }

    private static RecipeManager getRecipeManager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            return mc.level.getRecipeManager();
        }
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            return mc.getSingleplayerServer().getRecipeManager();
        }
        return null;
    }
}
