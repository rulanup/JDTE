package com.jdte.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorConfigScreenPolicyTest {

    @Test
    void mainMenuRoutesDirectlyToLoadedLocalDefaults() {
        TimeAcceleratorConfigScreenPolicy.ScreenRoute route =
                TimeAcceleratorConfigScreenPolicy.selectScreen(false, false, true);

        assertEquals(TimeAcceleratorConfigScreenPolicy.ScreenRoute.DIRECT_LOCAL_DEFAULTS_SECTION, route);
        assertTrue(route.usesStandardConfigurationParent());
    }

    @Test
    void everyActiveWorldRoutesDirectlyToLoadedServerValues() {
        TimeAcceleratorConfigScreenPolicy.ScreenRoute singleplayerOrLan =
                TimeAcceleratorConfigScreenPolicy.selectScreen(true, true, true);
        TimeAcceleratorConfigScreenPolicy.ScreenRoute multiplayer =
                TimeAcceleratorConfigScreenPolicy.selectScreen(true, true, false);

        assertEquals(TimeAcceleratorConfigScreenPolicy.ScreenRoute.DIRECT_SERVER_SECTION, singleplayerOrLan);
        assertEquals(TimeAcceleratorConfigScreenPolicy.ScreenRoute.DIRECT_SERVER_SECTION, multiplayer);
        assertTrue(singleplayerOrLan.usesStandardConfigurationParent());
    }

    @Test
    void unavailableDirectConfigFallsBackToStandardScreen() {
        assertEquals(TimeAcceleratorConfigScreenPolicy.ScreenRoute.STANDARD_CONFIGURATION_SCREEN,
                TimeAcceleratorConfigScreenPolicy.selectScreen(false, false, false));
        assertEquals(TimeAcceleratorConfigScreenPolicy.ScreenRoute.STANDARD_CONFIGURATION_SCREEN,
                TimeAcceleratorConfigScreenPolicy.selectScreen(true, false, true));
    }

    @Test
    void serverAndLocalProxyFieldsLockOnlyInAnActiveWorld() {
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.SERVER, false, false));
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.CLIENT, true, false));
        assertTrue(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.SERVER, false, true));
        assertTrue(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.CLIENT, true, true));
    }

    @Test
    void unrelatedConfigsRemainEditableEvenWhenAWorldIsActive() {
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.COMMON, false, true));
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.CLIENT, false, true));
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.STARTUP, false, true));
    }

    @Test
    void readOnlyElementCannotParticipateInWidgetOrResetUndoWrites() {
        Button widget = Button.builder(Component.literal("value"), ignored -> {}).build();
        ConfigurationScreen.ConfigurationSectionScreen.Element original =
                new ConfigurationScreen.ConfigurationSectionScreen.Element(
                        Component.literal("name"), Component.literal("tooltip"), widget, null, true);

        ConfigurationScreen.ConfigurationSectionScreen.Element readOnly =
                JDTEClientMod.makeReadOnly(original, widget);

        assertFalse(widget.active);
        assertFalse(readOnly.undoable());
        assertSame(widget, readOnly.widget());
    }
}
