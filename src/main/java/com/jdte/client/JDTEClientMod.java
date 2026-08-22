package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.renderers.AreaPreviewRenderBatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Locale;

@Mod(value = JDTE.MODID, dist = Dist.CLIENT)
public class JDTEClientMod {
    public JDTEClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, JDTEClientMod::createConfigurationScreen);
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

    private static Screen createConfigurationScreen(ModContainer modContainer, Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean multiplayerConnected = minecraft.getCurrentServer() != null && !minecraft.isSingleplayer();
        ModConfig serverConfig = findLoadedServerConfig(modContainer.getModId());
        if (TimeAcceleratorConfigScreenPolicy.selectScreen(multiplayerConnected, serverConfig != null)
                == TimeAcceleratorConfigScreenPolicy.ScreenRoute.DIRECT_SERVER_SECTION) {
            return new ConfigurationScreen.ConfigurationSectionScreen(
                    parent,
                    ModConfig.Type.SERVER,
                    serverConfig,
                    serverConfigTitle(modContainer, serverConfig),
                    JDTEClientMod::lockServerConfigFieldsInWorld);
        }
        return new ConfigurationScreen(modContainer, parent, JDTEClientMod::lockServerConfigFieldsInWorld);
    }

    private static ModConfig findLoadedServerConfig(String modId) {
        for (ModConfig modConfig : ModConfigs.getModConfigs(modId)) {
            if (modConfig.getType() == ModConfig.Type.SERVER
                    && modConfig.getSpec() instanceof ModConfigSpec spec
                    && spec.isLoaded()) {
                return modConfig;
            }
        }
        return null;
    }

    private static Component serverConfigTitle(ModContainer modContainer, ModConfig modConfig) {
        String configKey = modConfig.getFileName()
                .replaceAll("[^a-zA-Z0-9]+", ".")
                .replaceFirst("^\\.", "")
                .replaceFirst("\\.$", "")
                .toLowerCase(Locale.ENGLISH);
        String titleKey = modContainer.getModId() + ".configuration.section." + configKey + ".title";
        String fallbackKey = "neoforge.configuration.uitext.title.server";
        return Component.translatable(I18n.exists(titleKey) ? titleKey : fallbackKey,
                modContainer.getModInfo().getDisplayName());
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
