package com.jdte.common.jei.gelgenerator;

import com.direwolf20.justdirethings.datagen.JustDireItemTags;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipeTag;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record GelGeneratorJeiRecipe(
        ItemStack gelStack,
        List<ItemStack> foodStacks,
        ItemStack inputStack,
        ItemStack outputStack,
        Fluid inputFluid,
        Fluid outputFluid,
        int fluidAmount,
        int energyCost
) {
    public static List<GelGeneratorJeiRecipe> getRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        RecipeManager recipeManager = minecraft.level != null
                ? minecraft.level.getRecipeManager()
                : minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
        if (recipeManager == null) {
            return List.of();
        }

        List<GelGeneratorJeiRecipe> recipes = new ArrayList<>();

        for (RecipeHolder<GooSpreadRecipe> recipe : recipeManager.getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get())) {
            addBlockRecipe(recipes, recipe.value());
        }
        for (RecipeHolder<GooSpreadRecipeTag> recipe : recipeManager.getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE_TAG.get())) {
            addTagRecipe(recipes, recipe.value());
        }

        return recipes;
    }

    public static List<ItemStack> getMachines() {
        return List.of(
                new ItemStack(JDTEBlocks.ADVANCED_GEL_GENERATOR.get()),
                new ItemStack(JDTEBlocks.EXTENDED_GEL_GENERATOR.get())
        );
    }

    public boolean hasItemConversion() {
        return !inputStack.isEmpty() && !outputStack.isEmpty();
    }

    public boolean hasFluidConversion() {
        return inputFluid != Fluids.EMPTY && outputFluid != Fluids.EMPTY;
    }

    private static void addBlockRecipe(List<GelGeneratorJeiRecipe> recipes, GooSpreadRecipe recipe) {
        Fluid inputFluid = getFluid(recipe.getInput());
        Fluid outputFluid = getFluid(recipe.getOutput());
        if (inputFluid != Fluids.EMPTY && outputFluid != Fluids.EMPTY) {
            addRecipeForEachGelFood(recipes, recipe.getTierRequirement(),
                    ItemStack.EMPTY, ItemStack.EMPTY, inputFluid, outputFluid, GelGeneratorBE.FLUID_CONVERSION_AMOUNT);
            return;
        }

        ItemStack input = getInputItem(recipe.getInput());
        ItemStack output = GelGeneratorBE.getOutputItemForState(recipe.getOutput());
        if (input.isEmpty() || output.isEmpty()) {
            return;
        }
        addRecipeForEachGelFood(recipes, recipe.getTierRequirement(),
                input, output, Fluids.EMPTY, Fluids.EMPTY, 0);
    }

    private static void addTagRecipe(List<GelGeneratorJeiRecipe> recipes, GooSpreadRecipeTag recipe) {
        ItemStack output = GelGeneratorBE.getOutputItemForState(recipe.getOutput());
        if (output.isEmpty()) {
            return;
        }

        List<ItemStack> inputs = recipe.getInput().getItems()
                .map(GelGeneratorJeiRecipe::single)
                .filter(stack -> !stack.isEmpty())
                .sorted(Comparator.comparing(GelGeneratorJeiRecipe::itemId))
                .toList();
        for (ItemStack input : inputs) {
            addRecipeForEachGelFood(recipes, recipe.getTierRequirement(),
                    input, output, Fluids.EMPTY, Fluids.EMPTY, 0);
        }
    }

    private static void addRecipeForEachGelFood(List<GelGeneratorJeiRecipe> recipes, int tierRequirement,
                                                ItemStack input, ItemStack output,
                                                Fluid inputFluid, Fluid outputFluid, int fluidAmount) {
        for (int tier = Math.max(1, tierRequirement); tier <= 4; tier++) {
            ItemStack gel = getGelStack(tier);
            List<ItemStack> foods = getFoodStacksForTier(tier);
            if (gel.isEmpty() || foods.isEmpty()) {
                continue;
            }
            recipes.add(new GelGeneratorJeiRecipe(
                    gel.copy(),
                    foods,
                    input.copy(),
                    output.copy(),
                    inputFluid,
                    outputFluid,
                    fluidAmount,
                    GelGeneratorBE.STANDARD_ENERGY_COST
            ));
        }
    }

    private static ItemStack getGelStack(int tier) {
        return switch (tier) {
            case 1 -> new ItemStack(Registration.GooBlock_Tier1_ITEM.get());
            case 2 -> new ItemStack(Registration.GooBlock_Tier2_ITEM.get());
            case 3 -> new ItemStack(Registration.GooBlock_Tier3_ITEM.get());
            case 4 -> new ItemStack(Registration.GooBlock_Tier4_ITEM.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static List<ItemStack> getFoodStacksForTier(int tier) {
        TagKey<Item> tag = switch (tier) {
            case 1 -> JustDireItemTags.GOO_REVIVE_TIER_1;
            case 2 -> JustDireItemTags.GOO_REVIVE_TIER_2;
            case 3 -> JustDireItemTags.GOO_REVIVE_TIER_3;
            case 4 -> JustDireItemTags.GOO_REVIVE_TIER_4;
            default -> null;
        };
        if (tag == null) {
            return List.of();
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            ItemStack stack = new ItemStack(holder.value());
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        stacks.sort(Comparator.comparing(GelGeneratorJeiRecipe::itemId));
        return stacks;
    }

    private static ItemStack getInputItem(BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        return stack.is(Items.AIR) ? ItemStack.EMPTY : stack;
    }

    private static Fluid getFluid(BlockState state) {
        if (state.getBlock() instanceof LiquidBlock liquidBlock) {
            return liquidBlock.fluid;
        }
        return Fluids.EMPTY;
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy.is(Items.AIR) ? ItemStack.EMPTY : copy;
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
