package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.UpgradePopupDragHandler;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.utils.UpgradeSlotStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
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
    @Unique private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    @Unique private static final Map<Slot, int[]> JDTE_ORIGINAL_SLOT_POSITIONS = new WeakHashMap<>();
    @Unique private static final Map<String, int[]> JDTE_UPGRADE_POPUP_POSITIONS = new HashMap<>();
    @Unique private static final Map<String, int[]> JDTE_FILTER_POPUP_POSITIONS = new HashMap<>();
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
    @Unique private boolean jdte$filterPopupOpen = false;
    @Unique private boolean jdte$draggingFilterPopup = false;
    @Unique private int jdte$filterPopupX = JDTE_POPUP_UNINITIALIZED;
    @Unique private int jdte$filterPopupY = JDTE_POPUP_UNINITIALIZED;
    @Unique private int jdte$filterDragOffsetX;
    @Unique private int jdte$filterDragOffsetY;

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
    }

    @Inject(method = "renderBg", at = @At("HEAD"))
    private void jdte$prepareDynamicLayout(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;
        jdte$layoutSlots();
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen;drawSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", ordinal = 0))
    private void jdte$renderUpgradePopup(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        jdte$renderFilterPopup(guiGraphics);

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

    @Unique
    private void jdte$renderFilterPopup(GuiGraphics guiGraphics) {
        if (jdte$getActiveFilterSlots() <= 0) return;
        jdte$ensureFilterPopupPosition();

        int toggleX = jdte$getFilterToggleX();
        int toggleY = jdte$getFilterToggleY();
        guiGraphics.blitSprite(SOCIALBACKGROUND, toggleX, toggleY, 56, 20);
        guiGraphics.drawString(font, Component.translatable("jdte.panel.filter"), toggleX + 14, toggleY + 6, jdte$filterPopupOpen ? 0x2D4A1F : 4210752, false);

        if (!jdte$filterPopupOpen) return;

        int x = jdte$getFilterPopupX();
        int y = jdte$getFilterPopupY();
        int width = jdte$getFilterPopupWidth();
        int height = jdte$getFilterPopupHeight();
        Component title = Component.translatable("jdte.panel.filter");
        guiGraphics.blitSprite(SOCIALBACKGROUND, x + 20, y - 20, width - 40, 20);
        guiGraphics.blitSprite(SOCIALBACKGROUND, x, y, width, height);
        jdte$drawFilterSlotBackgrounds(guiGraphics);
        guiGraphics.drawString(font, title, x + 20 + (width - 40 - font.width(title)) / 2, y - 14, 4210752, false);
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
    private void jdte$drawFilterSlotBackgrounds(GuiGraphics guiGraphics) {
        int activeSlots = jdte$getActiveFilterSlots();
        if (activeSlots <= 0) return;
        int x = jdte$getFilterPopupX() + 8;
        int y = jdte$getFilterPopupY() + 8;
        for (int i = 0; i < activeSlots; i++) {
            int col = i % JDTE_FILTER_COLUMNS;
            int row = i / JDTE_FILTER_COLUMNS;
            guiGraphics.blitSprite(SLOT_SPRITE, x + col * JDTE_SLOT_SIZE, y + row * JDTE_SLOT_SIZE, 18, 18);
        }
    }

    @Unique
    private void jdte$layoutSlots() {
        int upgradeSlotIndex = 0;
        int filterSlotIndex = 0;
        int upgradeSlots = jdte$getUpgradeSlots();
        int upgradeCols = Math.min(JDTE_UPGRADE_COLUMNS, upgradeSlots);
        jdte$ensureUpgradePopupPosition();
        jdte$ensureFilterPopupPosition();
        int upgradeStartX = jdte$getUpgradePopupX() + 8;
        int upgradeStartY = jdte$getUpgradePopupY() + 8;
        int filterStartX = jdte$getFilterPopupX() + 8;
        int filterStartY = jdte$getFilterPopupY() + 8;

        for (Slot slot : container.slots) {
            int[] original = jdte$getOriginalSlotPosition(slot);
            SlotAccessor slotAccessor = (SlotAccessor) slot;

            if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
                slotAccessor.jdte$setX(-10000);
                slotAccessor.jdte$setY(-10000);
            } else if (slot instanceof DynamicFilterSlot) {
                if (jdte$filterPopupOpen) {
                    int col = filterSlotIndex % JDTE_FILTER_COLUMNS;
                    int row = filterSlotIndex / JDTE_FILTER_COLUMNS;
                    slotAccessor.jdte$setX(filterStartX + col * JDTE_SLOT_SIZE - getGuiLeft());
                    slotAccessor.jdte$setY(filterStartY + row * JDTE_SLOT_SIZE - getGuiTop());
                } else {
                    slotAccessor.jdte$setX(-10000);
                    slotAccessor.jdte$setY(-10000);
                }
                filterSlotIndex++;
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
    private int jdte$getFilterToggleX() {
        return Math.max(4, getGuiLeft() - 64);
    }

    @Unique
    private int jdte$getFilterToggleY() {
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
    private int jdte$getFilterPopupX() {
        return jdte$filterPopupX;
    }

    @Unique
    private int jdte$getFilterPopupY() {
        return jdte$filterPopupY;
    }

    @Unique
    private int jdte$getFilterPopupWidth() {
        int cols = Math.min(JDTE_FILTER_COLUMNS, Math.max(1, jdte$getActiveFilterSlots()));
        return cols * JDTE_SLOT_SIZE + 16;
    }

    @Unique
    private int jdte$getFilterPopupHeight() {
        return jdte$getFilterRows() * JDTE_SLOT_SIZE + 16;
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
    private void jdte$ensureFilterPopupPosition() {
        int popupWidth = jdte$getFilterPopupWidth();
        int popupHeight = jdte$getFilterPopupHeight() + 20;
        if (jdte$filterPopupX == JDTE_POPUP_UNINITIALIZED || jdte$filterPopupY == JDTE_POPUP_UNINITIALIZED) {
            int[] saved = JDTE_FILTER_POPUP_POSITIONS.get(jdte$getPopupMemoryKey());
            if (saved != null) {
                jdte$filterPopupX = saved[0];
                jdte$filterPopupY = saved[1];
            } else {
                jdte$filterPopupX = Math.max(4, getGuiLeft() - popupWidth - 8);
                jdte$filterPopupY = Math.max(28, topSectionTop + 72);
            }
        }
        jdte$filterPopupX = Math.max(0, Math.min(width - popupWidth - 4, jdte$filterPopupX));
        jdte$filterPopupY = Math.max(24, Math.min(height - popupHeight - 4, jdte$filterPopupY));
    }

    @Unique
    private int jdte$getMaxFilterRows() {
        if (container == null || container.filterHandler == null) return 0;
        int baseSlots = UpgradeSlotStorage.getBaseFilterSlots(container);
        if (baseSlots <= 0) {
            baseSlots = UpgradeHelper.getBaseFilterSlots(container.filterHandler);
        }
        int maxFilterSlots = UpgradeHelper.getMaxFilterSlots(baseSlots);
        return (maxFilterSlots + 8) / 9;
    }

    @Unique
    private int jdte$getActiveFilterSlots() {
        if (container == null) return 0;
        int count = 0;
        for (Slot slot : container.slots) {
            if (slot instanceof DynamicFilterSlot filterSlot && filterSlot.isActive()) {
                count++;
            }
        }
        return count;
    }

    @Unique
    private int jdte$getFilterRows() {
        int filterSlots = jdte$getActiveFilterSlots();
        if (filterSlots <= 0) return 0;
        return (filterSlots + JDTE_FILTER_COLUMNS - 1) / JDTE_FILTER_COLUMNS;
    }

    @Unique
    private int jdte$getUpgradeRows() {
        int upgradeSlots = jdte$getUpgradeSlots();
        if (upgradeSlots <= 0) return 0;
        return (upgradeSlots + JDTE_UPGRADE_COLUMNS - 1) / JDTE_UPGRADE_COLUMNS;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || jdte$getUpgradeSlots() <= 0) return;

        if (jdte$inUpgradeToggle(mouseX, mouseY)) {
            jdte$upgradePopupOpen = !jdte$upgradePopupOpen;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$inFilterToggle(mouseX, mouseY)) {
            jdte$filterPopupOpen = !jdte$filterPopupOpen;
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

        if (jdte$filterPopupOpen && jdte$inFilterPopupTitle(mouseX, mouseY)) {
            jdte$draggingFilterPopup = true;
            jdte$filterDragOffsetX = (int) mouseX - jdte$filterPopupX;
            jdte$filterDragOffsetY = (int) mouseY - jdte$filterPopupY;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inUpgradePopup(mouseX, mouseY) && !jdte$overUpgradeSlot(mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (jdte$filterPopupOpen && jdte$inFilterPopup(mouseX, mouseY) && !jdte$overFilterSlot(mouseX, mouseY)) {
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
        if (jdte$draggingFilterPopup && button == 0) {
            jdte$filterPopupX = (int) mouseX - jdte$filterDragOffsetX;
            jdte$filterPopupY = (int) mouseY - jdte$filterDragOffsetY;
            jdte$ensureFilterPopupPosition();
            return true;
        }
        return false;
    }

    @Override
    public void jdte$releaseUpgradePopup(int button) {
        if (button == 0) {
            jdte$draggingUpgradePopup = false;
            jdte$draggingFilterPopup = false;
            jdte$savePopupPositions();
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void jdte$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (jdte$inUpgradeToggle(mouseX, mouseY)
                || jdte$inFilterToggle(mouseX, mouseY)
                || (jdte$upgradePopupOpen && jdte$inUpgradePopup(mouseX, mouseY))
                || (jdte$filterPopupOpen && jdte$inFilterPopup(mouseX, mouseY))) {
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
    private boolean jdte$inFilterToggle(double mouseX, double mouseY) {
        return jdte$getActiveFilterSlots() > 0 && MiscTools.inBounds(jdte$getFilterToggleX(), jdte$getFilterToggleY(), 56, 20, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inFilterPopup(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getFilterPopupX(), jdte$getFilterPopupY() - 20, jdte$getFilterPopupWidth(), jdte$getFilterPopupHeight() + 20, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inFilterPopupTitle(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getFilterPopupX(), jdte$getFilterPopupY() - 20, jdte$getFilterPopupWidth(), 20, mouseX, mouseY);
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
    private boolean jdte$overFilterSlot(double mouseX, double mouseY) {
        for (Slot slot : container.slots) {
            if (slot instanceof DynamicFilterSlot && MiscTools.inBounds(getGuiLeft() + slot.x, getGuiTop() + slot.y, JDTE_SLOT_SIZE, JDTE_SLOT_SIZE, mouseX, mouseY)) {
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
        if (jdte$filterPopupX != JDTE_POPUP_UNINITIALIZED && jdte$filterPopupY != JDTE_POPUP_UNINITIALIZED) {
            JDTE_FILTER_POPUP_POSITIONS.put(jdte$getPopupMemoryKey(), new int[]{jdte$filterPopupX, jdte$filterPopupY});
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

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderUpgradeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;

        int upgradeSlots = jdte$getUpgradeSlots();
        if (upgradeSlots <= 0) return;

        // 检查鼠标是否在升级槽上
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
        return baseMachineBE instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE ? 24 : 5;
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
