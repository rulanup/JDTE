package com.jdte.common.integrations;

import com.jdte.common.recipes.FluidMixerRecipe;
import com.jdte.common.recipes.RecipeCacheSignal;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 可选的 Mekanism Chemical Infuser 配方桥接。
 *
 * <p>在 RecipeCacheSignal 代次变化时重建缓存，将 Mek 的 Chemical Infuser (ChemicalChemicalToChemicalRecipe) 配方
 * 转换为 JDTE {@link FluidMixerRecipe} 格式。仅转换输入/输出在流体注册表中有同名对应项的配方。</p>
 *
 * <p>此类只在 {@code ModList.get().isLoaded("mekanism")} 保护下加载。</p>
 */
public final class MekanismFluidMixerIntegration {
    private MekanismFluidMixerIntegration() {}

    private static long cachedGeneration = -1L;
    private static List<FluidMixerRecipe> cachedRecipes = Collections.emptyList();

    /**
     * 返回从 Mekanism Chemical Infuser 配方缓存转换而来的 FluidMixerRecipe 列表。
     * 线程安全地根据 RecipeCacheSignal 代次决定是否重建。
     */
    public static List<FluidMixerRecipe> getCachedRecipes(Level level) {
        long generation = RecipeCacheSignal.generation();
        if (generation != cachedGeneration) {
            cachedRecipes = buildCache(level);
            cachedGeneration = generation;
        }
        return cachedRecipes;
    }

    private static List<FluidMixerRecipe> buildCache(Level level) {
        if (level == null) return Collections.emptyList();

        try {
            var recipeTypeHolder = MekanismRecipeTypes.TYPE_CHEMICAL_INFUSING;
            if (recipeTypeHolder == null || !recipeTypeHolder.isBound()) {
                return Collections.emptyList();
            }

            var mekType = recipeTypeHolder.get();
            if (mekType == null) return Collections.emptyList();

            List<FluidMixerRecipe> results = new ArrayList<>();
            for (RecipeHolder<ChemicalChemicalToChemicalRecipe> holder : level.getRecipeManager().getAllRecipesFor(mekType)) {
                ChemicalChemicalToChemicalRecipe recipe = holder.value();
                try {
                    List<ChemicalStack> leftStacks = recipe.getLeftInput().getRepresentations();
                    List<ChemicalStack> rightStacks = recipe.getRightInput().getRepresentations();
                    List<ChemicalStack> outputStacks = recipe.getOutputDefinition();

                    if (leftStacks.isEmpty() || rightStacks.isEmpty() || outputStacks.isEmpty()) continue;

                    ChemicalStack left = leftStacks.getFirst();
                    ChemicalStack right = rightStacks.getFirst();
                    ChemicalStack output = outputStacks.getFirst();

                    FluidStack fluidA = chemicalToFluid(left);
                    FluidStack fluidB = chemicalToFluid(right);
                    FluidStack fluidOut = chemicalToFluid(output);

                    if (fluidA.isEmpty() || fluidB.isEmpty() || fluidOut.isEmpty()) continue;

                    results.add(new FluidMixerRecipe(
                            fluidA, fluidB,
                            Optional.empty(),
                            fluidOut,
                            100, // 默认处理时间 100 ticks
                            5000 // 默认能量消耗 5000 FE
                    ));
                } catch (Exception ignored) {
                    // 跳过单个异常配方
                }
            }
            return Collections.unmodifiableList(results);
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * 尝试将 Mekanism Chemical 映射到 NeoForge FluidStack。
     */
    private static FluidStack chemicalToFluid(ChemicalStack chemicalStack) {
        if (chemicalStack == null || chemicalStack.isEmpty()) return FluidStack.EMPTY;
        try {
            ResourceLocation chemicalId = chemicalStack.getChemicalHolder().unwrapKey()
                    .map(net.minecraft.resources.ResourceKey::location)
                    .orElse(null);
            if (chemicalId == null) return FluidStack.EMPTY;

            Fluid fluid = BuiltInRegistries.FLUID.get(chemicalId);
            if (fluid != null && fluid != Fluids.EMPTY) {
                int amount = (int) Math.min(Integer.MAX_VALUE, Math.max(1, chemicalStack.getAmount()));
                return new FluidStack(fluid, amount);
            }
        } catch (Exception ignored) {
        }
        return FluidStack.EMPTY;
    }
}
