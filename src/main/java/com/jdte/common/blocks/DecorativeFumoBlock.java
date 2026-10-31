package com.jdte.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecorativeFumoBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape NORTH_SHAPE = Block.box(3.9, 0, 3.8, 12.2, 13.6, 14.7);
    private static final VoxelShape EAST_SHAPE = Block.box(1.3, 0, 3.9, 12.2, 13.6, 12.2);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3.8, 0, 1.3, 12.7, 13.6, 12.2);
    private static final VoxelShape WEST_SHAPE = Block.box(3.8, 0, 3.8, 14.7, 13.6, 12.1);

    public DecorativeFumoBlock() {
        this(Properties.of().strength(0.2f).sound(SoundType.WOOL).noOcclusion());
    }

    private DecorativeFumoBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, placementFacing(context.getHorizontalDirection()));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    /** 返回玩偶面向的方向，使其在放置后正对玩家。 */
    public static Direction placementFacing(Direction playerHorizontalDirection) {
        return playerHorizontalDirection.getOpposite();
    }

    /** 返回按水平朝向旋转后的模型碰撞与选择范围。 */
    public static VoxelShape shapeFor(Direction facing) {
        return switch (facing) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case NORTH, UP, DOWN -> NORTH_SHAPE;
        };
    }
}