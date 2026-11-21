package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.util.MagicHelpers;
import net.minecraft.client.renderer.RenderPipelines;
import com.jdte.common.containers.LargePocketGeneratorContainer;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LargePocketGeneratorScreen extends AbstractContainerScreen<LargePocketGeneratorContainer> {
    private static final Identifier GUI =
            Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/pocketgenerator.png");
    private ItemStack pocketGenerator = ItemStack.EMPTY;
    private EnergyHandler energyStorage;

    public LargePocketGeneratorScreen(LargePocketGeneratorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderEnergyTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (renderFuelTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        super.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, left, top, 0, 0, imageWidth, imageHeight, 256, 256);

        refreshPocketGeneratorState();
        if (pocketGenerator.isEmpty() || !(pocketGenerator.getItem() instanceof PocketGenerator) || energyStorage == null) {
            return;
        }

        int maxBurn = pocketGenerator.getOrDefault(JustDireDataComponents.POCKETGEN_MAXBURN, 0);
        int burnRemaining = pocketGenerator.getOrDefault(JustDireDataComponents.POCKETGEN_COUNTER, 0);
        int burnBarHeight = fuelBarHeight(burnRemaining, maxBurn);
        if (burnBarHeight > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos + 80, topPos + 30 - burnBarHeight, 176, 13 - burnBarHeight, 14, burnBarHeight + 1, 256, 256);
        }

        int energyBarHeight = energyBarHeight(energyStorage.getAmountAsInt(), energyStorage.getCapacityAsInt());
        if (energyBarHeight > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos + 8, topPos + 78 - energyBarHeight, 176, 84 - energyBarHeight, 16, energyBarHeight + 1, 256, 256);
        }
    }

    private void renderEnergyTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
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
        String[] energyValues = energyTooltipValues(energyStorage.getAmountAsInt(), energyStorage.getCapacityAsInt(), minecraft.hasShiftDown());
        List<FormattedText> lines = Arrays.asList(
                Component.translatable("justdirethings.screen.energy",
                        energyValues[0], energyValues[1]),
                burnTicks > 0
                        ? Component.translatable("justdirethings.screen.burn_time", MagicHelpers.ticksInSeconds(burnTicks))
                        : Component.translatable("justdirethings.screen.no_fuel"),
                Component.translatable("justdirethings.screen.fepertick",
                        MagicHelpers.formatted(burnTicks > 0 ? fePerTick : 0))
        );
        guiGraphics.setTooltipForNextFrame(font, Language.getInstance().getVisualOrder(lines), mouseX, mouseY);
    }

    private boolean renderFuelTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            int burnTime = stack.getBurnTime(RecipeType.SMELTING, minecraft.level.fuelValues());
            if (burnTime > 0) {
                List<Component> lines = new ArrayList<>(getTooltipFromContainerItem(stack));
                lines.add(Component.translatable("justdirethings.screen.burnspeedmultiplier",
                        burnSpeedMultiplierTooltipValue(stack)).withStyle(net.minecraft.ChatFormatting.RED));
                guiGraphics.setTooltipForNextFrame(font, lines, stack.getTooltipImage(), mouseX, mouseY);
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
        energyStorage = ItemAccess.forStack(pocketGenerator).getCapability(Capabilities.Energy.ITEM);
    }

    static int fuelBarHeight(int burnRemaining, int maxBurn) {
        return maxBurn > 0 ? (int) Math.clamp((long) burnRemaining * 13 / maxBurn, 0, 13) : 0;
    }

    static int energyBarHeight(int energyStored, int maxEnergy) {
        return maxEnergy > 0 ? (int) Math.clamp((long) energyStored * 70 / maxEnergy, 0, 70) : 0;
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
