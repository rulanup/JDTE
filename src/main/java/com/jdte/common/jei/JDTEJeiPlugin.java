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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JDTEJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "jei_plugin");

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
        registration.addRecipes(InfusionRecipeCategory.RECIPE_TYPE, InfusionJeiRecipe.getRecipes());
        registration.addRecipes(PotionBrewerRecipeCategory.RECIPE_TYPE, PotionBrewerJeiRecipe.getRecipes());
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
}
