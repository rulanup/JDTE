package com.jdte.common.jei;

import com.jdte.JDTE;
import com.jdte.common.jei.gelgenerator.GelGeneratorJeiRecipe;
import com.jdte.common.jei.gelgenerator.GelGeneratorRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
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
        registration.addRecipeCategories(new GelGeneratorRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GelGeneratorRecipeCategory.RECIPE_TYPE, GelGeneratorJeiRecipe.getRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ItemStack machine : GelGeneratorJeiRecipe.getMachines()) {
            registration.addRecipeCatalyst(machine, GelGeneratorRecipeCategory.RECIPE_TYPE);
        }
    }
}
