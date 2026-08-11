package com.jdte.client.screens;

import com.jdte.common.blockentities.GreenhouseMatrixControllerBE;
import com.jdte.common.containers.GreenhouseMatrixContainer;
import com.jdte.common.network.data.GreenhouseMatrixControlPayload;
import com.jdte.common.network.data.GreenhouseMatrixPatternPagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class GreenhouseMatrixScreen extends AbstractContainerScreen<GreenhouseMatrixContainer> {
    private Button autoIoButton;
    private Button previousPatternPageButton;
    private Button nextPatternPageButton;
    public GreenhouseMatrixScreen(GreenhouseMatrixContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 224;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("jdte.screen.greenhouse_matrix.enabled"), button ->
                        PacketDistributor.sendToServer(new GreenhouseMatrixControlPayload(menu.getPos(), 0, !menu.isEnabled())))
                .bounds(leftPos + 8, topPos + 112, 78, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("jdte.screen.greenhouse_matrix.render"), button ->
                        PacketDistributor.sendToServer(new GreenhouseMatrixControlPayload(menu.getPos(), 1, !menu.isRenderEnabled())))
                .bounds(leftPos + 90, topPos + 112, 78, 20).build());
        autoIoButton = addRenderableWidget(Button.builder(autoIoLabel(), button ->
                        PacketDistributor.sendToServer(new GreenhouseMatrixControlPayload(menu.getPos(), 2, !menu.isAutoIoEnabled())))
                .bounds(leftPos + 172, topPos + 112, 78, 20).build());
        previousPatternPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button ->
                        requestPatternPage(menu.getAutoCraftingPage() - 1))
                .bounds(leftPos + 179, topPos + 19, 17, 12).build());
        nextPatternPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button ->
                        requestPatternPage(menu.getAutoCraftingPage() + 1))
                .bounds(leftPos + 278, topPos + 19, 17, 12).build());
        updatePatternPageButtons();
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (autoIoButton != null) autoIoButton.setMessage(autoIoLabel());
        updatePatternPageButtons();
    }

    private void requestPatternPage(int page) {
        menu.setAutoCraftingPage(page);
        PacketDistributor.sendToServer(new GreenhouseMatrixPatternPagePayload(
                menu.getPos(), menu.containerId, menu.getAutoCraftingPage()));
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        updatePatternPageButtons();
    }

    private void updatePatternPageButtons() {
        int count = menu.getAutoCraftingPageCount();
        if (previousPatternPageButton != null) {
            previousPatternPageButton.active = count > 1 && menu.getAutoCraftingPage() > 0;
        }
        if (nextPatternPageButton != null) {
            nextPatternPageButton.active = count > 1 && menu.getAutoCraftingPage() + 1 < count;
        }
    }

    private Component autoIoLabel() {
        return Component.translatable("jdte.screen.greenhouse_matrix.auto_io." + (menu.isAutoIoEnabled() ? "on" : "off"));
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202A24);
        graphics.fill(leftPos + 5, topPos + 18, leftPos + imageWidth - 5, topPos + 106, 0xFF0F1712);
        graphics.fill(leftPos + 176, topPos + 18, leftPos + imageWidth - 5, topPos + 106, 0xFF162019);
        graphics.fill(leftPos + 7, topPos + 140, leftPos + imageWidth - 7, topPos + 218, 0xFF8B8B8B);
        graphics.fill(leftPos + 176, topPos + 130, leftPos + imageWidth - 5, topPos + 218, 0xFF162019);
        for (int slot = 0; slot < GreenhouseMatrixControllerBE.GLOBAL_UPGRADE_SLOTS; slot++) {
            drawSlot(graphics, 181 + (slot % 2) * 18, 142 + (slot / 2) * 18,
                    menu.isQuickInstallEnabled());
        }
        for (int slot = 0; slot < GreenhouseMatrixControllerBE.CONTROLLER_UPGRADE_SLOTS; slot++) {
            drawSlot(graphics, 257 + (slot % 2) * 18, 142 + (slot / 2) * 18, true);
        }
        for (int slot = 0; slot < 16; slot++) {
            drawSlot(graphics, 198 + (slot % 4) * 18, 32 + (slot / 4) * 18,
                    menu.getAutoCraftingPageCount() > 0);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, boolean enabled) {
        int outer = enabled ? 0xFF9AA79D : 0xFF555B57;
        int inner = enabled ? 0xFF242B26 : 0xFF171A18;
        graphics.fill(leftPos + x, topPos + y, leftPos + x + 18, topPos + y + 18, outer);
        graphics.fill(leftPos + x + 1, topPos + y + 1, leftPos + x + 17, topPos + y + 17, inner);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xE8FFE8, false);
        graphics.drawCenteredString(font, Component.translatable("jdte.screen.greenhouse_matrix.pattern_page",
                        menu.getAutoCraftingPage() + 1, menu.getEffectiveAutoCraftingPageCount()),
                237, 21, menu.getAutoCraftingPageCount() > 0 ? 0xD9F5DE : 0x777777);
        int color = menu.isFormed() ? 0x55FF55 : 0xFF5555;
        graphics.drawString(font, Component.translatable(menu.isFormed()
                ? "jdte.screen.greenhouse_matrix.formed" : "jdte.screen.greenhouse_matrix.invalid"), 9, 23, color, false);
        if (!menu.isFormed()) graphics.drawString(font,
                Component.translatable("jdte.screen.greenhouse_matrix.error." + menu.getErrorCode()),
                9, 101, 0xFF9999, false);
        graphics.drawString(font, Component.translatable("jdte.screen.greenhouse_matrix.size",
                menu.getSizeX(), menu.getSizeY(), menu.getSizeZ()), 9, 36, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("jdte.screen.greenhouse_matrix.greenhouses",
                menu.getGreenhouseCount()), 9, 49, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("jdte.screen.greenhouse_matrix.speed",
                menu.getSpeedCount()), 9, 62, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("jdte.screen.greenhouse_matrix.efficiency",
                menu.getEfficiencyCount()), 9, 75, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("jdte.screen.greenhouse_matrix.conversions",
                menu.getSeedCount(), menu.getEssenceCount()), 9, 88, 0xD0D0D0, false);
        if (menu.isFormed()) graphics.drawString(font,
                Component.translatable("jdte.screen.greenhouse_matrix.simulation",
                        menu.getProductionGroupCount(), menu.getBufferedTypeCount(), menu.getBufferedItemCount()),
                9, 101, menu.isSimulationRebuilding() ? 0xFFFF77 : 0x77FF99, false);
        graphics.drawString(font, playerInventoryTitle, 8, 130, 0xFFFFFF, false);
        Component globalLabel = Component.translatable("jdte.screen.greenhouse_matrix.global_upgrades",
                menu.getQueuedUpgradeCount());
        graphics.drawString(font, globalLabel, 182, 132, 0xD9F5DE, false);
        Component controllerLabel = Component.translatable("jdte.screen.greenhouse_matrix.controller_upgrade");
        graphics.drawString(font, controllerLabel, imageWidth - 8 - font.width(controllerLabel),
                132, 0xD9F5DE, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderInvalidPatternOverlays(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderInvalidPatternOverlays(GuiGraphics graphics) {
        int mask = menu.getAutoCraftingInvalidMask();
        if (mask == 0) return;
        for (Slot slot : menu.slots) {
            int pageSlot = menu.getPatternPageSlot(slot);
            if (pageSlot >= 0 && (mask & 1 << pageSlot) != 0) {
                graphics.fill(leftPos + slot.x, topPos + slot.y,
                        leftPos + slot.x + 16, topPos + slot.y + 16, 0x88FF2222);
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && menu.isPatternSlot(hoveredSlot)) {
            int pageSlot = menu.getPatternPageSlot(hoveredSlot);
            if ((menu.getAutoCraftingInvalidMask() & 1 << pageSlot) != 0) {
                ItemStack stack = hoveredSlot.getItem();
                List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(stack));
                tooltip.add(Component.translatable("jdte.screen.greenhouse_matrix.invalid_pattern"));
                graphics.renderTooltip(font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
