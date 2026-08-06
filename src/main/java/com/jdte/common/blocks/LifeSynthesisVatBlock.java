package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.containers.LifeSynthesisContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class LifeSynthesisVatBlock extends JDTEMachineBlock {
    public LifeSynthesisVatBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(4.0F).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(BlockStateProperties.FACING, facing);
        return LifeSynthesisStructure.canPlace(context.getLevel(), context.getClickedPos(), facing) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide()
                && !LifeSynthesisStructure.placeParts(level, pos, LifeSynthesisStructure.horizontalFacing(state))) {
            boolean shouldDrop = !(entity instanceof Player player) || !player.isCreative();
            level.destroyBlock(pos, shouldDrop);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        return useWithFluidContainer(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state;
    }

    @Override
    public BlockState direRotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return state;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            LifeSynthesisStructure.removeFromController(level, pos, LifeSynthesisStructure.horizontalFacing(state));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LifeSynthesisVatBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos pos) {
        openScreen(player,new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new LifeSynthesisContainer(windowId, inventory, pos),
                Component.translatable("block.jdte.life_synthesis_vat")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof LifeSynthesisVatBE;
    }
}