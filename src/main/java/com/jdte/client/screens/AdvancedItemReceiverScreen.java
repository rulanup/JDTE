package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtons;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtonsDouble;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.containers.AdvancedItemReceiverContainer;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancedItemReceiverScreen extends BaseMachineScreen<AdvancedItemReceiverContainer> {
    private static final Component ITEM_SLOT_TOOLTIP = Component.translatable("jdte.slot.item_storage");

    public AdvancedItemReceiverScreen(AdvancedItemReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public void addFilterButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                leftPos + config.getItemReceiverAllowlistX(),
                topSectionTop + config.getItemReceiverAllowlistY(),
                filterData.allowlist, b -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.COMPARENBTBUTTON(
                leftPos + config.getItemReceiverCompareNBTX(),
                topSectionTop + config.getItemReceiverCompareNBTY(),
                filterData.compareNBT, b -> {
                    filterData.compareNBT = !filterData.compareNBT;
                    ((GrayscaleButton) b).toggleActive();
                    saveSettings();
                }));
        if (filterData.blockItemFilter != -1) {
            addRenderableWidget(ToggleButtonFactory.FILTERBLOCKITEMBUTTON(
                    leftPos + config.getItemReceiverCompareNBTX(),
                    topSectionTop + config.getItemReceiverCompareNBTY() + 18,
                    filterData.blockItemFilter, b -> {
                        filterData.blockItemFilter = ((ToggleButton) b).getTexturePosition();
                        saveSettings();
                    }));
        }
    }

    @Override
    public void addRedstoneButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                leftPos + config.getItemReceiverRedstoneX(),
                topSectionTop + config.getItemReceiverRedstoneY(),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addAreaButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.RENDERAREABUTTON(
                leftPos + config.getItemReceiverRenderAreaX(),
                topSectionTop + config.getItemReceiverRenderAreaY(),
                renderArea, b -> {
                    renderArea = !renderArea;
                    ((GrayscaleButton) b).toggleActive();
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
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(
                leftPos + config.getItemReceiverSpeedButtonX(),
                topSectionTop + config.getItemReceiverSpeedButtonY(), tickSpeed, b -> {
            tickSpeed = ((com.direwolf20.justdirethings.client.screens.widgets.NumberButton) b).getValue();
            PacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
        }));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index < 9) {
            guiGraphics.renderTooltip(font, ITEM_SLOT_TOOLTIP, mouseX, mouseY);
        }
    }
}
