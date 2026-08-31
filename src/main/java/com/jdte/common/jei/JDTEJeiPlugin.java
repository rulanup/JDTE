package com.jdte.common.jei;

import com.jdte.JDTE;
import com.jdte.client.jei.MachineScreenJeiGuiHandler;
import com.jdte.common.jei.gelgenerator.GelGeneratorJeiRecipe;
import com.jdte.common.jei.gelgenerator.GelGeneratorRecipeCategory;
import com.jdte.common.jei.infusion.InfusionJeiRecipe;
import com.jdte.common.jei.infusion.InfusionRecipeCategory;
import com.jdte.common.jei.potionbrewer.PotionBrewerJeiRecipe;
import com.jdte.common.jei.potionbrewer.PotionBrewerRecipeCategory;
import com.jdte.common.jei.lootfabricator.LootFabricatorJeiRecipe;
import com.jdte.common.jei.lootfabricator.LootFabricatorRecipeCategory;
import com.jdte.common.jei.greenhouse.GreenhouseJeiRecipe;
import com.jdte.common.jei.greenhouse.GreenhouseRecipeCategory;
import com.jdte.common.jei.biofactory.BioFactoryJeiRecipe;
import com.jdte.common.jei.biofactory.BioFactoryRecipeCategory;
import com.jdte.common.jei.lifesynthesis.LifeSynthesisJeiRecipe;
import com.jdte.common.jei.lifesynthesis.LifeSynthesisRecipeCategory;
import com.jdte.client.screens.GreenhouseScreen;
import com.jdte.client.screens.LargeGreenhouseScreen;
import com.jdte.client.screens.BioFactoryScreen;
import com.jdte.client.screens.LifeSynthesisScreen;
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
import com.jdte.common.content.JDTEContentControl;

@JeiPlugin
public class JDTEJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "jei_plugin");
    private static IJeiRuntime runtime;
    private static java.util.List<InfusionJeiRecipe> visibleSpawnEggRecipes = java.util.List.of();
    private static boolean infusionRecipesRegistered;
    private static java.util.List<LootFabricatorJeiRecipe> visibleLootFabricatorRecipes = java.util.List.of();
    private static boolean lootFabricatorRecipesRegistered;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        JDTEContentControl control = JDTEContentControl.current();
        registration.addRecipeCategories(
                new GelGeneratorRecipeCategory(guiHelper),
                new InfusionRecipeCategory(guiHelper),
                new PotionBrewerRecipeCategory(guiHelper),
                new LifeSynthesisRecipeCategory(guiHelper)
        );
        if (dynamicEnabled(control, JDTEContentControl.DynamicRecipeFamily.LOOT_FABRICATOR)
                && control.isBlockEnabled(JDTE.id("loot_fabricator"))) {
            registration.addRecipeCategories(new LootFabricatorRecipeCategory(guiHelper));
        }
        if (greenhouseBlockEnabled(control)) {
            registration.addRecipeCategories(new GreenhouseRecipeCategory(guiHelper));
        }
        if (control.isBlockEnabled(JDTE.id("bio_factory"))) {
            registration.addRecipeCategories(new BioFactoryRecipeCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        JDTEContentControl control = JDTEContentControl.current();
        registration.addRecipes(GelGeneratorRecipeCategory.RECIPE_TYPE, GelGeneratorJeiRecipe.getRecipes());
        registration.addRecipes(PotionBrewerRecipeCategory.RECIPE_TYPE, PotionBrewerJeiRecipe.getRecipes());
        if (greenhouseBlockEnabled(control)) {
            registration.addRecipes(GreenhouseRecipeCategory.RECIPE_TYPE, GreenhouseJeiRecipe.getRecipes());
        }
        if (control.isBlockEnabled(JDTE.id("bio_factory"))) {
            registration.addRecipes(BioFactoryRecipeCategory.RECIPE_TYPE, BioFactoryJeiRecipe.getRecipes());
        }
        registration.addRecipes(LifeSynthesisRecipeCategory.RECIPE_TYPE, LifeSynthesisJeiRecipe.getRecipes());
        if (dynamicEnabled(control, JDTEContentControl.DynamicRecipeFamily.LOOT_FABRICATOR)
                && control.isBlockEnabled(JDTE.id("loot_fabricator"))) {
            java.util.List<LootFabricatorJeiRecipe> lootFabricatorRecipes = LootFabricatorJeiRecipe.getRecipes();
            registration.addRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE, lootFabricatorRecipes);
            visibleLootFabricatorRecipes = lootFabricatorRecipes;
            lootFabricatorRecipesRegistered = !lootFabricatorRecipes.isEmpty();
        }
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
        JDTEContentControl control = JDTEContentControl.current();
        for (ItemStack machine : GelGeneratorJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, GelGeneratorRecipeCategory.RECIPE_TYPE);
        }
        for (ItemStack machine : InfusionJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, InfusionRecipeCategory.RECIPE_TYPE);
        }
        for (ItemStack machine : PotionBrewerJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, PotionBrewerRecipeCategory.RECIPE_TYPE);
        }
        if (dynamicEnabled(control, JDTEContentControl.DynamicRecipeFamily.LOOT_FABRICATOR)
                && control.isBlockEnabled(JDTE.id("loot_fabricator"))) {
            for (ItemStack machine : LootFabricatorJeiRecipe.getMachines()) {
                registration.addRecipeCatalyst(machine, LootFabricatorRecipeCategory.RECIPE_TYPE);
            }
        }
        if (control.isBlockEnabled(JDTE.id("greenhouse"))) {
            registration.addRecipeCatalyst(new ItemStack(JDTEItems.GREENHOUSE.get()), GreenhouseRecipeCategory.RECIPE_TYPE);
        }
        if (control.isBlockEnabled(JDTE.id("large_greenhouse"))) {
            registration.addRecipeCatalyst(new ItemStack(JDTEItems.LARGE_GREENHOUSE.get()), GreenhouseRecipeCategory.RECIPE_TYPE);
        }
        if (control.isBlockEnabled(JDTE.id("bio_factory"))) {
            registration.addRecipeCatalyst(new ItemStack(JDTEItems.BIO_FACTORY.get()), BioFactoryRecipeCategory.RECIPE_TYPE);
        }
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.LIFE_SYNTHESIS_VAT.get()), LifeSynthesisRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(BaseMachineScreen.class, new MachineScreenJeiGuiHandler());
        JDTEContentControl control = JDTEContentControl.current();
        if (control.isBlockEnabled(JDTE.id("greenhouse"))) {
            registration.addRecipeClickArea(GreenhouseScreen.class, 36, 7, 24, 18, GreenhouseRecipeCategory.RECIPE_TYPE);
        }
        if (control.isBlockEnabled(JDTE.id("large_greenhouse"))) {
            registration.addRecipeClickArea(LargeGreenhouseScreen.class, 64, 7, 24, 18, GreenhouseRecipeCategory.RECIPE_TYPE);
        }
        if (control.isBlockEnabled(JDTE.id("bio_factory"))) {
            registration.addRecipeClickArea(BioFactoryScreen.class, 35, 6, 32, 12, BioFactoryRecipeCategory.RECIPE_TYPE);
        }
        registration.addRecipeClickArea(LifeSynthesisScreen.class, 112, 7, 28, 12, LifeSynthesisRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        refreshSpawnEggRecipes();
        refreshLootFabricatorRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        visibleSpawnEggRecipes = java.util.List.of();
        infusionRecipesRegistered = false;
        visibleLootFabricatorRecipes = java.util.List.of();
        lootFabricatorRecipesRegistered = false;
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

    public static void refreshLootFabricatorRecipes() {
        if (runtime == null || !com.jdte.client.LootFabricatorLootClientCache.isSynced()) return;
        JDTEContentControl control = JDTEContentControl.current();
        if (!dynamicEnabled(control, JDTEContentControl.DynamicRecipeFamily.LOOT_FABRICATOR)
                || !control.isBlockEnabled(JDTE.id("loot_fabricator"))) {
            if (lootFabricatorRecipesRegistered && !visibleLootFabricatorRecipes.isEmpty()) {
                runtime.getRecipeManager().hideRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE,
                        visibleLootFabricatorRecipes);
            }
            visibleLootFabricatorRecipes = java.util.List.of();
            lootFabricatorRecipesRegistered = false;
            return;
        }
        java.util.List<LootFabricatorJeiRecipe> current = LootFabricatorJeiRecipe.getRecipes();
        if (!lootFabricatorRecipesRegistered) {
            runtime.getRecipeManager().addRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE, current);
            visibleLootFabricatorRecipes = current;
            lootFabricatorRecipesRegistered = true;
            return;
        }
        java.util.List<LootFabricatorJeiRecipe> removed = visibleLootFabricatorRecipes.stream()
                .filter(recipe -> !current.contains(recipe)).toList();
        java.util.List<LootFabricatorJeiRecipe> added = current.stream()
                .filter(recipe -> !visibleLootFabricatorRecipes.contains(recipe)).toList();
        if (!removed.isEmpty()) runtime.getRecipeManager().hideRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE, removed);
        if (!added.isEmpty()) runtime.getRecipeManager().addRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE, added);
        visibleLootFabricatorRecipes = current;
    }

    private static boolean dynamicEnabled(JDTEContentControl control,
                                          JDTEContentControl.DynamicRecipeFamily family) {
        return control.isDynamicRecipeGenerationEnabled(family);
    }

    private static boolean greenhouseBlockEnabled(JDTEContentControl control) {
        return control.isBlockEnabled(JDTE.id("greenhouse"))
                || control.isBlockEnabled(JDTE.id("large_greenhouse"));
    }
}
