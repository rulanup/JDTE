package com.jdte.common.factory;

import me.almana.logisticsnetworks.data.NetworkRegistry;
import me.almana.logisticsnetworks.entity.LogisticsNodeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

final class LogisticsNetworkFactoryMoveIntegration {
    private LogisticsNetworkFactoryMoveIntegration() {}

    static boolean preparePlacement(ServerLevel level, Entity entity) {
        if (!(entity instanceof LogisticsNodeEntity node)) return true;
        BlockPos attachedPos = node.blockPosition();
        node.setAttachedPos(attachedPos);
        if (level.getBlockState(attachedPos).isAir()) return false;
        node.setPos(Vec3.atCenterOf(attachedPos));
        node.setNoGravity(true);
        node.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    static void completePlacement(ServerLevel level, Entity entity) {
        if (!(entity instanceof LogisticsNodeEntity node)) return;
        NetworkRegistry registry = NetworkRegistry.get(level);
        UUID networkId = node.getNetworkId();
        if (networkId != null) {
            registry.addNodeToNetwork(networkId, node.getUUID());
            registry.markNetworkDirty(networkId);
        }
        registry.evictCapabilities(level, node.getAttachedPos());
    }

    static void prepareRemoval(ServerLevel level, Entity entity) {
        if (!(entity instanceof LogisticsNodeEntity node)) return;
        NetworkRegistry registry = NetworkRegistry.get(level);
        UUID networkId = node.getNetworkId();
        if (networkId != null) {
            registry.removeNodeFromNetwork(networkId, node.getUUID());
            registry.markNetworkDirty(networkId);
        }
        registry.evictCapabilities(level, node.getAttachedPos());
    }

}
