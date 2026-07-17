package com.jdte.common.factory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

public final class FactoryEntityMoveSupport {
    private FactoryEntityMoveSupport() {}

    public static boolean preparePlacement(ServerLevel level, Entity root) {
        if (!ModList.get().isLoaded("logisticsnetworks")) return true;
        return root.getSelfAndPassengers().allMatch(entity ->
                LogisticsNetworkFactoryMoveIntegration.preparePlacement(level, entity));
    }

    public static void completePlacement(ServerLevel level, Entity root) {
        if (!ModList.get().isLoaded("logisticsnetworks")) return;
        root.getSelfAndPassengers().forEach(entity ->
                LogisticsNetworkFactoryMoveIntegration.completePlacement(level, entity));
    }

    public static void prepareRemoval(ServerLevel level, Entity root) {
        if (!ModList.get().isLoaded("logisticsnetworks")) return;
        root.getSelfAndPassengers().forEach(entity ->
                LogisticsNetworkFactoryMoveIntegration.prepareRemoval(level, entity));
    }

}
