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

class AEGridAccelerationBackendTest {
    @AfterEach
    void resetClock() {
        AEVirtualTickContext.reset();
    }

    @Test
    void duplicateCoverageUsesTheMaximumContributionPerContributor() throws Exception {
        Grid gridA = AEGridTestFixture.grid(20);
        Grid gridB = AEGridTestFixture.grid(21);
        AEGridHandle handleA = new AEGridHandle(gridA);
        AEGridHandle handleB = new AEGridHandle(gridB);
        Object acceleratorOne = new Object();
        Object acceleratorTwo = new Object();
        AEGridAccelerationBackend backend = backend();
        backend.beginFrame(AEGridTestFixture.server());

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
        AEGridHandle handle = new AEGridHandle(AEGridTestFixture.grid(22));
        AEGridAccelerationBackend backend = backend();
        backend.beginFrame(AEGridTestFixture.server());

        backend.submit(handle, new Object(), 1023);
        backend.submit(handle, new Object(), 1023);

        assertEquals(2046, backend.additionalCyclesForTest(handle));
        assertEquals(2047, backend.totalMultiplierForTest(handle));
    }

    @Test
    void contributionAdditionSaturatesInsteadOfOverflowing() throws Exception {
        AEGridHandle handle = new AEGridHandle(AEGridTestFixture.grid(23));
        AEGridAccelerationBackend backend = backend();
        backend.beginFrame(AEGridTestFixture.server());

        backend.submit(handle, new Object(), Integer.MAX_VALUE);
        backend.submit(handle, new Object(), Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, backend.additionalCyclesForTest(handle));
        assertEquals(Integer.MAX_VALUE, backend.totalMultiplierForTest(handle));
    }

    @Test
    void aMultiGridHandleFansOutTheFullContribution() throws Exception {
        Grid gridA = AEGridTestFixture.grid(24);
        Grid gridB = AEGridTestFixture.grid(25);
        ServerLevel level = AEGridTestFixture.level("overworld");
        IGridNode nodeA = AEGridTestFixture.node(gridA, level, true, true);
        IGridNode nodeB = AEGridTestFixture.node(gridB, level, true, true);
        AEGridResolver resolver = new AEGridResolver((ignoredLevel, ignoredPos) ->
                AEGridTestFixture.host(Map.of(Direction.NORTH, nodeA, Direction.SOUTH, nodeB)));
        AEGridAccelerationBackend backend = new AEGridAccelerationBackend(resolver);
        backend.beginFrame(AEGridTestFixture.server());

        TargetResolution resolution = backend.resolve(level, BlockPos.ZERO);
        backend.submit(resolution.handle(), new Object(), 15);

        assertEquals(15, backend.additionalCyclesForTest(new AEGridHandle(gridA)));
        assertEquals(15, backend.additionalCyclesForTest(new AEGridHandle(gridB)));
    }

    @Test
    void nonPositiveContributionsCreateNoGridWork() throws Exception {
        AEGridHandle handle = new AEGridHandle(AEGridTestFixture.grid(26));
        AEGridAccelerationBackend backend = backend();
        MinecraftServer server = AEGridTestFixture.server();
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
        AEGridAccelerationBackend backend = backend();
        MinecraftServer server = AEGridTestFixture.server();
        MinecraftServer otherServer = AEGridTestFixture.server();
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
        AtomicReference<List<AEGridVirtualTickExecutor.GridWork>> observedWork =
                new AtomicReference<>();
        AEGridAccelerationBackend backend = new AEGridAccelerationBackend(
                new AEGridResolver((level, pos) -> null),
                (nativeTick, work) -> {
                    observedTick.set(nativeTick);
                    observedWork.set(List.copyOf(work));
                },
                () -> 77L);
        MinecraftServer server = AEGridTestFixture.server();
        AEGridHandle handle = new AEGridHandle(AEGridTestFixture.grid(27));
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
        AEGridAccelerationBackend backend = backend();
        MinecraftServer server = AEGridTestFixture.server();
        MinecraftServer otherServer = AEGridTestFixture.server();
        backend.beginFrame(server);
        long nativeStamp = AEVirtualTickContext.override(100L);
        long virtualStamp;
        try (AEVirtualTickContext.Scope ignored =
                     AEVirtualTickContext.enter(nativeStamp, 1)) {
            virtualStamp = AEVirtualTickContext.override(100L);
        }
        assertTrue(virtualStamp > nativeStamp);

        backend.onServerStopped(otherServer);
        assertThrows(IllegalStateException.class, () -> backend.beginFrame(server));
        assertEquals(virtualStamp, AEVirtualTickContext.override(100L));

        backend.onServerStopped(server);
        assertEquals(100L, AEVirtualTickContext.override(100L));
        backend.beginFrame(server);
        backend.clearFrame(server);
        backend.onServerStopped(server);
    }

    @Test
    void levelUnloadPrunesOnlyAnchorsFromThatDimension() throws Exception {
        Grid grid = AEGridTestFixture.grid(28);
        ServerLevel overworld = AEGridTestFixture.level("overworld");
        ServerLevel nether = AEGridTestFixture.level("the_nether");
        IGridNode overworldNode = AEGridTestFixture.node(grid, overworld, true, true);
        IGridNode netherNode = AEGridTestFixture.node(grid, nether, true, true);
        AEGridResolver resolver = new AEGridResolver((level, pos) ->
                side -> level == overworld ? overworldNode : netherNode);
        AEGridAccelerationBackend backend = new AEGridAccelerationBackend(resolver);
        MinecraftServer server = AEGridTestFixture.server();
        backend.beginFrame(server);

        TargetResolution first = backend.resolve(overworld, BlockPos.ZERO);
        TargetResolution second = backend.resolve(nether, BlockPos.ZERO);
        assertSame(first.handle(), second.handle());
        AEGridHandle handle = (AEGridHandle) first.handle();
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

    private static AEGridAccelerationBackend backend() {
        return new AEGridAccelerationBackend(new AEGridResolver((level, pos) -> null));
    }
}
