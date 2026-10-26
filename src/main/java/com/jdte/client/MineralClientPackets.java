package com.jdte.client;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.client.screens.MineralSurveyScreen;
import net.minecraft.client.Minecraft;
public final class MineralClientPackets {
    private MineralClientPackets() { }
    public static void slots(com.jdte.common.network.MineralPackets.Slots msg) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof com.jdte.common.containers.MineralExtractorContainer menu
                && menu.containerId == msg.containerId()) menu.applyOutputSnapshot(msg.page(), msg.stacks());
    }
    public static void open(MineralSurveyData data) { Minecraft.getInstance().setScreen(new MineralSurveyScreen(data)); }
}
