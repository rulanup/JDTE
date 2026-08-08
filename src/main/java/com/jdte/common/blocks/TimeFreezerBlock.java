package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.containers.TimeFreezerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;

public class TimeFreezerBlock extends JDTEMachineBlock {
    public TimeFreezerBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(2.5f).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return useWithFluidContainer(player.getItemInHand(hand), state, level, pos, player, hand, hit);
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
        openScreen(player, new SimpleMenuProvider((id, inventory, ignored) ->
                new TimeFreezerContainer(id, inventory, pos),
                Component.translatable("block.jdte.time_freezer")),
                buffer -> buffer.writeBlockPos(pos));
    }
}
