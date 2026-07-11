package com.jdte.common.network.handler;

import com.jdte.common.network.data.SpawnEggRecipeSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SpawnEggRecipeSyncPacket {
    private SpawnEggRecipeSyncPacket() {
    }

    public static void handle(SpawnEggRecipeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.jdte.client.SpawnEggRecipeClientCache.set(payload.recipes()));
    }
}
