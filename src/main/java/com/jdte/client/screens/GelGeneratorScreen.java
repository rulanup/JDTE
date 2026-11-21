package com.jdte.client.screens;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

import com.jdte.client.screens.util.MachineFluidBarRenderer;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.screens.util.MachineBarMath;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.containers.GelGeneratorContainer;
import com.jdte.common.network.data.GelGeneratorPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Arrays;

public abstract class GelGeneratorScreen<T extends GelGeneratorContainer> extends BaseMachineScreen<T> {
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int HIDDEN_BASE_FLUID_BAR_OFFSET = -10000;
    private static final Identifier JUMP_BOOST = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/jumpboost.png");
    private boolean autoBalanceLocal;
    private AutoBalanceWidget autoBalanceWidget;

    protected GelGeneratorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public int getFluidBarOffset() {
        return HIDDEN_BASE_FLUID_BAR_OFFSET;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        renderGelFluidTanks(guiGraphics);
        renderProgressArrow(guiGraphics);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        renderGelFluidTooltips(guiGraphics, mouseX, mouseY);
        renderMachineSlotTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderMachineSlotTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (hoveredSlot instanceof ResourceHandlerSlot slotItemHandler
                && slotItemHandler.getResourceHandler() == container.machineHandler
                && !hoveredSlot.hasItem()) {
            int idx = hoveredSlot.getSlotIndex();
            Component slotLabel = null;
            if (idx == GelGeneratorBE.GEL_SLOT) {
                slotLabel = Component.translatable("jdte.screen.gel_slot");
            } else if (idx == GelGeneratorBE.FOOD_SLOT) {
                slotLabel = Component.translatable("jdte.screen.food_slot");
            } else if (idx >= GelGeneratorBE.INPUT_START_SLOT && idx < GelGeneratorBE.INPUT_START_SLOT + GelGeneratorBE.INPUT_SLOTS) {
                slotLabel = Component.translatable("jdte.screen.input_slot");
            } else if (idx >= GelGeneratorBE.OUTPUT_START_SLOT && idx < GelGeneratorBE.OUTPUT_START_SLOT + GelGeneratorBE.OUTPUT_SLOTS) {
                slotLabel = Component.translatable("jdte.screen.output_slot");
            }
            if (slotLabel != null) {
                guiGraphics.setTooltipForNextFrame(font, slotLabel, mouseX, mouseY);
            }
        }
    }

    @Override
    public void init() {
        super.init();
        autoBalanceLocal = ((GelGeneratorContainer) container).isAutoBalanceInputs();
        var config = GuiUpgradeLayoutConfig.getInstance();
        autoBalanceWidget = new AutoBalanceWidget(
                getGuiLeft() + config.getGelGenAutoBalanceX(),
                getGuiTop() + config.getGelGenAutoBalanceY());
        autoBalanceWidget.setTooltip(Tooltip.create(Component.translatable(autoBalanceLocal ? "jdte.screen.auto_balance.on" : "jdte.screen.auto_balance.off")));
        addRenderableWidget(autoBalanceWidget);
    }

    @Override
    public void addFilterButtons() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                getGuiLeft() + config.getGelGenAllowlistButtonX(),
                getGuiTop() + config.getGelGenAllowlistButtonY(),
                filterData.allowlist, b -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.COMPARENBTBUTTON(
                getGuiLeft() + config.getGelGenCompareNBTButtonX(),
                getGuiTop() + config.getGelGenCompareNBTButtonY(),
                filterData.compareNBT, b -> {
                    filterData.compareNBT = !filterData.compareNBT;
                    ((GrayscaleButton) b).toggleActive();
                    saveSettings();
                }));
        if (filterData.blockItemFilter != -1) {
            addRenderableWidget(ToggleButtonFactory.FILTERBLOCKITEMBUTTON(
                    getGuiLeft() + config.getGelGenCompareNBTButtonX(),
                    getGuiTop() + config.getGelGenCompareNBTButtonY() + 18,
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
                getGuiLeft() + config.getGelGenRedstoneButtonX(),
                getGuiTop() + config.getGelGenRedstoneButtonY(),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void addTickSpeedButton() {
        var config = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(
                getGuiLeft() + config.getGelGenSpeedButtonX(),
                getGuiTop() + config.getGelGenSpeedButtonY(),
                tickSpeed, b -> {
                    tickSpeed = ((com.direwolf20.justdirethings.client.screens.widgets.NumberButton) b).getValue();
                    ClientPacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
                }));
    }

    private void toggleAutoBalance() {
        autoBalanceLocal = !autoBalanceLocal;
        autoBalanceWidget.setTooltip(Tooltip.create(Component.translatable(autoBalanceLocal ? "jdte.screen.auto_balance.on" : "jdte.screen.auto_balance.off")));
        ClientPacketDistributor.sendToServer(new GelGeneratorPayload(autoBalanceLocal));
    }

    private void renderGelFluidTanks(GuiGraphicsExtractor guiGraphics) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int capacity = Math.max(1, gelContainer.getOutputFluidCapacity());

        renderFluidTank(guiGraphics,
                getGuiLeft() + config.getGelGenInputFluidX(),
                getGuiTop() + config.getGelGenInputFluidY(),
                gelContainer.getFluidStack(),
                gelContainer.getFluidAmount(),
                capacity);
        renderFluidTank(guiGraphics,
                getGuiLeft() + config.getGelGenOutputFluidX(),
                getGuiTop() + config.getGelGenOutputFluidY(),
                gelContainer.getOutputFluidStack(),
                gelContainer.getOutputFluidAmount(),
                capacity);
    }

    private void renderFluidTank(GuiGraphicsExtractor guiGraphics, int x, int y, FluidStack fluidStack, int amount, int capacity) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);

        int fluidHeight = MachineBarMath.scaleClamped(amount, capacity, FLUID_INNER_HEIGHT);
        if (fluidHeight > 0) {
            MachineFluidBarRenderer.renderFluid(guiGraphics, fluidStack, x + 1, y + 71, 16, fluidHeight);
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderGelFluidTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int maxMb = gelContainer.getOutputFluidCapacity();

        int inputX = getGuiLeft() + config.getGelGenInputFluidX();
        int inputY = getGuiTop() + config.getGelGenInputFluidY();
        if (MiscTools.inBounds(inputX, inputY, 18, 72, mouseX, mouseY)) {
            FluidStack fluidStack = gelContainer.getFluidStack();
            guiGraphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(gelContainer.getFluidAmount()), MagicHelpers.withSuffix(maxMb))
            )), mouseX, mouseY);
            return;
        }

        int outputX = getGuiLeft() + config.getGelGenOutputFluidX();
        int outputY = getGuiTop() + config.getGelGenOutputFluidY();
        if (!MiscTools.inBounds(outputX, outputY, 18, 72, mouseX, mouseY)) {
            return;
        }

        FluidStack fluidStack = gelContainer.getOutputFluidStack();
        guiGraphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(gelContainer.getOutputFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }


    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        int x = getGuiLeft() + config.getGelGenProgressArrowX();
        int y = getGuiTop() + config.getGelGenProgressArrowY();
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int progressWidth = (gelContainer.getGelProgress() * 24) / gelContainer.getGelProgressMax();

        drawArrow(guiGraphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(guiGraphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(guiGraphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
        }
    }

    private void drawArrow(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int color) {
        int clampedWidth = Math.max(0, Math.min(24, width));
        if (clampedWidth <= 0) return;

        int bodyWidth = Math.min(16, clampedWidth);
        if (bodyWidth > 0) {
            guiGraphics.fill(x, y + 5, x + bodyWidth, y + 10, color);
        }
        int headWidth = clampedWidth - 16;
        if (headWidth <= 0) return;

        drawHeadPart(guiGraphics, x + 16, y + 4, Math.min(headWidth, 2), 7, color);
        if (headWidth > 2) drawHeadPart(guiGraphics, x + 18, y + 3, Math.min(headWidth - 2, 2), 9, color);
        if (headWidth > 4) drawHeadPart(guiGraphics, x + 20, y + 2, Math.min(headWidth - 4, 2), 11, color);
        if (headWidth > 6) drawHeadPart(guiGraphics, x + 22, y + 1, Math.min(headWidth - 6, 2), 13, color);
    }

    private void drawHeadPart(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        if (width > 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }

    /**
     * Custom toggle button that renders JDT's Jump Boost icon rotated 90° CW.
     * Full color when auto-balance is enabled, dimmed when disabled.
     */
    private class AutoBalanceWidget extends AbstractWidget {
        public AutoBalanceWidget(int x, int y) {
            super(x, y, 16, 16, Component.translatable("jdte.screen.auto_balance"));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

            var pose = guiGraphics.pose();
            pose.pushMatrix();
            pose.translate(getX() + 8, getY() + 8);
            pose.rotate((float) Math.toRadians(90));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUMP_BOOST, -8, -8, 0, 0, 16, 16, 16, 16, autoBalanceLocal ? -1 : 0xFF545454);
            pose.popMatrix();

        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            toggleAutoBalance();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
            this.defaultButtonNarrationText(narrationOutput);
        }
    }
}
