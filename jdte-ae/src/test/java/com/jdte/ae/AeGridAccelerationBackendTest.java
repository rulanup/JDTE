package com.jdte.ae;

import appeng.api.networking.IGridNode;
import appeng.me.Grid;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetResolution;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeGridAccelerationBackendTest {
    @AfterEach
    void resetClock() {
        AeVirtualTickContext.reset();
    }

    @Test
    void duplicateCoverageUsesTheMaximumContributionPerContributor() throws Exception {
        Grid gridA = AeGridTestFixture.grid(20);
        Grid gridB = AeGridTestFixture.grid(21);
        AeGridHandle handleA = new AeGridHandle(gridA);
        AeGridHandle handleB = new AeGridHandle(gridB);
        Object acceleratorOne = new Object();
        Object acceleratorTwo = new Object();
        AeGridAccelerationBackend backend = backend();
        backend.beginFrame(AeGridTestFixture.server());

        backend.submit(handleA, acceleratorOne, 15);
        backend.submit(handleA, acceleratorOne, 15);
        backend.submit(handleA, acceleratorTwo, 15);
        backend.submit(handleB, acceleratorOne, 15);

        assertEquals(30, backend.additionalCyclesForTest(handleA));
        assertEquals(15, backend.additionalCyclesForTest(handleB));
        assertEquals(31, backend.totalMultiplierForTest(handleA));
    }

    @Test
    void two1024ContributorsProduce2047TotalMultiplier() throws Exception {
        AeGridHandle handle = new AeGridHandle(AeGridTestFixture.grid(22));
        AeGridAccelerationBackend backend = backend();
        backend.beginFrame(AeGridTestFixture.server());

        backend.submit(handle, new Object(), 1023);
        backend.submit(handle, new Object(), 1023);

        assertEquals(2046, backend.additionalCyclesForTest(handle));
        assertEquals(2047, backend.totalMultiplierForTest(handle));
    }

    @Test
    void contributionAdditionSaturatesInsteadOfOverflowing() throws Exception {
        AeGridHandle handle = new AeGridHandle(AeGridTestFixture.grid(23));
        AeGridAccelerationBackend backend = backend();
        backend.beginFrame(AeGridTestFixture.server());

        backend.submit(handle, new Object(), Integer.MAX_VALUE);
        backend.submit(handle, new Object(), Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, backend.additionalCyclesForTest(handle));
        assertEquals(Integer.MAX_VALUE, backend.totalMultiplierForTest(handle));
    }

    @Test
    void aMultiGridHandleFansOutTheFullContribution() throws Exception {
        Grid gridA = AeGridTestFixture.grid(24);
        Grid gridB = AeGridTestFixture.grid(25);
        ServerLevel level = AeGridTestFixture.level("overworld");
        IGridNode nodeA = AeGridTestFixture.node(gridA, level, true, true);
        IGridNode nodeB = AeGridTestFixture.node(gridB, level, true, true);
        AeGridResolver resolver = new AeGridResolver((ignoredLevel, ignoredPos) ->
                AeGridTestFixture.host(Map.of(Direction.NORTH, nodeA, Direction.SOUTH, nodeB)));
        AeGridAccelerationBackend backend = new AeGridAccelerationBackend(resolver);
        backend.beginFrame(AeGridTestFixture.server());

        TargetResolution resolution = backend.resolve(level, BlockPos.ZERO);
        backend.submit(resolution.handle(), new Object(), 15);

        assertEquals(15, backend.additionalCyclesForTest(new AeGridHandle(gridA)));
        assertEquals(15, backend.additionalCyclesForTest(new AeGridHandle(gridB)));
    }

    @Test
    void nonPositiveContributionsCreateNoGridWork() throws Exception {
        AeGridHandle handle = new AeGridHandle(AeGridTestFixture.grid(26));
        AeGridAccelerationBackend backend = backend();
        MinecraftServer server = AeGridTestFixture.server();
        backend.beginFrame(server);

        backend.submit(handle, new Object(), 0);
        backend.submit(handle, new Object(), -1);

        assertEquals(0, backend.additionalCyclesForTest(handle));
        assertEquals(1, backend.totalMultiplierForTest(handle));
        backend.clearFrame(server);
        assertEquals(0, backend.additionalCyclesForTest(handle));
    }

    @Test
    void frameCannotReenterAndRequiresTheMatchingServer() throws Exception {
        AeGridAccelerationBackend backend = backend();
        MinecraftServer server = AeGridTestFixture.server();
        MinecraftServer otherServer = AeGridTestFixture.server();
        backend.beginFrame(server);

        assertThrows(IllegalStateException.class, () -> backend.beginFrame(server));
        assertThrows(IllegalStateException.class, () -> backend.executeFrame(otherServer));
        assertThrows(IllegalStateException.class, () -> backend.clearFrame(otherServer));

        backend.clearFrame(server);
        backend.beginFrame(server);
        backend.clearFrame(server);
    }

    @Test
    void executeFramePassesAggregatedWorkAtOneNativeTick() throws Exception {
        AtomicLong observedTick = new AtomicLong(Long.MIN_VALUE);
        AtomicReference<List<AeGridVirtualTickExecutor.GridWork>> observedWork =
                new AtomicReference<>();
        AeGridAccelerationBackend backend = new AeGridAccelerationBackend(
                new AeGridResolver((level, pos) -> null),
                (nativeTick, work) -> {
                    observedTick.set(nativeTick);
                    observedWork.set(List.copyOf(work));
                },
                () -> 77L);
        MinecraftServer server = AeGridTestFixture.server();
        AeGridHandle handle = new AeGridHandle(AeGridTestFixture.grid(27));
        backend.beginFrame(server);
        backend.submit(handle, new Object(), 15);
        backend.submit(handle, new Object(), 15);

        backend.executeFrame(server);

        assertEquals(77L, observedTick.get());
        assertEquals(1, observedWork.get().size());
        assertSame(handle, observedWork.get().getFirst().target());
        assertEquals(30, observedWork.get().getFirst().additionalCycles());
        backend.clearFrame(server);
    }

    @Test
    void serverStopClearsOnlyItsMatchingOpenFrame() throws Exception {
        AeGridAccelerationBackend backend = backend();
        MinecraftServer server = AeGridTestFixture.server();
        MinecraftServer otherServer = AeGridTestFixture.server();
        backend.beginFrame(server);
        long nativeStamp = AeVirtualTickContext.override(100L);
        long virtualStamp;
        try (AeVirtualTickContext.Scope ignored =
                     AeVirtualTickContext.enter(nativeStamp, 1)) {
            virtualStamp = AeVirtualTickContext.override(100L);
        }
        assertTrue(virtualStamp > nativeStamp);

        backend.onServerStopped(otherServer);
        assertThrows(IllegalStateException.class, () -> backend.beginFrame(server));
        assertEquals(virtualStamp, AeVirtualTickContext.override(100L));

        backend.onServerStopped(server);
        assertEquals(100L, AeVirtualTickContext.override(100L));
        backend.beginFrame(server);
        backend.clearFrame(server);
        backend.onServerStopped(server);
    }

    @Test
    void levelUnloadPrunesOnlyAnchorsFromThatDimension() throws Exception {
        Grid grid = AeGridTestFixture.grid(28);
        ServerLevel overworld = AeGridTestFixture.level("overworld");
        ServerLevel nether = AeGridTestFixture.level("the_nether");
        IGridNode overworldNode = AeGridTestFixture.node(grid, overworld, true, true);
        IGridNode netherNode = AeGridTestFixture.node(grid, nether, true, true);
        AeGridResolver resolver = new AeGridResolver((level, pos) ->
                side -> level == overworld ? overworldNode : netherNode);
        AeGridAccelerationBackend backend = new AeGridAccelerationBackend(resolver);
        MinecraftServer server = AeGridTestFixture.server();
        backend.beginFrame(server);

        TargetResolution first = backend.resolve(overworld, BlockPos.ZERO);
        TargetResolution second = backend.resolve(nether, BlockPos.ZERO);
        assertSame(first.handle(), second.handle());
        AeGridHandle handle = (AeGridHandle) first.handle();
        assertEquals(2, handle.anchors().size());
        backend.submit(handle, new Object(), 15);

        backend.onLevelUnload(overworld);
        assertEquals(1, handle.anchors().size());
        assertEquals(15, backend.additionalCyclesForTest(handle));

        backend.onLevelUnload(nether);
        assertEquals(0, handle.anchors().size());
        assertEquals(0, backend.additionalCyclesForTest(handle));
        backend.clearFrame(server);
    }

    private static AeGridAccelerationBackend backend() {
        return new AeGridAccelerationBackend(new AeGridResolver((level, pos) -> null));
    }
}
