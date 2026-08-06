package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.containers.MineralExtractorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class MineralExtractorBlock extends BaseMachineBlock {
    /** 玩家放置时控制器朝向玩家的水平方向；仅当 FACING 为垂直时生效。 */
    public static final DirectionProperty HORIZONTAL_FACING =
            DirectionProperty.create("horizontal_facing", Direction.Plane.HORIZONTAL);

    private static final VoxelShape UP_SHAPE = createUpShape();
    private static final VoxelShape DOWN_SHAPE = rotateX(rotateX(UP_SHAPE));
    private static final Map<Direction, VoxelShape> VERTICAL_UP_SHAPES = createVerticalShapes(UP_SHAPE);
    private static final Map<Direction, VoxelShape> VERTICAL_DOWN_SHAPES = createVerticalShapes(DOWN_SHAPE);
    private static final Map<Direction, VoxelShape> HORIZONTAL_SHAPES = createHorizontalShapes();

    public MineralExtractorBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(3.0F).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
        registerDefaultState(defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 机体安装方向由实际点击面决定，避免远距离放置时因视线角度接近水平而错误侧躺。
        Direction facing = context.getClickedFace();
        // getHorizontalDirection() 是玩家看向的方向；控制器朝向玩家需要取反方向。
        Direction controllerFacing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(BlockStateProperties.FACING, facing)
                .setValue(HORIZONTAL_FACING, controllerFacing);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.getValue(BlockStateProperties.FACING).getAxis().isHorizontal()
                ? super.rotate(state, rotation)
                : state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState direRotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return rotate(state, rotation);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return FluidContainerTransfer.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (facing.getAxis().isHorizontal()) {
            return HORIZONTAL_SHAPES.getOrDefault(facing, UP_SHAPE);
        }
        Direction horizontal = state.getValue(HORIZONTAL_FACING);
        if (facing == Direction.UP) {
            return VERTICAL_UP_SHAPES.getOrDefault(horizontal, UP_SHAPE);
        }
        return VERTICAL_DOWN_SHAPES.getOrDefault(horizontal, DOWN_SHAPE);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MineralExtractorBE(pos, state);
    }

    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new MineralExtractorContainer(id, inventory, pos),
                Component.translatable("block.jdte.mineral_extractor")),
                buffer -> buffer.writeBlockPos(pos));
    }

    @Override public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof MineralExtractorBE;
    }

    private static VoxelShape createUpShape() {
        VoxelShape shape = Block.box(0, 0, 0, 16, 3, 16);
        shape = Shapes.or(shape, Block.box(2, 3, 2, 14, 5, 14));
        shape = Shapes.or(shape, Block.box(1, 4, 1, 3, 13, 3));
        shape = Shapes.or(shape, Block.box(13, 4, 1, 15, 13, 3));
        shape = Shapes.or(shape, Block.box(1, 4, 13, 3, 13, 15));
        shape = Shapes.or(shape, Block.box(13, 4, 13, 15, 13, 15));
        shape = Shapes.or(shape, Block.box(3, 5, 2, 13, 12, 2.5));
        shape = Shapes.or(shape, Block.box(3, 5, 13.5, 13, 12, 14));
        shape = Shapes.or(shape, Block.box(2, 5, 3, 2.5, 12, 13));
        shape = Shapes.or(shape, Block.box(13.5, 5, 3, 14, 12, 13));
        shape = Shapes.or(shape, Block.box(6, 5, 6, 10, 6, 10));
        shape = Shapes.or(shape, Block.box(6, 11, 6, 10, 12, 10));
        shape = Shapes.or(shape, Block.box(2, 12, 2, 14, 14, 14));
        shape = Shapes.or(shape, Block.box(5, 14, 5, 11, 15, 11));
        shape = Shapes.or(shape, Block.box(6, 15, 6, 10, 16, 10));
        shape = Shapes.or(shape, Block.box(4, 1, 0, 7, 4, 2));
        shape = Shapes.or(shape, Block.box(9, 1, 14, 12, 4, 16));
        shape = Shapes.or(shape, Block.box(14, 1, 6, 16, 4, 10));
        shape = Shapes.or(shape, Block.box(0, 5, 5, 2, 10, 11));
        return shape.optimize();
    }

    private static Map<Direction, VoxelShape> createVerticalShapes(VoxelShape base) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction horizontal : Direction.Plane.HORIZONTAL) {
            // 与 blockstate 的 y 旋转保持一致：模型控制器基准位于 west 面。
            shapes.put(horizontal, rotateY(base,
                    Math.floorMod(horizontal.get2DDataValue() - 1, 4)));
        }
        return Map.copyOf(shapes);
    }

    private static Map<Direction, VoxelShape> createHorizontalShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        VoxelShape north = rotateX(UP_SHAPE);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, rotateY(north, 1));
        shapes.put(Direction.SOUTH, rotateY(north, 2));
        shapes.put(Direction.WEST, rotateY(north, 3));
        return Map.copyOf(shapes);
    }

    private static VoxelShape rotateX(VoxelShape shape) {
        VoxelShape rotated = Shapes.empty();
        for (var box : shape.toAabbs()) {
            rotated = Shapes.or(rotated, Shapes.box(box.minX, box.minZ, 1.0D - box.maxY,
                    box.maxX, box.maxZ, 1.0D - box.minY));
        }
        return rotated.optimize();
    }

    private static VoxelShape rotateY(VoxelShape shape, int quarterTurns) {
        VoxelShape rotated = shape;
        for (int i = 0; i < Math.floorMod(quarterTurns, 4); i++) {
            VoxelShape next = Shapes.empty();
            for (var box : rotated.toAabbs()) {
                next = Shapes.or(next, Shapes.box(1.0D - box.maxZ, box.minY, box.minX,
                        1.0D - box.minZ, box.maxY, box.maxX));
            }
            rotated = next.optimize();
        }
        return rotated;
    }
}