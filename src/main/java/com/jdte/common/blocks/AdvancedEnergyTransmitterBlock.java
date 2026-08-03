package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.EnergyTransmitter;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.containers.AdvancedEnergyTransmitterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AdvancedEnergyTransmitterBlock extends EnergyTransmitter {
    public AdvancedEnergyTransmitterBlock() {
        super();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedEnergyTransmitterBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos blockPos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new AdvancedEnergyTransmitterContainer(windowId, inventory, blockPos),
                Component.translatable("block.jdte.advanced_energy_transmitter")),
                buffer -> buffer.writeBlockPos(blockPos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof AdvancedEnergyTransmitterBE;
    }
}
