package com.jdte.common.factory;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import com.extendedae_plus.content.wireless.WirelessTransceiverBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

final class ExtendedAEPlusFactoryMoveIntegration {
    private ExtendedAEPlusFactoryMoveIntegration() {}

    static void prepareNetworkDetach(BlockEntity blockEntity) {
        if (!(blockEntity instanceof WirelessTransceiverBlockEntity transceiver)) return;

        // The move snapshot already contains the original frequency. Clearing the live endpoint first prevents the
        // add-on's removal callback from mutating AE2's connection list while GridNode.destroy() iterates it.
        transceiver.setFrequencyForced(0L);

        Set<IGridNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Direction direction : Direction.values()) {
            IGridNode node = transceiver.getGridNode(direction);
            if (node == null || !visited.add(node)) continue;
            for (IGridConnection connection : List.copyOf(node.getConnections())) {
                if (!connection.isInWorld()) connection.destroy();
            }
        }
    }
}
