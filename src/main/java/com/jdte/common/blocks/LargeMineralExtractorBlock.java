package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.containers.LargeMineralExtractorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public final class LargeMineralExtractorBlock extends BaseMachineBlock {
    public LargeMineralExtractorBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(5.0F).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(BlockStateProperties.FACING, facing);
        return LargeMineralExtractorStructure.canPlace(context.getLevel(), context.getClickedPos(), facing)
                ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide()
                && !LargeMineralExtractorStructure.placeParts(
                        level, pos, LargeMineralExtractorStructure.horizontalFacing(state))) {
            boolean shouldDrop = !(entity instanceof Player player) || !player.isCreative();
            level.destroyBlock(pos, shouldDrop);
        }
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state; }
    @Override public BlockState direRotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return state;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            LargeMineralExtractorStructure.removeFromController(
                    level, pos, LargeMineralExtractorStructure.horizontalFacing(state));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargeMineralExtractorBE(pos, state);
    }

    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new LargeMineralExtractorContainer(id, inventory, pos),
                Component.translatable("block.jdte.large_mineral_extractor")),
                buffer -> buffer.writeBlockPos(pos));
    }

    @Override public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof LargeMineralExtractorBE;
    }
}