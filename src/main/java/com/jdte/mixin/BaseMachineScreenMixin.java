package com.jdte.mixin;

import com.jdte.common.network.JDTEPacketHandler;
import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.AutoIoConfigClientCache;
import com.jdte.client.AutoIoConfigScreenBridge;
import com.jdte.client.UpgradePopupDragHandler;
import com.jdte.client.screens.util.AutoIoConfigPanelHelper;
import com.jdte.client.screens.util.FilterPageWidgetHelper;
import com.jdte.client.screens.util.GuiSpriteCompat;
import com.jdte.client.screens.util.MachineFluidBarRenderer;
import com.jdte.client.screens.util.UpgradeSlotLayoutHelper;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.containers.BioCrusherContainer;
import com.jdte.common.containers.BioFactoryContainer;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.containers.FilterPageHolder;
import com.jdte.common.containers.GreenhouseContainer;
import com.jdte.common.containers.LootFabricatorContainer;
import com.jdte.common.containers.LargeGreenhouseContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.UpgradeSlotStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;
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
import java.util.Collections;
import java.util.List;

@Mixin(value = BaseMachineScreen.class, remap = false)
public abstract class BaseMachineScreenMixin extends AbstractContainerScreenMixin implements UpgradePopupDragHandler, AutoIoConfigScreenBridge {
    @Shadow protected BaseMachineContainer container;
    @Shadow protected BaseMachineBE baseMachineBE;
    @Shadow protected int topSectionLeft;
    @Shadow protected int topSectionTop;
    @Shadow protected int topSectionHeight;
    @Shadow protected int extraWidth;
    @Shadow protected int extraHeight;
    @Shadow protected ResourceLocation BACKGROUND_SPRITE;

    @Unique private static final ResourceLocation JDTE_IO_CONFIG = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/hammer3.png");
    @Unique private int jdte$filterPressed = 0;
    @Unique private static final int JDTE_SLOT_SIZE = 18;
    @Unique private int jdte$baseImageHeight = -1;
    @Unique private int jdte$filterPrevX;
    @Unique private int jdte$filterNextX;
    @Unique private int jdte$filterButtonsY;
    @Unique private boolean jdte$ioConfigOpen;
    @Unique private int jdte$ioConfigButtonX;
    @Unique private int jdte$ioConfigButtonY;
    @Unique private boolean jdte$rebuildingForResize;

    @Unique
    private int jdte$getUpgradeSlots() {
        if (container == null) return 0;
        return UpgradeSlotStorage.getUpgradeSlots(container);
    }

    @Inject(remap = false, method = "init", at = @At("HEAD"))
    private void jdte$expandLowerSection(CallbackInfo ci) {
        if (jdte$baseImageHeight < 0) {
            jdte$baseImageHeight = imageHeight;
        }
        imageHeight = jdte$baseImageHeight;
    }

    @Inject(remap = false, method = "setTopSection", at = @At("TAIL"))
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

    @Inject(remap = false, method = "renderBg", at = @At("HEAD"))
    private void jdte$prepareDynamicLayout(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        jdte$refreshStaleLayout();
        if (container == null || baseMachineBE == null) return;
        jdte$clampFilterPage();
        UpgradeSlotLayoutHelper.layoutSlots(container, jdte$getUpgradeSlots());
    }

    @Inject(remap = false, method = "renderInventoryBackground", at = @At("HEAD"), cancellable = true)
    private void jdte$renderForgeInventoryBackground(GuiGraphics guiGraphics, int guiLeft, int guiTop, CallbackInfo ci) {
        int inventoryHeight = imageHeight - 73;
        if (inventoryHeight > 0) {
            // Forge JDT's atlas no longer contains the framed inventory backdrop from the 1.21 build.
            // Draw the shared panel frame here; BaseMachineScreen still renders every inventory slot normally.
            GuiSpriteCompat.blitSprite(guiGraphics, BACKGROUND_SPRITE, guiLeft, guiTop + 75, imageWidth, inventoryHeight);
        }
        ci.cancel();
    }

    /**
     * Forge 1.20.1 can keep an open container screen alive across a window resize.
     * Rebuild the widgets if its logical bounds or container origin missed that resize.
     */
    @Unique
    private void jdte$refreshStaleLayout() {
        if (jdte$rebuildingForResize) return;

        Minecraft minecraft = Minecraft.getInstance();
        int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
        int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
        if (scaledWidth <= 0 || scaledHeight <= 0) return;

        int expectedLeft = (width - imageWidth) / 2;
        int expectedTop = (height - imageHeight) / 2;
        boolean staleWindowBounds = width != scaledWidth || height != scaledHeight;
        boolean staleGuiOrigin = getGuiLeft() != expectedLeft || getGuiTop() != expectedTop;
        if (!staleWindowBounds && !staleGuiOrigin) return;

        jdte$rebuildingForResize = true;
        try {
            ((Screen) (Object) this).resize(minecraft, scaledWidth, scaledHeight);
        } finally {
            jdte$rebuildingForResize = false;
        }
    }

    @Inject(remap = false, method = "renderBg", at = @At("TAIL"))
    private void jdte$renderUpgradePopup(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        int slots = jdte$getUpgradeSlots();
        if (slots <= 0) return;
        UpgradeSlotLayoutHelper.renderFixedUpgradePanels(guiGraphics, slots, getGuiLeft(), getGuiTop());
    }

    @Inject(remap = false, method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void jdte$hideInactiveFilterSlots(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
            ci.cancel();
        }
    }

    @Inject(remap = false, method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void jdte$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;

        if (jdte$hasIoConfigTarget()) {
            jdte$updateIoConfigButtonPosition();
            if (jdte$inIoConfigButton(mouseX, mouseY)) {
                jdte$ioConfigOpen = !jdte$ioConfigOpen;
                jdte$playClickSound();
                cir.setReturnValue(true);
                return;
            }
            if (jdte$ioConfigOpen) {
                int side = AutoIoConfigPanelHelper.getSideAt(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), mouseX, mouseY);
                if (side >= 0) {
                    jdte$toggleIoConfigSide(side);
                    jdte$playClickSound();
                    cir.setReturnValue(true);
                    return;
                }
                if (jdte$inIoConfigPanel(mouseX, mouseY)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (jdte$hasFilterUpgrades()) {
            jdte$updateFilterButtonPositions();
            int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
            int maxPage = FilterPageWidgetHelper.getMaxFilterPage(container, baseMachineBE, slotsPerPage);
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

    @Override
    public List<Rect2i> jdte$getAutoIoConfigExtraAreas() {
        if (!jdte$hasIoConfigTarget()) {
            return Collections.emptyList();
        }

        jdte$updateIoConfigButtonPosition();
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(jdte$ioConfigButtonX, jdte$ioConfigButtonY, AutoIoConfigPanelHelper.BUTTON_SIZE, AutoIoConfigPanelHelper.BUTTON_SIZE));
        if (jdte$ioConfigOpen) {
            areas.add(new Rect2i(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), AutoIoConfigPanelHelper.PANEL_SIZE, AutoIoConfigPanelHelper.PANEL_SIZE));
        }
        return areas;
    }

    @Inject(remap = false, method = "init", at = @At("TAIL"))
    private void jdte$initFilterPage(CallbackInfo ci) {
        jdte$updateFilterButtonPositions();
        if (jdte$hasIoConfigTarget()) {
            AutoIoConfigClientCache.requestSync(baseMachineBE);
        } else {
            jdte$ioConfigOpen = false;
        }
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
    private void jdte$changeFilterPage(int delta, int slotsPerPage, int maxPage) {
        int newPage = Math.max(0, Math.min(maxPage, jdte$getFilterPage() + delta));
        if (newPage == jdte$getFilterPage()) return;
        if (container instanceof FilterPageHolder holder) {
            holder.jdte$setFilterPage(newPage);
        }
        UpgradeSlotLayoutHelper.layoutSlots(container, jdte$getUpgradeSlots());
        JDTEPacketHandler.CHANNEL.sendToServer(new FilterPagePayload(newPage));
        jdte$playClickSound();
    }

    @Unique
    private void jdte$clampFilterPage() {
        if (container instanceof BioCrusherContainer || container instanceof LootFabricatorContainer
                || container instanceof GreenhouseContainer || container instanceof BioFactoryContainer
                || container instanceof LargeGreenhouseContainer) return;
        if (!(container instanceof FilterPageHolder holder)) return;
        if (!jdte$hasFilterUpgrades()) {
            if (holder.jdte$getFilterPage() != 0) {
                holder.jdte$setFilterPage(0);
                JDTEPacketHandler.CHANNEL.sendToServer(new FilterPagePayload(0));
            }
            return;
        }
        int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
        int maxPage = FilterPageWidgetHelper.getMaxFilterPage(container, baseMachineBE, slotsPerPage);
        if (holder.jdte$getFilterPage() > maxPage) {
            holder.jdte$setFilterPage(0);
            JDTEPacketHandler.CHANNEL.sendToServer(new FilterPagePayload(0));
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

    @Inject(remap = false, method = "renderBg", at = @At("TAIL"))
    private void jdte$renderFilterPageButtons(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (container == null || baseMachineBE == null) return;
        if (!jdte$hasFilterUpgrades()) return;
        jdte$updateFilterButtonPositions();

        int slotsPerPage = UpgradeHelper.getFilterSlotsPerUpgrade();
        int maxPage = FilterPageWidgetHelper.getMaxFilterPage(container, baseMachineBE, slotsPerPage);
        int currentPage = jdte$getFilterPage();

        FilterPageWidgetHelper.renderButtons(guiGraphics, font, jdte$filterPrevX, jdte$filterNextX, jdte$filterButtonsY,
                currentPage, maxPage, jdte$filterPressed);
    }

    @Inject(remap = false, method = "renderBg", at = @At("TAIL"))
    private void jdte$renderIoConfig(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!jdte$hasIoConfigTarget()) return;
        jdte$updateIoConfigButtonPosition();
        if (jdte$ioConfigOpen) {
            AutoIoConfigPanelHelper.renderPanel(guiGraphics, BACKGROUND_SPRITE, jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(),
                    AutoIoConfigClientCache.getInputMask(baseMachineBE), AutoIoConfigClientCache.getOutputMask(baseMachineBE));
        }
        AutoIoConfigPanelHelper.drawSmallIconButton(guiGraphics, jdte$ioConfigButtonX, jdte$ioConfigButtonY, JDTE_IO_CONFIG, true);
    }

    @Unique
    private boolean jdte$hasIoConfigTarget() {
        return container != null && AutoIoConfigHelper.hasConfigurableIo(baseMachineBE);
    }

    @Unique
    private void jdte$updateIoConfigButtonPosition() {
        jdte$ioConfigButtonX = getGuiLeft() + 8 - 14 - 16;
        jdte$ioConfigButtonY = getGuiTop() + 56;
    }

    @Unique
    private boolean jdte$inIoConfigButton(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$ioConfigButtonX, jdte$ioConfigButtonY, AutoIoConfigPanelHelper.BUTTON_SIZE, AutoIoConfigPanelHelper.BUTTON_SIZE, mouseX, mouseY);
    }

    @Unique
    private int jdte$getIoConfigPanelX() {
        return topSectionLeft - AutoIoConfigPanelHelper.PANEL_SIZE;
    }

    @Unique
    private int jdte$getIoConfigPanelY() {
        return topSectionTop + topSectionHeight - AutoIoConfigPanelHelper.PANEL_SIZE;
    }

    @Unique
    private boolean jdte$inIoConfigPanel(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), AutoIoConfigPanelHelper.PANEL_SIZE, AutoIoConfigPanelHelper.PANEL_SIZE, mouseX, mouseY);
    }

    @Unique
    private void jdte$toggleIoConfigSide(int side) {
        AutoIoConfigHelper.IoMasks masks = AutoIoConfigHelper.cycleSide(
                AutoIoConfigClientCache.getInputMask(baseMachineBE), AutoIoConfigClientCache.getOutputMask(baseMachineBE), side,
                AutoIoConfigHelper.supportsInput(baseMachineBE),
                AutoIoConfigHelper.supportsOutput(baseMachineBE));
        AutoIoConfigClientCache.updateAndSend(baseMachineBE, masks.inputMask(), masks.outputMask());
    }

    @Unique
    private void jdte$playClickSound() {
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Inject(remap = false, method = "renderTooltip", at = @At("TAIL"))
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
                        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(
                                UpgradeSlotLayoutHelper.buildEmptyUpgradeSlotTooltip(baseMachineBE)), mouseX, mouseY);
                    }
                    return;
                }
            }
        }
    }

    @ModifyConstant(remap = false, method = "addAreaButtons", constant = @Constant(doubleValue = 5.0D), require = 0)
    private double jdte$areaRadiusMax(double original) {
        return UpgradeHelper.getMaxAreaRadius(baseMachineBE);
    }

    @ModifyConstant(remap = false, method = "addAreaButtons", constant = @Constant(intValue = 9), require = 0)
    private int jdte$areaOffsetMax(int original) {
        return UpgradeHelper.getMaxAreaOffset(baseMachineBE);
    }

    @ModifyConstant(remap = false, method = "addAreaButtons", constant = @Constant(intValue = -9), require = 0)
    private int jdte$areaOffsetMin(int original) {
        return -UpgradeHelper.getMaxAreaOffset(baseMachineBE);
    }

    @Unique
    private int jdte$getClickerFluidBarOffset() {
        return 204;
    }

    @Inject(remap = false, method = "renderBg", at = @At("TAIL"))
    private void jdte$renderClickerFluidBar(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!UpgradeHelper.hasFluidStorageUpgrade(baseMachineBE)) {
            return;
        }

        int offset = jdte$getClickerFluidBarOffset();
        int maxMb = UpgradeHelper.getClickerFluidCapacity(baseMachineBE);
        MachineFluidBarRenderer.renderBar(guiGraphics, container.getFluidStack(), container.getFluidAmount(), maxMb,
                topSectionLeft + offset, topSectionTop + 5);
    }

    @Inject(remap = false, method = "renderTooltip", at = @At("TAIL"))
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
                Component.translatable("justdirethings.screen.fluid", fluidStack.getDisplayName(), MagicHelpers.withSuffix(container.getFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }

    @Inject(remap = false, method = "renderTooltip", at = @At("TAIL"))
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

    @Inject(remap = false, method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderIoConfigTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!jdte$hasIoConfigTarget()) return;
        jdte$updateIoConfigButtonPosition();

        if (jdte$inIoConfigButton(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.io_config"), mouseX, mouseY);
            return;
        }
        if (!jdte$ioConfigOpen) return;

        int side = AutoIoConfigPanelHelper.getSideAt(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), mouseX, mouseY);
        if (side >= 0) {
            int mode = AutoIoConfigHelper.getMode(
                    AutoIoConfigClientCache.getInputMask(baseMachineBE), AutoIoConfigClientCache.getOutputMask(baseMachineBE), side);
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable(AutoIoConfigPanelHelper.getSideTranslationKey(side)),
                    Component.translatable(AutoIoConfigPanelHelper.getModeTranslationKey(mode)),
                    Component.translatable(AutoIoConfigPanelHelper.getAvailabilityTranslationKey(
                            AutoIoConfigHelper.supportsInput(baseMachineBE),
                            AutoIoConfigHelper.supportsOutput(baseMachineBE))).withStyle(ChatFormatting.GRAY)
            )), mouseX, mouseY);
        }
    }
}
