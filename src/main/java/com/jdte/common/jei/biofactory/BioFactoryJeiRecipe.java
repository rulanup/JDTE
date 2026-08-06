package com.jdte.common.jei.biofactory;

import com.jdte.common.recipes.BioFactoryRecipe;
import com.jdte.common.integrations.ProductiveBeesBioFactoryIntegration;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record BioFactoryJeiRecipe(ResourceLocation id, List<ItemStack> specimens, List<JeiInput> inputs,
                                  List<JeiOutput> outputs,
                                  Optional<ResourceLocation> processFluid, int processFluidAmount,
                                  Optional<ResourceLocation> outputFluid, int outputFluidAmount,
                                  int lifeFluidAmount, int timeFluidAmount,
                                  int processTicks, int energy) {
    public static List<BioFactoryJeiRecipe> getRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        var manager = minecraft.level != null ? minecraft.level.getRecipeManager()
                : minecraft.getConnection() != null ? minecraft.getConnection().getRecipeManager() : null;
        if (manager == null) return List.of();
        List<BioFactoryJeiRecipe> result = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get())) {
            BioFactoryRecipe recipe = holder.value();
            List<JeiOutput> outputs = recipe.outputs().stream()
                    .map(output -> new JeiOutput(List.of(output.stack()), output.chance())).toList();
            List<JeiInput> inputs = recipe.inputs().stream()
                    .map(input -> new JeiInput(List.of(input.ingredient().getItems()), input.count())).toList();
            result.add(new BioFactoryJeiRecipe(holder.id(), List.of(recipe.specimen().getItems()),
                    inputs, outputs, recipe.processFluid(),
                    recipe.processFluidAmount(), recipe.outputFluid(), recipe.outputFluidAmount(),
                    com.jdte.setup.JDTEConfig.COMMON.bioFactoryLifeFluidPerCycle.get(),
                    com.jdte.setup.JDTEConfig.COMMON.bioFactoryTimeFluidPerCycle.get(),
                    recipe.processTicks(), recipe.energy()));
        }
        if (ModList.get().isLoaded("productivebees") && minecraft.level != null) {
            for (var recipe : ProductiveBeesBioFactoryIntegration.getJeiRecipes(
                    minecraft.level, manager)) {
                List<JeiOutput> outputs = recipe.outputs().stream()
                        .map(output -> new JeiOutput(output.stacks(), output.chance())).toList();
                List<JeiInput> inputs = recipe.foods().isEmpty() ? List.of()
                        : List.of(new JeiInput(recipe.foods(), 0));
                result.add(new BioFactoryJeiRecipe(recipe.id(), List.of(recipe.specimen()), inputs, outputs,
                        recipe.processFluid(), recipe.processFluid().isPresent()
                                ? com.jdte.setup.JDTEConfig.COMMON.bioFactoryProcessFluidPerCycle.get() : 0,
                        Optional.empty(), 0,
                        multiplyCost(com.jdte.setup.JDTEConfig.COMMON.bioFactoryLifeFluidPerCycle.get(),
                                com.jdte.setup.JDTEConfig.COMMON.bioFactoryExternalLifeFluidCostMultiplier.get()),
                        multiplyCost(com.jdte.setup.JDTEConfig.COMMON.bioFactoryTimeFluidPerCycle.get(),
                                com.jdte.setup.JDTEConfig.COMMON.bioFactoryExternalTimeFluidCostMultiplier.get()),
                        600, 1000));
            }
        }
        result.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        return result;
    }

    private static int multiplyCost(int value, int multiplier) {
        long result = (long) Math.max(0, value) * Math.max(0, multiplier);
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    public record JeiOutput(List<ItemStack> stacks, float chance) { }
    public record JeiInput(List<ItemStack> stacks, int count) { }
}
