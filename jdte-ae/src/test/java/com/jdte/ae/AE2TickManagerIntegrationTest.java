package com.jdte.ae;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.util.Platform;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AE2TickManagerIntegrationTest {
    @Test
    void nominal1024InvokesRealTickManagerWithOneElapsedTickPerRound() throws Exception {
        ThreadGroup originalServerThreadGroup = Platform.serverThreadGroup;
        Platform.serverThreadGroup = Thread.currentThread().getThreadGroup();
        try {
            ServerLevel level = AeGridTestFixture.level("overworld");
            AtomicInteger calls = new AtomicInteger();
            List<Integer> observedTicksSinceLastCall = new ArrayList<>();
            IGridNodeListener<Object> listener = (owner, node) -> {
            };
            GridNode node = new GridNode(level, new Object(), listener, Set.<GridFlags>of());
            node.addService(IGridTickable.class, new IGridTickable() {
                @Override
                public TickingRequest getTickingRequest(IGridNode gridNode) {
                    return new TickingRequest(1, 1, false);
                }

                @Override
                public TickRateModulation tickingRequest(IGridNode gridNode,
                                                         int ticksSinceLastCall) {
                    calls.incrementAndGet();
                    observedTicksSinceLastCall.add(ticksSinceLastCall);
                    return TickRateModulation.URGENT;
                }
            });

            markReady(node);
            try {
                Grid grid = node.getInternalGrid();
                AeGridVirtualTickExecutor executor = new AeGridVirtualTickExecutor();
                executor.execute(100L, List.of(new AeGridVirtualTickExecutor.GridWork(
                        new RealGridTarget(grid, level), 1023)));

                assertEquals(1023, calls.get());
                assertEquals(1023, observedTicksSinceLastCall.size());
                assertTrue(observedTicksSinceLastCall.stream().allMatch(ticks -> ticks == 1));
            } finally {
                node.destroy();
            }
        } finally {
            AeVirtualTickContext.reset();
            Platform.serverThreadGroup = originalServerThreadGroup;
        }
    }

    private static void markReady(GridNode node) throws ReflectiveOperationException {
        Method method = GridNode.class.getDeclaredMethod("markReady");
        method.setAccessible(true);
        method.invoke(node);
    }

    private record RealGridTarget(Grid grid, ServerLevel level)
            implements AeGridVirtualTickExecutor.GridTickTarget {
        @Override
        public int serialNumber() {
            return grid.getSerialNumber();
        }

        @Override
        public boolean isValid() {
            return !grid.isEmpty();
        }

        @Override
        public List<ServerLevel> loadedLevels() {
            return List.of(level);
        }

        @Override
        public String anchorSummary() {
            return level.dimension().location() + "@(0,0,0)";
        }

        @Override
        public void onServerStartTick() {
            grid.onServerStartTick();
        }

        @Override
        public void onLevelStartTick(ServerLevel serverLevel) {
            grid.onLevelStartTick(serverLevel);
        }

        @Override
        public void onLevelEndTick(ServerLevel serverLevel) {
            grid.onLevelEndTick(serverLevel);
        }

        @Override
        public void onServerEndTick() {
            grid.onServerEndTick();
        }
    }
}
