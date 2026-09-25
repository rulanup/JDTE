package com.jdte.ae;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.me.Grid;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetHandle;
import com.jdte.common.acceleration.ExternalTimeAccelerationBackend.TargetResolution;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AEGridResolver {
    private final NodeHostLookup lookup;
    private final IdentityHashMap<Grid, AEGridHandle> handles = new IdentityHashMap<>();

    public AEGridResolver() {
        this(GridHelper::getNodeHost);
    }

    AEGridResolver(NodeHostLookup lookup) {
        this.lookup = Objects.requireNonNull(lookup, "lookup");
    }

    void beginFrame() {
        handles.clear();
    }

    TargetResolution resolve(ServerLevel level, BlockPos pos) {
        IInWorldGridNodeHost host = lookup.find(level, pos);
        if (host == null) {
            return TargetResolution.notExternal();
        }

        Set<IGridNode> seenNodes = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Grid> activeGrids = Collections.newSetFromMap(new IdentityHashMap<>());
        List<AEGridHandle> activeHandles = new ArrayList<>();
        for (Direction side : Direction.values()) {
            IGridNode node = host.getGridNode(side);
            if (node == null || !seenNodes.add(node)) {
                continue;
            }
            if (node.isOnline()
                    && node.hasGridBooted()
                    && node.getGrid() instanceof Grid grid) {
                AEGridHandle handle = handles.computeIfAbsent(grid, AEGridHandle::new);
                handle.addAnchor(node, level, pos);
                if (activeGrids.add(grid)) {
                    activeHandles.add(handle);
                }
            }
        }

        if (activeHandles.isEmpty()) {
            return TargetResolution.inactiveExternal();
        }
        if (activeHandles.size() == 1) {
            return TargetResolution.active(activeHandles.getFirst());
        }
        return TargetResolution.active(new AEGridHandleGroup(activeHandles));
    }

    void clearFrame() {
        handles.clear();
    }

    Set<Grid> onLevelUnload(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        Set<Grid> removed = Collections.newSetFromMap(new IdentityHashMap<>());
        handles.entrySet().removeIf(entry -> {
            if (entry.getValue().removeAnchorsIn(level)) {
                removed.add(entry.getKey());
                return true;
            }
            return false;
        });
        return removed;
    }

    @FunctionalInterface
    interface NodeHostLookup {
        IInWorldGridNodeHost find(ServerLevel level, BlockPos pos);
    }
}

final class AEGridHandleGroup implements TargetHandle {
    private final List<AEGridHandle> handles;

    AEGridHandleGroup(List<AEGridHandle> handles) {
        if (handles.size() < 2) {
            throw new IllegalArgumentException("A grid handle group requires multiple handles");
        }
        this.handles = List.copyOf(handles);
    }

    List<AEGridHandle> handles() {
        return handles;
    }
}
