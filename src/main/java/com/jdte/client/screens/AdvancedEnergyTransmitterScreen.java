package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtons;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtonsDouble;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.containers.AdvancedEnergyTransmitterContainer;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.common.network.data.AdvancedEnergyTransmitterPayload;
import com.jdte.common.network.data.AdvancedEnergyTransmitterBindingPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancedEnergyTransmitterScreen extends BaseMachineScreen<AdvancedEnergyTransmitterContainer> {
    private static final ResourceLocation BIND_PLAYER_ICON = ResourceLocation.fromNamespaceAndPath(
            "justdirethings", "textures/gui/buttons/player.png");

    private boolean showParticles;
    private boolean boundButtonState;
    private GrayscaleButton boundPlayerButton;

    public AdvancedEnergyTransmitterScreen(AdvancedEnergyTransmitterContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        showParticles = container.isShowingParticles();
        boundButtonState = container.hasBoundPlayer();
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(ToggleButtonFactory.SHOWPARTICLESBUTTON(
                leftPos + 116, topSectionTop + 62, showParticles, button -> {
                    showParticles = !showParticles;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));
    }

    @Override
    public void addFilterButtons() {
        super.addFilterButtons();
        boundPlayerButton = new GrayscaleButton(
                leftPos + 44, topSectionTop + 62, 16, 16,
                BIND_PLAYER_ICON,
                Component.translatable("jdte.screen.energy_transmitter.bind_player"),
                boundButtonState, button -> {
                    if (baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter) {
                        PacketDistributor.sendToServer(new AdvancedEnergyTransmitterBindingPayload(
                                transmitter.getBlockPos()));
                    }
                });
        addRenderableWidget(boundPlayerButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean bound = menu.hasBoundPlayer();
        if (boundPlayerButton != null && bound != boundButtonState) {
            boundButtonState = bound;
            boundPlayerButton.toggleActive();
        }
    }

    @Override
    public void addRedstoneButtons() {
        super.addRedstoneButtons();
    }

    @Override
    public void addAreaButtons() {
        addRenderableWidget(ToggleButtonFactory.RENDERAREABUTTON(
                leftPos + 152,
                topSectionTop + 62,
                renderArea, button -> {
                    renderArea = !renderArea;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));

        double maxRadius = UpgradeHelper.getMaxAreaRadius(baseMachineBE);
        int maxOffset = UpgradeHelper.getMaxAreaOffset(baseMachineBE);
        valueButtonsDoubleList.add(new ValueButtonsDouble(leftPos + 25, topSectionTop + 12, xRadius, 0, maxRadius, font, (button, value) -> {
            xRadius = value;
            saveSettings();
        }));
        valueButtonsDoubleList.add(new ValueButtonsDouble(leftPos + 75, topSectionTop + 12, yRadius, 0, maxRadius, font, (button, value) -> {
            yRadius = value;
            saveSettings();
        }));
        valueButtonsDoubleList.add(new ValueButtonsDouble(leftPos + 125, topSectionTop + 12, zRadius, 0, maxRadius, font, (button, value) -> {
            zRadius = value;
            saveSettings();
        }));

        valueButtonsList.add(new ValueButtons(leftPos + 25, topSectionTop + 27, xOffset, -maxOffset, maxOffset, font, (button, value) -> {
            xOffset = value;
            saveSettings();
        }));
        valueButtonsList.add(new ValueButtons(leftPos + 75, topSectionTop + 27, yOffset, -maxOffset, maxOffset, font, (button, value) -> {
            yOffset = value;
            saveSettings();
        }));
        valueButtonsList.add(new ValueButtons(leftPos + 125, topSectionTop + 27, zOffset, -maxOffset, maxOffset, font, (button, value) -> {
            zOffset = value;
            saveSettings();
        }));

        valueButtonsList.forEach(valueButtons -> valueButtons.widgetList.forEach(this::addRenderableWidget));
        valueButtonsDoubleList.forEach(valueButtons -> valueButtons.widgetList.forEach(this::addRenderableWidget));
    }

    @Override
    public void addTickSpeedButton() {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        AdvancedEnergyTransmitterEnergySource.Status status = menu.getEnergyNetworkStatus();
        String translation = switch (status) {
            case ONLINE -> "jdte.screen.energy_transmitter.me_online_direct";
            case OFFLINE -> "jdte.screen.energy_transmitter.me_offline";
            case UNAVAILABLE -> "jdte.screen.energy_transmitter.me_unavailable";
        };
        int color = switch (status) {
            case ONLINE -> 0x2E7D32;
            case OFFLINE -> 0xB03030;
            case UNAVAILABLE -> 0x606060;
        };
        Component networkText = Component.translatable(translation);
        int centerX = leftPos + imageWidth / 2;
        renderScaledCenteredText(graphics, networkText, centerX, topSectionTop + 42, color);

        Component playerText;
        int playerColor;
        if (!menu.hasBoundPlayer()) {
            playerText = Component.translatable("jdte.screen.energy_transmitter.player_unbound");
            playerColor = 0x606060;
        } else if (menu.isBoundPlayerOnline()) {
            playerText = Component.translatable("jdte.screen.energy_transmitter.player_online",
                    menu.getBoundPlayerName());
            playerColor = 0x2E7D32;
        } else {
            playerText = Component.translatable("jdte.screen.energy_transmitter.player_offline",
                    menu.getBoundPlayerName());
            playerColor = 0xB03030;
        }
        renderScaledCenteredText(graphics, playerText, centerX, topSectionTop + 51, playerColor);
    }

    private void renderScaledCenteredText(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        float scale = 7.0F / 9.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawCenteredString(font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    @Override
    public void saveSettings() {
        super.saveSettings();
        if (baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter) {
            PacketDistributor.sendToServer(new AdvancedEnergyTransmitterPayload(
                    transmitter.getBlockPos(), showParticles));
        }
    }
}