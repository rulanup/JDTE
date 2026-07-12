package com.jdte.client;

import com.jdte.JDTE;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = JDTE.MODID, dist = Dist.CLIENT)
public class JDTEClientMod {
    public JDTEClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(WrenchScrollHandler::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onInteraction);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onClientTick);
    }
}
