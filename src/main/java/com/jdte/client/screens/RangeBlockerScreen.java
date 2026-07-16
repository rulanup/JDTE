package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.mojang.blaze3d.systems.RenderSystem;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.containers.RangeBlockerContainer;
import com.jdte.common.network.data.RangeBlockerPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
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
    private static final List<TextureLocalization> TARGET_TEXTURES = List.of(
            texture("textures/gui/buttons/mobscanner.png", "screen.jdte.range_blocker.target.0"),
            texture("textures/gui/buttons/passivemob.png", "screen.jdte.range_blocker.target.1"),
            texture("textures/gui/buttons/entity-all.png", "screen.jdte.range_blocker.target.2"),
            texture("textures/gui/buttons/filter-item.png", "screen.jdte.range_blocker.target.3"),
            texture("textures/gui/buttons/item.png", "screen.jdte.range_blocker.target.4"),
            texture("textures/gui/buttons/target-both.png", "screen.jdte.range_blocker.target.5"));

    private int mode;
    private int target;
    private boolean blacklist;
    private ToggleButton targetButton;

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
        addRenderableWidget(new ToggleButton(leftPos + 8, topSectionTop + 62, 16, 16,
                LIST_TEXTURES, blacklist ? 0 : 1, button -> {
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
                    updateTargetButton();
                    saveSettings();
                }));
        updateTargetButton();
    }

    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }

    private static TextureLocalization texture(String path, String translation) {
        return new TextureLocalization(ResourceLocation.fromNamespaceAndPath(JDT, path),
                Component.translatable(translation));
    }

    private void updateTargetButton() {
        if (targetButton != null) {
            targetButton.active = RangeBlockerBE.Mode.values()[
                    Math.floorMod(mode, RangeBlockerBE.Mode.values().length)]
                    == RangeBlockerBE.Mode.CONTAINMENT;
        }
    }

    private static final class GrayToggleButton extends ToggleButton {
        private final List<TextureLocalization> textures;

        private GrayToggleButton(int x, int y, List<TextureLocalization> textures,
                                 int position, Button.OnPress onPress) {
            super(x, y, 16, 16, textures, position, onPress);
            this.textures = textures;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (active) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                return;
            }
            ResourceLocation texture = textures.get(getTexturePosition()).texture();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(0.38F, 0.38F, 0.38F, 0.65F);
            graphics.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override public void saveSettings() {
        super.saveSettings();
        if (baseMachineBE instanceof RangeBlockerBE blocker) {
            blocker.applyClientSettings(mode, target, blacklist);
        }
        PacketDistributor.sendToServer(new RangeBlockerPayload(mode, target, blacklist));
    }
}
