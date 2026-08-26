package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.containers.ExtendedFluidGeneratorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Arrays;

public class ExtendedFluidGeneratorScreen extends BaseMachineScreen<ExtendedFluidGeneratorContainer> {
    protected final ExtendedFluidGeneratorContainer container;
    protected GeneratorFluidT1BE generatorBE;

    public ExtendedFluidGeneratorScreen(ExtendedFluidGeneratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        if (container.baseMachineBE instanceof GeneratorFluidT1BE fluidGenerator) {
            this.generatorBE = fluidGenerator;
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void setTopSection() {
        extraWidth = 0;
        extraHeight = 0;
    }

    @Override
    public void addTickSpeedButton() {
        // No-op: fluid generators do not expose tick speed controls.
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
    public void powerBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(baseMachineBE instanceof PoweredMachineBE poweredMachineBE)) {
            return;
        }
        if (!MiscTools.inBounds(topSectionLeft + 5, topSectionTop + 5, 18, 72, mouseX, mouseY)) {
            return;
        }

        if (hasShiftDown()) {
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("justdirethings.screen.energy",
                            MagicHelpers.formatted(container.getEnergy()),
                            MagicHelpers.formatted(poweredMachineBE.getMaxEnergy())),
                    Component.translatable("justdirethings.screen.fepertick",
                            MagicHelpers.formatted(generatorBE.getFePerFuelTick()))
            )), mouseX, mouseY);
        } else {
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("justdirethings.screen.energy",
                            MagicHelpers.withSuffix(container.getEnergy()),
                            MagicHelpers.withSuffix(poweredMachineBE.getMaxEnergy())),
                    Component.translatable("justdirethings.screen.fepertick",
                            MagicHelpers.formatted(generatorBE.getFePerFuelTick()))
            )), mouseX, mouseY);
        }
    }
}
