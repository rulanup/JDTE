package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccelerationTargetProxyTest {

    @Test
    void singleProxyResolvesToFinalOrdinaryTarget() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos proxyPos = new BlockPos(1, 2, 3);
        BlockPos targetPos = new BlockPos(4, 5, 6);
        TargetGraph graph = new TargetGraph();
        graph.put(level, proxyPos, fakeProxy(new TimeAccelerationTarget(level, targetPos)));
        graph.put(level, targetPos, fakeBlockEntity());

        Optional<TimeAccelerationTarget> result = resolve(graph, level, proxyPos);

        assertTrue(result.isPresent());
        assertSame(level, result.orElseThrow().level());
        assertEquals(targetPos, result.orElseThrow().pos());
    }

    @Test
    void multipleProxiesResolveToFinalTarget() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos firstPos = new BlockPos(1, 0, 0);
        BlockPos secondPos = new BlockPos(2, 0, 0);
        BlockPos targetPos = new BlockPos(3, 0, 0);
        TargetGraph graph = new TargetGraph();
        graph.put(level, firstPos, fakeProxy(new TimeAccelerationTarget(level, secondPos)));
        graph.put(level, secondPos, fakeProxy(new TimeAccelerationTarget(level, targetPos)));
        graph.put(level, targetPos, fakeBlockEntity());

        Optional<TimeAccelerationTarget> result = resolve(graph, level, firstPos);

        assertEquals(new TimeAccelerationTarget(level, targetPos), result.orElseThrow());
    }

    @Test
    void selfLoopIsInvalid() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos pos = new BlockPos(1, 0, 0);
        TargetGraph graph = new TargetGraph();
        graph.put(level, pos, fakeProxy(new TimeAccelerationTarget(level, pos)));

        assertFalse(resolve(graph, level, pos).isPresent());
    }

    @Test
    void multiNodeLoopIsInvalid() throws Exception {
        ServerLevel level = serverLevelFixture();
        BlockPos firstPos = new BlockPos(1, 0, 0);
        BlockPos secondPos = new BlockPos(2, 0, 0);
        TargetGraph graph = new TargetGraph();
        graph.put(level, firstPos, fakeProxy(new TimeAccelerationTarget(level, secondPos)));
        graph.put(level, secondPos, fakeProxy(new TimeAccelerationTarget(level, firstPos)));

        assertFalse(resolve(graph, level, firstPos).isPresent());
    }

    @Test
    void proxyChainBeyondMaximumDepthIsInvalid() throws Exception {
        ServerLevel level = serverLevelFixture();
        TargetGraph graph = new TargetGraph();
        for (int index = 0; index <= 10; index++) {
            BlockPos currentPos = new BlockPos(index, 0, 0);
            BlockPos nextPos = new BlockPos(index + 1, 0, 0);
            graph.put(level, currentPos,
                    fakeProxy(new TimeAccelerationTarget(level, nextPos)));
        }
        graph.put(level, new BlockPos(11, 0, 0), fakeBlockEntity());

        assertFalse(resolve(graph, level, BlockPos.ZERO).isPresent());
    }

    @Test
    void sameCoordinateInDifferentWorldsIsNotTheSameNode() throws Exception {
        ServerLevel firstLevel = serverLevelFixture();
        ServerLevel secondLevel = serverLevelFixture();
        BlockPos pos = new BlockPos(7, 8, 9);
        TargetGraph graph = new TargetGraph();
        graph.put(firstLevel, pos, fakeProxy(new TimeAccelerationTarget(secondLevel, pos)));
        graph.put(secondLevel, pos, fakeBlockEntity());

        Optional<TimeAccelerationTarget> result = resolve(graph, firstLevel, pos);

        assertTrue(result.isPresent());
        assertSame(secondLevel, result.orElseThrow().level());
        assertEquals(pos, result.orElseThrow().pos());
    }

    private static Optional<TimeAccelerationTarget> resolve(TargetGraph graph, ServerLevel level, BlockPos pos) {
        return ExtendedTimeAccelerationManager.resolveTimeAccelerationTarget(level, pos, graph::get);
    }

    private static ServerLevel serverLevelFixture() throws Exception {
        return (ServerLevel) unsafe().allocateInstance(ServerLevel.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static FakeBlockEntity fakeBlockEntity() throws Exception {
        return (FakeBlockEntity) unsafe().allocateInstance(FakeBlockEntity.class);
    }

    private static FakeProxy fakeProxy(TimeAccelerationTarget target) throws Exception {
        FakeProxy proxy = (FakeProxy) unsafe().allocateInstance(FakeProxy.class);
        proxy.target = target;
        return proxy;
    }

    static final class TargetGraph {
        private final Map<ServerLevel, Map<BlockPos, BlockEntity>> entities = new java.util.IdentityHashMap<>();

        private void put(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
            entities.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), blockEntity);
        }

        private BlockEntity get(ServerLevel level, BlockPos pos) {
            Map<BlockPos, BlockEntity> levelEntities = entities.get(level);
            return levelEntities == null ? null : levelEntities.get(pos);
        }
    }

    static class FakeBlockEntity extends BlockEntity {
        private FakeBlockEntity() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }
    }

    static final class FakeProxy extends FakeBlockEntity implements TimeAccelerationTargetProxy {
        private TimeAccelerationTarget target;

        private FakeProxy() {
            super();
        }

        @Override
        public TimeAccelerationTarget getTimeAccelerationTarget() {
            return target;
        }
    }
}
