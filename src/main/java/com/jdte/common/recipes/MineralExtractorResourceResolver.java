package com.jdte.common.recipes;

import com.jdte.JDTE;
import com.jdte.common.minerals.MineralExtractorFluidRoles;
import com.jdte.setup.JDTERecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/** Resolves the datapack-selected mineral extractor production fluids by their stable recipe id. */
public final class MineralExtractorResourceResolver {
    public static final ResourceLocation RECIPE_ID = JDTE.id("mineral_extractor_resources");
    private static final ResourceLocation DEFAULT_FORTUNE_FLUID = ResourceLocation.fromNamespaceAndPath("justdirethings", "xp_fluid_source");
    private static final ResourceLocation DEFAULT_ACCELERATION_FLUID = ResourceLocation.fromNamespaceAndPath("justdirethings", "time_fluid_source");
    private static final MineralExtractorFluidRoles DEFAULT_ROLES = new MineralExtractorFluidRoles(
            DEFAULT_FORTUNE_FLUID, DEFAULT_ACCELERATION_FLUID);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean FALLBACK_WARNING_LOGGED = new AtomicBoolean();

    private MineralExtractorResourceResolver() {
    }

    public static MineralExtractorFluidRoles resolve(Level level) {
        return level == null ? fallback("no level is available") : resolve(level.getRecipeManager());
    }

    public static MineralExtractorFluidRoles resolve(RecipeManager recipeManager) {
        if (recipeManager == null) {
            return fallback("the recipe manager is unavailable");
        }

        return recipeManager.getAllRecipesFor(JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_TYPE.get()).stream()
                .filter(holder -> holder.id().equals(RECIPE_ID))
                .findFirst()
                .filter(holder -> hasRegisteredFluids(holder.value()))
                .<MineralExtractorFluidRoles>map(holder -> new MineralExtractorFluidRoles(
                        holder.value().fortuneFluid(), holder.value().accelerationFluid()))
                .orElseGet(() -> fallback("recipe " + RECIPE_ID + " is missing, unsynchronised, or references an unknown fluid"));
    }

    private static boolean hasRegisteredFluids(MineralExtractorResourcesRecipe recipe) {
        return BuiltInRegistries.FLUID.containsKey(recipe.fortuneFluid())
                && BuiltInRegistries.FLUID.containsKey(recipe.accelerationFluid());
    }

    private static MineralExtractorFluidRoles fallback(String reason) {
        if (FALLBACK_WARNING_LOGGED.compareAndSet(false, true)) {
            LOGGER.warn("Using default mineral extractor fluid roles because {}", reason);
        }
        return DEFAULT_ROLES;
    }
}
