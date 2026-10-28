package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.GeneratorT1;
import com.jdte.common.blockentities.ExtendedGeneratorBE;
import com.jdte.common.containers.ExtendedGeneratorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** Preserve the native generator's non-directional state and collision shape. */
public class ExtendedGeneratorBlock extends GeneratorT1 {
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ExtendedGeneratorBE(pos,state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var item=player.getItemInHand(hand).getItem();
        if (item instanceof com.direwolf20.justdirethings.common.items.FerricoreWrench
                || item instanceof com.direwolf20.justdirethings.common.items.MachineSettingsCopier) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ExtendedGeneratorBE) || !(player instanceof ServerPlayer server)) return InteractionResult.FAIL;
        NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inv,p)->new ExtendedGeneratorContainer(id,inv,pos),
                Component.translatable("block.jdte.extended_generator")),buf->buf.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }
}
