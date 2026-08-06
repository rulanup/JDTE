package com.jdte.common.factory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** Placeholder for the 1.20.1 Logistics Networks node API binding. */
final class LogisticsNetworkFactoryMoveIntegration {
    private LogisticsNetworkFactoryMoveIntegration() {
    }

    static boolean preparePlacement(ServerLevel level, Entity entity) {
        return true;
    }

    static void completePlacement(ServerLevel level, Entity entity) {
    }

    static void prepareRemoval(ServerLevel level, Entity entity) {
    }
}
