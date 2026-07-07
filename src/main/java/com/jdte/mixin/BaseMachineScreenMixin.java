package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.UpgradePopupDragHandler;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.containers.FilterPageHolder;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.UpgradeSlotStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(BaseMachineScreen.class)
public abstract class BaseMachineScreenMixin extends AbstractContainerScreenMixin implements UpgradePopupDragHandler {
    @Shadow protected BaseMachineContainer container;
    @Shadow protected BaseMachineBE baseMachineBE;
    @Shadow protected int topSectionLeft;
    @Shadow protected int topSectionTop;
    @Shadow protected int extraWidth;
    @Shadow protected int extraHeight;
    @Shadow protected ResourceLocation SOCIALBACKGROUND;

    @Unique private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");
    @Unique private static final ResourceLocation JDTE_FILTER_PREV = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_prev.png");
    @Unique private static final ResourceLocation JDTE_FILTER_NEXT = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_next.png");
    @Unique private int jdte$filterPressed = 0;
    @Unique private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    @Unique private static final Map<Slot, int[]> JDTE_ORIGINAL_SLOT_POSITIONS = new WeakHashMap<>();
    @Unique private static final int JDTE_SLOT_SIZE = 18;
    @Unique private static final int JDTE_FILTER_COLUMNS = 9;
    @Unique private int jdte$baseImageHeight = -1;
    @Unique private int jdte$filterPrevX;
    @Unique private int jdte$filterNextX;
    @Unique private int jdte$filterButtonsY;

    @Unique
    private int jdte$getUpgradeSlots() {
        if (container == null) return 0;
        return UpgradeSlotStorage.getUpgradeSlots(container);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void jdte$expandLowerSection(CallbackInfo ci) {
        if (jdte$baseImageHeight < 0) {
            jdte$baseImageHeight = imageHeight;
        }
        imageHeight = jdte$baseImageHeight;
    }

    @Inject(method = "setTopSection", at = @At("TAIL"))
    private void jdte$expandMachinePanel(CallbackInfo ci) {
        this.extraHeight = Math.max(this.extraHeight, 0);
        if (baseMachineBE != null) {
            if (UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
                this.extraWidth = Math.max(this.extraWidth, 60);
            }
            if (UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.FILTER) > 0) {
                this.extraWidth = Math.max(this.extraWidth, 60);
            }
        }
    }

    @Inject(method = "renderBg", at = @At("HEAD"))
    private void jdte$prepareDynamicLayout(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;
        jdte$clampFilterPage();
        jdte$layoutSlots();
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen;drawSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", ordinal = 0))
    private void jdte$renderUpgradePopup(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        int slots = jdte$getUpgradeSlots();
        if (slots <= 0) return;
        jdte$renderFixedUpgradePanels(guiGraphics);
    }

    @Unique
    private void jdte$renderFixedUpgradePanels(GuiGraphics guiGraphics) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        int totalSlots = jdte$getUpgradeSlots();
        int half = 4;
        boolean hasLeftPanel = totalSlots > 4;

        // Right panel (slots 0-3)
        jdte$drawSlotPanel(guiGraphics, config, config.getFirstSlotX(), config.getFirstSlotY(), Math.min(half, totalSlots));

        // Left panel (slots 4-7, only for 8-slot machines)
        if (hasLeftPanel) {
            jdte$drawSlotPanel(guiGraphics, config, config.getLeftFirstSlotX(), config.getLeftFirstSlotY(), totalSlots - half);
        }
    }

    @Unique
    private void jdte$drawSlotPanel(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config, int originX, int originY, int slotCount) {
        int panelW = config.getColumns() * config.getSlotSize() + 2 * config.getPanelPadding();
        int panelH = config.getRows() * config.getSlotSize() + 2 * config.getPanelPadding();
        int panelX = getGuiLeft() + originX - config.getPanelPadding();
        int panelY = getGuiTop() + originY - config.getPanelPadding();

        guiGraphics.blitSprite(SOCIALBACKGROUND, panelX, panelY, panelW, panelH);

        for (int i = 0; i < slotCount; i++) {
            int sx = getGuiLeft() + originX;
            int sy = getGuiTop() + originY + i * config.getSlotSpacing();
            guiGraphics.blitSprite(SLOT_SPRITE, sx, sy, config.getSlotSize(), config.getSlotSize());
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void jdte$hideInactiveFilterSlots(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
            ci.cancel();
        }
    }

    @Unique
    private void jdte$layoutSlots() {
        int upgradeSlotIndex = 0;
        int upgradeSlots = jdte$getUpgradeSlots();
        var config = GuiUpgradeLayoutConfig.getInstance();
        boolean isEightSlot = upgradeSlots > 4;

        for (Slot slot : container.slots) {
            int[] original = jdte$getOriginalSlotPosition(slot);
            SlotAccessor slotAccessor = (SlotAccessor) slot;

            if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
                slotAccessor.jdte$setX(-10000);
                slotAccessor.jdte$setY(-10000);
            } else if (slot instanceof UpgradeSlot) {
                if (isEightSlot && upgradeSlotIndex >= 4) {
                    // Left panel (slots 4-7)
                    int row = upgradeSlotIndex - 4;
                    slotAccessor.jdte$setX(config.getLeftFirstSlotX() + 1);
                    slotAccessor.jdte$setY(config.getLeftFirstSlotY() + row * config.getSlotSpacing() + 1);
                } else {
                    // Right panel (slots 0-3)
                    int row = upgradeSlotIndex % 4;
                    slotAccessor.jdte$setX(config.getFirstSlotX() + 1);
                    slotAccessor.jdte$setY(config.getFirstSlotY() + row * config.getSlotSpacing() + 1);
                }
                upgradeSlotIndex++;
            } else if (jdte$isPlayerInventorySlot(slot)) {
                slotAccessor.jdte$setX(original[0]);
                slotAccessor.jdte$setY(original[1]);
            } else {
                slotAccessor.jdte$setX(original[0]);
                slotAccessor.jdte$setY(original[1]);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;

        if (jdte$hasFilterUpgrades()) {
            jdte$updateFilterButtonPositions();
            int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
            int maxPage = jdte$getMaxFilterPage(slotsPerPage);
            if (jdte$inFilterPrevButton(mouseX, mouseY)) {
                if (jdte$getFilterPage() > 0) {
                    jdte$filterPressed = -1;
                    jdte$changeFilterPage(-1, slotsPerPage, maxPage);
                }
                cir.setReturnValue(true);
                return;
            }
            if (jdte$inFilterNextButton(mouseX, mouseY)) {
                if (jdte$getFilterPage() < maxPage) {
                    jdte$filterPressed = 1;
                    jdte$changeFilterPage(1, slotsPerPage, maxPage);
                }
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Override
    public boolean jdte$dragUpgradePopup(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void jdte$releaseUpgradePopup(int button) {
        jdte$filterPressed = 0;
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void jdte$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
    }

    @Unique
    private boolean jdte$isPlayerInventorySlot(Slot slot) {
        return slot instanceof SlotItemHandler slotItemHandler && slotItemHandler.getItemHandler() instanceof InvWrapper;
    }

    @Unique
    private int[] jdte$getOriginalSlotPosition(Slot slot) {
        return JDTE_ORIGINAL_SLOT_POSITIONS.computeIfAbsent(slot, key -> new int[]{key.x, key.y});
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void jdte$initFilterPage(CallbackInfo ci) {
        jdte$updateFilterButtonPositions();
    }

    @Unique
    private void jdte$updateFilterButtonPositions() {
        if (container == null || baseMachineBE == null) return;
        if (!jdte$hasFilterUpgrades()) return;
        int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
        jdte$filterPrevX = getGuiLeft() + 8 - 14;
        jdte$filterNextX = getGuiLeft() + 8 + slotsPerPage * JDTE_SLOT_SIZE;
        jdte$filterButtonsY = getGuiTop() + 56;
    }

    @Unique
    private boolean jdte$hasFilterUpgrades() {
        return UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.FILTER) > 0;
    }

    @Unique
    private int jdte$getFilterPage() {
        if (container instanceof FilterPageHolder holder) {
            return holder.jdte$getFilterPage();
        }
        return 0;
    }

    @Unique
    private int jdte$getMaxFilterPage(int slotsPerPage) {
        int baseSlots = UpgradeSlotStorage.getBaseFilterSlots(container);
        if (baseSlots <= 0 && container.filterHandler != null) {
            baseSlots = UpgradeHelper.getBaseFilterSlots(container.filterHandler);
        }
        int activeSlots = UpgradeHelper.getActiveFilterSlots(baseMachineBE, baseSlots);
        return Math.max(0, (activeSlots - 1) / slotsPerPage);
    }

    @Unique
    private void jdte$changeFilterPage(int delta, int slotsPerPage, int maxPage) {
        int newPage = Math.max(0, Math.min(maxPage, jdte$getFilterPage() + delta));
        if (newPage == jdte$getFilterPage()) return;
        if (container instanceof FilterPageHolder holder) {
            holder.jdte$setFilterPage(newPage);
        }
        jdte$layoutSlots();
        PacketDistributor.sendToServer(new FilterPagePayload(newPage));
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Unique
    private void jdte$clampFilterPage() {
        if (!(container instanceof FilterPageHolder holder)) return;
        if (!jdte$hasFilterUpgrades()) {
            if (holder.jdte$getFilterPage() != 0) {
                holder.jdte$setFilterPage(0);
                PacketDistributor.sendToServer(new FilterPagePayload(0));
            }
            return;
        }
        int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
        int maxPage = jdte$getMaxFilterPage(slotsPerPage);
        if (holder.jdte$getFilterPage() > maxPage) {
            holder.jdte$setFilterPage(0);
            PacketDistributor.sendToServer(new FilterPagePayload(0));
        }
    }

    @Unique
    private boolean jdte$inFilterPrevButton(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$filterPrevX, jdte$filterButtonsY, 12, 12, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inFilterNextButton(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$filterNextX, jdte$filterButtonsY, 12, 12, mouseX, mouseY);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void jdte$renderFilterPageButtons(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;
        if (!jdte$hasFilterUpgrades()) return;
        jdte$updateFilterButtonPositions();

        int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
        int maxPage = jdte$getMaxFilterPage(slotsPerPage);
        int currentPage = jdte$getFilterPage();

        int prevX = jdte$filterPrevX;
        int nextX = jdte$filterNextX;
        int y = jdte$filterButtonsY;
        boolean prevActive = currentPage > 0;
        boolean nextActive = currentPage < maxPage;

        RenderSystem.setShaderColor(1f, 1f, 1f, prevActive ? 1f : 0.3f);
        com.mojang.blaze3d.vertex.PoseStack poseStack = guiGraphics.pose();
        if (jdte$filterPressed == -1) {
            poseStack.pushPose();
            poseStack.translate(prevX + 6, y + 6, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(JDTE_FILTER_NEXT, -8, -8, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(prevX, y, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            guiGraphics.blit(JDTE_FILTER_PREV, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, nextActive ? 1f : 0.3f);
        if (jdte$filterPressed == 1) {
            poseStack.pushPose();
            poseStack.translate(nextX, y, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            guiGraphics.blit(JDTE_FILTER_NEXT, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(nextX + 6, y + 6, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(JDTE_FILTER_PREV, -8, -8, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.drawString(font, Component.literal((currentPage + 1) + "/" + (maxPage + 1)), nextX + 12 + 2, y + 2, 0xFF404040, false);
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderUpgradeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;

        int upgradeSlots = jdte$getUpgradeSlots();
        if (upgradeSlots <= 0) return;

        for (Slot slot : container.slots) {
            if (slot instanceof UpgradeSlot) {
                if (MiscTools.inBounds(getGuiLeft() + slot.x, getGuiTop() + slot.y, 18, 18, mouseX, mouseY)) {
                    if (slot.hasItem()) {
                        guiGraphics.renderTooltip(font, slot.getItem(), mouseX, mouseY);
                    } else {
                        List<FormattedText> lines = new ArrayList<>();
                        lines.add(Component.translatable("jdte.upgrade.slot.empty"));
                        lines.add(Component.empty());

                        for (UpgradeType type : UpgradeType.values()) {
                            if (!jdte$isUpgradeCompatible(type)) continue;

                            int current = UpgradeHelper.countUpgrades(baseMachineBE, type);
                            int max = type.getMaxPerMachine();
                            boolean canAdd = current < max && !jdte$hasOppositeSpeedUpgrade(type);

                            Component name = Component.translatable("item.jdte." + type.getSerializedName() + "_upgrade");
                            lines.add(Component.literal("  ")
                                    .append(name)
                                    .append(Component.literal(": " + current + "/" + max))
                                    .copy()
                                    .withStyle(canAdd ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
                        }

                        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(lines), mouseX, mouseY);
                    }
                    return;
                }
            }
        }
    }

    @Unique
    private boolean jdte$isUpgradeCompatible(UpgradeType type) {
        switch (type) {
            case FLUID_STORAGE:
                return baseMachineBE instanceof ClickerT1BE;
            case GENERATOR:
                return baseMachineBE instanceof GeneratorT1BE || baseMachineBE instanceof GeneratorFluidT1BE;
            case RANGE:
                return baseMachineBE instanceof AreaAffectingBE;
            case FILTER:
                return baseMachineBE instanceof FilterableBE;
            default:
                return true;
        }
    }

    @Unique
    private boolean jdte$hasOppositeSpeedUpgrade(UpgradeType type) {
        if (type == UpgradeType.OVERCLOCK) {
            return UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.UNDERCLOCK) > 0;
        }
        if (type == UpgradeType.UNDERCLOCK) {
            return UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.OVERCLOCK) > 0;
        }
        return false;
    }

    @ModifyConstant(method = "addAreaButtons", constant = @Constant(doubleValue = 5.0D), require = 0)
    private double jdte$areaRadiusMax(double original) {
        return UpgradeHelper.getMaxAreaRadius(baseMachineBE);
    }

    @ModifyConstant(method = "addAreaButtons", constant = @Constant(intValue = 9), require = 0)
    private int jdte$areaOffsetMax(int original) {
        return UpgradeHelper.getMaxAreaOffset(baseMachineBE);
    }

    @ModifyConstant(method = "addAreaButtons", constant = @Constant(intValue = -9), require = 0)
    private int jdte$areaOffsetMin(int original) {
        return -UpgradeHelper.getMaxAreaOffset(baseMachineBE);
    }

    @Unique
    private int jdte$getClickerFluidBarOffset() {
        return 204;
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void jdte$renderClickerFluidBar(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
            return;
        }

        int offset = jdte$getClickerFluidBarOffset();
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 0, 0, 18, 72, 36, 72);
        if (maxMb > 0) {
            int remaining = (container.getFluidAmount() * 70) / maxMb;
            if (remaining > 0) {
                jdte$renderFluid(guiGraphics, topSectionLeft + offset + 1, topSectionTop + 5 + 72 - 1, 16, remaining);
            }
        }
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 18, 0, 18, 72, 36, 72);
    }

    @Unique
    private void jdte$renderFluid(GuiGraphics guiGraphics, int startX, int startY, int width, int height) {
        FluidStack fluidStack = container.getFluidStack();
        if (fluidStack.isEmpty() || height <= 0) return;

        net.minecraft.world.level.material.Fluid fluid = fluidStack.getFluid();
        net.minecraft.resources.ResourceLocation fluidStill = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid).getStillTexture();
        net.minecraft.client.renderer.texture.TextureAtlasSprite fluidStillSprite = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);

        com.mojang.blaze3d.vertex.PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(red, green, blue, 1.0f);

        int zLevel = 0;
        float uMin = fluidStillSprite.getU0();
        float uMax = fluidStillSprite.getU1();
        float vMin = fluidStillSprite.getV0();
        float vMax = fluidStillSprite.getV1();
        int textureWidth = fluidStillSprite.contents().width();
        int textureHeight = fluidStillSprite.contents().height();

        com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
        com.mojang.blaze3d.vertex.BufferBuilder vertexBuffer = tesselator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);

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

        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(vertexBuffer.buildOrThrow());
        poseStack.popPose();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderClickerFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
            return;
        }

        int offset = jdte$getClickerFluidBarOffset();
        if (!MiscTools.inBounds(topSectionLeft + offset, topSectionTop + 5, 18, 72, mouseX, mouseY)) {
            return;
        }

        FluidStack fluidStack = container.getFluidStack();
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(container.getFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderFilterButtonTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;
        if (!jdte$hasFilterUpgrades()) return;
        jdte$updateFilterButtonPositions();

        if (jdte$inFilterPrevButton(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.filter_prev"), mouseX, mouseY);
        } else if (jdte$inFilterNextButton(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.filter_next"), mouseX, mouseY);
        }
    }
}
