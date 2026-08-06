package com.jdte.common.network.handler;

import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class LootFabricatorLootSyncPacket {
    private LootFabricatorLootSyncPacket() { }

    public static void handle(LootFabricatorLootSyncPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(LootFabricatorLootSyncPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> com.jdte.client.LootFabricatorLootClientCache.set(payload.drops()));
        }
    }
}
