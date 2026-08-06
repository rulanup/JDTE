package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.renderers.AreaPreviewRenderBatch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;

public class JDTEClientMod {
    public JDTEClientMod() {
        MinecraftForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onMouseScroll);
        MinecraftForge.EVENT_BUS.addListener(WrenchScrollHandler::onMouseScroll);
        MinecraftForge.EVENT_BUS.addListener(FactoryPackageScrollHandler::onMouseScroll);
        MinecraftForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onInteraction);
        MinecraftForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderLevel);
        MinecraftForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderLevel);
        MinecraftForge.EVENT_BUS.addListener(AreaPreviewRenderBatch::onRenderLevel);
        MinecraftForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderGui);
        MinecraftForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderGui);
        MinecraftForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseDragged);
        MinecraftForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseReleased);
        MinecraftForge.EVENT_BUS.addListener(ScreenEventHandlers::onScreenOpening);
    }
}
