package com.jdte.client.screens;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.containers.RangeBlockerContainer;
import com.jdte.common.network.data.RangeBlockerPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class RangeBlockerScreen extends BaseMachineScreen<RangeBlockerContainer> {
    private static final String JDT = "justdirethings";
    private static final List<TextureLocalization> MODE_TEXTURES = List.of(
            texture("textures/item/abilityupgrades/orescanner.png", "jdte.screen.range_blocker.mode.0"),
            texture("textures/item/abilityupgrades/earthquake.png", "jdte.screen.range_blocker.mode.1"),
            texture("textures/item/abilityupgrades/mindfog.png", "jdte.screen.range_blocker.mode.2"));
    private static final List<TextureLocalization> LIST_TEXTURES = List.of(
            texture("textures/gui/buttons/allowlistfalse.png", "jdte.screen.range_blocker.blacklist"),
            texture("textures/gui/buttons/allowlisttrue.png", "jdte.screen.range_blocker.allowlist"));
    private static final List<TextureLocalization> TARGET_TEXTURES = List.of(
            texture("textures/gui/buttons/mobscanner.png", "jdte.screen.range_blocker.target.0"),
            texture("textures/gui/buttons/passivemob.png", "jdte.screen.range_blocker.target.1"),
            texture("textures/gui/buttons/entity-all.png", "jdte.screen.range_blocker.target.2"),
            texture("textures/gui/buttons/filter-item.png", "jdte.screen.range_blocker.target.3"),
            texture("textures/gui/buttons/item.png", "jdte.screen.range_blocker.target.4"),
            texture("textures/gui/buttons/target-both.png", "jdte.screen.range_blocker.target.5"));

    private int mode;
    private int target;
    private boolean blacklist;
    private ToggleButton targetButton;
    private ToggleButton listButton;

    public RangeBlockerScreen(RangeBlockerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (baseMachineBE instanceof RangeBlockerBE blocker) {
            mode = blocker.getMode().ordinal();
            target = blocker.getTarget().ordinal();
            blacklist = blocker.isBlacklist();
        }
    }

    @Override
    public void addFilterButtons() {
        listButton = addRenderableWidget(new GrayToggleButton(
                leftPos + 8, topSectionTop + 62, LIST_TEXTURES, blacklist ? 0 : 1, button -> {
                    blacklist = !blacklist;
                    saveSettings();
                }));
        targetButton = addRenderableWidget(new GrayToggleButton(
                leftPos + 26, topSectionTop + 62, TARGET_TEXTURES, target, button -> {
                    target = ((ToggleButton) button).getTexturePosition();
                    saveSettings();
                }));
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new ToggleButton(leftPos + 80, topSectionTop + 62, 16, 16,
                MODE_TEXTURES, mode, button -> {
                    mode = ((ToggleButton) button).getTexturePosition();
                    updateFilterButtons();
                    saveSettings();
                }));
        updateFilterButtons();
    }

    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }

    private static TextureLocalization texture(String path, String translation) {
        return new TextureLocalization(Identifier.fromNamespaceAndPath(JDT, path),
                Component.translatable(translation));
    }

    private void updateFilterButtons() {
        RangeBlockerBE.Mode selectedMode = RangeBlockerBE.Mode.values()[
                Math.floorMod(mode, RangeBlockerBE.Mode.values().length)];
        if (targetButton != null) targetButton.active = selectedMode == RangeBlockerBE.Mode.CONTAINMENT;
        if (listButton != null) listButton.active = selectedMode != RangeBlockerBE.Mode.SILENCE;
    }

    private static final class GrayToggleButton extends ToggleButton {
        private final List<TextureLocalization> textures;

        private GrayToggleButton(int x, int y, List<TextureLocalization> textures,
                                 int position, Button.OnPress onPress) {
            super(x, y, 16, 16, textures, position, onPress);
            this.textures = textures;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (active) {
                super.extractContents(graphics, mouseX, mouseY, partialTick);
                return;
            }
            Identifier texture = textures.get(getTexturePosition()).texture();
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 0, 0, width, height, width, height, 0xA6616161);
        }
    }

    @Override public void saveSettings() {
        super.saveSettings();
        if (baseMachineBE instanceof RangeBlockerBE blocker) {
            blocker.applyClientSettings(mode, target, blacklist);
        }
        ClientPacketDistributor.sendToServer(new RangeBlockerPayload(mode, target, blacklist));
    }
}
