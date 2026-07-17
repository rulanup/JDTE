package com.jdte.common.factory;

import com.jdte.common.factory.FactoryPackageStorage.TickRecord;
import com.jdte.mixin.LevelTicksAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

import java.util.ArrayList;
import java.util.List;

public final class FactoryScheduledTickSupport {
    private FactoryScheduledTickSupport() {}

    public static List<TickRecord> capture(ServerLevel level, BlockPos origin, BlockPos max) {
        BoundingBox bounds = BoundingBox.fromCorners(origin, max);
        List<TickRecord> result = new ArrayList<>();
        capture(level.getBlockTicks(), bounds, origin, level.getGameTime(), false, result);
        capture(level.getFluidTicks(), bounds, origin, level.getGameTime(), true, result);
        return List.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private static <T> void capture(LevelTicks<T> ticks, BoundingBox bounds, BlockPos origin, long gameTime,
                                    boolean fluid, List<TickRecord> output) {
        var containers = ((LevelTicksAccessor<T>) (Object) ticks).jdte$getAllContainers();
        int minChunkX = SectionPos.blockToSectionCoord(bounds.minX());
        int maxChunkX = SectionPos.blockToSectionCoord(bounds.maxX());
        int minChunkZ = SectionPos.blockToSectionCoord(bounds.minZ());
        int maxChunkZ = SectionPos.blockToSectionCoord(bounds.maxZ());
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                var container = containers.get(ChunkPos.asLong(chunkX, chunkZ));
                if (container == null) continue;
                container.getAll().filter(tick -> bounds.isInside(tick.pos())).forEach(tick -> {
                    var typeId = fluid
                            ? BuiltInRegistries.FLUID.getKey((net.minecraft.world.level.material.Fluid) tick.type())
                            : BuiltInRegistries.BLOCK.getKey((net.minecraft.world.level.block.Block) tick.type());
                    long delay = Math.max(0L, tick.triggerTick() - gameTime);
                    output.add(new TickRecord(fluid, typeId, tick.pos().subtract(origin),
                            (int) Math.min(Integer.MAX_VALUE, delay), tick.priority().getValue()));
                });
            }
        }
    }

    public static void clear(ServerLevel level, BlockPos origin, BlockPos max) {
        BoundingBox bounds = BoundingBox.fromCorners(origin, max);
        level.getBlockTicks().clearArea(bounds);
        level.getFluidTicks().clearArea(bounds);
    }

    public static void restore(ServerLevel level, BlockPos targetOrigin, List<TickRecord> records,
                               FactoryTransform transform) {
        for (TickRecord record : records) {
            BlockPos pos = targetOrigin.offset(transform.position(record.relativePos()));
            TickPriority priority = TickPriority.byValue(record.priority());
            if (record.fluid()) {
                BuiltInRegistries.FLUID.getOptional(record.type())
                        .ifPresent(fluid -> level.scheduleTick(pos, fluid, record.delay(), priority));
            } else {
                BuiltInRegistries.BLOCK.getOptional(record.type())
                        .ifPresent(block -> level.scheduleTick(pos, block, record.delay(), priority));
            }
        }
    }
}
