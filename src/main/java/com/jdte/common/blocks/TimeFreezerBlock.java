package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.containers.TimeFreezerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TimeFreezerBlock extends BaseMachineBlock {
    public TimeFreezerBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(2.5f).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(itemStack, blockState, level, blockPos, player, hand, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeFreezerBE(pos, state);
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof TimeFreezerBE;
    }

    @Override
    public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new TimeFreezerContainer(id, inventory, pos), Component.translatable("block.jdte.time_freezer")),
                buffer -> buffer.writeBlockPos(pos));
    }
}
