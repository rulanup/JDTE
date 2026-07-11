package com.jdte.common.network.handler;

import com.jdte.client.LootFabricatorLootClientCache;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class LootFabricatorLootSyncPacket {
    private LootFabricatorLootSyncPacket() { }

    public static void handle(LootFabricatorLootSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> LootFabricatorLootClientCache.set(payload.drops()));
    }
}
