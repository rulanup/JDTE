package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.containers.FactoryPackerContainer;
import com.jdte.common.network.data.FactoryPackerStartPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class FactoryPackerScreen extends BaseMachineScreen<FactoryPackerContainer> {
    private final FactoryPackerContainer packerContainer;
    private ToggleButton startButton;

    public FactoryPackerScreen(FactoryPackerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.packerContainer = menu;
    }

    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }

    @Override
    public void init() {
        super.init();
        startButton = addRenderableWidget(new ToggleButton(getGuiLeft() + 104, getGuiTop() + 18, 16, 16,
                List.of(new TextureLocalization(
                        ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/positionswap.png"),
                        Component.translatable("screen.jdte.factory_packer.start"))), 0,
                button -> PacketDistributor.sendToServer(new FactoryPackerStartPayload())));
        updateButton();
    }

    @Override public void addTickSpeedButton() {
        // Factory Packer throughput is controlled by bounded server budgets, not JDT's machine tick speed.
    }

    @Override
    public void addAreaButtons() {
        int firstNewRenderable = renderables.size();
        super.addAreaButtons();
        if (firstNewRenderable < renderables.size()
                && renderables.get(firstNewRenderable) instanceof GrayscaleButton renderAreaButton) {
            renderAreaButton.setY(renderAreaButton.getY() - 18);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButton();
    }

    private void updateButton() {
        if (startButton == null) return;
        FactoryPackerBE.Phase phase = FactoryPackerBE.Phase.values()[
                Math.floorMod(packerContainer.getPhase(), FactoryPackerBE.Phase.values().length)];
        startButton.active = phase == FactoryPackerBE.Phase.IDLE;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        graphics.blitSprite(net.minecraft.resources.ResourceLocation.withDefaultNamespace("container/slot"),
                getGuiLeft() + 79, getGuiTop() + 17, 18, 18);
        int statusX = getGuiLeft() + 89;
        int statusY = getGuiTop() + 40;
        FactoryPackerBE.Phase phase = FactoryPackerBE.Phase.values()[
                Math.floorMod(packerContainer.getPhase(), FactoryPackerBE.Phase.values().length)];
        graphics.drawCenteredString(font,
                Component.translatable("screen.jdte.factory_packer.phase." + phase.ordinal()),
                statusX, statusY, 0x606060);
        if (phase == FactoryPackerBE.Phase.IDLE && packerContainer.getErrorCode() > 0) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.jdte.factory_packer.failed", packerContainer.getErrorCode()),
                    statusX, statusY + 12, 0xB03030);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (packerContainer.getPhase() == FactoryPackerBE.Phase.IDLE.ordinal()
                && packerContainer.getErrorCode() > 0
                && mouseX >= getGuiLeft() + 59 && mouseX < getGuiLeft() + 119
                && mouseY >= getGuiTop() + 51 && mouseY < getGuiTop() + 65) {
            graphics.renderTooltip(font,
                    Component.translatable("screen.jdte.factory_packer.error." + packerContainer.getErrorCode()),
                    mouseX, mouseY);
        }
    }
}
