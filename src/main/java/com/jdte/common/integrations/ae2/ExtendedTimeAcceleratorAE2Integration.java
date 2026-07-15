package com.jdte.common.integrations.ae2;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class ExtendedTimeAcceleratorAE2Integration {
    private ExtendedTimeAcceleratorAE2Integration() {
    }

    public static boolean hasTickable(ServerLevel level, BlockPos pos) {
        return !findEndpoints(level, pos).isEmpty();
    }

    public static Result accelerate(ServerLevel level, BlockPos pos, int requestedTicks, long deadline) {
        List<Endpoint> active = findEndpoints(level, pos);
        if (active.isEmpty()) {
            return new Result(0, false, true);
        }

        int executed = 0;
        for (; executed < requestedTicks && !active.isEmpty() && System.nanoTime() < deadline; executed++) {
            Iterator<Endpoint> iterator = active.iterator();
            while (iterator.hasNext()) {
                Endpoint endpoint = iterator.next();
                TickRateModulation modulation = endpoint.tickable.tickingRequest(endpoint.node, 1);
                if (modulation == TickRateModulation.SLEEP) {
                    iterator.remove();
                }
            }
        }
        return new Result(executed, true, active.isEmpty());
    }

    private static List<Endpoint> findEndpoints(ServerLevel level, BlockPos pos) {
        IInWorldGridNodeHost host = GridHelper.getNodeHost(level, pos);
        if (host == null) {
            return new ArrayList<>();
        }

        Set<IGridNode> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Endpoint> endpoints = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            IGridNode node = host.getGridNode(direction);
            if (node == null || !seen.add(node)) {
                continue;
            }
            IGridTickable tickable = node.getService(IGridTickable.class);
            if (tickable != null) {
                endpoints.add(new Endpoint(node, tickable));
            }
        }
        return endpoints;
    }

    public record Result(int executed, boolean valid, boolean idle) {
    }

    private record Endpoint(IGridNode node, IGridTickable tickable) {
    }
}
