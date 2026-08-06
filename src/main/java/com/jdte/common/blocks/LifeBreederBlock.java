package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.containers.LifeBreederContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class LifeBreederBlock extends JDTEMachineBlock {
    public LifeBreederBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(4.0F).requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        return useWithFluidContainer(stack, state, level, pos, player, hand, hit);
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LifeBreederBE(pos, state); }
    @Override public void openMenu(Player player, BlockPos pos) {
        openScreen(player,new SimpleMenuProvider((id, inventory, ignored) -> new LifeBreederContainer(id, inventory, pos),
                Component.translatable("block.jdte.life_breeder")), buffer -> buffer.writeBlockPos(pos));
    }
    @Override public boolean isValidBE(BlockEntity blockEntity) { return blockEntity instanceof LifeBreederBE; }
}
