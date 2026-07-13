package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.containers.EntitySuppressorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EntitySuppressorBlock extends BaseMachineBlock {
    public EntitySuppressorBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(2.5f).isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EntitySuppressorBE(pos, state); }
    @Override public boolean isValidBE(BlockEntity blockEntity) { return blockEntity instanceof EntitySuppressorBE; }
    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new EntitySuppressorContainer(id, inventory, pos), Component.translatable("block.jdte.entity_suppressor")),
                buffer -> buffer.writeBlockPos(pos));
    }
}
