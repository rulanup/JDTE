package com.jdte.mixin;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseScreen;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
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
public abstract class BaseMachineScreenMixin<T extends BaseMachineContainer> extends BaseScreen<T> {
    @Shadow protected BaseMachineContainer container;
    @Shadow protected BaseMachineBE baseMachineBE;
    @Shadow protected int topSectionLeft;
    @Shadow protected int topSectionTop;

    @Unique private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/fluidbar.png");
    @Unique private static final ResourceLocation JDTE_SLOT = ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/gui/justslot.png");
    @Unique private boolean jdte$upgradePopupOpen = false;
    @Unique private boolean jdte$draggingUpgradePopup = false;
    @Unique private int jdte$upgradePopupX;
    @Unique private int jdte$upgradePopupY;
    @Unique private int jdte$upgradeDragOffsetX;
    @Unique private int jdte$upgradeDragOffsetY;

    protected BaseMachineScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
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
        if (jdte$upgradePopupX <= 0 || jdte$upgradePopupY <= 0) {
            jdte$upgradePopupX = getGuiLeft() + imageWidth + 8;
            jdte$upgradePopupY = Math.max(8, topSectionTop);
        }
        jdte$updateUpgradeSlots();
    }

    @Inject(method = "renderBg", at = @At("HEAD"))
    private void jdte$updateSlotsBeforeRender(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        jdte$updateUpgradeSlots();
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/direwolf20/justdirethings/client/screens/basescreens/BaseMachineScreen;drawSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", ordinal = 0))
    private void jdte$drawToggleBeforeSlots(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
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
            jdte$savePopupState();
            jdte$updateUpgradeSlots();
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
            jdte$savePopupState();
            jdte$updateUpgradeSlots();
            cir.setReturnValue(true);
            return;
        }

        if (jdte$upgradePopupOpen && jdte$inPopup(mouseX, mouseY) && !jdte$overUpgradeSlot(mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (jdte$draggingUpgradePopup) {
            jdte$upgradePopupX = Math.max(0, Math.min(width - jdte$popupWidth(), (int) mouseX - jdte$upgradeDragOffsetX));
            jdte$upgradePopupY = Math.max(0, Math.min(height - jdte$popupHeight(), (int) mouseY - jdte$upgradeDragOffsetY));
            jdte$savePopupState();
            jdte$updateUpgradeSlots();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        jdte$draggingUpgradePopup = false;
        return super.mouseReleased(mouseX, mouseY, button);
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
        int x = jdte$toggleX();
        int y = jdte$toggleY();
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF555555);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, jdte$upgradePopupOpen ? 0xFF6D8A4E : 0xFF373737);
        guiGraphics.drawString(font, Component.literal("U"), x + 6, y + 5, 0xFFFFFF, false);
    }

    @Unique
    private void jdte$drawUpgradePopup(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = jdte$upgradePopupX;
        int y = jdte$upgradePopupY;

        // Shadow
        guiGraphics.fill(x - 2, y - 2, x + jdte$popupWidth() + 2, y + jdte$popupHeight() + 2, 0x80000000);
        // Title bar
        guiGraphics.fill(x, y, x + jdte$popupWidth(), y + 16, 0xFF555555);
        // Window body
        guiGraphics.fill(x, y + 16, x + jdte$popupWidth(), y + jdte$popupHeight(), 0xFF373737);
        guiGraphics.fill(x + 1, y + 17, x + jdte$popupWidth() - 1, y + jdte$popupHeight() - 1, 0xFF2A2A2A);

        // Title
        guiGraphics.drawString(font, Component.translatable("jdte.screen.upgrades"), x + 5, y + 4, 0xFFFFFF, false);

        // Close button
        int closeX = x + jdte$popupWidth() - 12;
        int closeY = y + 3;
        guiGraphics.fill(closeX, closeY, closeX + 10, closeY + 10, 0xFF8B8B8B);
        guiGraphics.drawString(font, Component.literal("x"), closeX + 2, closeY + 1, 0xFFFFFF, false);

        // Slots
        int slotCount = jdte$getUpgradeSlotCount();
        int cols = Math.min(slotCount, 4);
        int rows = (slotCount + 3) / 4;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int i = row * cols + col;
                if (i >= slotCount) break;
                guiGraphics.blit(JDTE_SLOT, x + 9 + (col * 18), y + 22 + (row * 18), 0, 0, 18, 18);
            }
        }
    }

    @Unique
    private int jdte$getUpgradeSlotCount() {
        int count = 0;
        for (Slot menuSlot : menu.slots) {
            if (menuSlot instanceof UpgradeSlot) {
                count++;
            }
        }
        return count;
    }

    @Unique
    private void jdte$updateUpgradeSlots() {
        int slot = 0;
        int cols = 4;
        for (Slot menuSlot : menu.slots) {
            if (!(menuSlot instanceof UpgradeSlot)) {
                continue;
            }

            int x = -10000;
            int y = -10000;
            if (jdte$upgradePopupOpen) {
                int row = slot / cols;
                int col = slot % cols;
                x = jdte$upgradePopupX - getGuiLeft() + 9 + (col * 18);
                y = jdte$upgradePopupY - getGuiTop() + 22 + (row * 18);
            }
            ((SlotAccessor) menuSlot).jdte$setX(x);
            ((SlotAccessor) menuSlot).jdte$setY(y);
            slot++;
        }
    }

    @Unique
    private boolean jdte$overUpgradeSlot(double mouseX, double mouseY) {
        for (Slot menuSlot : menu.slots) {
            if (menuSlot instanceof UpgradeSlot && MiscTools.inBounds(getGuiLeft() + menuSlot.x, getGuiTop() + menuSlot.y, 16, 16, mouseX, mouseY)) {
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
        return MiscTools.inBounds(jdte$upgradePopupX, jdte$upgradePopupY, jdte$popupWidth(), 16, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inPopupClose(double mouseX, double mouseY) {
        int closeX = jdte$upgradePopupX + jdte$popupWidth() - 12;
        int closeY = jdte$upgradePopupY + 3;
        return MiscTools.inBounds(closeX, closeY, 10, 10, mouseX, mouseY);
    }

    @Unique
    private boolean jdte$inToggle(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$toggleX(), jdte$toggleY(), 18, 18, mouseX, mouseY);
    }

    @Unique
    private int jdte$toggleX() {
        return getGuiLeft() + imageWidth - 20;
    }

    @Unique
    private int jdte$toggleY() {
        return topSectionTop - 20;
    }

    @Unique
    private int jdte$popupWidth() {
        int slotCount = jdte$getUpgradeSlotCount();
        int cols = Math.min(slotCount, 4);
        return Math.max(88, 18 + (cols * 18) + 6);
    }

    @Unique
    private int jdte$popupHeight() {
        int slotCount = jdte$getUpgradeSlotCount();
        int rows = (slotCount + 3) / 4;
        return 22 + (rows * 18) + 6;
    }

    @Unique
    private int jdte$getClickerFluidBarOffset() {
        return baseMachineBE instanceof com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE ? 24 : 5;
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

        int offset = jdte$getClickerFluidBarOffset();
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 0, 0, 18, 72, 36, 72);
        if (maxMb > 0) {
            int remaining = (container.getFluidAmount() * 70) / maxMb;
            ((BaseMachineScreen<?>) (Object) this).renderFluid(guiGraphics, topSectionLeft + offset + 1, topSectionTop + 76, 16, remaining);
        }
        guiGraphics.blit(JDTE_FLUID_BAR, topSectionLeft + offset, topSectionTop + 5, 18, 0, 18, 72, 36, 72);
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
