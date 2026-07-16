package com.jdte.mixin;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.AutoIoConfigClientCache;
import com.jdte.client.AutoIoConfigScreenBridge;
import com.jdte.client.UpgradePopupDragHandler;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import com.jdte.common.containers.BioCrusherContainer;
import com.jdte.common.containers.BioFactoryContainer;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.containers.FilterPageHolder;
import com.jdte.common.containers.LootFabricatorContainer;
import com.jdte.common.containers.GreenhouseContainer;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.BioFactoryUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.UpgradeSlotStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(BaseMachineScreen.class)
public abstract class BaseMachineScreenMixin extends AbstractContainerScreenMixin implements UpgradePopupDragHandler, AutoIoConfigScreenBridge {
    @Shadow protected BaseMachineContainer container;
    @Shadow protected BaseMachineBE baseMachineBE;
    @Shadow protected int topSectionLeft;
    @Shadow protected int topSectionTop;
    @Shadow protected int topSectionHeight;
    @Shadow protected int extraWidth;
    @Shadow protected int extraHeight;
    @Shadow protected ResourceLocation SOCIALBACKGROUND;

    @Unique private static final ResourceLocation JDTE_FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");
    @Unique private static final ResourceLocation JDTE_FILTER_PREV = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_prev.png");
    @Unique private static final ResourceLocation JDTE_FILTER_NEXT = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_next.png");
    @Unique private static final ResourceLocation JDTE_IO_CONFIG = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/hammer3.png");
    @Unique private static final ResourceLocation JDTE_IO_NORTH = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-north.png");
    @Unique private static final ResourceLocation JDTE_IO_SOUTH = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-south.png");
    @Unique private static final ResourceLocation JDTE_IO_WEST = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-west.png");
    @Unique private static final ResourceLocation JDTE_IO_EAST = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-east.png");
    @Unique private static final ResourceLocation JDTE_IO_UP = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-up.png");
    @Unique private static final ResourceLocation JDTE_IO_DOWN = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-down.png");
    @Unique private static final ResourceLocation JDTE_UPGRADE_SLOT_PANEL = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/upgrade_slot_panel.png");
    @Unique private int jdte$filterPressed = 0;
    @Unique private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    @Unique private static final int JDTE_UPGRADE_SLOT_PANEL_WIDTH = 32;
    @Unique private static final int JDTE_UPGRADE_SLOT_PANEL_HEIGHT = 86;
    @Unique private static final Map<Slot, int[]> JDTE_ORIGINAL_SLOT_POSITIONS = new WeakHashMap<>();
    @Unique private static final int JDTE_SLOT_SIZE = 18;
    @Unique private static final int JDTE_FILTER_COLUMNS = 9;
    @Unique private static final int JDTE_IO_BUTTON_SIZE = 12;
    @Unique private static final int JDTE_IO_BUTTON_SPACING = 12;
    @Unique private static final int JDTE_IO_PANEL_PADDING = 6;
    @Unique private static final int JDTE_IO_PANEL_SIZE = JDTE_IO_PANEL_PADDING * 2 + JDTE_IO_BUTTON_SPACING * 3;
    @Unique private static final int JDTE_IO_SIDE_NORTH = 0;
    @Unique private static final int JDTE_IO_SIDE_SOUTH = 1;
    @Unique private static final int JDTE_IO_SIDE_WEST = 2;
    @Unique private static final int JDTE_IO_SIDE_EAST = 3;
    @Unique private static final int JDTE_IO_SIDE_UP = 4;
    @Unique private static final int JDTE_IO_SIDE_DOWN = 5;
    @Unique private int jdte$baseImageHeight = -1;
    @Unique private int jdte$filterPrevX;
    @Unique private int jdte$filterNextX;
    @Unique private int jdte$filterButtonsY;
    @Unique private boolean jdte$ioConfigOpen;
    @Unique private int jdte$ioConfigButtonX;
    @Unique private int jdte$ioConfigButtonY;

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
        int panelX = getGuiLeft() + originX - (JDTE_UPGRADE_SLOT_PANEL_WIDTH - config.getSlotSize()) / 2;
        int panelY = getGuiTop() + originY - (JDTE_UPGRADE_SLOT_PANEL_HEIGHT - config.getRows() * config.getSlotSize()) / 2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(JDTE_UPGRADE_SLOT_PANEL, panelX, panelY, 0, 0,
                JDTE_UPGRADE_SLOT_PANEL_WIDTH, JDTE_UPGRADE_SLOT_PANEL_HEIGHT,
                JDTE_UPGRADE_SLOT_PANEL_WIDTH, JDTE_UPGRADE_SLOT_PANEL_HEIGHT);

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
            } else if (container instanceof AdvancedPotionBrewerContainer && jdte$layoutPotionBrewerSlot(slot, slotAccessor, config)) {
                continue;
            } else if (container instanceof BioCrusherContainer && jdte$layoutBioCrusherSlot(slot, slotAccessor, config)) {
                continue;
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
    private boolean jdte$layoutPotionBrewerSlot(Slot slot, SlotAccessor slotAccessor, GuiUpgradeLayoutConfig config) {
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_0) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot0X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot0Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_1) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot1X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot1Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_2) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot2X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot2Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.INGREDIENT_SLOT) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerIngredientSlotX());
            slotAccessor.jdte$setY(config.getPotionBrewerIngredientSlotY());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.FUEL_SLOT) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerFuelSlotX());
            slotAccessor.jdte$setY(config.getPotionBrewerFuelSlotY());
            return true;
        }
        int extraIngredientCount = Math.min(AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT, config.getPotionBrewerExtraIngredientCount());
        for (int i = 0; i < extraIngredientCount; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + i) == slot) {
                slotAccessor.jdte$setX(config.getPotionBrewerExtraIngredientStartX() + i * config.getPotionBrewerExtraIngredientSpacing());
                slotAccessor.jdte$setY(config.getPotionBrewerExtraIngredientStartY());
                return true;
            }
        }
        int outputCount = Math.min(AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT, config.getPotionBrewerOutputCount());
        for (int i = 0; i < outputCount; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.OUTPUT_SLOT_START + i) == slot) {
                slotAccessor.jdte$setX(config.getPotionBrewerOutputStartX());
                slotAccessor.jdte$setY(config.getPotionBrewerOutputStartY() + i * config.getPotionBrewerOutputSpacing());
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean jdte$layoutBioCrusherSlot(Slot slot, SlotAccessor slotAccessor, GuiUpgradeLayoutConfig config) {
        if (!(slot instanceof BioCrusherContainer.BioCrusherUpgradeSlot upgradeSlot)) {
            return false;
        }

        if (upgradeSlot.getKind() == BioCrusherContainer.UpgradeKind.SHARPNESS) {
            slotAccessor.jdte$setX(config.getBioCrusherSharpnessSlotX());
            slotAccessor.jdte$setY(config.getBioCrusherSharpnessSlotY());
            return true;
        }

        slotAccessor.jdte$setX(config.getBioCrusherLootingSlotX());
        slotAccessor.jdte$setY(config.getBioCrusherLootingSlotY());
        return true;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
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
                int side = jdte$getIoConfigSideAt(mouseX, mouseY);
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

    @Override
    public List<Rect2i> jdte$getAutoIoConfigExtraAreas() {
        if (!jdte$hasIoConfigTarget()) {
            return Collections.emptyList();
        }

        jdte$updateIoConfigButtonPosition();
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(jdte$ioConfigButtonX, jdte$ioConfigButtonY, JDTE_IO_BUTTON_SIZE, JDTE_IO_BUTTON_SIZE));
        if (jdte$ioConfigOpen) {
            areas.add(new Rect2i(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), JDTE_IO_PANEL_SIZE, JDTE_IO_PANEL_SIZE));
        }
        return areas;
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
        if (container instanceof BioCrusherContainer || container instanceof LootFabricatorContainer
                || container instanceof GreenhouseContainer || container instanceof BioFactoryContainer) return;
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

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void jdte$renderIoConfig(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!jdte$hasIoConfigTarget()) return;
        jdte$updateIoConfigButtonPosition();
        if (jdte$ioConfigOpen) {
            jdte$renderIoConfigPanel(guiGraphics);
        }
        jdte$drawSmallIconButton(guiGraphics, jdte$ioConfigButtonX, jdte$ioConfigButtonY, JDTE_IO_CONFIG, true);
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
        return MiscTools.inBounds(jdte$ioConfigButtonX, jdte$ioConfigButtonY, JDTE_IO_BUTTON_SIZE, JDTE_IO_BUTTON_SIZE, mouseX, mouseY);
    }

    @Unique
    private int jdte$getIoConfigPanelX() {
        return topSectionLeft - JDTE_IO_PANEL_SIZE;
    }

    @Unique
    private int jdte$getIoConfigPanelY() {
        return topSectionTop + topSectionHeight - JDTE_IO_PANEL_SIZE;
    }

    @Unique
    private boolean jdte$inIoConfigPanel(double mouseX, double mouseY) {
        return MiscTools.inBounds(jdte$getIoConfigPanelX(), jdte$getIoConfigPanelY(), JDTE_IO_PANEL_SIZE, JDTE_IO_PANEL_SIZE, mouseX, mouseY);
    }

    @Unique
    private void jdte$renderIoConfigPanel(GuiGraphics guiGraphics) {
        int panelX = jdte$getIoConfigPanelX();
        int panelY = jdte$getIoConfigPanelY();
        guiGraphics.blitSprite(SOCIALBACKGROUND, panelX, panelY, JDTE_IO_PANEL_SIZE, JDTE_IO_PANEL_SIZE);

        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_NORTH);
        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_WEST);
        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_UP);
        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_EAST);
        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_SOUTH);
        jdte$drawIoConfigSide(guiGraphics, JDTE_IO_SIDE_DOWN);
    }

    @Unique
    private void jdte$drawIoConfigSide(GuiGraphics guiGraphics, int side) {
        jdte$drawIoSideButton(guiGraphics, jdte$getIoSideX(side), jdte$getIoSideY(side),
                jdte$getIoSideIcon(side), jdte$getIoSideMode(side));
    }

    @Unique
    private int jdte$getIoConfigSideAt(double mouseX, double mouseY) {
        for (int side = 0; side < AutoIoConfigData.SIDE_COUNT; side++) {
            if (MiscTools.inBounds(jdte$getIoSideX(side), jdte$getIoSideY(side), JDTE_IO_BUTTON_SIZE, JDTE_IO_BUTTON_SIZE, mouseX, mouseY)) {
                return side;
            }
        }
        return -1;
    }

    @Unique
    private int jdte$getIoSideX(int side) {
        int col = switch (side) {
            case JDTE_IO_SIDE_WEST -> 0;
            case JDTE_IO_SIDE_NORTH, JDTE_IO_SIDE_SOUTH, JDTE_IO_SIDE_UP -> 1;
            default -> 2;
        };
        return jdte$getIoGridX(col);
    }

    @Unique
    private int jdte$getIoSideY(int side) {
        int row = switch (side) {
            case JDTE_IO_SIDE_NORTH -> 0;
            case JDTE_IO_SIDE_WEST, JDTE_IO_SIDE_EAST, JDTE_IO_SIDE_UP -> 1;
            default -> 2;
        };
        return jdte$getIoGridY(row);
    }

    @Unique
    private int jdte$getIoGridX(int col) {
        return jdte$getIoConfigPanelX() + JDTE_IO_PANEL_PADDING + col * JDTE_IO_BUTTON_SPACING;
    }

    @Unique
    private int jdte$getIoGridY(int row) {
        return jdte$getIoConfigPanelY() + JDTE_IO_PANEL_PADDING + row * JDTE_IO_BUTTON_SPACING;
    }

    @Unique
    private ResourceLocation jdte$getIoSideIcon(int side) {
        return switch (side) {
            case JDTE_IO_SIDE_NORTH -> JDTE_IO_NORTH;
            case JDTE_IO_SIDE_SOUTH -> JDTE_IO_SOUTH;
            case JDTE_IO_SIDE_WEST -> JDTE_IO_WEST;
            case JDTE_IO_SIDE_EAST -> JDTE_IO_EAST;
            case JDTE_IO_SIDE_UP -> JDTE_IO_UP;
            default -> JDTE_IO_DOWN;
        };
    }

    @Unique
    private int jdte$getIoSideMode(int side) {
        return AutoIoConfigHelper.getMode(jdte$getIoInputMask(), jdte$getIoOutputMask(), side);
    }

    @Unique
    private int jdte$getIoInputMask() {
        return AutoIoConfigClientCache.getInputMask(baseMachineBE);
    }

    @Unique
    private int jdte$getIoOutputMask() {
        return AutoIoConfigClientCache.getOutputMask(baseMachineBE);
    }

    @Unique
    private void jdte$toggleIoConfigSide(int side) {
        AutoIoConfigHelper.IoMasks masks = AutoIoConfigHelper.cycleSide(
                jdte$getIoInputMask(), jdte$getIoOutputMask(), side,
                AutoIoConfigHelper.supportsInput(baseMachineBE),
                AutoIoConfigHelper.supportsOutput(baseMachineBE));
        AutoIoConfigClientCache.updateAndSend(baseMachineBE, masks.inputMask(), masks.outputMask());
    }

    @Unique
    private void jdte$drawIoSideButton(GuiGraphics guiGraphics, int x, int y, ResourceLocation icon, int mode) {
        float red;
        float green;
        float blue;
        switch (mode) {
            case AutoIoConfigHelper.MODE_BOTH -> {
                red = 1.0F;
                green = 1.0F;
                blue = 1.0F;
            }
            case AutoIoConfigHelper.MODE_INPUT -> {
                red = 1.0F;
                green = 0.55F;
                blue = 0.1F;
            }
            case AutoIoConfigHelper.MODE_OUTPUT -> {
                red = 0.25F;
                green = 0.55F;
                blue = 1.0F;
            }
            default -> {
                red = 0.33F;
                green = 0.33F;
                blue = 0.33F;
            }
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(0.75F, 0.75F, 1.0F);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    @Unique
    private void jdte$drawSmallIconButton(GuiGraphics guiGraphics, int x, int y, ResourceLocation icon, boolean active) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(0.75f, 0.75f, 1.0f);
        RenderSystem.setShaderColor(active ? 1.0f : 0.33f, active ? 1.0f : 0.33f, active ? 1.0f : 0.33f, 1.0f);
        guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();
    }

    @Unique
    private void jdte$playClickSound() {
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
                            int max = UpgradeHelper.getMaxUpgrades(baseMachineBE, type);
                            boolean canAdd = current < max && !jdte$hasOppositeSpeedUpgrade(type);

                            Component name = Component.translatable("item.jdte." + type.getSerializedName() + "_upgrade");
                            lines.add(Component.literal("  ")
                                    .append(name)
                                    .append(Component.literal(": " + current + "/" + max))
                                    .copy()
                                    .withStyle(canAdd ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
                        }

                        if (baseMachineBE instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator) {
                            int current = fabricator.getLootingLevel();
                            lines.add(Component.literal("  ")
                                    .append(Component.translatable("item.jdte.looting_upgrade"))
                                    .append(Component.literal(": " + current + "/3"))
                                    .copy()
                                    .withStyle(current < 3 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
                        }

                        if (baseMachineBE instanceof com.jdte.common.blockentities.BioFactoryBE factory
                                && net.neoforged.fml.ModList.get().isLoaded("productivebees")) {
                            int productivityTotal = factory.getUpgradeHandler().getProductivityCount();
                            for (ResourceLocation id : BioFactoryUpgradeItemStackHandler.getProductivityUpgradeIds()) {
                                net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                                    ItemStack upgrade = new ItemStack(item);
                                    int tier = BioFactoryUpgradeItemStackHandler.getProductivityTier(upgrade);
                                    int current = factory.getUpgradeHandler().countProductivityTier(tier);
                                    lines.add(Component.literal("  ")
                                            .append(upgrade.getHoverName())
                                            .append(Component.literal(": " + current + " (" + productivityTotal + "/4)"))
                                            .copy()
                                            .withStyle(productivityTotal < 4 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
                                });
                            }
                        }

                        if (baseMachineBE instanceof com.jdte.common.blockentities.BioFactoryBE factory) {
                            int current = factory.getUpgradeHandler().getLootingCount();
                            lines.add(Component.literal("  ")
                                    .append(Component.translatable("item.jdte.looting_upgrade"))
                                    .append(Component.literal(": " + current + "/4"))
                                    .copy()
                                    .withStyle(current < 4 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
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
        return UpgradeHelper.isUpgradeCompatible(baseMachineBE, type);
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

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void jdte$renderIoConfigTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!jdte$hasIoConfigTarget()) return;
        jdte$updateIoConfigButtonPosition();

        if (jdte$inIoConfigButton(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.io_config"), mouseX, mouseY);
            return;
        }
        if (!jdte$ioConfigOpen) return;

        int side = jdte$getIoConfigSideAt(mouseX, mouseY);
        if (side >= 0) {
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable(jdte$getIoSideTranslationKey(side)),
                    Component.translatable(jdte$getIoModeTranslationKey(jdte$getIoSideMode(side))),
                    Component.translatable(jdte$getIoAvailabilityTranslationKey()).withStyle(ChatFormatting.GRAY)
            )), mouseX, mouseY);
        }
    }

    @Unique
    private String jdte$getIoModeTranslationKey(int mode) {
        return switch (mode) {
            case AutoIoConfigHelper.MODE_BOTH -> "jdte.screen.io_config.both";
            case AutoIoConfigHelper.MODE_INPUT -> "jdte.screen.io_config.input";
            case AutoIoConfigHelper.MODE_OUTPUT -> "jdte.screen.io_config.output";
            default -> "jdte.screen.io_config.disabled";
        };
    }

    @Unique
    private String jdte$getIoAvailabilityTranslationKey() {
        boolean input = AutoIoConfigHelper.supportsInput(baseMachineBE);
        boolean output = AutoIoConfigHelper.supportsOutput(baseMachineBE);
        if (input && output) return "jdte.screen.io_config.available.both";
        if (input) return "jdte.screen.io_config.available.input";
        if (output) return "jdte.screen.io_config.available.output";
        return "jdte.screen.io_config.available.none";
    }

    @Unique
    private String jdte$getIoSideTranslationKey(int side) {
        return switch (side) {
            case JDTE_IO_SIDE_NORTH -> "jdte.screen.io_config.north";
            case JDTE_IO_SIDE_SOUTH -> "jdte.screen.io_config.south";
            case JDTE_IO_SIDE_WEST -> "jdte.screen.io_config.west";
            case JDTE_IO_SIDE_EAST -> "jdte.screen.io_config.east";
            case JDTE_IO_SIDE_UP -> "jdte.screen.io_config.up";
            default -> "jdte.screen.io_config.down";
        };
    }
}
