package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.jdte.common.containers.LargeFuelCanisterContainer;
import com.jdte.common.items.LargeFuelCanisterItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.world.item.crafting.RecipeType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class LargeFuelCanisterScreen extends AbstractContainerScreen<LargeFuelCanisterContainer> {
    private static final Identifier GUI =
            Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/fuelcanister.png");

    public LargeFuelCanisterScreen(LargeFuelCanisterContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, left, top, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = menu.getCurrentStack();
        float itemEquivalent = (float) FuelCanister.getFuelLevel(stack) / LargeFuelCanisterItem.getMinimumFuelConsumed();
        Component itemLine = Component.translatable("justdirethings.fuelcanisteritemsamt",
                MagicHelpers.formatted(itemEquivalent));
        Component fuelLine = Component.translatable("justdirethings.fuelcanisteramt",
                MagicHelpers.formatted(FuelCanister.getFuelLevel(stack)));
        int centerX = imageWidth / 2;
        guiGraphics.text(font, itemLine, centerX - font.width(itemLine) / 2, 5, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.text(font, fuelLine, centerX - font.width(fuelLine) / 2, 15, Color.DARK_GRAY.getRGB(), false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(stack));
            if (menu.handler.isValid(hoveredSlot.getSlotIndex(), ItemResource.of(stack))) {
                int burnTime = stack.getBurnTime(RecipeType.SMELTING, minecraft.level.fuelValues());
                if (minecraft.hasShiftDown()) {
                    lines.add(Component.translatable("justdirethings.fuelcanisteramt",
                            MagicHelpers.formatted(burnTime)).withStyle(ChatFormatting.AQUA));
                    lines.add(Component.translatable("justdirethings.fuelcanisteramtstack",
                            MagicHelpers.formatted(burnTime * stack.getCount())).withStyle(ChatFormatting.AQUA));
                } else {
                    float single = (float) burnTime / LargeFuelCanisterItem.getMinimumFuelConsumed();
                    float total = single * stack.getCount();
                    lines.add(Component.translatable("justdirethings.fuelcanisteritemsamt",
                            MagicHelpers.formatted(single)).withStyle(ChatFormatting.AQUA));
                    lines.add(Component.translatable("justdirethings.fuelcanisteritemsamtstack",
                            MagicHelpers.formatted(total)).withStyle(ChatFormatting.AQUA));
                }
            }
            guiGraphics.setTooltipForNextFrame(font, lines, stack.getTooltipImage(), mouseX, mouseY);
            return;
        }
        super.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor guiGraphics, net.minecraft.world.inventory.Slot slot, int mouseX, int mouseY) {
        super.extractSlot(guiGraphics, slot, mouseX, mouseY);
        if (!slot.getItem().isEmpty() && !menu.handler.isValid(slot.getSlotIndex(), ItemResource.of(slot.getItem()))) {
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x7FFF0000);
        }
    }
}
