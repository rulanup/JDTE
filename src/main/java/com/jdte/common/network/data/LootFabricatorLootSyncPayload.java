package com.jdte.common.network.data;

import com.jdte.common.utils.LootDropInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record LootFabricatorLootSyncPayload(Map<ResourceLocation, List<LootDropInfo>> drops) {
    public static LootFabricatorLootSyncPayload decode(FriendlyByteBuf buf) {
        int eggCount = buf.readVarInt();
        Map<ResourceLocation, List<LootDropInfo>> drops = new HashMap<>(eggCount);
        for (int i = 0; i < eggCount; i++) {
            ResourceLocation egg = buf.readResourceLocation();
            int dropCount = buf.readVarInt();
            List<LootDropInfo> dropInfos = new ArrayList<>(dropCount);
            for (int j = 0; j < dropCount; j++) {
                dropInfos.add(new LootDropInfo(buf.readResourceLocation(), buf.readVarInt(), buf.readVarInt(),
                        buf.readUtf(32)));
            }
            drops.put(egg, List.copyOf(dropInfos));
        }
        return new LootFabricatorLootSyncPayload(Map.copyOf(drops));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(drops.size());
        drops.forEach((egg, entries) -> {
            buf.writeResourceLocation(egg);
            buf.writeVarInt(entries.size());
            entries.forEach(drop -> {
                buf.writeResourceLocation(drop.itemId());
                buf.writeVarInt(drop.minCount());
                buf.writeVarInt(drop.maxCount());
                buf.writeUtf(drop.chanceLabel(), 32);
            });
        });
    }
}
