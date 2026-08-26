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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void powerBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(baseMachineBE instanceof PoweredMachineBE poweredMachineBE)) {
            return;
        }
        if (!MiscTools.inBounds(topSectionLeft + 5, topSectionTop + 5, 18, 72, mouseX, mouseY)) {
            return;
        }

        List<Component> lines = new ArrayList<>();
        if (Screen.hasShiftDown()) {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.formatted(container.getEnergy()),
                    MagicHelpers.formatted(poweredMachineBE.getMaxEnergy())));
        } else {
            lines.add(Component.translatable("justdirethings.screen.energy",
                    MagicHelpers.withSuffix(container.getEnergy()),
                    MagicHelpers.withSuffix(poweredMachineBE.getMaxEnergy())));
        }
        lines.add(Component.translatable("justdirethings.screen.fepertick",
                MagicHelpers.formatted(generatorBE.getFePerFuelTick())));
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }
}
