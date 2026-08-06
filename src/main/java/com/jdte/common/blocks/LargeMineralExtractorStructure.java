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

/** 3x3x2 大型矿物提取机结构，控制器位于正面中央底层。 */
public final class LargeMineralExtractorStructure {
    public static final int WIDTH = 3;
    public static final int DEPTH = 3;
    public static final int HEIGHT = 2;
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);

    private LargeMineralExtractorStructure() {
    }

    public static Direction horizontalFacing(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }

    public static BlockPos position(BlockPos controller, Direction facing, int x, int depth, int y) {
        return controller.relative(facing.getClockWise(), x)
                .relative(facing.getOpposite(), depth)
                .above(y);
    }

    public static BlockPos controllerPosition(BlockPos partPos, BlockState state) {
        Direction facing = state.getValue(LargeMineralExtractorPartBlock.FACING);
        int x = state.getValue(LargeMineralExtractorPartBlock.X).offset();
        int depth = state.getValue(LargeMineralExtractorPartBlock.DEPTH).offset();
        int y = state.getValue(LargeMineralExtractorPartBlock.LAYER).offset();
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
        BlockState template = JDTEBlocks.LARGE_MINERAL_EXTRACTOR_PART.get().defaultBlockState()
                .setValue(LargeMineralExtractorPartBlock.FACING, facing);
        List<BlockPos> placed = new ArrayList<>(WIDTH * DEPTH * HEIGHT - 1);
        for (int y = 0; y < HEIGHT; y++) {
            for (int depth = 0; depth < DEPTH; depth++) {
                for (int x = -1; x <= 1; x++) {
                    if (isController(x, depth, y)) continue;
                    BlockPos pos = position(controller, facing, x, depth, y);
                    BlockState state = template
                            .setValue(LargeMineralExtractorPartBlock.X,
                                    LargeMineralExtractorPartBlock.HorizontalPart.fromOffset(x))
                            .setValue(LargeMineralExtractorPartBlock.DEPTH,
                                    LargeMineralExtractorPartBlock.DepthPart.fromOffset(depth))
                            .setValue(LargeMineralExtractorPartBlock.LAYER,
                                    LargeMineralExtractorPartBlock.LayerPart.fromOffset(y));
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
        remove(level, controller, facing, false);
    }

    public static void removeFromPart(Level level, BlockPos partPos, BlockState state, boolean dropController) {
        if (REMOVING.get()) return;
        Direction facing = state.getValue(LargeMineralExtractorPartBlock.FACING);
        BlockPos controller = controllerPosition(partPos, state);
        remove(level, controller, facing, dropController);
    }

    private static void remove(Level level, BlockPos controller, Direction facing, boolean dropController) {
        if (REMOVING.get()) return;
        REMOVING.set(true);
        try {
            if (dropController && level.getBlockState(controller).is(JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get())) {
                level.destroyBlock(controller, true);
            }
            for (int y = 0; y < HEIGHT; y++) {
                for (int depth = 0; depth < DEPTH; depth++) {
                    for (int x = -1; x <= 1; x++) {
                        if (isController(x, depth, y)) continue;
                        BlockPos pos = position(controller, facing, x, depth, y);
                        BlockState partState = level.getBlockState(pos);
                        if (partState.is(JDTEBlocks.LARGE_MINERAL_EXTRACTOR_PART.get())
                                && controllerPosition(pos, partState).equals(controller)) {
                            level.removeBlock(pos, false);
                            level.invalidateCapabilities(pos);
                        }
                    }
                }
            }
        } finally {
            REMOVING.set(false);
        }
    }

    public static List<BoundaryNeighbor> boundaryNeighbors(BlockPos controller, Direction facing) {
        List<BlockPos> members = new ArrayList<>(WIDTH * DEPTH * HEIGHT);
        Set<BlockPos> occupied = new HashSet<>(WIDTH * DEPTH * HEIGHT);
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
            if (member.equals(controller)) continue;
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