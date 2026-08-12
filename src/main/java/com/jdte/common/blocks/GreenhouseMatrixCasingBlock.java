package com.jdte.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

public class GreenhouseMatrixCasingBlock extends Block {
    public static final BooleanProperty CONNECT_DOWN = BooleanProperty.create("connect_down");
    public static final BooleanProperty CONNECT_UP = BooleanProperty.create("connect_up");
    public static final BooleanProperty CONNECT_NORTH = BooleanProperty.create("connect_north");
    public static final BooleanProperty CONNECT_SOUTH = BooleanProperty.create("connect_south");
    public static final BooleanProperty CONNECT_WEST = BooleanProperty.create("connect_west");
    public static final BooleanProperty CONNECT_EAST = BooleanProperty.create("connect_east");

    public GreenhouseMatrixCasingBlock() {
        super(Properties.of().strength(5.0F).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(defaultBlockState()
                .setValue(CONNECT_DOWN, false)
                .setValue(CONNECT_UP, false)
                .setValue(CONNECT_NORTH, false)
                .setValue(CONNECT_SOUTH, false)
                .setValue(CONNECT_WEST, false)
                .setValue(CONNECT_EAST, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECT_DOWN, CONNECT_UP, CONNECT_NORTH, CONNECT_SOUTH, CONNECT_WEST, CONNECT_EAST);
    }

    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                               LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(connectionProperty(direction), connects(neighborState));
    }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    public static BlockState connectedState(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(connectionProperty(direction),
                    connects(level.getBlockState(pos.relative(direction))));
        }
        return state;
    }

    @Override protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return connects(adjacentState) || super.skipRendering(state, adjacentState, direction);
    }

    @Override protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    private static boolean connects(BlockState state) {
        return GreenhouseMatrixStructure.isShell(state.getBlock());
    }

    private static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case DOWN -> CONNECT_DOWN;
            case UP -> CONNECT_UP;
            case NORTH -> CONNECT_NORTH;
            case SOUTH -> CONNECT_SOUTH;
            case WEST -> CONNECT_WEST;
            case EAST -> CONNECT_EAST;
        };
    }
}
