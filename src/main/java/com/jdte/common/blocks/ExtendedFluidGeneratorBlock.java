package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.GeneratorFluidT1;
import com.jdte.common.blockentities.ExtendedFluidGeneratorBE;
import com.jdte.common.containers.ExtendedFluidGeneratorContainer;
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
public class ExtendedFluidGeneratorBlock extends GeneratorFluidT1 {
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ExtendedFluidGeneratorBE(pos,state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var item=player.getItemInHand(hand).getItem();
        if (item instanceof com.direwolf20.justdirethings.common.items.FerricoreWrench
                || item instanceof com.direwolf20.justdirethings.common.items.MachineSettingsCopier) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ExtendedFluidGeneratorBE) || !(player instanceof ServerPlayer server)) return InteractionResult.FAIL;
        NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inv,p)->new ExtendedFluidGeneratorContainer(id,inv,pos),
                Component.translatable("block.jdte.extended_fluid_generator")),buf->buf.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }
}
