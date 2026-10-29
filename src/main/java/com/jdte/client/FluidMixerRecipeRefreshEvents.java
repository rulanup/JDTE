package com.jdte.client;

import com.jdte.JDTE;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=JDTE.MODID,value=Dist.CLIENT)
public final class FluidMixerRecipeRefreshEvents {
    private FluidMixerRecipeRefreshEvents() {}
    @SubscribeEvent public static void recipes(RecipesUpdatedEvent event) {
        if(ModList.get().isLoaded("jei")){
            com.jdte.common.jei.JDTEJeiPlugin.refreshFluidMixerRecipes();
            com.jdte.common.jei.JDTEJeiPlugin.refreshBioExtractorRecipes();
        }
    }
}
