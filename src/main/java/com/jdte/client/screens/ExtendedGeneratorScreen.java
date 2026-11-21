package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.containers.ExtendedGeneratorContainer;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExtendedGeneratorScreen extends BaseMachineScreen<ExtendedGeneratorContainer> {
    private final ExtendedGeneratorContainer container;
    private final GeneratorT1BE generatorBE;

    public ExtendedGeneratorScreen(ExtendedGeneratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.generatorBE = (GeneratorT1BE) container.baseMachineBE;
    }

    @Override
    public void setTopSection() {
        extraWidth = 0;
        extraHeight = 0;
    }

    @Override
    public void addTickSpeedButton() {
    }

    @Override
    public void addRedstoneButtons() {
        addRenderableWidget(ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + 104,
                topSectionTop + 38,
                redstoneMode.ordinal(),
                button -> {
                    redstoneMode = MiscHelpers.RedstoneMode.values()[((ToggleButton) button).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUSTSLOT, getGuiLeft() + 79, getGuiTop() + 30, 0, 18, 18, 18, 256, 256);
        int maxBurn = container.getMaxBurn();
        int burnRemaining = container.getBurnRemaining();
        int flameHeight = 18;
        if (maxBurn > 0) {
            int filled = burnRemaining * flameHeight / maxBurn;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUSTSLOT, getGuiLeft() + 79, getGuiTop() + 48 - filled, 18, 36 - filled, 18, filled + 3, 256, 256);
        }
    }

    @Override
    public void powerBarTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!(baseMachineBE instanceof PoweredMachineBE poweredMachineBE)) {
            return;
        }
        if (!MiscTools.inBounds(topSectionLeft + 5, topSectionTop + 5, 18, 72, mouseX, mouseY)) {
            return;
        }

        int burnRemaining = container.getBurnRemaining();
        List<Component> lines = new ArrayList<>();
        if (minecraft.hasShiftDown()) {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.formatted(container.getEnergy()),
                    MagicHelpers.formatted(poweredMachineBE.getMaxEnergy())));
        } else {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.withSuffix(container.getEnergy()),
                    MagicHelpers.withSuffix(poweredMachineBE.getMaxEnergy())));
        }
        lines.add(Component.translatable("justdirethings.screen.fepertick",
                MagicHelpers.formatted(burnRemaining > 0 ? generatorBE.fePerTick() : 0)));
        lines.add(burnRemaining > 0
                ? Component.translatable("justdirethings.screen.burn_time", MagicHelpers.ticksInSeconds(burnRemaining))
                : Component.translatable("justdirethings.screen.no_fuel"));
        guiGraphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack hoveredStack = hoveredSlot.getItem();
            int burnTime = hoveredStack.getBurnTime(RecipeType.SMELTING, minecraft.level.fuelValues());
            if (burnTime > 0) {
                int burnSpeedMultiplier = burnSpeedMultiplierTooltipValue(hoveredStack);
                List<Component> components = new ArrayList<>(getTooltipFromContainerItem(hoveredStack));
                components.add(Component.translatable("justdirethings.screen.burnspeedmultiplier", burnSpeedMultiplier)
                        .withStyle(ChatFormatting.RED));
                guiGraphics.setTooltipForNextFrame(font, components, hoveredStack.getTooltipImage(), mouseX, mouseY);
                return;
            }
        }
        super.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    public static int burnSpeedMultiplierTooltipValue(ItemStack stack) {
        return PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(stack);
    }
}
