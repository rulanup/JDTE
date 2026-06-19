package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.UpgradePopupDragHandler;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.containers.FilterPageHolder;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.UpgradeSlotStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
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

import java.util.Arrays;
import java.util.HashMap;
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
    @Unique private static final Map<String, int[]> JDTE_UPGRADE_POPUP_POSITIONS = new HashMap<>();
    @Unique private static final int JDTE_POPUP_UNINITIALIZED = -1;
    @Unique private static final int JDTE_SLOT_SIZE = 18;
    @Unique private static final int JDTE_UPGRADE_COLUMNS = 4;
    @Unique private static final int JDTE_FILTER_COLUMNS = 9;
    @Unique private int jdte$baseImageHeight = -1;
    @Unique private boolean jdte$upgradePopupOpen = false;
    @Unique private boolean jdte$draggingUpgradePopup = false;
    @Unique private int jdte$upgradePopupX = JDTE_POPUP_UNINITIALIZED;
    @Unique private int jdte$upgradePopupY = JDTE_POPUP_UNINITIALIZED;
    @Unique private int jdte$upgradeDragOffsetX;
    @Unique private int jdte$upgradeDragOffsetY;
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
        jdte$layoutSlots();
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen;drawSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", ordinal = 0))
    private void jdte$renderUpgradePopup(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (jdte$getUpgradeSlots() <= 0) return;
        jdte$ensureUpgradePopupPosition();

        int toggleX = jdte$getUpgradeToggleX();
        int toggleY = jdte$getUpgradeToggleY();
        guiGraphics.blitSprite(SOCIALBACKGROUND, toggleX, toggleY, 56, 20);
        guiGraphics.drawString(font, Component.translatable("jdte.panel.upgrade"), toggleX + 11, toggleY + 6, jdte$upgradePopupOpen ? 0x2D4A1F : 4210752, false);

        if (!jdte$upgradePopupOpen) return;

        int x = jdte$getUpgradePopupX();
        int y = jdte$getUpgradePopupY();
        int width = jdte$getUpgradePopupWidth();
        int height = jdte$getUpgradePopupHeight();
        guiGraphics.blitSprite(SOCIALBACKGROUND, x + 20, y - 20, width - 40, 20);
        guiGraphics.blitSprite(SOCIALBACKGROUND, x, y, width, height);
        jdte$drawUpgradeSlotBackgrounds(guiGraphics);
        guiGraphics.drawString(font, Component.translatable("jdte.panel.upgrade"), x + 20 + (width - 40 - font.width(Component.translatable("jdte.panel.upgrade"))) / 2, y - 14, 4210752, false);
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void jdte$hideInactiveFilterSlots(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
            ci.cancel();
        }
    }

    @Unique
    private void jdte$drawUpgradeSlotBackgrounds(GuiGraphics guiGraphics) {
        int slots = jdte$getUpgradeSlots();
        int cols = Math.min(JDTE_UPGRADE_COLUMNS, Math.max(1, slots));
        int x = jdte$getUpgradePopupX() + 8;
        int y = jdte$getUpgradePopupY() + 8;
        for (int i = 0; i < slots; i++) {
            int col = i % cols;
            int row = i / cols;
            guiGraphics.blitSprite(SLOT_SPRITE, x + col * JDTE_SLOT_SIZE, y + row * JDTE_SLOT_SIZE, 18, 18);
        }
    }

    @Unique
    private void jdte$layoutSlots() {
        int upgradeSlotIndex = 0;
        int upgradeSlots = jdte$getUpgradeSlots();
        int upgradeCols = Math.min(JDTE_UPGRADE_COLUMNS, upgradeSlots);
        jdte$ensureUpgradePopupPosition();
        int upgradeStartX = jdte$getUpgradePopupX() + 8;
        int upgradeStartY = jdte$getUpgradePopupY() + 8;

        for (Slot slot : container.slots) {
            int[] original = jdte$getOriginalSlotPosition(slot);
            SlotAccessor slotAccessor = (SlotAccessor) slot;

            if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
                slotAccessor.jdte$setX(-10000);
                slotAccessor.jdte$setY(-10000);
            } else if (slot instanceof UpgradeSlot) {
                if (jdte$upgradePopupOpen) {
                    int col = upgradeSlotIndex % upgradeCols;
                    int row = upgradeSlotIndex / upgradeCols;
                    slotAccessor.jdte$setX(upgradeStartX + col * JDTE_SLOT_SIZE - getGuiLeft());
                    slotAccessor.jdte$setY(upgradeStartY + row * JDTE_SLOT_SIZE - getGuiTop());
                } else {
                    slotAccessor.jdte$setX(-10000);
                    slotAccessor.jdte$setY(-10000);
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

    @Unique
    private int jdte$getUpgradeToggleX() {
        return Math.min(width - 60, getGuiLeft() + imageWidth + 8);
    }

    @Unique
    private int jdte$getUpgradeToggleY() {
        return Math.max(28, topSectionTop + 16);
    }

    @Unique
    private int jdte$getUpgradePopupX() {
        return jdte$upgradePopupX;
    }

    @Unique
    private int jdte$getUpgradePopupY() {
        return jdte$upgradePopupY;
    }

    @Unique
    private int jdte$getUpgradePopupWidth() {
        int cols = Math.min(JDTE_UPGRADE_COLUMNS, Math.max(1, jdte$getUpgradeSlots()));
        return cols * JDTE_SLOT_SIZE + 16;
    }

    @Unique
    private int jdte$getUpgradePopupHeight() {
        return jdte$getUpgradeRows() * JDTE_SLOT_SIZE + 16;
    }

    @Unique
    private void jdte$ensureUpgradePopupPosition() {
        int popupWidth = jdte$getUpgradePopupWidth();
        int popupHeight = jdte$getUpgradePopupHeight() + 20;
        if (jdte$upgradePopupX == JDTE_POPUP_UNINITIALIZED || jdte$upgradePopupY == JDTE_POPUP_UNINITIALIZED) {
            int[] saved = JDTE_UPGRADE_POPUP_POSITIONS.get(jdte$getPopupMemoryKey());
            if (saved != null) {
                jdte$upgradePopupX = saved[0];
                jdte$upgradePopupY = saved[1];
            } else {
                jdte$upgradePopupX = Math.min(width - popupWidth - 4, getGuiLeft() + imageWidth + 8);
                jdte$upgradePopupY = Math.max(28, topSectionTop + 72);
            }
        }
        jdte$upgradePopupX = Math.max(0, Math.min(width - popupWidth - 4, jdte$upgradePopupX));
        jdte$upgradePopupY = Math.max(24, Math.min(height - popupHeight - 4, jdte$upgradePopupY));
    }

    @Unique
    private int jdte$getUpgradeRows() {
        int upgradeSlots = jdte$getUpgradeSlots();
        if (upgradeSlots <= 0) return 0;
        return (upgradeSlots + JDTE_UPGRADE_COLUMNS - 1) / JDTE_UPGRADE_COLUMNS;
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

        if (jdte$getUpgradeSlots() <= 0) return;

        if (jdte$inUpgradeToggle(mouseX, mouseY)) {
            jdte$upgradePopupOpen = !jdte$upgradePopupOpen;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inUpgradePopupTitle(mouseX, mouseY)) {
            jdte$draggingUpgradePopup = true;
            jdte$upgradeDragOffsetX = (int) mouseX - jdte$upgradePopupX;
            jdte$upgradeDragOffsetY = (int) mouseY - jdte$upgradePopupY;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inUpgradePopup(mouseX, mouseY) && !jdte$overUpgradeSlot(mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean jdte$dragUpgradePopup(double mouseX, double mouseY, int button) {
        if (jdte$draggingUpgradePopup && button == 0) {
            jdte$upgradePopupX = (int) mouseX - jdte$upgradeDragOffsetX;
            jdte$upgradePopupY = (int) mouseY - jdte$upgradeDragOffsetY;
            jdte$ensureUpgradePopupPosition();
            return true;
        }
        return false;
    }

    @Override
    public void jdte$releaseUpgradePopup(int button) {
        if (button == 0) {
            jdte$draggingUpgradePopup = false;
            jdte$filterPressed = 0;
            jdte$savePopupPositions();
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void jdte$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (jdte$inUpgradeToggle(mouseX, mouseY)
                || (jdte$upgradePopupOpen && jdte$inUpgradePopup(mouseX, mouseY))) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean jdte$inUpgradeToggle(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getUpgradeToggleX(), jdte$getUpgradeToggleY(), 56, 20, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inUpgradePopup(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getUpgradePopupX(), jdte$getUpgradePopupY() - 20, jdte$getUpgradePopupWidth(), jdte$getUpgradePopupHeight() + 20, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inUpgradePopupTitle(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getUpgradePopupX(), jdte$getUpgradePopupY() - 20, jdte$getUpgradePopupWidth(), 20, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$overUpgradeSlot(double mouseX, double mouseY) {
        for (Slot slot : container.slots) {
            if (slot instanceof UpgradeSlot && MiscTools.inBounds(getGuiLeft() + slot.x, getGuiTop() + slot.y, JDTE_SLOT_SIZE, JDTE_SLOT_SIZE, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private String jdte$getPopupMemoryKey() {
        if (baseMachineBE == null) return "unknown";
        String level = baseMachineBE.getLevel() == null ? "unknown" : baseMachineBE.getLevel().dimension().location().toString();
        return level + ":" + baseMachineBE.getBlockPos().asLong();
    }

    @Unique
    private void jdte$savePopupPositions() {
        if (jdte$upgradePopupX != JDTE_POPUP_UNINITIALIZED && jdte$upgradePopupY != JDTE_POPUP_UNINITIALIZED) {
            JDTE_UPGRADE_POPUP_POSITIONS.put(jdte$getPopupMemoryKey(), new int[]{jdte$upgradePopupX, jdte$upgradePopupY});
        }
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
        jdte$filterPrevX = getGuiLeft() + 8 - 22;
        jdte$filterNextX = getGuiLeft() + 8 + slotsPerPage * JDTE_SLOT_SIZE + 4;
        jdte$filterButtonsY = getGuiTop() + 54;
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
        PacketDistributor.sendToServer(new FilterPagePayload(newPage));
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Unique
    private boolean jdte$inFilterPrevButton(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$filterPrevX, jdte$filterButtonsY, 20, 18, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inFilterNextButton(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$filterNextX, jdte$filterButtonsY, 20, 18, mouseX, mouseY);
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
        if (jdte$filterPressed == -1) {
            com.mojang.blaze3d.vertex.PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(prevX + 2 + 16, y + 1 + 16, 0);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(JDTE_FILTER_NEXT, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        } else {
            guiGraphics.blit(JDTE_FILTER_PREV, prevX + 2, y + 1, 0, 0, 16, 16, 16, 16);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, nextActive ? 1f : 0.3f);
        com.mojang.blaze3d.vertex.PoseStack poseStack = guiGraphics.pose();
        if (jdte$filterPressed == 1) {
            guiGraphics.blit(JDTE_FILTER_NEXT, nextX + 2, y + 1, 0, 0, 16, 16, 16, 16);
        } else {
            poseStack.pushPose();
            poseStack.translate(nextX + 2 + 16, y + 1 + 16, 0);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(JDTE_FILTER_PREV, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.drawString(font, Component.literal((currentPage + 1) + "/" + (maxPage + 1)), nextX, y + 20, 4210752, false);
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
                        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                                Component.translatable("jdte.upgrade.slot.empty")
                        )), mouseX, mouseY);
                    }
                    return;
                }
            }
        }
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
}
