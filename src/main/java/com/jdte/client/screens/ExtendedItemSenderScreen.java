package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtons;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtonsDouble;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.containers.ExtendedItemSenderContainer;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedItemSenderScreen extends BaseMachineScreen<ExtendedItemSenderContainer> {
    private static final Component ITEM_SLOT_TOOLTIP = Component.translatable("jdte.slot.item_storage");

    public ExtendedItemSenderScreen(ExtendedItemSenderContainer container, Inventory inv, Component name) {
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
                leftPos + config.getItemSenderAllowlistX(),
                topSectionTop + config.getItemSenderAllowlistY(),
                filterData.allowlist, b -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.COMPARENBTBUTTON(
                leftPos + config.getItemSenderCompareNBTX(),
                topSectionTop + config.getItemSenderCompareNBTY(),
                filterData.compareNBT, b -> {
                    filterData.compareNBT = !filterData.compareNBT;
                    ((GrayscaleButton) b).toggleActive();
                    saveSettings();
                }));
        if (filterData.blockItemFilter != -1) {
            addRenderableWidget(ToggleButtonFactory.FILTERBLOCKITEMBUTTON(
                    leftPos + config.getItemSenderCompareNBTX(),
                    topSectionTop + config.getItemSenderCompareNBTY() + 18,
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
                leftPos + config.getItemSenderRedstoneX(),
                topSectionTop + config.getItemSenderRedstoneY(),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addAreaButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.RENDERAREABUTTON(
                leftPos + config.getItemSenderRenderAreaX(),
                topSectionTop + config.getItemSenderRenderAreaY(),
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
                leftPos + config.getItemSenderSpeedButtonX(),
                topSectionTop + config.getItemSenderSpeedButtonY(), tickSpeed, b -> {
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
