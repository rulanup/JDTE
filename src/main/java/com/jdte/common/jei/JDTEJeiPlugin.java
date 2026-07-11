package com.jdte.common.jei;

import com.jdte.JDTE;
import com.jdte.client.jei.AutoIoConfigJeiGuiHandler;
import com.jdte.common.jei.gelgenerator.GelGeneratorJeiRecipe;
import com.jdte.common.jei.gelgenerator.GelGeneratorRecipeCategory;
import com.jdte.common.jei.infusion.InfusionJeiRecipe;
import com.jdte.common.jei.infusion.InfusionRecipeCategory;
import com.jdte.common.jei.potionbrewer.PotionBrewerJeiRecipe;
import com.jdte.common.jei.potionbrewer.PotionBrewerRecipeCategory;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.jdte.setup.JDTEItems;

@JeiPlugin
public class JDTEJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "jei_plugin");
    private static IJeiRuntime runtime;
    private static java.util.List<InfusionJeiRecipe> visibleSpawnEggRecipes = java.util.List.of();
    private static boolean infusionRecipesRegistered;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GelGeneratorRecipeCategory(guiHelper),
                new InfusionRecipeCategory(guiHelper),
                new PotionBrewerRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GelGeneratorRecipeCategory.RECIPE_TYPE, GelGeneratorJeiRecipe.getRecipes());
        registration.addRecipes(PotionBrewerRecipeCategory.RECIPE_TYPE, PotionBrewerJeiRecipe.getRecipes());
        registration.addIngredientInfo(JDTEItems.LIFE_APPLE.get(),
                Component.translatable("jei.jdte.life_apple.info"));
        registration.addIngredientInfo(JDTEItems.WITHER_ESSENCE.get(),
                Component.translatable("jei.jdte.wither_essence.info"));
        registration.addIngredientInfo(JDTEItems.ENDER_DRAGON_ESSENCE.get(),
                Component.translatable("jei.jdte.ender_dragon_essence.info"));
        registration.addIngredientInfo(JDTEItems.ELDER_GUARDIAN_ESSENCE.get(),
                Component.translatable("jei.jdte.elder_guardian_essence.info"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ItemStack machine : GelGeneratorJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, GelGeneratorRecipeCategory.RECIPE_TYPE);
        }
        for (ItemStack machine : InfusionJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, InfusionRecipeCategory.RECIPE_TYPE);
        }
        for (ItemStack machine : PotionBrewerJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, PotionBrewerRecipeCategory.RECIPE_TYPE);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(BaseMachineScreen.class, new AutoIoConfigJeiGuiHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        refreshSpawnEggRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        visibleSpawnEggRecipes = java.util.List.of();
        infusionRecipesRegistered = false;
    }

    public static void refreshSpawnEggRecipes() {
        if (runtime == null) {
            return;
        }
        if (!com.jdte.client.SpawnEggRecipeClientCache.isSynced()) {
            return;
        }

        java.util.List<InfusionJeiRecipe> currentSpawnEggRecipes = InfusionJeiRecipe.getSpawnEggRecipes();
        if (!infusionRecipesRegistered) {
            runtime.getRecipeManager().addRecipes(InfusionRecipeCategory.RECIPE_TYPE, InfusionJeiRecipe.getOrderedRecipes());
            visibleSpawnEggRecipes = currentSpawnEggRecipes;
            infusionRecipesRegistered = true;
            return;
        }

        java.util.List<InfusionJeiRecipe> removed = visibleSpawnEggRecipes.stream()
                .filter(recipe -> !currentSpawnEggRecipes.contains(recipe))
                .toList();
        java.util.List<InfusionJeiRecipe> added = currentSpawnEggRecipes.stream()
                .filter(recipe -> !visibleSpawnEggRecipes.contains(recipe))
                .toList();
        if (!removed.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(InfusionRecipeCategory.RECIPE_TYPE, removed);
        }
        if (!added.isEmpty()) {
            runtime.getRecipeManager().addRecipes(InfusionRecipeCategory.RECIPE_TYPE, added);
        }
        visibleSpawnEggRecipes = currentSpawnEggRecipes;
    }
}
