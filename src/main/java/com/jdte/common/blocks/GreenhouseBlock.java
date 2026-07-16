package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.containers.GreenhouseContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class GreenhouseBlock extends BaseMachineBlock {
    public static final BooleanProperty CONNECT_NORTH = BooleanProperty.create("connect_north");
    public static final BooleanProperty CONNECT_EAST = BooleanProperty.create("connect_east");
    public static final BooleanProperty CONNECT_SOUTH = BooleanProperty.create("connect_south");
    public static final BooleanProperty CONNECT_WEST = BooleanProperty.create("connect_west");

    public GreenhouseBlock() {
        super(Properties.of()
                .sound(SoundType.GLASS)
                .strength(3.0F)
                .noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
        registerDefaultState(defaultBlockState()
                .setValue(CONNECT_NORTH, false)
                .setValue(CONNECT_EAST, false)
                .setValue(CONNECT_SOUTH, false)
                .setValue(CONNECT_WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECT_NORTH, CONNECT_EAST, CONNECT_SOUTH, CONNECT_WEST);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BooleanProperty property = connectionProperty(direction);
        return property == null ? state : state.setValue(property, neighborState.is(this));
    }

    private static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> CONNECT_NORTH;
            case EAST -> CONNECT_EAST;
            case SOUTH -> CONNECT_SOUTH;
            case WEST -> CONNECT_WEST;
            default -> null;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        BlockPos pos = context.getClickedPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = connectionProperty(direction);
            state = state.setValue(property, context.getLevel().getBlockState(pos.relative(direction)).is(this));
        }
        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new GreenhouseContainer(windowId, inventory, pos),
                Component.translatable("block.jdte.greenhouse")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof GreenhouseBE;
    }
}
