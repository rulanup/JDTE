package com.jdte.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LargeGreenhouseStructure {
    public static final int WIDTH = 3;
    public static final int DEPTH = 3;
    public static final int HEIGHT = 2;
    public static final int PART_COUNT = WIDTH * DEPTH * HEIGHT;
    // Pre-0.5.6 greenhouses were 3 blocks tall; removal still sweeps the old top layer.
    private static final int LEGACY_HEIGHT = 3;

    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);

    private LargeGreenhouseStructure() {
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
        Direction facing = state.getValue(LargeGreenhousePartBlock.FACING);
        int x = state.getValue(LargeGreenhousePartBlock.X).offset();
        int depth = state.getValue(LargeGreenhousePartBlock.DEPTH).offset();
        int y = state.getValue(LargeGreenhousePartBlock.LAYER).offset();
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
        BlockState partBlock = com.jdte.setup.JDTEBlocks.LARGE_GREENHOUSE_PART.get().defaultBlockState()
                .setValue(LargeGreenhousePartBlock.FACING, facing);
        List<BlockPos> placed = new ArrayList<>(PART_COUNT - 1);
        for (int y = 0; y < HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    if (isController(x, depth, y)) continue;
                    BlockPos pos = position(controller, facing, x, depth, y);
                    BlockState state = partBlock
                            .setValue(LargeGreenhousePartBlock.X, LargeGreenhousePartBlock.HorizontalPart.fromOffset(x))
                            .setValue(LargeGreenhousePartBlock.DEPTH, LargeGreenhousePartBlock.DepthPart.fromOffset(depth))
                            .setValue(LargeGreenhousePartBlock.LAYER, LargeGreenhousePartBlock.LayerPart.fromOffset(y));
                    if (!level.setBlock(pos, state, 3)) {
                        for (BlockPos rollback : placed) {
                            level.removeBlock(rollback, false);
                            level.invalidateCapabilities(rollback);
                        }
                        return false;
                    }
                    level.invalidateCapabilities(pos);
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
        Direction facing = state.getValue(LargeGreenhousePartBlock.FACING);
        BlockPos controller = controllerPosition(partPos, state);
        REMOVING.set(true);
        try {
            if (level.getBlockState(controller).is(com.jdte.setup.JDTEBlocks.LARGE_GREENHOUSE.get())) {
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
                    if (state.is(com.jdte.setup.JDTEBlocks.LARGE_GREENHOUSE_PART.get())
                            && controllerPosition(pos, state).equals(controller)) {
                        level.removeBlock(pos, false);
                        level.invalidateCapabilities(pos);
                    }
                }
            }
        }
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