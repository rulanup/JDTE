package com.jdte.client.jei.biofactory;

import com.jdte.common.recipes.BioFactoryRecipe;
import com.jdte.common.integrations.ProductiveBeesBioFactoryIntegration;
import com.jdte.common.content.JDTEContentControl;
import com.jdte.setup.JDTERecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record BioFactoryJeiRecipe(Identifier id, List<ItemStack> specimens, List<JeiInput> inputs,
                                  List<JeiOutput> outputs,
                                  Optional<Identifier> processFluid, int processFluidAmount,
                                  boolean processFluidConsumed,
                                  Optional<Identifier> outputFluid, int outputFluidAmount,
                                  int lifeFluidAmount, int timeFluidAmount,
                                  int processTicks, int energy) {
    public static List<BioFactoryJeiRecipe> getRecipes() {
        JDTEContentControl control = JDTEContentControl.current();
        if (!control.isBlockEnabled(Identifier.fromNamespaceAndPath("jdte", "bio_factory"))) {
            return List.of();
        }
        boolean dynamicGeneration = control.isDynamicRecipeGenerationEnabled(
                JDTEContentControl.DynamicRecipeFamily.BIO_FACTORY);
        Minecraft minecraft = Minecraft.getInstance();
        var recipes = com.jdte.client.ClientRecipeEvents.recipes();
        List<BioFactoryJeiRecipe> result = new ArrayList<>();
        for (var holder : recipes.byType(JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get())) {
            if (!control.isRecipeEnabled(holder.id().identifier())) continue;
            BioFactoryRecipe recipe = holder.value();
            List<JeiOutput> outputs = recipe.outputs().stream()
                    .map(output -> new JeiOutput(List.of(output.stack()), output.chance())).toList();
            List<JeiInput> inputs = recipe.inputs().stream()
                    .map(input -> new JeiInput(input.ingredient().items().map(ItemStack::new).toList(), input.count())).toList();
            result.add(new BioFactoryJeiRecipe(holder.id().identifier(), recipe.specimen().items().map(ItemStack::new).toList(),
                    inputs, outputs, recipe.processFluid(),
                    recipe.processFluidAmount(), recipe.processFluid().isPresent() && recipe.processFluidAmount() > 0,
                    recipe.outputFluid(), recipe.outputFluidAmount(),
                    com.jdte.setup.JDTEConfig.COMMON.bioFactoryLifeFluidPerCycle.get(),
                    com.jdte.setup.JDTEConfig.COMMON.bioFactoryTimeFluidPerCycle.get(),
                    recipe.processTicks(), recipe.energy()));
        }
        if (dynamicGeneration && ModList.get().isLoaded("productivebees") && minecraft.level != null) {
            for (var recipe : ProductiveBeesBioFactoryIntegration.getJeiRecipes(
                    minecraft.level, recipes)) {
                if (!control.isRecipeEnabled(recipe.id())) continue;
                List<JeiOutput> outputs = recipe.outputs().stream()
                        .map(output -> new JeiOutput(output.stacks(), output.chance())).toList();
                List<JeiInput> inputs = recipe.foods().isEmpty() ? List.of()
                        : List.of(new JeiInput(recipe.foods(), 0));
                result.add(new BioFactoryJeiRecipe(recipe.id(), List.of(recipe.specimen()), inputs, outputs,
                        recipe.processFluid(), recipe.processFluid().isPresent()
                                ? com.jdte.setup.JDTEConfig.COMMON.bioFactoryProcessFluidPerCycle.get() : 0,
                        false,
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
