package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.client.renderers.AreaPreviewRenderBatch;
import com.jdte.setup.JDTEConfig;
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
        NeoForge.EVENT_BUS.addListener(TimeMultitoolContinuousMiningEvents::onInteraction);
        NeoForge.EVENT_BUS.addListener(TimeMultitoolContinuousMiningEvents::onMouseButton);
        NeoForge.EVENT_BUS.addListener(TimeMultitoolContinuousMiningEvents::onKey);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(AreaPreviewRenderBatch::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(FactoryPackagePreviewClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(WrenchAreaSelectionClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(TimeMultitoolContinuousMiningEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseDragged);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onMouseReleased);
        NeoForge.EVENT_BUS.addListener(ScreenEventHandlers::onScreenOpening);
    }

    private static Screen createConfigurationScreen(ModContainer modContainer, Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean activeWorld = minecraft.level != null;
        ModConfig serverConfig = findLoadedConfig(modContainer.getModId(), JDTEConfig.SERVER_SPEC);
        ModConfig localConfig = findLoadedConfig(modContainer.getModId(), JDTEConfig.LOCAL_SPEC);
        ConfigurationScreen.ConfigurationSectionScreen.Filter filter = JDTEClientMod::lockRuntimeConfigFieldsInWorld;
        ConfigurationScreen standardParent = new ConfigurationScreen(modContainer, parent, filter);
        return switch (TimeAcceleratorConfigScreenPolicy.selectScreen(
                activeWorld, serverConfig != null, localConfig != null)) {
            case DIRECT_SERVER_SECTION -> new ConfigurationScreen.ConfigurationSectionScreen(
                    standardParent,
                    ModConfig.Type.SERVER,
                    serverConfig,
                    configTitle(modContainer, serverConfig, "neoforge.configuration.uitext.title.server"),
                    filter);
            case DIRECT_LOCAL_DEFAULTS_SECTION -> new ConfigurationScreen.ConfigurationSectionScreen(
                    standardParent,
                    ModConfig.Type.CLIENT,
                    localConfig,
                    configTitle(modContainer, localConfig, "neoforge.configuration.uitext.title.client"),
                    filter);
            case STANDARD_CONFIGURATION_SCREEN -> standardParent;
        };
    }

    private static ModConfig findLoadedConfig(String modId, ModConfigSpec expectedSpec) {
        for (ModConfig modConfig : ModConfigs.getModConfigs(modId)) {
            if (modConfig.getSpec() == expectedSpec && expectedSpec.isLoaded()) {
                return modConfig;
            }
        }
        return null;
    }

    private static Component configTitle(ModContainer modContainer, ModConfig modConfig, String fallbackKey) {
        String configKey = modConfig.getFileName()
                .replaceAll("[^a-zA-Z0-9]+", ".")
                .replaceFirst("^\\.", "")
                .replaceFirst("\\.$", "")
                .toLowerCase(Locale.ENGLISH);
        String titleKey = modContainer.getModId() + ".configuration.section." + configKey + ".title";
        return Component.translatable(I18n.exists(titleKey) ? titleKey : fallbackKey,
                modContainer.getModInfo().getDisplayName());
    }

    private static ConfigurationScreen.ConfigurationSectionScreen.Element lockRuntimeConfigFieldsInWorld(
            ConfigurationScreen.ConfigurationSectionScreen.Context context,
            String key,
            ConfigurationScreen.ConfigurationSectionScreen.Element original) {
        if (original == null) {
            return null;
        }
        boolean localDefaultsConfig = context.modConfig().getSpec() == JDTEConfig.LOCAL_SPEC;
        if (!TimeAcceleratorConfigScreenPolicy.shouldLockFields(
                context.modConfig().getType(), localDefaultsConfig, Minecraft.getInstance().level != null)) {
            return original;
        }
        AbstractWidget widget = original.getWidget(Minecraft.getInstance().options);
        return makeReadOnly(original, widget);
    }

    static ConfigurationScreen.ConfigurationSectionScreen.Element makeReadOnly(
            ConfigurationScreen.ConfigurationSectionScreen.Element original,
            AbstractWidget widget) {
        widget.active = false;
        return new ConfigurationScreen.ConfigurationSectionScreen.Element(
                original.name(),
                original.tooltip(),
                widget,
                original.option(),
                false);
    }
}
