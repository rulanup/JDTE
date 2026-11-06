package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.AdvancedEntityAcceleratorBE;
import com.jdte.common.containers.AdvancedEntityAcceleratorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class AdvancedEntityAcceleratorBlock extends JDTEMachineBlock {
    public AdvancedEntityAcceleratorBlock() {
        super(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.5f, 3_600_000.0f)
                .isRedstoneConductor(BaseMachineBlock::never)
        );
    }

    @Override public net.minecraft.world.InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        return useWithFluidContainer(player.getItemInHand(hand),state,level,pos,player,hand,hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedEntityAcceleratorBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos blockPos) {
        openScreen(player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new AdvancedEntityAcceleratorContainer(windowId, playerInventory, blockPos), Component.translatable("block.jdte.advanced_entity_accelerator")), (buf -> {
            buf.writeBlockPos(blockPos);
        }));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof AdvancedEntityAcceleratorBE;
    }
}
