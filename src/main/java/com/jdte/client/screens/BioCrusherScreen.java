package com.jdte.client.screens;

import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.input.MouseButtonEvent;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.containers.BioCrusherContainer;
import com.jdte.common.network.data.BioCrusherPayload;
import com.jdte.common.network.data.FilterPagePayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public abstract class BioCrusherScreen<T extends BioCrusherContainer> extends BaseMachineScreen<T> {
    private static final Identifier SCANNER_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png");
    private static final Identifier GLOWING_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/glowing.png");
    private static final Identifier NIGHTVISION_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/nightvision.png");
    private static final Identifier OUTPUT_PREV = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final Identifier OUTPUT_NEXT = Identifier.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private static final int OUTPUT_PAGE_BUTTON_SIZE = 12;
    private static final int OUTPUT_PAGE_ICON_SIZE = 16;

    private static final Component TOOLTIP_HOSTILE = Component.translatable("jdte.screen.bio_crusher.mode.hostile");
    private static final Component TOOLTIP_FRIENDLY = Component.translatable("jdte.screen.bio_crusher.mode.friendly");
    private static final Component TOOLTIP_ALL = Component.translatable("jdte.screen.bio_crusher.mode.all");
    private static final Component TOOLTIP_PREV = Component.translatable("jdte.screen.filter_prev");
    private static final Component TOOLTIP_NEXT = Component.translatable("jdte.screen.filter_next");

    private int localMode;
    private int modeBtnX;
    private int modeBtnY;

    protected BioCrusherScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public int getFluidBarOffset() {
        return 204;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (!hasFilterUpgrade()) {
            renderModeButton(guiGraphics);
        }
        renderDedicatedUpgradeGhosts(guiGraphics);
        renderOutputPageButtons(guiGraphics);
    }

    @Override
    public void init() {
        super.init();
        var config = GuiUpgradeLayoutConfig.getInstance();
        modeBtnX = leftPos + config.getBioCrusherModeButtonX();
        modeBtnY = getGuiTop() + config.getBioCrusherModeButtonY();
        localMode = ((BioCrusherContainer) container).getMode();
    }

    private boolean hasFilterUpgrade() {
        if (container.baseMachineBE instanceof BioCrusherBE crusher) {
            return UpgradeHelper.countUpgrades(crusher, UpgradeType.FILTER) > 0;
        }
        return false;
    }

    private boolean isModeButtonClicked(double mouseX, double mouseY) {
        return MiscTools.inBounds(modeBtnX, modeBtnY, 16, 16, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && isOutputPrevClicked(mouseX, mouseY)) {
            changeOutputPage(-1);
            return true;
        }
        if (button == 0 && isOutputNextClicked(mouseX, mouseY)) {
            changeOutputPage(1);
            return true;
        }
        if (button == 0 && !hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            localMode = (localMode + 1) % 3;
            ClientPacketDistributor.sendToServer(new BioCrusherPayload(localMode));
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderModeButton(GuiGraphicsExtractor guiGraphics) {
        Identifier icon = switch (localMode) {
            case BioCrusherBE.MODE_HOSTILE -> SCANNER_ICON;
            case BioCrusherBE.MODE_FRIENDLY -> GLOWING_ICON;
            default -> NIGHTVISION_ICON;
        };
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, modeBtnX, modeBtnY, 0, 0, 16, 16, 16, 16);
    }

    private void renderOutputPageButtons(GuiGraphicsExtractor guiGraphics) {
        BioCrusherContainer bioCrusherContainer = bioCrusherContainer();
        if (!bioCrusherContainer.hasOutputSlots()) {
            return;
        }
        int maxPage = bioCrusherContainer.getMaxOutputPage();
        if (maxPage <= 0) {
            return;
        }

        int currentPage = bioCrusherContainer.getOutputPage();
        int prevX = getOutputPrevX();
        int nextX = getOutputNextX();
        int y = getOutputButtonsY();
        boolean prevActive = currentPage > 0;
        boolean nextActive = currentPage < maxPage;

        drawOutputPageButton(guiGraphics, prevX, y, OUTPUT_PREV, prevActive, false);
        drawOutputPageButton(guiGraphics, nextX, y, OUTPUT_NEXT, nextActive, false);
        guiGraphics.text(font, Component.literal((currentPage + 1) + "/" + (maxPage + 1)), nextX + OUTPUT_PAGE_BUTTON_SIZE + 2, y + 2, 0xFF404040, false);
    }

    private void drawOutputPageButton(GuiGraphicsExtractor guiGraphics, int x, int y, Identifier icon, boolean active, boolean flip) {
        var poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        if (flip) {
            poseStack.translate(x + OUTPUT_PAGE_BUTTON_SIZE / 2.0f, y + OUTPUT_PAGE_BUTTON_SIZE / 2.0f);
            poseStack.scale(0.75f, 0.75f);
            poseStack.rotate((float) Math.toRadians(180));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, -OUTPUT_PAGE_ICON_SIZE / 2, -OUTPUT_PAGE_ICON_SIZE / 2, 0, 0,
                    OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, active ? -1 : 0x4DFFFFFF);
        } else {
            poseStack.translate(x, y);
            poseStack.scale(0.75f, 0.75f);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0,
                    OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, OUTPUT_PAGE_ICON_SIZE, active ? -1 : 0x4DFFFFFF);
        }
        poseStack.popMatrix();
    }

    private int getOutputPrevX() {
        return getGuiLeft() + 8 - 14;
    }

    private int getOutputNextX() {
        return getGuiLeft() + 8 + BioCrusherBE.OUTPUT_SLOTS_PER_PAGE * 18;
    }

    private int getOutputButtonsY() {
        return getGuiTop() + 56;
    }

    private boolean isOutputPrevClicked(double mouseX, double mouseY) {
        if (!bioCrusherContainer().hasOutputSlots() || bioCrusherContainer().getMaxOutputPage() <= 0) {
            return false;
        }
        return MiscTools.inBounds(getOutputPrevX(), getOutputButtonsY(), OUTPUT_PAGE_BUTTON_SIZE, OUTPUT_PAGE_BUTTON_SIZE, mouseX, mouseY);
    }

    private boolean isOutputNextClicked(double mouseX, double mouseY) {
        if (!bioCrusherContainer().hasOutputSlots() || bioCrusherContainer().getMaxOutputPage() <= 0) {
            return false;
        }
        return MiscTools.inBounds(getOutputNextX(), getOutputButtonsY(), OUTPUT_PAGE_BUTTON_SIZE, OUTPUT_PAGE_BUTTON_SIZE, mouseX, mouseY);
    }

    private void changeOutputPage(int delta) {
        BioCrusherContainer bioCrusherContainer = bioCrusherContainer();
        int oldPage = bioCrusherContainer.getOutputPage();
        int newPage = Math.clamp(oldPage + delta, 0, bioCrusherContainer.getMaxOutputPage());
        if (newPage == oldPage) {
            return;
        }
        bioCrusherContainer.setOutputPage(newPage);
        ClientPacketDistributor.sendToServer(new FilterPagePayload(newPage));
        net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private BioCrusherContainer bioCrusherContainer() {
        return (BioCrusherContainer) container;
    }

    private void renderDedicatedUpgradeGhosts(GuiGraphicsExtractor guiGraphics) {
        for (Slot slot : container.slots) {
            if (!(slot instanceof BioCrusherContainer.BioCrusherUpgradeSlot upgradeSlot) || slot.hasItem()) {
                continue;
            }
            ItemStack ghost = getDedicatedUpgradeGhostStack(upgradeSlot);
            if (ghost.isEmpty()) {
                continue;
            }
            GhostItemRenderer.draw(guiGraphics, ghost, getGuiLeft() + slot.x, getGuiTop() + slot.y, 0x99707070);
        }
    }

    private ItemStack getDedicatedUpgradeGhostStack(BioCrusherContainer.BioCrusherUpgradeSlot slot) {
        return switch (slot.getKind()) {
            case SHARPNESS -> new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get());
            case LOOTING -> new ItemStack(JDTEItems.LOOTING_UPGRADE.get());
        };
    }

    private Component getDedicatedUpgradeSlotTooltip(BioCrusherContainer.BioCrusherUpgradeSlot slot) {
        return switch (slot.getKind()) {
            case SHARPNESS -> Component.translatable("jdte.screen.bio_crusher.sharpness_slot");
            case LOOTING -> Component.translatable("jdte.screen.bio_crusher.looting_slot");
        };
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        if (hoveredSlot instanceof BioCrusherContainer.BioCrusherUpgradeSlot upgradeSlot && !hoveredSlot.hasItem()) {
            guiGraphics.setTooltipForNextFrame(font, getDedicatedUpgradeSlotTooltip(upgradeSlot), mouseX, mouseY);
            return;
        }
        if (isOutputPrevClicked(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(font, TOOLTIP_PREV, mouseX, mouseY);
            return;
        }
        if (isOutputNextClicked(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(font, TOOLTIP_NEXT, mouseX, mouseY);
            return;
        }
        if (!hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            Component tooltip = switch (localMode) {
                case BioCrusherBE.MODE_HOSTILE -> TOOLTIP_HOSTILE;
                case BioCrusherBE.MODE_FRIENDLY -> TOOLTIP_FRIENDLY;
                default -> TOOLTIP_ALL;
            };
            guiGraphics.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }
}
