package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class AdvancedEnergyTransmitterContainer extends BaseMachineContainer {
    public AdvancedEnergyTransmitterContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedEnergyTransmitterContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_ENERGY_TRANSMITTER.get(), windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter) {
            addDataSlots(transmitter.getTransmitterData());
        }
        addPlayerSlots(player.getInventory());
    }

    public int getTargetCount() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter ? transmitter.getTargetCount() : 0;
    }

    public int getScanProgress() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter ? transmitter.getScanProgress() : 100;
    }

    public int getLastAttemptedTargets() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter ? transmitter.getLastAttemptedTargets() : 0;
    }

    public int getLastTransferred() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter ? transmitter.getLastTransferred() : 0;
    }

    public boolean isShowingParticles() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter
                && transmitter.isShowingParticles();
    }

    public AdvancedEnergyTransmitterEnergySource.Status getEnergyNetworkStatus() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter
                ? transmitter.getEnergyNetworkStatus()
                : AdvancedEnergyTransmitterEnergySource.Status.UNAVAILABLE;
    }

    public boolean hasBoundPlayer() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter
                && transmitter.hasBoundPlayer();
    }

    public boolean isBoundPlayerOnline() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter
                && transmitter.isBoundPlayerOnline();
    }

    public String getBoundPlayerName() {
        return baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter
                ? transmitter.getBoundPlayerName() : "";
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());
    }
}
