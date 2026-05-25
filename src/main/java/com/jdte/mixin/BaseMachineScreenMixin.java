package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
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

@Mixin(BaseMachineScreen.class)
public abstract class BaseMachineScreenMixin {
    @Shadow protected BaseMachineContainer container;
    @Shadow protected BaseMachineBE baseMachineBE;
    @Shadow protected int topSectionLeft;
    @Shadow protected int topSectionTop;

    @Unique private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");
    @Unique private static final ResourceLocation JDTE_SLOT = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/justslot.png");
    @Unique private static final int POPUP_UNINITIALIZED = -1;
    @Unique private static final int SLOT_SIZE = 18;
    @Unique private static final int TITLE_HEIGHT = 16;
    @Unique private static final int CLOSE_BUTTON_SIZE = 10;
    @Unique private static final int POPUP_PADDING = 6;
    @Unique private static final int SLOT_COLS = 4;

    @Unique private boolean jdte$upgradePopupOpen = false;
    @Unique private boolean jdte$draggingUpgradePopup = false;
    @Unique private int jdte$upgradePopupX = POPUP_UNINITIALIZED;
    @Unique private int jdte$upgradePopupY = POPUP_UNINITIALIZED;
    @Unique private int jdte$upgradeDragOffsetX;
    @Unique private int jdte$upgradeDragOffsetY;
    @Unique private boolean jdte$needsSlotUpdate = true;

    @Unique
    private Font jdte$getFont() {
        return Minecraft.getInstance().font;
    }

    @Unique
    private int jdte$getImageWidth() {
        if (this instanceof AbstractContainerScreenAccessor accessor) {
            return accessor.jdte$getImageWidth();
        }
        return 176; // 默认值
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void jdte$initPopup(CallbackInfo ci) {
        // 从方块实体加载位置
        if (baseMachineBE instanceof com.jdte.common.upgrades.UpgradePositionHolder holder) {
            jdte$upgradePopupX = holder.jdte$getPopupX();
            jdte$upgradePopupY = holder.jdte$getPopupY();
            jdte$upgradePopupOpen = holder.jdte$isPopupOpen();
        }
        // 如果位置无效，使用默认位置
        if (jdte$upgradePopupX == POPUP_UNINITIALIZED || jdte$upgradePopupY == POPUP_UNINITIALIZED) {
            BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
            jdte$upgradePopupX = screen.getGuiLeft() + jdte$getImageWidth() + 8;
            jdte$upgradePopupY = Math.max(8, topSectionTop);
        }
        jdte$needsSlotUpdate = true;
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen;drawSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", ordinal = 0))
    private void jdte$drawToggleBeforeSlots(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (jdte$needsSlotUpdate) {
            jdte$updateUpgradeSlots();
            jdte$needsSlotUpdate = false;
        }
        jdte$drawUpgradeToggle(guiGraphics);
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$drawPopupAfterTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (jdte$upgradePopupOpen) {
            jdte$drawUpgradePopup(guiGraphics, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        if (jdte$inToggle(mouseX, mouseY)) {
            jdte$upgradePopupOpen = !jdte$upgradePopupOpen;
            jdte$needsSlotUpdate = true;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inPopupTitle(mouseX, mouseY)) {
            jdte$draggingUpgradePopup = true;
            jdte$upgradeDragOffsetX = (int) mouseX - jdte$upgradePopupX;
            jdte$upgradeDragOffsetY = (int) mouseY - jdte$upgradePopupY;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inPopupClose(mouseX, mouseY)) {
            jdte$upgradePopupOpen = false;
            jdte$needsSlotUpdate = true;
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inPopup(mouseX, mouseY) && !jdte$overUpgradeSlot(mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (jdte$draggingUpgradePopup && button == 0) {
            BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
            jdte$upgradePopupX = Math.max(0, Math.min(screen.width - jdte$popupWidth(), (int) mouseX - jdte$upgradeDragOffsetX));
            jdte$upgradePopupY = Math.max(0, Math.min(screen.height - jdte$popupHeight(), (int) mouseY - jdte$upgradeDragOffsetY));
            jdte$needsSlotUpdate = true;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void jdte$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (jdte$draggingUpgradePopup) {
            jdte$draggingUpgradePopup = false;
            jdte$savePopupState();
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void jdte$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (jdte$inToggle(mouseX, mouseY) || (jdte$upgradePopupOpen && jdte$inPopup(mouseX, mouseY))) {
            cir.setReturnValue(false);
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
    private void jdte$drawUpgradeToggle(GuiGraphics guiGraphics) {
        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        int x = screen.getGuiLeft() + jdte$getImageWidth() - 20;
        int y = topSectionTop - 20;
        Font font = jdte$getFont();
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF555555);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF8B8B8B);
        guiGraphics.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, jdte$upgradePopupOpen ? 0xFF6D8A4E : 0xFF373737);
        guiGraphics.drawString(font, Component.literal("U"), x + 6, y + 5, 0xFFFFFF, false);
    }

    @Unique
    private void jdte$drawUpgradePopup(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = jdte$upgradePopupX;
        int y = jdte$upgradePopupY;
        int width = jdte$popupWidth();
        int height = jdte$popupHeight();
        Font font = jdte$getFont();

        // Shadow
        guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x80000000);
        // Title bar
        guiGraphics.fill(x, y, x + width, y + TITLE_HEIGHT, 0xFF555555);
        // Window body
        guiGraphics.fill(x, y + TITLE_HEIGHT, x + width, y + height, 0xFF373737);
        guiGraphics.fill(x + 1, y + TITLE_HEIGHT + 1, x + width - 1, y + height - 1, 0xFF2A2A2A);

        // Title
        guiGraphics.drawString(font, Component.translatable("jdte.screen.upgrades"), x + 5, y + 4, 0xFFFFFF, false);

        // Close button
        int closeX = x + width - 12;
        int closeY = y + 3;
        guiGraphics.fill(closeX, closeY, closeX + CLOSE_BUTTON_SIZE, closeY + CLOSE_BUTTON_SIZE, 0xFF8B8B8B);
        guiGraphics.drawString(font, Component.literal("x"), closeX + 2, closeY + 1, 0xFFFFFF, false);

        // Slots
        int slotCount = jdte$getUpgradeSlotCount();
        int cols = Math.min(slotCount, SLOT_COLS);
        int rows = (slotCount + SLOT_COLS - 1) / SLOT_COLS;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int i = row * cols + col;
                if (i >= slotCount) break;
                guiGraphics.blit(JDTE_SLOT, x + 9 + (col * SLOT_SIZE), y + 22 + (row * SLOT_SIZE), 0, 0, SLOT_SIZE, SLOT_SIZE);
            }
        }
    }

    @Unique
    private int jdte$getUpgradeSlotCount() {
        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        int count = 0;
        for (Slot menuSlot : screen.getMenu().slots) {
            if (menuSlot instanceof UpgradeSlot) {
                count++;
            }
        }
        return count;
    }

    @Unique
    private void jdte$updateUpgradeSlots() {
        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        int slot = 0;
        for (Slot menuSlot : screen.getMenu().slots) {
            if (!(menuSlot instanceof UpgradeSlot)) {
                continue;
            }

            int x = -10000;
            int y = -10000;
            if (jdte$upgradePopupOpen) {
                int row = slot / SLOT_COLS;
                int col = slot % SLOT_COLS;
                x = jdte$upgradePopupX - screen.getGuiLeft() + 9 + (col * SLOT_SIZE);
                y = jdte$upgradePopupY - screen.getGuiTop() + 22 + (row * SLOT_SIZE);
            }
            ((SlotAccessor) menuSlot).jdte$setX(x);
            ((SlotAccessor) menuSlot).jdte$setY(y);
            slot++;
        }
    }

    @Unique
    private boolean jdte$overUpgradeSlot(double mouseX, double mouseY) {
        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        for (Slot menuSlot : screen.getMenu().slots) {
            if (menuSlot instanceof UpgradeSlot && MiscTools.inBounds(screen.getGuiLeft() + menuSlot.x, screen.getGuiTop() + menuSlot.y, SLOT_SIZE - 2, SLOT_SIZE - 2, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean jdte$inPopup(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$upgradePopupX, jdte$upgradePopupY, jdte$popupWidth(), jdte$popupHeight(), mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inPopupTitle(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$upgradePopupX, jdte$upgradePopupY, jdte$popupWidth(), TITLE_HEIGHT, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inPopupClose(double mouseX, double mouseY) {
        int closeX = jdte$upgradePopupX + jdte$popupWidth() - 12;
        int closeY = jdte$upgradePopupY + 3;
        return MiscTools.inBounds(closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inToggle(double mouseX, double mouseY) {
        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        int x = screen.getGuiLeft() + jdte$getImageWidth() - 20;
        int y = topSectionTop - 20;
        return MiscTools.inBounds(x, y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY);
    }

    @Unique
    private int jdte$popupWidth() {
        int slotCount = jdte$getUpgradeSlotCount();
        int cols = Math.min(slotCount, SLOT_COLS);
        return Math.max(88, TITLE_HEIGHT + POPUP_PADDING + (cols * SLOT_SIZE) + POPUP_PADDING);
    }

    @Unique
    private int jdte$popupHeight() {
        int slotCount = jdte$getUpgradeSlotCount();
        int rows = (slotCount + SLOT_COLS - 1) / SLOT_COLS;
        return 22 + (rows * SLOT_SIZE) + POPUP_PADDING;
    }

    @Unique
    private void jdte$savePopupState() {
        if (baseMachineBE instanceof com.jdte.common.upgrades.UpgradePositionHolder holder) {
            holder.jdte$setPopupX(jdte$upgradePopupX);
            holder.jdte$setPopupY(jdte$upgradePopupY);
            holder.jdte$setPopupOpen(jdte$upgradePopupOpen);
        }
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void jdte$renderClickerFluidBar(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
            return;
        }

        int offset = baseMachineBE instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE ? 24 : 5;
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 0, 0, 18, 72, 36, 72);
        if (maxMb > 0) {
            int remaining = (container.getFluidAmount() * 70) / maxMb;
            // 自行实现流体条绘制
            jdte$renderFluidBar(guiGraphics, topSectionLeft + offset + 1, topSectionTop + 76, 16, remaining);
        }
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 18, 0, 18, 72, 36, 72);
    }

    @Unique
    private void jdte$renderFluidBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (height <= 0) return;
        guiGraphics.fill(x, y - height, x + width, y, 0xFF3ABDFE); // 蓝色流体条
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderClickerFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
            return;
        }

        BaseMachineScreen<?> screen = (BaseMachineScreen<?>) (Object) this;
        int offset = baseMachineBE instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE ? 24 : 5;
        if (!MiscTools.inBounds(topSectionLeft + offset, topSectionTop + 5, 18, 72, mouseX, mouseY)) {
            return;
        }

        FluidStack fluidStack = container.getFluidStack();
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        guiGraphics.renderTooltip(jdte$getFont(), Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(container.getFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }
}
