package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.containers.RangeBlockerContainer;
import com.jdte.common.network.data.RangeBlockerPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class RangeBlockerScreen extends BaseMachineScreen<RangeBlockerContainer> {
    private static final String JDT = "justdirethings";
    private static final List<TextureLocalization> MODE_TEXTURES = List.of(
            texture("textures/item/abilityupgrades/orescanner.png", "screen.jdte.range_blocker.mode.0"),
            texture("textures/item/abilityupgrades/earthquake.png", "screen.jdte.range_blocker.mode.1"));
    private static final List<TextureLocalization> LIST_TEXTURES = List.of(
            texture("textures/gui/buttons/allowlistfalse.png", "screen.jdte.range_blocker.blacklist"),
            texture("textures/gui/buttons/allowlisttrue.png", "screen.jdte.range_blocker.allowlist"));

    private int mode;
    private boolean blacklist;

    public RangeBlockerScreen(RangeBlockerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (baseMachineBE instanceof RangeBlockerBE blocker) {
            mode = blocker.getMode().ordinal();
            blacklist = blocker.isBlacklist();
        }
    }

    @Override
    public void addFilterButtons() {
        addRenderableWidget(new ToggleButton(leftPos + 8, topSectionTop + 62, 16, 16,
                LIST_TEXTURES, blacklist ? 0 : 1, button -> {
                    blacklist = !blacklist;
                    saveSettings();
                }));
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new ToggleButton(leftPos + 80, topSectionTop + 62, 16, 16,
                MODE_TEXTURES, mode, button -> {
                    mode = ((ToggleButton) button).getTexturePosition();
                    saveSettings();
                }));
    }

    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }

    private static TextureLocalization texture(String path, String translation) {
        return new TextureLocalization(ResourceLocation.fromNamespaceAndPath(JDT, path),
                Component.translatable(translation));
    }

    @Override public void saveSettings() {
        super.saveSettings();
        if (baseMachineBE instanceof RangeBlockerBE blocker) {
            blocker.applyClientSettings(mode, blacklist);
        }
        PacketDistributor.sendToServer(new RangeBlockerPayload(mode, blacklist));
    }
}
