package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.CreativeGreenhouseBE;
import com.jdte.common.containers.CreativeGreenhouseContainer;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** A no-cost greenhouse which exposes each configured crop product indefinitely. */
public class CreativeGreenhouseBlock extends BaseMachineBlock {
    public CreativeGreenhouseBlock() {
        super(Properties.of().sound(SoundType.GLASS).strength(3.0F).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeGreenhouseBE(JDTEBlockEntities.CREATIVE_GREENHOUSE.get(), pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new CreativeGreenhouseContainer(windowId, inventory, pos),
                Component.translatable("block.jdte.creative_greenhouse")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof CreativeGreenhouseBE;
    }
}
