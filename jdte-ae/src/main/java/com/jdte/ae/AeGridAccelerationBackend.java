package com.jdte.ae;

import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import com.jdte.ae.AeGridVirtualTickExecutor.GridWork;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetHandle;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetResolution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class AeGridAccelerationBackend implements ExternalTimeAccelerationBackend {
    public static final AeGridAccelerationBackend INSTANCE = new AeGridAccelerationBackend();

    private final AeGridResolver resolver;
    private final FrameExecutor executor;
    private final LongSupplier currentTick;
    private final IdentityHashMap<Grid, GridContribution> contributions = new IdentityHashMap<>();
    private MinecraftServer currentServer;
    private MinecraftServer clockServer;

    public AeGridAccelerationBackend() {
        this(new AeGridResolver(), new AeGridVirtualTickExecutor()::execute,
                () -> TickHandler.instance().getCurrentTick());
    }

    AeGridAccelerationBackend(AeGridResolver resolver) {
        this(resolver, new AeGridVirtualTickExecutor()::execute,
                () -> TickHandler.instance().getCurrentTick());
    }

    AeGridAccelerationBackend(AeGridResolver resolver, FrameExecutor executor,
                              LongSupplier currentTick) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.currentTick = Objects.requireNonNull(currentTick, "currentTick");
    }

    @Override
    public void beginFrame(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        if (currentServer != null) {
            throw new IllegalStateException("An AE acceleration frame is already open");
        }
        if (clockServer != null && clockServer != server) {
            AeVirtualTickContext.reset();
        }
        clockServer = server;
        contributions.clear();
        resolver.beginFrame();
        currentServer = server;
    }

    @Override
    public TargetResolution resolve(ServerLevel level, BlockPos pos) {
        requireOpenFrame();
        return resolver.resolve(
                Objects.requireNonNull(level, "level"),
                Objects.requireNonNull(pos, "pos"));
    }

    @Override
    public void submit(TargetHandle target, Object contributor, int additionalCycles) {
        if (additionalCycles <= 0) {
            return;
        }
        requireOpenFrame();
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(contributor, "contributor");
        if (target instanceof AeGridHandle handle) {
            submit(handle, contributor, additionalCycles);
            return;
        }
        if (target instanceof AeGridHandleGroup group) {
            for (AeGridHandle handle : group.handles()) {
                submit(handle, contributor, additionalCycles);
            }
            return;
        }
        throw new IllegalArgumentException("Unsupported AE grid target handle: "
                + target.getClass().getName());
    }

    @Override
    public void executeFrame(MinecraftServer server) {
        requireMatchingFrame(server);
        long frameTick = currentTick.getAsLong();
        List<GridWork> work = contributions.values().stream()
                .map(contribution -> new GridWork(
                        contribution.handle(), contribution.totalCycles()))
                .filter(item -> item.additionalCycles() > 0)
                .toList();
        executor.execute(frameTick, work);
    }

    @Override
    public void clearFrame(MinecraftServer server) {
        requireMatchingFrame(server);
        clearFrameState();
    }

    @Override
    public void onLevelUnload(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        if (currentServer == null) {
            return;
        }
        for (Grid removedGrid : resolver.onLevelUnload(level)) {
            contributions.remove(removedGrid);
        }
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        if (currentServer == server) {
            clearFrameState();
        }
        if (clockServer == server) {
            AeVirtualTickContext.reset();
            clockServer = null;
        }
    }

    private void clearFrameState() {
        contributions.clear();
        resolver.clearFrame();
        currentServer = null;
    }

    private void requireOpenFrame() {
        if (currentServer == null) {
            throw new IllegalStateException("No AE acceleration frame is open");
        }
    }

    private void requireMatchingFrame(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        requireOpenFrame();
        if (currentServer != server) {
            throw new IllegalStateException("AE acceleration frame belongs to another server");
        }
    }

    int additionalCyclesForTest(AeGridHandle handle) {
        GridContribution contribution = contributions.get(handle.grid());
        return contribution == null ? 0 : contribution.totalCycles();
    }

    int totalMultiplierForTest(AeGridHandle handle) {
        int additionalCycles = additionalCyclesForTest(handle);
        return additionalCycles == Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : additionalCycles + 1;
    }

    private void submit(AeGridHandle handle, Object contributor, int additionalCycles) {
        contributions.computeIfAbsent(handle.grid(), ignored -> new GridContribution(handle))
                .add(contributor, additionalCycles);
    }

    static final class GridContribution {
        private final AeGridHandle handle;
        private final IdentityHashMap<Object, Integer> byContributor = new IdentityHashMap<>();

        private GridContribution(AeGridHandle handle) {
            this.handle = handle;
        }

        AeGridHandle handle() {
            return handle;
        }

        private void add(Object contributor, int cycles) {
            byContributor.merge(contributor, cycles, Math::max);
        }

        int totalCycles() {
            long total = 0L;
            for (int cycles : byContributor.values()) {
                total = Math.min(Integer.MAX_VALUE, total + cycles);
            }
            return (int) total;
        }
    }

    @FunctionalInterface
    interface FrameExecutor {
        void execute(long frameTick, List<GridWork> work);
    }
}
