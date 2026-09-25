package com.jdte.ae;

import appeng.api.networking.IGridNode;
import appeng.me.Grid;
import com.jdte.ae.AEGridVirtualTickExecutor.GridTickTarget;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AEGridHandle implements TargetHandle, GridTickTarget {
    private final Grid grid;
    private final Set<IGridNode> anchoredNodes =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Anchor> anchors = new ArrayList<>();

    AEGridHandle(Grid grid) {
        this.grid = Objects.requireNonNull(grid, "grid");
    }

    Grid grid() {
        return grid;
    }

    void addAnchor(IGridNode node, ServerLevel level, BlockPos pos) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");
        if (anchoredNodes.add(node)) {
            anchors.add(new Anchor(node, level, pos.immutable()));
        }
    }

    List<Anchor> anchors() {
        return List.copyOf(anchors);
    }

    boolean removeAnchorsIn(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        for (Iterator<Anchor> iterator = anchors.iterator(); iterator.hasNext(); ) {
            Anchor anchor = iterator.next();
            if (anchor.level() == level) {
                anchoredNodes.remove(anchor.node());
                iterator.remove();
            }
        }
        return anchors.isEmpty();
    }

    @Override
    public boolean isValid() {
        if (grid.isEmpty()) {
            return false;
        }
        return anchors.stream().anyMatch(anchor ->
                anchor.node().isOnline()
                        && anchor.node().hasGridBooted()
                        && anchor.node().getGrid() == grid);
    }

    @Override
    public List<ServerLevel> loadedLevels() {
        Set<ServerLevel> levels = Collections.newSetFromMap(new IdentityHashMap<>());
        for (IGridNode node : grid.getNodes()) {
            if (node.isOnline()
                    && node.hasGridBooted()
                    && node.getGrid() == grid
                    && node.getLevel() != null) {
                levels.add(node.getLevel());
            }
        }
        return levels.stream()
                .sorted(Comparator.comparing(level ->
                        level.dimension().location().toString()))
                .toList();
    }

    @Override
    public int serialNumber() {
        return grid.getSerialNumber();
    }

    @Override
    public String anchorSummary() {
        return anchors.stream()
                .map(anchor -> anchor.level().dimension().location()
                        + "@(" + anchor.pos().getX()
                        + "," + anchor.pos().getY()
                        + "," + anchor.pos().getZ() + ")")
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }

    @Override
    public void onServerStartTick() {
        grid.onServerStartTick();
    }

    @Override
    public void onLevelStartTick(ServerLevel level) {
        grid.onLevelStartTick(level);
    }

    @Override
    public void onLevelEndTick(ServerLevel level) {
        grid.onLevelEndTick(level);
    }

    @Override
    public void onServerEndTick() {
        grid.onServerEndTick();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof AEGridHandle handle && grid == handle.grid;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(grid);
    }

    record Anchor(IGridNode node, ServerLevel level, BlockPos pos) {
        Anchor {
            Objects.requireNonNull(node, "node");
            Objects.requireNonNull(level, "level");
            Objects.requireNonNull(pos, "pos");
        }
    }
}
