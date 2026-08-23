package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.jdte.common.containers.LargePocketGeneratorContainer;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LargePocketGeneratorScreen extends AbstractContainerScreen<LargePocketGeneratorContainer> {
    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/pocketgenerator.png");
    private ItemStack pocketGenerator = ItemStack.EMPTY;
    private IEnergyStorage energyStorage;

    public LargePocketGeneratorScreen(LargePocketGeneratorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderEnergyTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (renderFuelTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
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

        refreshPocketGeneratorState();
        if (pocketGenerator.isEmpty() || !(pocketGenerator.getItem() instanceof PocketGenerator) || energyStorage == null) {
            return;
        }

        int maxBurn = pocketGenerator.getOrDefault(JustDireDataComponents.POCKETGEN_MAXBURN, 0);
        int burnRemaining = pocketGenerator.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0);
        int burnBarHeight = fuelBarHeight(burnRemaining, maxBurn);
        if (burnBarHeight > 0) {
            guiGraphics.blit(GUI, leftPos + 80, topPos + 30 - burnBarHeight, 176, 13 - burnBarHeight, 14, burnBarHeight + 1);
        }

        int energyBarHeight = energyBarHeight(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
        if (energyBarHeight > 0) {
            guiGraphics.blit(GUI, leftPos + 8, topPos + 78 - energyBarHeight, 176, 84 - energyBarHeight, 16, energyBarHeight + 1);
        }
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (mouseX <= leftPos + 7 || mouseX >= leftPos + 25 || mouseY <= topPos + 7 || mouseY >= topPos + 80) {
            return;
        }
        refreshPocketGeneratorState();
        if (energyStorage == null) {
            return;
        }
        int burnTicks = pocketGenerator.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0);
        int fePerTick = 0;
        if (pocketGenerator.getItem() instanceof PocketGenerator pocketGeneratorItem) {
            fePerTick = pocketGeneratorItem.getFePerFuelTick() * pocketGeneratorItem.getBurnSpeedMultiplier(pocketGenerator);
        }
        String[] energyValues = energyTooltipValues(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored(), hasShiftDown());
        List<FormattedText> lines = Arrays.asList(
                Component.translatable("justdirethings.screen.energy",
                        energyValues[0], energyValues[1]),
                burnTicks > 0
                        ? Component.translatable("justdirethings.screen.burn_time", MagicHelpers.ticksInSeconds(burnTicks))
                        : Component.translatable("justdirethings.screen.no_fuel"),
                Component.translatable("justdirethings.screen.fepertick",
                        MagicHelpers.formatted(burnTicks > 0 ? fePerTick : 0))
        );
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(lines), mouseX, mouseY);
    }

    private boolean renderFuelTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            int burnTime = stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(stack));
                lines.add(Component.translatable("justdirethings.screen.burnspeedmultiplier",
                        burnSpeedMultiplierTooltipValue(stack)).withStyle(net.minecraft.ChatFormatting.RED));
                guiGraphics.renderTooltip(font, lines, stack.getTooltipImage(), mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    private void refreshPocketGeneratorState() {
        pocketGenerator = menu.getCurrentStack();
        if (pocketGenerator.isEmpty() || !(pocketGenerator.getItem() instanceof PocketGenerator)) {
            energyStorage = null;
            return;
        }
        energyStorage = pocketGenerator.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    static int fuelBarHeight(int burnRemaining, int maxBurn) {
        return maxBurn > 0 ? burnRemaining * 13 / maxBurn : 0;
    }

    static int energyBarHeight(int energyStored, int maxEnergy) {
        return maxEnergy > 0 ? energyStored * 70 / maxEnergy : 0;
    }

    static int burnSpeedMultiplierTooltipValue(ItemStack fuelStack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuelStack);
    }

    static String[] energyTooltipValues(int energyStored, int maxEnergyStored, boolean shiftDown) {
        if (shiftDown) {
            return new String[]{MagicHelpers.formatted(energyStored), MagicHelpers.formatted(maxEnergyStored)};
        }
        return new String[]{MagicHelpers.withSuffix(energyStored), MagicHelpers.withSuffix(maxEnergyStored)};
    }
}
