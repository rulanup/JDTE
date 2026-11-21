package com.jdte.client.screens;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.containers.LargeGreenhouseContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.client.screens.util.GreenhouseSlotCountRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class LargeGreenhouseScreen extends BaseMachineScreen<LargeGreenhouseContainer> {
    private static final Identifier PREV = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final Identifier NEXT = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final Component SEED_TOOLTIP = Component.translatable("jdte.slot.large_greenhouse_seed");
    private static final Component OUTPUT_TOOLTIP = Component.translatable("jdte.slot.large_greenhouse_output");
    private final LargeGreenhouseContainer greenhouseContainer;
    private NumberButton multiplierButton;

    public LargeGreenhouseScreen(LargeGreenhouseContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        greenhouseContainer = container;
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countLabel) {
        if (greenhouseContainer.isOutputSlot(slot) && stack.getCount() > 1) {
            GreenhouseSlotCountRenderer.extractRenderState(graphics, font, stack, slot, imageWidth, countLabel);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, countLabel);
    }

    @Override
    public void setTopSection() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        extraWidth = layout.getLootFabricatorExtraWidth();
        extraHeight = layout.getLootFabricatorExtraHeight();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        renderEnergyBar(graphics);
        renderMachineSlotBackgrounds(graphics);
        renderProgressArrow(graphics);
        renderPageControls(graphics);
    }

    private void renderEnergyBar(GuiGraphicsExtractor graphics) {
        int maxEnergy = Math.max(1, greenhouseContainer.getLargeGreenhouse().getMaxEnergy());
        int fill = Math.clamp((int) ((long) greenhouseContainer.getEnergy() * 70L / maxEnergy), 0, 70);
        int x = topSectionLeft + getEnergyBarOffset();
        int y = topSectionTop + 5;
        graphics.blit(RenderPipelines.GUI_TEXTURED, POWERBAR, x, y, 0, 0, 18, 72, 36, 72);
        graphics.blit(RenderPipelines.GUI_TEXTURED, POWERBAR, x + 1, y + 70 - fill, 19, 69 - fill, 17, fill + 1, 36, 72);
    }

    private void renderMachineSlotBackgrounds(GuiGraphicsExtractor graphics) {
        int machineSlots = greenhouseContainer.getSeedSlotsPerPage()
                + greenhouseContainer.getOutputSlotsPerPage();
        for (int i = 0; i < Math.min(machineSlots, container.slots.size()); i++) {
            Slot slot = container.slots.get(i);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderProgressArrow(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + layout.getGreenhouseProgressArrowX();
        int y = getGuiTop() + layout.getGreenhouseProgressArrowY();
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

    private void renderPageControls(GuiGraphicsExtractor graphics) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int buttonSize = layout.getGreenhouseOutputPageButtonSize();
        if (greenhouseContainer.getMaxOutputPage() <= 0) return;
        drawPageControl(graphics,
                layout.getGreenhouseOutputPrevX(), layout.getGreenhouseOutputNextX(),
                layout.getGreenhouseOutputPrevY(), layout.getGreenhouseOutputPageTextX(),
                layout.getGreenhouseOutputPageTextY(), greenhouseContainer.getOutputPage(),
                greenhouseContainer.getMaxOutputPage(), buttonSize);
    }

    private void drawPageControl(GuiGraphicsExtractor graphics, int previousX, int nextX, int y,
                                 int textX, int textY, int page, int maxPage, int buttonSize) {
        if (maxPage <= 0) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PREV, getGuiLeft() + previousX, getGuiTop() + y, 0, 0,
                buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.blit(RenderPipelines.GUI_TEXTURED, NEXT, getGuiLeft() + nextX, getGuiTop() + y, 0, 0,
                buttonSize, buttonSize, buttonSize, buttonSize);
        graphics.text(font, (page + 1) + "/" + (maxPage + 1),
                getGuiLeft() + textX, getGuiTop() + textY, 0xFF404040, false);
    }

    @Override
    public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = layout.getGreenhouseRedstoneButtonX();
        int y = layout.getGreenhouseRedstoneButtonY();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + x, getGuiTop() + y, redstoneMode.ordinal(), button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int x = layout.getGreenhouseSpeedButtonX();
        int y = layout.getGreenhouseSpeedButtonY();
        multiplierButton = new NumberButton(
                getGuiLeft() + x,
                getGuiTop() + y,
                24, 12, greenhouseContainer.getMultiplier(), 1, greenhouseContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.large_greenhouse.multiplier"), button -> {
                    int multiplier = ((NumberButton) button).getValue();
                    ClientPacketDistributor.sendToServer(new TimeAcceleratorPayload(multiplier));
                });
        addRenderableWidget(multiplierButton);
    }
    @Override public int getFluidBarOffset() {
        return 204;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = greenhouseContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(greenhouseContainer.getMultiplier(), multiplierButton.max));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button != 0) return super.mouseClicked(event, doubleClick);
        var layout = GuiUpgradeLayoutConfig.getInstance();
        int buttonSize = layout.getGreenhouseOutputPageButtonSize();
        int delta = pageDelta(mouseX, mouseY, layout.getGreenhouseOutputPrevX(),
                layout.getGreenhouseOutputNextX(), layout.getGreenhouseOutputPrevY(), buttonSize);
        if (delta == 0 || greenhouseContainer.getMaxOutputPage() <= 0) {
            return super.mouseClicked(event, doubleClick);
        }
        greenhouseContainer.setOutputPage(greenhouseContainer.getOutputPage() + delta);
        ClientPacketDistributor.sendToServer(new FilterPagePayload(greenhouseContainer.getOutputPage()));
        playPageSound();
        return true;
    }

    private int pageDelta(double mouseX, double mouseY, int previousX, int nextX, int y, int buttonSize) {
        if (MiscTools.inBounds(getGuiLeft() + previousX, getGuiTop() + y,
                buttonSize, buttonSize, mouseX, mouseY)) return -1;
        if (MiscTools.inBounds(getGuiLeft() + nextX, getGuiTop() + y,
                buttonSize, buttonSize, mouseX, mouseY)) return 1;
        return 0;
    }

    private void playPageSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            if (greenhouseContainer.isPlantTemplateSlot(hoveredSlot)) {
                graphics.setTooltipForNextFrame(font, SEED_TOOLTIP, mouseX, mouseY);
            } else if (greenhouseContainer.isOutputSlot(hoveredSlot)) {
                graphics.setTooltipForNextFrame(font, OUTPUT_TOOLTIP, mouseX, mouseY);
            }
        }
    }
}
