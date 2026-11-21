package com.jdte.client.jei.bioextractor;

import com.jdte.common.recipes.BioExtractorRecipe;
import com.jdte.common.recipes.BioFactoryOutput;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

public record BioExtractorJeiRecipe(Identifier id,
                                   Identifier entityType,
                                   ItemStack entityIcon,
                                   Optional<FluidStack> outputFluid,
                                   List<BioFactoryOutput> outputItems,
                                   int energy,
                                   int cooldown,
                                   float damage) {

    public static List<BioExtractorJeiRecipe> getRecipes() {
        List<BioExtractorJeiRecipe> recipes = new ArrayList<>();
        for (RecipeHolder<BioExtractorRecipe> holder : com.jdte.client.ClientRecipeEvents.recipes().byType(JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get())) {
            BioExtractorRecipe recipe = holder.value();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(recipe.entityType());
            ItemStack icon = type == null ? new ItemStack(Items.EGG)
                    : SpawnEggItem.byId(type).map(ItemStack::new).orElseGet(() -> new ItemStack(Items.EGG));
            recipes.add(new BioExtractorJeiRecipe(
                    holder.id().identifier(),
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

}
