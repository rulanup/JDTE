package com.jdte.common.network.handler;

import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SpawnEggRecipeSyncPacket {
    private SpawnEggRecipeSyncPacket() {
    }

    public static void handle(SpawnEggRecipeSyncPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(SpawnEggRecipeSyncPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> com.jdte.client.SpawnEggRecipeClientCache.set(payload.recipes()));
        }
    }
}
