package com.jdte.common.jei.infusion;

import com.jdte.common.blockentities.AdvancedInfusionMachineBE;
import com.jdte.common.blockentities.InfusionMachineBE;
import com.jdte.common.recipes.InfusionRecipe;
import com.jdte.common.utils.InfusionFluidHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record InfusionJeiRecipe(
        ItemStack inputStack,
        FluidStack fluidStack,
        ItemStack outputStack,
        int energyCost
) {
    public static List<InfusionJeiRecipe> getRecipes() {
        List<InfusionJeiRecipe> recipes = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager != null) {
            for (RecipeHolder<InfusionRecipe> holder : recipeManager.getAllRecipesFor(JDTERecipes.INFUSION_RECIPE_TYPE.get())) {
                InfusionRecipe recipe = holder.value();
                addRecipe(recipes, seen, recipe.getInput(), recipe.getFluidInput(), recipe.getOutput(), recipe.getEnergyCost());
            }
        }

        addFluidContainerFillRecipes(recipes, seen);
        addVanillaBottleRecipes(recipes, seen);
        return recipes;
    }

    public static List<ItemStack> getMachines() {
        return List.of(
                new ItemStack(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get()),
                new ItemStack(JDTEBlocks.EXTENDED_INFUSION_MACHINE.get())
        );
    }

    private static RecipeManager getRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager();
        }
        return minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
    }

    private static void addFluidContainerFillRecipes(List<InfusionJeiRecipe> recipes, Set<String> seen) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack input = single(new ItemStack(item));
            if (input.isEmpty() || !hasFluidItemCapability(input)) {
                continue;
            }

            for (Fluid fluid : BuiltInRegistries.FLUID) {
                if (!InfusionFluidHelper.isFillableSourceFluid(fluid)) {
                    continue;
                }

                InfusionJeiRecipe recipe = createFluidContainerFillRecipe(input, fluid);
                if (recipe != null) {
                    addRecipe(recipes, seen, recipe.inputStack(), recipe.fluidStack(), recipe.outputStack(), recipe.energyCost());
                }
            }
        }
    }

    private static boolean hasFluidItemCapability(ItemStack input) {
        ItemStack container = input.copy();
        IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);
        return handler != null && handler.getTanks() > 0;
    }

    private static InfusionJeiRecipe createFluidContainerFillRecipe(ItemStack input, Fluid fluid) {
        ItemStack container = single(input);
        ItemStack originalContainer = container.copy();
        IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || handler.getTanks() <= 0) {
            return null;
        }

        FluidStack available = new FluidStack(fluid, InfusionMachineBE.BASE_FLUID_CAPACITY);
        int fillAmount = handler.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillAmount <= 0 || fillAmount > available.getAmount()) {
            return null;
        }

        FluidStack toFill = new FluidStack(fluid, fillAmount);
        int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return null;
        }

        ItemStack result = single(handler.getContainer());
        if (result.isEmpty() || ItemStack.isSameItemSameComponents(originalContainer, result)) {
            return null;
        }

        return new InfusionJeiRecipe(
                originalContainer,
                new FluidStack(fluid, filled),
                result,
                AdvancedInfusionMachineBE.BASE_ENERGY_COST
        );
    }

    private static void addVanillaBottleRecipes(List<InfusionJeiRecipe> recipes, Set<String> seen) {
        ItemStack output = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        output.setCount(1);
        addRecipe(recipes, seen,
                new ItemStack(Items.GLASS_BOTTLE),
                new FluidStack(Fluids.WATER, InfusionFluidHelper.BOTTLE_FLUID_AMOUNT),
                output,
                AdvancedInfusionMachineBE.BASE_ENERGY_COST);

        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (InfusionFluidHelper.isFillableSourceFluid(fluid) && InfusionFluidHelper.isHoneyFluid(fluid)) {
                addRecipe(recipes, seen,
                        new ItemStack(Items.GLASS_BOTTLE),
                        new FluidStack(fluid, InfusionFluidHelper.BOTTLE_FLUID_AMOUNT),
                        new ItemStack(Items.HONEY_BOTTLE),
                        AdvancedInfusionMachineBE.BASE_ENERGY_COST);
            }
        }
    }

    private static void addRecipe(List<InfusionJeiRecipe> recipes, Set<String> seen,
                                  ItemStack input, FluidStack fluid, ItemStack output, int energyCost) {
        ItemStack normalizedInput = single(input);
        ItemStack normalizedOutput = copyNonAir(output);
        if (normalizedInput.isEmpty() || fluid.isEmpty() || normalizedOutput.isEmpty()) {
            return;
        }

        String key = recipeKey(normalizedInput, fluid, normalizedOutput);
        if (!seen.add(key)) {
            return;
        }

        recipes.add(new InfusionJeiRecipe(
                normalizedInput,
                fluid.copy(),
                normalizedOutput,
                energyCost
        ));
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy.is(Items.AIR) ? ItemStack.EMPTY : copy;
    }

    private static ItemStack copyNonAir(ItemStack stack) {
        ItemStack copy = stack.copy();
        return copy.is(Items.AIR) ? ItemStack.EMPTY : copy;
    }

    private static String recipeKey(ItemStack input, FluidStack fluid, ItemStack output) {
        return stackKey(input) + "|" + fluidKey(fluid) + "|" + stackKey(output);
    }

    private static String stackKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "@" + stack.getCount() + "@" + stack.getComponents();
    }

    private static String fluidKey(FluidStack stack) {
        return BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "@" + stack.getAmount();
    }
}
