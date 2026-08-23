package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.jdte.common.containers.LargePocketGeneratorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LargePocketGeneratorScreen extends AbstractContainerScreen<LargePocketGeneratorContainer> {
    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/pocketgenerator.png");

    public LargePocketGeneratorScreen(LargePocketGeneratorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(GUI, left, top, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (renderEnergyTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        if (renderFuelTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (mouseX <= leftPos + 7 || mouseX >= leftPos + 25 || mouseY <= topPos + 7 || mouseY >= topPos + 80) {
            return false;
        }
        ItemStack stack = menu.getBoundStack();
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) {
            return false;
        }
        int burnTicks = stack.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0);
        int fePerTick = 0;
        if (stack.getItem() instanceof PocketGenerator pocketGenerator) {
            fePerTick = pocketGenerator.getFePerFuelTick() * pocketGenerator.getBurnSpeedMultiplier(stack);
        }

        List<Component> lines = new ArrayList<>();
        if (Screen.hasShiftDown()) {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.formatted(energy.getEnergyStored()),
                    MagicHelpers.formatted(energy.getMaxEnergyStored())));
        } else {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.withSuffix(energy.getEnergyStored()),
                    MagicHelpers.withSuffix(energy.getMaxEnergyStored())));
        }
        lines.add(burnTicks > 0
                ? Component.translatable("justdirethings.screen.burn_time", MagicHelpers.ticksInSeconds(burnTicks))
                : Component.translatable("justdirethings.screen.no_fuel"));
        lines.add(Component.translatable("justdirethings.screen.fepertick",
                MagicHelpers.formatted(burnTicks > 0 ? fePerTick : 0)));
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        return true;
    }

    private boolean renderFuelTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            int burnTime = stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(stack));
                guiGraphics.renderTooltip(font, lines, stack.getTooltipImage(), mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}
