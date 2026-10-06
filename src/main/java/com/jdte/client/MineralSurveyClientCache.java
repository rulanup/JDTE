package com.jdte.client;

import com.jdte.client.jei.JDTEJeiPlugin;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralSurveyIndex;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class MineralSurveyClientCache {
    private static List<MineralSurveyData> surveys = List.of();
    private static boolean synced;

    private MineralSurveyClientCache() { }

    public static void set(List<MineralSurveyData> syncedSurveys) {
        List<MineralSurveyData> updated = List.copyOf(syncedSurveys);
        if (synced && surveys.equals(updated)) return;
        surveys = updated;
        synced = true;
        JDTEJeiPlugin.refreshMineralExtractorRecipes();
    }

    public static List<MineralSurveyData> get() {
        if (!synced) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
                    return MineralSurveyIndex.surveys(mc.getSingleplayerServer());
                }
            } catch (Throwable ignored) {
            }
        }
        return surveys;
    }

    public static boolean isSynced() {
        if (synced) return true;
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void reset() {
        surveys = List.of();
        synced = false;
    }
}
