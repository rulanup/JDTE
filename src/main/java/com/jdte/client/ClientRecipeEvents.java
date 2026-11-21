package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.common.recipes.MachineRecipeHelper;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class ClientRecipeEvents {
    private ClientRecipeEvents() {}

    public static RecipeMap recipes() { return MachineRecipeHelper.clientRecipes(); }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        MachineRecipeHelper.setClientRecipes(event.getRecipeMap());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MachineRecipeHelper.setClientRecipes(RecipeMap.EMPTY);
    }
}
