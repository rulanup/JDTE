package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.containers.GreenhouseContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public class GreenhouseScreen extends BaseMachineScreen<GreenhouseContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final Component SEED_TOOLTIP = Component.translatable("jdte.slot.greenhouse_seed");
    private static final Component OUTPUT_TOOLTIP = Component.translatable("jdte.slot.greenhouse_output");
    private final GreenhouseContainer greenhouseContainer;
    private NumberButton multiplierButton;

    public GreenhouseScreen(GreenhouseContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        greenhouseContainer = container;
    }

    @Override
    public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderEnergyBar(graphics);
        renderMachineSlotBackgrounds(graphics);
        renderProgressArrow(graphics);
        renderOutputPage(graphics);
    }

    private void renderEnergyBar(GuiGraphics graphics) {
        int maxEnergy = Math.max(1, greenhouseContainer.getGreenhouse().getMaxEnergy());
        int fill = Math.clamp((int) ((long) greenhouseContainer.getEnergy() * 70L / maxEnergy), 0, 70);
        int x = topSectionLeft + getEnergyBarOffset();
        int y = topSectionTop + 5;
        graphics.blit(POWERBAR, x, y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWERBAR, x + 1, y + 70 - fill, 19, 69 - fill, 17, fill + 1, 36, 72);
    }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        int machineSlots = GreenhouseBE.INPUT_SLOTS + greenhouseContainer.getOutputSlotsPerPage();
        for (int i = 0; i < Math.min(machineSlots, container.slots.size()); i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderProgressArrow(GuiGraphics graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + layout.getLootFabricatorProgressArrowX();
        int y = getGuiTop() + layout.getLootFabricatorProgressArrowY();
        int stage = Math.clamp(greenhouseContainer.getProgress() * 5
                / Math.max(1, greenhouseContainer.getProgressMax()), 0, 5);
        graphics.fill(x, y + 10, x + 24, y + 14, 0xFF38271E);
        graphics.fill(x + 1, y + 10, x + 23, y + 12, 0xFF6A4930);
        graphics.fill(x + 3, y + 9, x + 5, y + 11, 0xFF4D8A42);
        if (stage >= 1) graphics.fill(x + 11, y + 7, x + 13, y + 11, 0xFF4D8A42);
        if (stage >= 2) graphics.fill(x + 8, y + 7, x + 12, y + 9, 0xFF68A84F);
        if (stage >= 3) graphics.fill(x + 12, y + 4, x + 14, y + 11, 0xFF4D8A42);
        if (stage >= 4) graphics.fill(x + 13, y + 4, x + 18, y + 7, 0xFF79B957);
        if (stage >= 5) graphics.fill(x + 9, y + 2, x + 13, y + 5, 0xFF8BC55D);
    }

    private void renderOutputPage(GuiGraphics graphics) {
        if (greenhouseContainer.getMaxOutputPage() <= 0) return;
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
        graphics.blit(PREV, getGuiLeft() + layout.getLootFabricatorOutputPrevX(),
                getGuiTop() + layout.getLootFabricatorOutputPrevY(), 0, 0,
                buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.blit(NEXT, getGuiLeft() + layout.getLootFabricatorOutputNextX(),
                getGuiTop() + layout.getLootFabricatorOutputNextY(), 0, 0,
                buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.drawString(font,
                (greenhouseContainer.getOutputPage() + 1) + "/" + (greenhouseContainer.getMaxOutputPage() + 1),
                getGuiLeft() + layout.getLootFabricatorOutputPageTextX(),
                getGuiTop() + layout.getLootFabricatorOutputPageTextY(), 0x404040, false);
    }

    @Override
    public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + layout.getLootFabricatorRedstoneButtonX(),
                getGuiTop() + layout.getLootFabricatorRedstoneButtonY(), redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        multiplierButton = new NumberButton(
                getGuiLeft() + layout.getLootFabricatorSpeedButtonX(),
                getGuiTop() + layout.getLootFabricatorSpeedButtonY(),
                24, 12, greenhouseContainer.getMultiplier(), 1, greenhouseContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.greenhouse.multiplier"), button -> {
                    int multiplier = ((NumberButton) button).getValue();
                    PacketDistributor.sendToServer(new TimeAcceleratorPayload(multiplier));
                });
        addRenderableWidget(multiplierButton);
    }
    @Override public int getFluidBarOffset() { return 204; }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = greenhouseContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(greenhouseContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && greenhouseContainer.getMaxOutputPage() > 0) {
            var layout = GuiUpgradeLayoutConfig.getInstance();
            int buttonSize = layout.getLootFabricatorOutputPageButtonSize();
            int page = greenhouseContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputPrevX(),
                    getGuiTop() + layout.getLootFabricatorOutputPrevY(), buttonSize, buttonSize, mouseX, mouseY)) {
                page--;
            } else if (MiscTools.inBounds(getGuiLeft() + layout.getLootFabricatorOutputNextX(),
                    getGuiTop() + layout.getLootFabricatorOutputNextY(), buttonSize, buttonSize, mouseX, mouseY)) {
                page++;
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            greenhouseContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new FilterPagePayload(greenhouseContainer.getOutputPage()));
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            if (greenhouseContainer.isPlantTemplateSlot(hoveredSlot)) {
                graphics.renderTooltip(font, SEED_TOOLTIP, mouseX, mouseY);
            } else if (greenhouseContainer.isOutputSlot(hoveredSlot)) {
                graphics.renderTooltip(font, OUTPUT_TOOLTIP, mouseX, mouseY);
            }
        }
    }
}
