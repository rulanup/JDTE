package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtons;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ValueButtonsDouble;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.jdte.common.containers.ItemSenderContainerBase;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public abstract class ItemSenderScreenBase<T extends ItemSenderContainerBase> extends BaseMachineScreen<T> {
    private static final Component ITEM_SLOT_TOOLTIP = Component.translatable("jdte.slot.item_storage");

    protected ItemSenderScreenBase(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    protected abstract boolean useBasicLayout();

    private int allowlistX(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderAllowlistX() : config.getItemSenderAllowlistX();
    }

    private int allowlistY(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderAllowlistY() : config.getItemSenderAllowlistY();
    }

    private int compareNBTX(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderCompareNBTX() : config.getItemSenderCompareNBTX();
    }

    private int compareNBTY(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderCompareNBTY() : config.getItemSenderCompareNBTY();
    }

    private int redstoneX(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderRedstoneX() : config.getItemSenderRedstoneX();
    }

    private int redstoneY(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderRedstoneY() : config.getItemSenderRedstoneY();
    }

    private int renderAreaX(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderRenderAreaX() : config.getItemSenderRenderAreaX();
    }

    private int renderAreaY(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderRenderAreaY() : config.getItemSenderRenderAreaY();
    }

    private int speedButtonX(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderSpeedButtonX() : config.getItemSenderSpeedButtonX();
    }

    private int speedButtonY(GuiUpgradeLayoutConfig config) {
        return useBasicLayout() ? config.getBasicItemSenderSpeedButtonY() : config.getItemSenderSpeedButtonY();
    }

    @Override
    public void addFilterButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                leftPos + allowlistX(config),
                topSectionTop + allowlistY(config),
                filterData.allowlist, b -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.COMPARENBTBUTTON(
                leftPos + compareNBTX(config),
                topSectionTop + compareNBTY(config),
                filterData.compareNBT, b -> {
                    filterData.compareNBT = !filterData.compareNBT;
                    ((GrayscaleButton) b).toggleActive();
                    saveSettings();
                }));
        if (filterData.blockItemFilter != -1) {
            addRenderableWidget(ToggleButtonFactory.FILTERBLOCKITEMBUTTON(
                    leftPos + compareNBTX(config),
                    topSectionTop + compareNBTY(config) + 18,
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
                leftPos + redstoneX(config),
                topSectionTop + redstoneY(config),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addAreaButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.RENDERAREABUTTON(
                leftPos + renderAreaX(config),
                topSectionTop + renderAreaY(config),
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
                leftPos + speedButtonX(config),
                topSectionTop + speedButtonY(config), tickSpeed, b -> {
            tickSpeed = ((com.direwolf20.justdirethings.client.screens.widgets.NumberButton) b).getValue();
            com.direwolf20.justdirethings.common.network.PacketHandler.CHANNEL.sendToServer(new TickSpeedPayload(tickSpeed));
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
