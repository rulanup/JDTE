package com.jdte.common.blocks;

import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 生命合成舱 3×3×2 多方块结构。
 * 控制器位于正面中央底层 (0,0,0)，其余 17 格为无状态部件。
 * 旧版 3×3×3 屋顶层保留迁移清理支持。
 */
public final class LifeSynthesisStructure {
    public static final int WIDTH = 3;
    public static final int DEPTH = 3;
    public static final int HEIGHT = 2;
    private static final int LEGACY_HEIGHT = 3;
    public static final int PART_COUNT = WIDTH * DEPTH * HEIGHT;

    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);

    private LifeSynthesisStructure() {
    }

    public static Direction horizontalFacing(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }

    public static BlockPos position(BlockPos controller, Direction facing, int x, int depth, int y) {
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        return controller.relative(right, x).relative(back, depth).above(y);
    }

    public static BlockPos controllerPosition(BlockPos partPos, BlockState state) {
        Direction facing = state.getValue(LifeSynthesisPartBlock.FACING);
        int x = state.getValue(LifeSynthesisPartBlock.X).offset();
        int depth = state.getValue(LifeSynthesisPartBlock.DEPTH).offset();
        int y = state.getValue(LifeSynthesisPartBlock.LAYER).offset();
        return position(partPos, facing, -x, -depth, -y);
    }

    public static boolean canPlace(Level level, BlockPos controller, Direction facing) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    if (isController(x, depth, y)) continue;
                    BlockPos pos = position(controller, facing, x, depth, y);
                    if (!level.isAreaLoaded(pos, 0)
                            || !level.getWorldBorder().isWithinBounds(pos)
                            || !level.getBlockState(pos).canBeReplaced()) return false;
                }
            }
        }
        return true;
    }

    public static boolean placeParts(Level level, BlockPos controller, Direction facing) {
        BlockState partBlock = JDTEBlocks.LIFE_SYNTHESIS_PART.get().defaultBlockState()
                .setValue(LifeSynthesisPartBlock.FACING, facing);
        List<BlockPos> placed = new ArrayList<>(PART_COUNT - 1);
        for (int y = 0; y < HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    if (isController(x, depth, y)) continue;
                    BlockPos pos = position(controller, facing, x, depth, y);
                    BlockState state = partBlock
                            .setValue(LifeSynthesisPartBlock.X, LifeSynthesisPartBlock.HorizontalPart.fromOffset(x))
                            .setValue(LifeSynthesisPartBlock.DEPTH, LifeSynthesisPartBlock.DepthPart.fromOffset(depth))
                            .setValue(LifeSynthesisPartBlock.LAYER, LifeSynthesisPartBlock.LayerPart.fromOffset(y));
                    if (!level.setBlock(pos, state, 3)) {
                        for (BlockPos rollback : placed) level.removeBlock(rollback, false);
                        return false;
                    }
                    placed.add(pos);
                }
            }
        }
        return true;
    }

    public static void removeFromController(Level level, BlockPos controller, Direction facing) {
        if (REMOVING.get()) return;
        REMOVING.set(true);
        try {
            removeParts(level, controller, facing);
        } finally {
            REMOVING.set(false);
        }
    }

    public static void removeFromPart(Level level, BlockPos partPos, BlockState state, boolean dropController) {
        if (REMOVING.get()) return;
        Direction facing = state.getValue(LifeSynthesisPartBlock.FACING);
        BlockPos controller = controllerPosition(partPos, state);
        REMOVING.set(true);
        try {
            if (level.getBlockState(controller).is(JDTEBlocks.LIFE_SYNTHESIS_VAT.get())) {
                level.destroyBlock(controller, dropController);
            }
            removeParts(level, controller, facing);
        } finally {
            REMOVING.set(false);
        }
    }

    private static void removeParts(Level level, BlockPos controller, Direction facing) {
        for (int y = 0; y < LEGACY_HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    if (isController(x, depth, y)) continue;
                    BlockPos pos = position(controller, facing, x, depth, y);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(JDTEBlocks.LIFE_SYNTHESIS_PART.get())
                            && controllerPosition(pos, state).equals(controller)) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    /**
     * 静默移除旧版第三层。REMOVING 会阻止部件 onRemove 反向拆除控制器。
     * 区域未加载时返回 false，控制器会在后续 tick 重试。
     */
    public static boolean removeLegacyRoof(Level level, BlockPos controller, Direction facing) {
        if (REMOVING.get()) return false;
        List<BlockPos> roof = new ArrayList<>(WIDTH * DEPTH);
        for (int depth = 0; depth < DEPTH; depth++) {
            for (int x = -1; x <= 1; x++) {
                BlockPos pos = position(controller, facing, x, depth, LEGACY_HEIGHT - 1);
                if (!level.isAreaLoaded(pos, 0)) return false;
                roof.add(pos);
            }
        }
        REMOVING.set(true);
        try {
            for (BlockPos pos : roof) {
                BlockState state = level.getBlockState(pos);
                if (state.is(JDTEBlocks.LIFE_SYNTHESIS_PART.get())
                        && state.getValue(LifeSynthesisPartBlock.LAYER) == LifeSynthesisPartBlock.LayerPart.ROOF
                        && controllerPosition(pos, state).equals(controller)) {
                    level.removeBlock(pos, false);
                }
            }
        } finally {
            REMOVING.set(false);
        }
        return true;
    }

    public static List<BoundaryNeighbor> boundaryNeighbors(BlockPos controller, Direction facing) {
        List<BlockPos> members = new ArrayList<>(PART_COUNT);
        Set<BlockPos> occupied = new HashSet<>(PART_COUNT);
        for (int y = 0; y < HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    BlockPos member = position(controller, facing, x, depth, y);
                    members.add(member);
                    occupied.add(member);
                }
            }
        }

        List<BoundaryNeighbor> neighbors = new ArrayList<>();
        for (BlockPos member : members) {
            for (Direction exposedSide : Direction.values()) {
                BlockPos neighbor = member.relative(exposedSide);
                if (!occupied.contains(neighbor)) {
                    neighbors.add(new BoundaryNeighbor(neighbor, exposedSide));
                }
            }
        }
        return List.copyOf(neighbors);
    }

    public record BoundaryNeighbor(BlockPos pos, Direction exposedSide) {
    }

    private static boolean isController(int x, int depth, int y) {
        return x == 0 && depth == 0 && y == 0;
    }
}