package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.containers.LootFabricatorContainer;
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

public class LootFabricatorBlock extends BaseMachineBlock {
    public LootFabricatorBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(3.0F).isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(itemStack, blockState, level, blockPos, player, hand, hit);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LootFabricatorBE(pos, state); }
    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new LootFabricatorContainer(id, inventory, pos),
                Component.translatable("block.jdte.loot_fabricator")), buf -> buf.writeBlockPos(pos));
    }
    @Override public boolean isValidBE(BlockEntity blockEntity) { return blockEntity instanceof LootFabricatorBE; }
}
