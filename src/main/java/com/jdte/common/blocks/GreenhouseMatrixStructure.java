package com.jdte.common.blocks;

import com.jdte.common.blockentities.GreenhouseMatrixControllerBE;
import com.jdte.common.blockentities.GreenhouseMatrixAutoCraftingBE;
import com.jdte.common.blockentities.GreenhouseMatrixPortBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.greenhouse.GreenhouseMatrixEnhancement;
import com.jdte.common.greenhouse.GreenhouseMatrixPortType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class GreenhouseMatrixStructure {
    public static final int MIN_SIZE = 5;
    public static final int MAX_SIZE = 18;
    private static final int MAX_SHELL_BLOCKS = 6 * MAX_SIZE * MAX_SIZE;

    public record ScanResult(boolean formed, String error, BlockPos min, BlockPos max,
                             List<BlockPos> greenhouses, List<BlockPos> ports,
                             List<BlockPos> autoCraftingPages,
                             EnumMap<GreenhouseMatrixEnhancement, Integer> enhancements) {
        public static ScanResult invalid(String error) {
            return new ScanResult(false, error, BlockPos.ZERO, BlockPos.ZERO,
                    List.of(), List.of(), List.of(), emptyCounts());
        }
    }

    private GreenhouseMatrixStructure() {}

    public static ScanResult scan(ServerLevel level, BlockPos controllerPos) {
        Set<BlockPos> shell = collectShell(level, controllerPos);
        if (shell.isEmpty()) return ScanResult.invalid("disconnected");
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int controllers = 0;
        for (BlockPos pos : shell) {
            minX = Math.min(minX, pos.getX()); minY = Math.min(minY, pos.getY()); minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX()); maxY = Math.max(maxY, pos.getY()); maxZ = Math.max(maxZ, pos.getZ());
            if (level.getBlockEntity(pos) instanceof GreenhouseMatrixControllerBE) controllers++;
        }
        if (controllers != 1) return ScanResult.invalid("controller_count");
        int sx = maxX - minX + 1, sy = maxY - minY + 1, sz = maxZ - minZ + 1;
        if (!validSize(sx) || !validSize(sy) || !validSize(sz)) return ScanResult.invalid("size");

        BlockPos min = new BlockPos(minX, minY, minZ);
        BlockPos max = new BlockPos(maxX, maxY, maxZ);
        List<BlockPos> greenhouses = new ArrayList<>();
        List<BlockPos> ports = new ArrayList<>();
        List<BlockPos> autoCraftingPages = new ArrayList<>();
        EnumSet<GreenhouseMatrixPortType> portTypes = EnumSet.noneOf(GreenhouseMatrixPortType.class);
        EnumMap<GreenhouseMatrixEnhancement, Integer> enhancements = emptyCounts();
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockPos pos = cursor.immutable();
            if (!level.isLoaded(pos)) return ScanResult.invalid("unloaded");
            boolean boundary = pos.getX() == minX || pos.getX() == maxX
                    || pos.getY() == minY || pos.getY() == maxY
                    || pos.getZ() == minZ || pos.getZ() == maxZ;
            BlockState state = level.getBlockState(pos);
            if (boundary) {
                if (!isShell(state.getBlock())) return ScanResult.invalid("open_shell");
                if (level.getBlockEntity(pos) instanceof GreenhouseMatrixPortBE
                        && state.getBlock() instanceof GreenhouseMatrixPortBlock portBlock) {
                    ports.add(pos);
                    portTypes.add(portBlock.portType());
                }
                continue;
            }
            if (level.getBlockEntity(pos) instanceof GreenhouseBE
                    || level.getBlockEntity(pos) instanceof LargeGreenhouseBE) {
                greenhouses.add(pos);
            } else if (state.getBlock() instanceof GreenhouseMatrixAutoCraftingBlock) {
                if (!(level.getBlockEntity(pos) instanceof GreenhouseMatrixAutoCraftingBE)) {
                    return ScanResult.invalid("invalid_interior");
                }
                autoCraftingPages.add(pos);
                enhancements.merge(GreenhouseMatrixEnhancement.AUTO_CRAFTING, 1, Integer::sum);
            } else if (state.getBlock() instanceof GreenhouseMatrixEnhancementBlock enhancer) {
                enhancements.merge(enhancer.enhancement(), 1, Integer::sum);
            } else if (!state.isAir() && !state.is(com.jdte.setup.JDTEBlocks.LARGE_GREENHOUSE_PART.get())) {
                return ScanResult.invalid("invalid_interior");
            }
        }
        if (greenhouses.isEmpty()) return ScanResult.invalid("no_greenhouse");
        if (portTypes.size() != GreenhouseMatrixPortType.values().length) return ScanResult.invalid("missing_port");
        if (hasCraftingEnhancementConflict(enhancements)) {
            return ScanResult.invalid("conflicting_crafting_enhancements");
        }
        return new ScanResult(true, "", min, max, List.copyOf(greenhouses), List.copyOf(ports),
                sortAutoCraftingPages(autoCraftingPages), enhancements);
    }

    public static boolean isShell(Block block) {
        return block instanceof GreenhouseMatrixCasingBlock
                || block instanceof GreenhouseMatrixControllerBlock
                || block instanceof GreenhouseMatrixPortBlock;
    }

    private static Set<BlockPos> collectShell(ServerLevel level, BlockPos start) {
        if (!isShell(level.getBlockState(start).getBlock())) return Set.of();
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && found.size() <= MAX_SHELL_BLOCKS) {
            BlockPos pos = queue.removeFirst();
            if (!found.add(pos) || !level.isLoaded(pos)) continue;
            for (var direction : net.minecraft.core.Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!found.contains(next) && level.isLoaded(next) && isShell(level.getBlockState(next).getBlock())) {
                    queue.addLast(next);
                }
            }
        }
        return found.size() > MAX_SHELL_BLOCKS ? Set.of() : found;
    }

    private static boolean validSize(int size) { return size >= MIN_SIZE && size <= MAX_SIZE; }

    public static boolean hasCraftingEnhancementConflict(EnumMap<GreenhouseMatrixEnhancement, Integer> counts) {
        return counts != null
                && counts.getOrDefault(GreenhouseMatrixEnhancement.SEED_CONVERSION, 0) > 0
                && counts.getOrDefault(GreenhouseMatrixEnhancement.AUTO_CRAFTING, 0) > 0;
    }

    public static List<BlockPos> sortAutoCraftingPages(List<BlockPos> positions) {
        Comparator<BlockPos> comparator = Comparator.comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingInt(pos -> pos.getZ())
                .thenComparingInt(pos -> pos.getX());
        TreeSet<BlockPos> sorted = new TreeSet<>(comparator);
        if (positions != null) {
            for (BlockPos position : positions) {
                if (position != null) sorted.add(position.immutable());
            }
        }
        return List.copyOf(sorted);
    }

    private static EnumMap<GreenhouseMatrixEnhancement, Integer> emptyCounts() {
        EnumMap<GreenhouseMatrixEnhancement, Integer> counts = new EnumMap<>(GreenhouseMatrixEnhancement.class);
        for (GreenhouseMatrixEnhancement type : GreenhouseMatrixEnhancement.values()) counts.put(type, 0);
        return counts;
    }
}
