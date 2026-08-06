package com.jdte.common.network.handler;

import com.jdte.client.LootFabricatorLootClientCache;
import com.jdte.common.network.data.LootFabricatorLootSyncPayload;
import com.jdte.common.network.PacketContext;

public final class LootFabricatorLootSyncPacket {
    private LootFabricatorLootSyncPacket() { }

    public static void handle(LootFabricatorLootSyncPayload payload, PacketContext context) {
        context.enqueueWork(() -> LootFabricatorLootClientCache.set(payload.drops()));
    }
}
