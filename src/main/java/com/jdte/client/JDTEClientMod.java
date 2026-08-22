package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.renderers.AreaPreviewRenderBatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = JDTE.MODID, dist = Dist.CLIENT)
public class JDTEClientMod {
    public JDTEClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) ->
                new ConfigurationScreen(modContainer, parent, JDTEClientMod::lockServerConfigFieldsInWorld));
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(WrenchScrollHandler::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(FactoryPackageScrollHandler::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onInteraction);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(AreaPreviewRenderBatch::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseDragged);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseReleased);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onScreenOpening);
    }

    private static ConfigurationScreen.ConfigurationSectionScreen.Element lockServerConfigFieldsInWorld(
            ConfigurationScreen.ConfigurationSectionScreen.Context context,
            String key,
            ConfigurationScreen.ConfigurationSectionScreen.Element original) {
        if (original == null) {
            return null;
        }
        if (!TimeAcceleratorConfigScreenPolicy.shouldLockFields(context.modConfig().getType(), Minecraft.getInstance().level != null)) {
            return original;
        }
        AbstractWidget widget = original.getWidget(Minecraft.getInstance().options);
        widget.active = false;
        return new ConfigurationScreen.ConfigurationSectionScreen.Element(
                original.name(),
                original.tooltip(),
                widget,
                original.option(),
                original.undoable());
    }
}
