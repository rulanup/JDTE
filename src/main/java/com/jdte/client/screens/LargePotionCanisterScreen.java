package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.containers.LargePotionCanisterContainer;
import com.jdte.common.items.LargePotionCanisterItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LargePotionCanisterScreen extends AbstractContainerScreen<LargePotionCanisterContainer> {
    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/fuelcanister.png");
    private static final ResourceLocation FLUID_BAR =
            ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/fluidbar.png");

    public LargePotionCanisterScreen(LargePotionCanisterContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(GUI, left, top, 0, 0, imageWidth, imageHeight);

        int barLeft = getGuiLeft() + 5;
        int barTop = getGuiTop() + 5;
        guiGraphics.blit(FLUID_BAR, barLeft, barTop, 0, 0, 18, 72, 36, 72);

        int maxMb = LargePotionCanisterItem.getPotionCapacityMb();
        int amount = PotionCanister.getPotionAmount(menu.getBoundStack());
        if (maxMb > 0 && amount > 0) {
            int height = amount * 70 / maxMb;
            renderFluid(guiGraphics, getGuiLeft() + 6, getGuiTop() + 76, 16, height);
        }

        guiGraphics.blit(FLUID_BAR, barLeft, barTop, 18, 0, 18, 72, 36, 72);
    }

    private void renderFluid(GuiGraphics guiGraphics, int x, int bottomY, int width, int height) {
        if (height <= 0) {
            return;
        }
        int topY = bottomY - height;
        guiGraphics.fill(x, topY, x + width, bottomY, 0xC0385DC6);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (renderFluidBarTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean renderFluidBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = menu.getBoundStack();
        PotionContents contents = PotionCanister.getPotionContents(stack);
        int amount = PotionCanister.getPotionAmount(stack);
        if (amount == 0 || contents.equals(PotionContents.EMPTY)) {
            return false;
        }
        if (!MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 5, 18, 72, mouseX, mouseY)) {
            return false;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(MagicHelpers.formatted(amount) + "/" +
                MagicHelpers.formatted(LargePotionCanisterItem.getPotionCapacityMb())));
        contents.addPotionTooltip(lines::add, 1.0F, 20.0F);
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        return true;
    }
}
