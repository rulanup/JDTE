package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.containers.FactoryPackerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FactoryPackerBlock extends BaseMachineBlock {
    public FactoryPackerBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(3.5F)
                .explosionResistance(3_600_000.0F)
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FactoryPackerBE(pos, state);
    }

    @Override public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof FactoryPackerBE;
    }

    @Override
    public BlockState direRotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return level.getBlockEntity(pos) instanceof FactoryPackerBE packer && packer.isBusy()
                ? state : super.direRotate(state, level, pos, rotation);
    }

    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                        new FactoryPackerContainer(id, inventory, pos),
                        Component.translatable("block.jdte.factory_packer")),
                buffer -> buffer.writeBlockPos(pos));
    }
}
