package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.LootFabricatorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.common.network.data.TickSpeedPayload;
import com.jdte.common.network.data.FilterPagePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import com.direwolf20.justdirethings.util.MiscTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class LootFabricatorScreen extends BaseMachineScreen<LootFabricatorContainer> {
    private static final ResourceLocation PREV = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_prev.png");
    private static final ResourceLocation NEXT = ResourceLocation.fromNamespaceAndPath("jdte", "textures/gui/filter_next.png");
    private final LootFabricatorContainer lootContainer;
    public LootFabricatorScreen(LootFabricatorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.lootContainer = container;
    }
    @Override public void setTopSection() { extraWidth = 96; extraHeight = 56; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderMachineSlotBackgrounds(graphics);
        renderProgressArrow(graphics);
        renderFluidBar(graphics, getGuiLeft() + 184, getGuiTop() + 9, lootContainer.getLifeFluidAmount(), 0xFFB8143A);
        renderFluidBar(graphics, getGuiLeft() + 204, getGuiTop() + 9, lootContainer.getTimeFluidAmount(), 0xFF8A3DB8);
        if (lootContainer.getMaxOutputPage() > 0) {
            graphics.blit(PREV, getGuiLeft() + 64, getGuiTop() + 76, 0, 0, 16, 16, 16, 16);
            graphics.blit(NEXT, getGuiLeft() + 152, getGuiTop() + 76, 0, 0, 16, 16, 16, 16);
            graphics.drawString(font, (lootContainer.getOutputPage() + 1) + "/" + (lootContainer.getMaxOutputPage() + 1), getGuiLeft() + 116, getGuiTop() + 80, 0x404040, false);
        }
    }

    private void renderMachineSlotBackgrounds(GuiGraphics graphics) {
        int machineAndUpgradeSlots = 16 + com.jdte.common.blockentities.LootFabricatorBE.INPUT_SLOTS
                + com.jdte.common.blockentities.LootFabricatorBE.UPGRADE_SLOTS;
        for (int i = 0; i < Math.min(machineAndUpgradeSlots, lootContainer.slots.size()); i++) {
            Slot slot = lootContainer.slots.get(i);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderProgressArrow(GuiGraphics graphics) {
        int x = getGuiLeft() + 48;
        int y = getGuiTop() + 37;
        int progressWidth = (lootContainer.getProgress() * 24) / Math.max(1, lootContainer.getProgressMax());
        drawArrow(graphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(graphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) drawArrow(graphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFFDC143C);
    }

    private void drawArrow(GuiGraphics graphics, int x, int y, int width, int color) {
        int clamped = Math.clamp(width, 0, 24);
        if (clamped <= 0) return;
        int body = Math.min(16, clamped);
        graphics.fill(x, y + 5, x + body, y + 10, color);
        int head = clamped - 16;
        if (head > 0) drawHeadPart(graphics, x + 16, y + 4, Math.min(head, 2), 7, color);
        if (head > 2) drawHeadPart(graphics, x + 18, y + 3, Math.min(head - 2, 2), 9, color);
        if (head > 4) drawHeadPart(graphics, x + 20, y + 2, Math.min(head - 4, 2), 11, color);
        if (head > 6) drawHeadPart(graphics, x + 22, y + 1, Math.min(head - 6, 2), 13, color);
    }

    private void drawHeadPart(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width > 0) graphics.fill(x, y, x + width, y + height, color);
    }

    private void renderFluidBar(GuiGraphics graphics, int x, int y, int amount, int color) {
        graphics.fill(x, y, x + 18, y + 72, 0xFF222222);
        int height = Math.clamp((amount * 70) / com.jdte.common.blockentities.LootFabricatorBE.BASE_FLUID_CAPACITY, 0, 70);
        graphics.fill(x + 1, y + 71 - height, x + 17, y + 71, color);
        graphics.renderOutline(x, y, 18, 72, 0xFF8B8B8B);
    }

    @Override public void addTickSpeedButton() {
        addRenderableWidget(ToggleButtonFactory.TICKSPEEDBUTTON(getGuiLeft() + 48, getGuiTop() + 18, tickSpeed, button -> {
            tickSpeed = ((NumberButton) button).getValue();
            PacketDistributor.sendToServer(new TickSpeedPayload(tickSpeed));
        }));
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && lootContainer.getMaxOutputPage() > 0) {
            int page = lootContainer.getOutputPage();
            if (MiscTools.inBounds(getGuiLeft() + 64, getGuiTop() + 76, 16, 16, mouseX, mouseY)) page--;
            else if (MiscTools.inBounds(getGuiLeft() + 152, getGuiTop() + 76, 16, 16, mouseX, mouseY)) page++;
            else return super.mouseClicked(mouseX, mouseY, button);
            lootContainer.setOutputPage(page);
            PacketDistributor.sendToServer(new FilterPagePayload(lootContainer.getOutputPage()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (MiscTools.inBounds(getGuiLeft() + 184, getGuiTop() + 9, 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.loot_fabricator.life_fluid", lootContainer.getLifeFluidAmount(), com.jdte.common.blockentities.LootFabricatorBE.BASE_FLUID_CAPACITY), mouseX, mouseY);
        } else if (MiscTools.inBounds(getGuiLeft() + 204, getGuiTop() + 9, 18, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("jdte.screen.loot_fabricator.time_fluid", lootContainer.getTimeFluidAmount(), com.jdte.common.blockentities.LootFabricatorBE.BASE_FLUID_CAPACITY), mouseX, mouseY);
        }
    }
}
