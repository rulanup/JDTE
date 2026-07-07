package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.containers.GelGeneratorContainer;
import com.jdte.common.network.data.GelGeneratorPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class GelGeneratorScreen<T extends GelGeneratorContainer> extends BaseMachineScreen<T> {
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int HIDDEN_BASE_FLUID_BAR_OFFSET = -10000;
    private static final ResourceLocation JUMP_BOOST = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/jumpboost.png");
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        renderGelFluidTanks(guiGraphics);
        renderProgressArrow(guiGraphics);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        renderGelFluidTooltips(guiGraphics, mouseX, mouseY);
        renderMachineSlotTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderMachineSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hoveredSlot instanceof SlotItemHandler slotItemHandler
                && slotItemHandler.getItemHandler() == container.machineHandler
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
                guiGraphics.renderTooltip(font, slotLabel, mouseX, mouseY);
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
                    PacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
                }));
    }

    private void toggleAutoBalance() {
        autoBalanceLocal = !autoBalanceLocal;
        autoBalanceWidget.setTooltip(Tooltip.create(Component.translatable(autoBalanceLocal ? "jdte.screen.auto_balance.on" : "jdte.screen.auto_balance.off")));
        PacketDistributor.sendToServer(new GelGeneratorPayload(autoBalanceLocal));
    }

    private void renderGelFluidTanks(GuiGraphics guiGraphics) {
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

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack, int amount, int capacity) {
        guiGraphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);

        int fluidHeight = Math.min(FLUID_INNER_HEIGHT, (amount * FLUID_INNER_HEIGHT) / capacity);
        if (fluidHeight > 0) {
            renderFluidStack(guiGraphics, fluidStack, x + 1, y + 71, 16, fluidHeight);
        }

        guiGraphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderGelFluidTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int maxMb = gelContainer.getOutputFluidCapacity();

        int inputX = getGuiLeft() + config.getGelGenInputFluidX();
        int inputY = getGuiTop() + config.getGelGenInputFluidY();
        if (MiscTools.inBounds(inputX, inputY, 18, 72, mouseX, mouseY)) {
            FluidStack fluidStack = gelContainer.getFluidStack();
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
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
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(gelContainer.getOutputFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }

    private void renderFluidStack(GuiGraphics guiGraphics, FluidStack fluidStack, int startX, int startY, int width, int height) {
        if (fluidStack.isEmpty() || height <= 0) return;

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite fluidStillSprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.setShaderColor(red, green, blue, 1.0f);

        int zLevel = 0;
        float uMin = fluidStillSprite.getU0();
        float uMax = fluidStillSprite.getU1();
        float vMin = fluidStillSprite.getV0();
        float vMax = fluidStillSprite.getV1();
        int textureWidth = fluidStillSprite.contents().width();
        int textureHeight = fluidStillSprite.contents().height();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        int yOffset = 0;
        while (yOffset < height) {
            int drawHeight = Math.min(textureHeight, height - yOffset);
            int drawY = startY - yOffset - drawHeight;
            float vMaxAdjusted = vMin + (vMax - vMin) * ((float) drawHeight / textureHeight);

            int xOffset = 0;
            while (xOffset < width) {
                int drawWidth = Math.min(textureWidth, width - xOffset);
                float uMaxAdjusted = uMin + (uMax - uMin) * ((float) drawWidth / textureWidth);

                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset, drawY + drawHeight, zLevel).setUv(uMin, vMaxAdjusted);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset + drawWidth, drawY + drawHeight, zLevel).setUv(uMaxAdjusted, vMaxAdjusted);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset + drawWidth, drawY, zLevel).setUv(uMaxAdjusted, vMin);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset, drawY, zLevel).setUv(uMin, vMin);

                xOffset += drawWidth;
            }
            yOffset += drawHeight;
        }

        BufferUploader.drawWithShader(vertexBuffer.buildOrThrow());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
    }

    private void renderProgressArrow(GuiGraphics guiGraphics) {
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

    private void drawArrow(GuiGraphics guiGraphics, int x, int y, int width, int color) {
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

    private void drawHeadPart(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
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
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!autoBalanceLocal) {
                RenderSystem.setShaderColor(0.33f, 0.33f, 0.33f, 1.0f);
            } else {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(getX() + 8, getY() + 8, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(90));
            guiGraphics.blit(JUMP_BOOST, -8, -8, 0, 0, 16, 16, 16, 16);
            pose.popPose();

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            toggleAutoBalance();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
            this.defaultButtonNarrationText(narrationOutput);
        }
    }
}
