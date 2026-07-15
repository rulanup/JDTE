package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.containers.GreenhouseContainer;
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

import javax.annotation.Nullable;

public class GreenhouseBlock extends BaseMachineBlock {
    public GreenhouseBlock() {
        super(Properties.of()
                .sound(SoundType.GLASS)
                .strength(3.0F)
                .noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new GreenhouseContainer(windowId, inventory, pos),
                Component.translatable("block.jdte.greenhouse")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof GreenhouseBE;
    }
}
