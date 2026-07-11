package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SpawnEggRecipeSyncPayload(Map<ResourceLocation, ResourceLocation> recipes) implements CustomPacketPayload {
    public static final Type<SpawnEggRecipeSyncPayload> TYPE = new Type<>(JDTE.id("spawn_egg_recipe_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnEggRecipeSyncPayload> STREAM_CODEC = StreamCodec.of(
            SpawnEggRecipeSyncPayload::encode,
            SpawnEggRecipeSyncPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SpawnEggRecipeSyncPayload payload) {
        buf.writeVarInt(payload.recipes().size());
        payload.recipes().forEach((drop, egg) -> {
            buf.writeResourceLocation(drop);
            buf.writeResourceLocation(egg);
        });
    }

    private static SpawnEggRecipeSyncPayload decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, ResourceLocation> recipes = new HashMap<>();
        for (int i = 0; i < size; i++) {
            recipes.put(buf.readResourceLocation(), buf.readResourceLocation());
        }
        return new SpawnEggRecipeSyncPayload(Map.copyOf(recipes));
    }

    @Override
    public Type<SpawnEggRecipeSyncPayload> type() {
        return TYPE;
    }
}
