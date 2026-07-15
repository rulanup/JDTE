package com.jdte.client.screens;

import com.jdte.common.containers.CrystalIncubatorContainer;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.setup.JDTEConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrystalIncubatorScreen extends ExtendedItemReceiverScreen<CrystalIncubatorContainer> {
    private int multiplier;

    public CrystalIncubatorScreen(CrystalIncubatorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        multiplier = container.getMultiplier();
    }

    @Override
    public void init() {
        super.init();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        addRenderableWidget(new NumberButton(
                leftPos + layout.getItemReceiverSpeedButtonX(),
                topSectionTop + layout.getItemReceiverSpeedButtonY(),
                34, 12, multiplier, 1, JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get(),
                Component.translatable("jdte.screen.multiplier"), button -> {
                    multiplier = ((NumberButton) button).getValue();
                    PacketDistributor.sendToServer(new TimeAcceleratorPayload(multiplier));
                }));
    }

    @Override
    public void addTickSpeedButton() {
    }

    @Override
    public int getFluidBarOffset() {
        return 204;
    }
}
