package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.containers.EntitySuppressorContainer;
import com.jdte.common.network.data.EntitySuppressorPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class EntitySuppressorScreen extends BaseMachineScreen<EntitySuppressorContainer> {
    private static final String JDT = "justdirethings";
    private static final List<TextureLocalization> MODE_TEXTURES = List.of(
            texture("textures/item/abilityupgrades/deathprotection.png", "screen.jdte.entity_suppressor.mode.0"),
            texture("textures/item/abilityupgrades/noai.png", "screen.jdte.entity_suppressor.mode.1"),
            texture("textures/item/abilityupgrades/waterbreathing.png", "screen.jdte.entity_suppressor.mode.2"));
    private static final List<TextureLocalization> TARGET_TEXTURES = List.of(
            texture("textures/gui/buttons/mobscanner.png", "screen.jdte.entity_suppressor.target.0"),
            texture("textures/gui/buttons/passivemob.png", "screen.jdte.entity_suppressor.target.1"),
            texture("textures/gui/buttons/entity-all.png", "screen.jdte.entity_suppressor.target.2"),
            texture("textures/gui/buttons/filter-item.png", "screen.jdte.entity_suppressor.target.3"),
            texture("textures/gui/buttons/item.png", "screen.jdte.entity_suppressor.target.4"),
            texture("textures/gui/buttons/target-both.png", "screen.jdte.entity_suppressor.target.5"));
    private static final List<TextureLocalization> LIST_TEXTURES = List.of(
            texture("textures/gui/buttons/allowlistfalse.png", "screen.jdte.entity_suppressor.blacklist"),
            texture("textures/gui/buttons/allowlisttrue.png", "screen.jdte.entity_suppressor.allowlist"));

    private int mode;
    private int target;
    private boolean blacklist;
    private ToggleButton modeButton;
    private ToggleButton targetButton;
    private ToggleButton listButton;

    public EntitySuppressorScreen(EntitySuppressorContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (baseMachineBE instanceof EntitySuppressorBE suppressor) {
            mode = suppressor.getMode().ordinal();
            target = suppressor.getTarget().ordinal();
            blacklist = suppressor.isBlacklist();
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
        modeButton = addRenderableWidget(new ToggleButton(
                leftPos + 80, topSectionTop + 62, 16, 16, MODE_TEXTURES, mode, button -> {
                    mode = ((ToggleButton) button).getTexturePosition();
                    updateEntityButtons();
                    saveSettings();
                }));
        updateEntityButtons();
    }

    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }

    private void updateEntityButtons() {
        boolean entityMode = mode != EntitySuppressorBE.Mode.DISABLE_PARTICLES.ordinal();
        if (targetButton != null) targetButton.active = entityMode;
        if (listButton != null) listButton.active = entityMode;
    }

    private static TextureLocalization texture(String path, String translation) {
        return new TextureLocalization(ResourceLocation.fromNamespaceAndPath(JDT, path), Component.translatable(translation));
    }

    private static final class GrayToggleButton extends ToggleButton {
        private final List<TextureLocalization> textures;

        private GrayToggleButton(int x, int y, List<TextureLocalization> textures, int position, Button.OnPress onPress) {
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
        if (baseMachineBE instanceof EntitySuppressorBE suppressor) {
            suppressor.applyClientSettings(mode, target, blacklist);
        }
        PacketDistributor.sendToServer(new EntitySuppressorPayload(mode, target, blacklist));
    }
}
