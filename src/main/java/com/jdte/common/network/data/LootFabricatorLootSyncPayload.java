package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import com.jdte.common.utils.LootDropInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record LootFabricatorLootSyncPayload(Map<Identifier, List<LootDropInfo>> drops) implements CustomPacketPayload {
    public static final Type<LootFabricatorLootSyncPayload> TYPE = new Type<>(JDTE.id("loot_fabricator_loot_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootFabricatorLootSyncPayload> STREAM_CODEC = StreamCodec.of(
            LootFabricatorLootSyncPayload::encode, LootFabricatorLootSyncPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, LootFabricatorLootSyncPayload payload) {
        buf.writeVarInt(payload.drops().size());
        payload.drops().forEach((egg, drops) -> {
            buf.writeIdentifier(egg);
            buf.writeVarInt(drops.size());
            drops.forEach(drop -> {
                buf.writeIdentifier(drop.itemId());
                buf.writeVarInt(drop.minCount());
                buf.writeVarInt(drop.maxCount());
                buf.writeUtf(drop.chanceLabel(), 32);
            });
        });
    }

    private static LootFabricatorLootSyncPayload decode(RegistryFriendlyByteBuf buf) {
        int eggCount = buf.readVarInt();
        Map<Identifier, List<LootDropInfo>> drops = new HashMap<>();
        for (int i = 0; i < eggCount; i++) {
            Identifier egg = buf.readIdentifier();
            int dropCount = buf.readVarInt();
            List<LootDropInfo> dropInfos = new ArrayList<>(dropCount);
            for (int j = 0; j < dropCount; j++) {
                dropInfos.add(new LootDropInfo(buf.readIdentifier(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(32)));
            }
            drops.put(egg, List.copyOf(dropInfos));
        }
        return new LootFabricatorLootSyncPayload(Map.copyOf(drops));
    }

    @Override public Type<LootFabricatorLootSyncPayload> type() { return TYPE; }
}
