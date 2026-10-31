package com.jdte.common.jei;

import com.jdte.JDTE;
import com.jdte.client.jei.fluidmixer.*;
import com.jdte.client.jei.mineralextractor.MineralExtractorJeiRecipe;
import com.jdte.client.jei.mineralextractor.MineralExtractorRecipeCategory;
import com.jdte.client.jei.AutoIoConfigJeiGuiHandler;
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

@JeiPlugin
public class JDTEJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "jei_plugin");
    private static IJeiRuntime runtime;
    private static java.util.List<MineralExtractorJeiRecipe> visibleMineralRecipes = java.util.List.of();
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
        registration.addRecipeCategories(
                new GelGeneratorRecipeCategory(guiHelper),
                new InfusionRecipeCategory(guiHelper),
                new PotionBrewerRecipeCategory(guiHelper),
                new LootFabricatorRecipeCategory(guiHelper),
                new GreenhouseRecipeCategory(guiHelper),
                new BioFactoryRecipeCategory(guiHelper),
                new com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory(guiHelper),
                new LifeSynthesisRecipeCategory(guiHelper),
                new FluidMixerRecipeCategory(guiHelper),
                new MineralExtractorRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        visibleMixerRecipes=FluidMixerJeiRecipe.getRecipes();
        registration.addRecipes(FluidMixerRecipeCategory.RECIPE_TYPE, visibleMixerRecipes);
        registration.addRecipes(GelGeneratorRecipeCategory.RECIPE_TYPE, GelGeneratorJeiRecipe.getRecipes());
        registration.addRecipes(PotionBrewerRecipeCategory.RECIPE_TYPE, PotionBrewerJeiRecipe.getRecipes());
        registration.addRecipes(GreenhouseRecipeCategory.RECIPE_TYPE, GreenhouseJeiRecipe.getRecipes());
        registration.addRecipes(BioFactoryRecipeCategory.RECIPE_TYPE, BioFactoryJeiRecipe.getRecipes());
        visibleBioRecipes=com.jdte.client.jei.bioextractor.BioExtractorJeiRecipe.getRecipes();
        registration.addRecipes(com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.RECIPE_TYPE,visibleBioRecipes);
        registration.addRecipes(LifeSynthesisRecipeCategory.RECIPE_TYPE, LifeSynthesisJeiRecipe.getRecipes());
        java.util.List<LootFabricatorJeiRecipe> lootFabricatorRecipes = LootFabricatorJeiRecipe.getRecipes();
        registration.addRecipes(LootFabricatorRecipeCategory.RECIPE_TYPE, lootFabricatorRecipes);
        visibleLootFabricatorRecipes = lootFabricatorRecipes;
        lootFabricatorRecipesRegistered = !lootFabricatorRecipes.isEmpty();
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
        for (ItemStack machine : LootFabricatorJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, LootFabricatorRecipeCategory.RECIPE_TYPE);
        }
        for (ItemStack machine : MineralExtractorJeiRecipe.getMachines())
            registration.addRecipeCatalyst(machine, MineralExtractorRecipeCategory.RECIPE_TYPE);
        for(ItemStack machine : FluidMixerJeiRecipe.getMachines()) registration.addRecipeCatalyst(machine,FluidMixerRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.GREENHOUSE.get()), GreenhouseRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.LARGE_GREENHOUSE.get()), GreenhouseRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.ADVANCED_BIO_EXTRACTOR.get()),com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.EXTENDED_BIO_EXTRACTOR.get()),com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.BIO_FACTORY.get()), BioFactoryRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(JDTEItems.LIFE_SYNTHESIS_VAT.get()), LifeSynthesisRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(BaseMachineScreen.class, new AutoIoConfigJeiGuiHandler());
        registration.addRecipeClickArea(com.jdte.client.screens.AdvancedFluidMixerScreen.class,91,42,24,17,FluidMixerRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(com.jdte.client.screens.ExtendedFluidMixerScreen.class,91,42,24,17,FluidMixerRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(GreenhouseScreen.class, 36, 7, 24, 18, GreenhouseRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(LargeGreenhouseScreen.class, 64, 7, 24, 18, GreenhouseRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(BioFactoryScreen.class, 35, 6, 32, 12, BioFactoryRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(LifeSynthesisScreen.class, 112, 7, 28, 12, LifeSynthesisRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (!com.jdte.setup.JDTEConfig.COMMON.showFumoDolls.get()) jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, java.util.List.of(new net.minecraft.world.item.ItemStack(com.jdte.setup.JDTEItems.CAINIAOYO_FUMO.get())));
        refreshMineralExtractorRecipes();
        refreshFluidMixerRecipes();
        refreshBioExtractorRecipes();
        refreshSpawnEggRecipes();
        refreshLootFabricatorRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        visibleMixerRecipes = java.util.List.of();
        visibleBioRecipes = java.util.List.of();
        visibleMineralRecipes = java.util.List.of();
        visibleSpawnEggRecipes = java.util.List.of();
        infusionRecipesRegistered = false;
        visibleLootFabricatorRecipes = java.util.List.of();
        lootFabricatorRecipesRegistered = false;
    }

    private static java.util.List<FluidMixerJeiRecipe> visibleMixerRecipes=java.util.List.of();
    public static void refreshFluidMixerRecipes() {
        if(runtime==null)return;
        var current=FluidMixerJeiRecipe.getRecipes();
        if(!visibleMixerRecipes.isEmpty())runtime.getRecipeManager().hideRecipes(FluidMixerRecipeCategory.RECIPE_TYPE,visibleMixerRecipes);
        if(!current.isEmpty())runtime.getRecipeManager().addRecipes(FluidMixerRecipeCategory.RECIPE_TYPE,current);
        visibleMixerRecipes=current;
    }
    public static void refreshMineralExtractorRecipes() {
        if (runtime == null || !com.jdte.client.MineralSurveyClientCache.isSynced()) return;
        var current = MineralExtractorJeiRecipe.getRecipes();
        if (!visibleMineralRecipes.isEmpty()) runtime.getRecipeManager().hideRecipes(MineralExtractorRecipeCategory.RECIPE_TYPE, visibleMineralRecipes);
        if (!current.isEmpty()) runtime.getRecipeManager().addRecipes(MineralExtractorRecipeCategory.RECIPE_TYPE, current);
        visibleMineralRecipes = current;
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
    private static java.util.List<com.jdte.client.jei.bioextractor.BioExtractorJeiRecipe> visibleBioRecipes=java.util.List.of();
    public static void refreshBioExtractorRecipes(){
        if(runtime==null)return;
        var current=com.jdte.client.jei.bioextractor.BioExtractorJeiRecipe.getRecipes();var type=com.jdte.client.jei.bioextractor.BioExtractorRecipeCategory.RECIPE_TYPE;
        if(!visibleBioRecipes.isEmpty())runtime.getRecipeManager().hideRecipes(type,visibleBioRecipes);
        if(!current.isEmpty())runtime.getRecipeManager().addRecipes(type,current);visibleBioRecipes=current;
    }
}
