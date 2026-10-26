package com.jdte.client;

import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.jei.JDTEJeiPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "jdte", value = Dist.CLIENT)
public final class MineralSurveyClientCache {
    private static final List<MineralSurveyData> surveys = new ArrayList<>();
    private static boolean synced;
    private MineralSurveyClientCache() { }
    public static void accept(boolean reset, boolean complete, List<MineralSurveyData> chunk) {
        if (reset) { surveys.clear(); synced = false; }
        surveys.addAll(chunk);
        if (complete) { synced = true; refreshJei(); }
    }
    private static void refreshJei() {
        if (net.minecraftforge.fml.ModList.get().isLoaded("jei")) JDTEJeiPlugin.refreshMineralExtractorRecipes();
    }
    @SubscribeEvent public static void recipesUpdated(net.minecraftforge.client.event.RecipesUpdatedEvent event) {
        if (synced) refreshJei();
    }
    public static List<MineralSurveyData> get() { return List.copyOf(surveys); }
    public static boolean isSynced() { return synced; }
    @SubscribeEvent public static void disconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        surveys.clear(); synced = false;
    }
}
