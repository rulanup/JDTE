package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.PotionBrewerRecipeLockClientCache;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import com.jdte.common.network.data.PotionBrewerRecipeLockPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedPotionBrewerScreen extends BaseMachineScreen<AdvancedPotionBrewerContainer> {
    private static final ResourceLocation BREWING_STAND_BG = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final ResourceLocation FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final ResourceLocation BREW_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final ResourceLocation RECIPE_LOCK_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/waterbreathing.png");
    private static final Component SLOT_LOCK_ON = Component.translatable("jdte.screen.slot_lock.on");
    private static final Component SLOT_LOCK_OFF = Component.translatable("jdte.screen.slot_lock.off");
    private static final int VANILLA_BOTTLE_GHOST_SRC_X = 57;
    private static final int VANILLA_BOTTLE_GHOST_SRC_Y = 52;
    private static final int VANILLA_BOTTLE_GHOST_SIZE = 16;
    private static final int FUEL_BAR_WIDTH = 18;
    private static final int FUEL_BAR_HEIGHT = 4;
    private static final int BREW_PROGRESS_WIDTH = 9;
    private static final int BREW_PROGRESS_HEIGHT = 28;
    private static final int BUBBLES_WIDTH = 12;
    private static final int BUBBLES_HEIGHT = 29;
    private static final int FLUID_INNER_HEIGHT = 70;
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public AdvancedPotionBrewerScreen(AdvancedPotionBrewerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public void init() {
        super.init();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(new RecipeLockWidget(
                getGuiLeft() + layout.getPotionBrewerRecipeLockButtonX(),
                getGuiTop() + layout.getPotionBrewerRecipeLockButtonY()));
        PacketDistributor.sendToServer(new PotionBrewerRecipeLockPayload(brewerContainer().getBlockPos(), false, true));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        int x = getGuiLeft();
        int y = getGuiTop();
        var layout = GuiUpgradeLayoutConfig.getInstance();

        AdvancedPotionBrewerContainer brewerContainer = (AdvancedPotionBrewerContainer) container;

        guiGraphics.blit(BREWING_STAND_BG,
                x + layout.getPotionBrewerBgX(),
                y + layout.getPotionBrewerBgY(),
                layout.getPotionBrewerBgSrcX(),
                layout.getPotionBrewerBgSrcY(),
                layout.getPotionBrewerBgWidth(),
                layout.getPotionBrewerBgHeight());

        renderOutputBottleGhosts(guiGraphics, x, y, layout);
        renderLockedRecipeGhosts(guiGraphics);
        renderPotionBrewerFluidTanks(guiGraphics, x, y, brewerContainer, layout);
        renderFuelBar(guiGraphics, x, y, brewerContainer, layout);
        renderBrewBubbles(guiGraphics, x, y, brewerContainer, layout);
        renderBrewArrow(guiGraphics, x, y, brewerContainer, layout);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        renderPotionBrewerFluidTooltips(guiGraphics, mouseX, mouseY);
        renderPotionBrewerSlotTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void addTickSpeedButton() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(
                getGuiLeft() + layout.getPotionBrewerSpeedButtonX(),
                getGuiTop() + layout.getPotionBrewerSpeedButtonY(),
                tickSpeed, b -> {
                    tickSpeed = ((NumberButton) b).getValue();
                    PacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
                }));
    }

    @Override
    public void addRedstoneButtons() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + layout.getPotionBrewerRedstoneButtonX(),
                getGuiTop() + layout.getPotionBrewerRedstoneButtonY(),
                redstoneMode.ordinal(), b -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    private void renderOutputBottleGhosts(GuiGraphics guiGraphics, int x, int y, GuiUpgradeLayoutConfig layout) {
        for (int i = 0; i < AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.OUTPUT_SLOT_START + i).hasItem()) {
                continue;
            }
            int slotX = x + layout.getPotionBrewerOutputStartX();
            int slotY = y + layout.getPotionBrewerOutputStartY() + i * layout.getPotionBrewerOutputSpacing();
            guiGraphics.blit(BREWING_STAND_BG,
                    slotX + 1,
                    slotY + 1,
                    VANILLA_BOTTLE_GHOST_SRC_X,
                    VANILLA_BOTTLE_GHOST_SRC_Y,
                    VANILLA_BOTTLE_GHOST_SIZE,
                    VANILLA_BOTTLE_GHOST_SIZE);
        }
    }

    private void renderLockedRecipeGhosts(GuiGraphics guiGraphics) {
        if (!isRecipeLocked()) {
            return;
        }
        for (int i = 0; i < AdvancedPotionBrewerBE.TOTAL_SLOTS && i < container.slots.size(); i++) {
            if (!shouldRenderRecipeLockGhost(i)) {
                continue;
            }
            Slot slot = container.slots.get(i);
            if (slot.hasItem()) {
                continue;
            }
            ItemStack template = PotionBrewerRecipeLockClientCache.getTemplate(brewerContainer().getBlockPos(), i);
            if (template.isEmpty()) {
                continue;
            }
            RenderSystem.setShaderColor(0.42f, 0.42f, 0.42f, 0.55f);
            guiGraphics.renderFakeItem(template, getGuiLeft() + slot.x, getGuiTop() + slot.y);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private boolean shouldRenderRecipeLockGhost(int slot) {
        return slot == AdvancedPotionBrewerBE.INGREDIENT_SLOT
                || (slot >= AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                && slot < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT);
    }

    private void renderPotionBrewerSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(hoveredSlot instanceof SlotItemHandler slotItemHandler) || slotItemHandler.getItemHandler() != container.machineHandler) {
            return;
        }
        int slot = slotItemHandler.getSlotIndex();
        Component slotLabel = getPotionBrewerSlotLabel(slot);
        if (slotLabel == null || hoveredSlot.hasItem()) {
            return;
        }

        List<FormattedText> lines = new ArrayList<>();
        lines.add(slotLabel);
        ItemStack lockedTemplate = getLockedTemplateForTooltip(slot);
        if (!lockedTemplate.isEmpty()) {
            lines.add(Component.translatable("jdte.screen.slot_locked_item", lockedTemplate.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(lines), mouseX, mouseY);
    }

    private Component getPotionBrewerSlotLabel(int slot) {
        if (slot >= AdvancedPotionBrewerBE.BOTTLE_SLOT_0 && slot <= AdvancedPotionBrewerBE.BOTTLE_SLOT_2) {
            return Component.translatable("jdte.screen.potion_brewer.bottle_slot", slot - AdvancedPotionBrewerBE.BOTTLE_SLOT_0 + 1);
        }
        if (slot == AdvancedPotionBrewerBE.INGREDIENT_SLOT) {
            return Component.translatable("jdte.screen.potion_brewer.ingredient_slot", 1);
        }
        if (slot == AdvancedPotionBrewerBE.FUEL_SLOT) {
            return Component.translatable("jdte.screen.potion_brewer.fuel_slot");
        }
        if (slot >= AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START
                && slot < AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT) {
            return Component.translatable("jdte.screen.potion_brewer.ingredient_slot", slot - AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 2);
        }
        if (slot >= AdvancedPotionBrewerBE.OUTPUT_SLOT_START
                && slot < AdvancedPotionBrewerBE.OUTPUT_SLOT_START + AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT) {
            return Component.translatable("jdte.screen.potion_brewer.output_slot", slot - AdvancedPotionBrewerBE.OUTPUT_SLOT_START + 1);
        }
        return null;
    }

    private ItemStack getLockedTemplateForTooltip(int slot) {
        if (!isRecipeLocked() || !shouldRenderRecipeLockGhost(slot)) {
            return ItemStack.EMPTY;
        }
        return PotionBrewerRecipeLockClientCache.getTemplate(brewerContainer().getBlockPos(), slot);
    }

    private boolean isRecipeLocked() {
        return brewerContainer().isRecipeLocked() || PotionBrewerRecipeLockClientCache.isLocked(brewerContainer().getBlockPos());
    }

    private AdvancedPotionBrewerContainer brewerContainer() {
        return (AdvancedPotionBrewerContainer) container;
    }

    private void renderPotionBrewerFluidTanks(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int capacity = Math.max(1, brewerContainer.getPotionBrewerFluidCapacity());
        renderFluidTank(guiGraphics,
                x + layout.getPotionBrewerWaterFluidX(),
                y + layout.getPotionBrewerWaterFluidY(),
                brewerContainer.getWaterFluidStack(),
                brewerContainer.getWaterFluidAmount(),
                capacity);
        renderFluidTank(guiGraphics,
                x + layout.getPotionBrewerTimeFluidX(),
                y + layout.getPotionBrewerTimeFluidY(),
                brewerContainer.getTimeFluidStack(),
                brewerContainer.getTimeFluidAmount(),
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

    private void renderPotionBrewerFluidTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        AdvancedPotionBrewerContainer brewerContainer = brewerContainer();
        int capacity = brewerContainer.getPotionBrewerFluidCapacity();

        int waterX = getGuiLeft() + layout.getPotionBrewerWaterFluidX();
        int waterY = getGuiTop() + layout.getPotionBrewerWaterFluidY();
        if (MiscTools.inBounds(waterX, waterY, 18, 72, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, brewerContainer.getWaterFluidStack(), brewerContainer.getWaterFluidAmount(), capacity, mouseX, mouseY);
            return;
        }

        int timeX = getGuiLeft() + layout.getPotionBrewerTimeFluidX();
        int timeY = getGuiTop() + layout.getPotionBrewerTimeFluidY();
        if (MiscTools.inBounds(timeX, timeY, 18, 72, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, brewerContainer.getTimeFluidStack(), brewerContainer.getTimeFluidAmount(), capacity, mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, FluidStack fluidStack, int amount, int capacity, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(amount), MagicHelpers.withSuffix(capacity))
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

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int fuel = brewerContainer.getFuel();
        if (fuel > 0) {
            int fuelWidth = Mth.clamp((FUEL_BAR_WIDTH * fuel + AdvancedPotionBrewerBE.FUEL_PER_BLAZE - 1) / AdvancedPotionBrewerBE.FUEL_PER_BLAZE,
                    0,
                    FUEL_BAR_WIDTH);
            if (fuelWidth > 0) {
                guiGraphics.blitSprite(FUEL_LENGTH_SPRITE,
                        FUEL_BAR_WIDTH,
                        FUEL_BAR_HEIGHT,
                        0,
                        0,
                        x + layout.getPotionBrewerFuelBarX(),
                        y + layout.getPotionBrewerFuelBarBottomY() - FUEL_BAR_HEIGHT,
                        fuelWidth,
                        FUEL_BAR_HEIGHT);
            }
        }
    }

    private void renderBrewBubbles(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int remainingTicks = Math.max(0, maxProgress - progress);
            int bubbleHeight = BUBBLE_LENGTHS[remainingTicks / 2 % BUBBLE_LENGTHS.length];
            if (bubbleHeight > 0) {
                guiGraphics.blitSprite(BUBBLES_SPRITE,
                        BUBBLES_WIDTH,
                        BUBBLES_HEIGHT,
                        0,
                        BUBBLES_HEIGHT - bubbleHeight,
                        x + layout.getPotionBrewerBubblesX(),
                        y + layout.getPotionBrewerBubblesBottomY() - bubbleHeight,
                        BUBBLES_WIDTH,
                        bubbleHeight);
            }
        }
    }

    private void renderBrewArrow(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer, GuiUpgradeLayoutConfig layout) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int arrowHeight = (progress * BREW_PROGRESS_HEIGHT) / maxProgress;
            if (arrowHeight > 0) {
                guiGraphics.blitSprite(BREW_PROGRESS_SPRITE,
                        BREW_PROGRESS_WIDTH,
                        BREW_PROGRESS_HEIGHT,
                        0,
                        0,
                        x + layout.getPotionBrewerArrowX(),
                        y + layout.getPotionBrewerArrowBottomY() - BREW_PROGRESS_HEIGHT,
                        BREW_PROGRESS_WIDTH,
                        arrowHeight);
            }
        }
    }

    private class RecipeLockWidget extends AbstractWidget {
        private boolean lastTooltipLocked = false;

        private RecipeLockWidget(int x, int y) {
            super(x, y, 16, 16, SLOT_LOCK_OFF);
            setTooltip(Tooltip.create(SLOT_LOCK_OFF));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean locked = isRecipeLocked();
            setMessage(locked ? SLOT_LOCK_ON : SLOT_LOCK_OFF);
            if (locked != lastTooltipLocked) {
                setTooltip(Tooltip.create(locked ? SLOT_LOCK_ON : SLOT_LOCK_OFF));
                lastTooltipLocked = locked;
            }
            RenderSystem.setShaderColor(locked ? 1.0f : 0.33f, locked ? 1.0f : 0.33f, locked ? 1.0f : 0.33f, 1.0f);
            guiGraphics.blit(RECIPE_LOCK_ICON, getX(), getY(), 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            boolean nextLocked = !isRecipeLocked();
            PacketDistributor.sendToServer(new PotionBrewerRecipeLockPayload(brewerContainer().getBlockPos(), nextLocked, false));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
            this.defaultButtonNarrationText(narrationOutput);
        }
    }
}
