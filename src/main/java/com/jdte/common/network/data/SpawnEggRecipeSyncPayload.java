package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SpawnEggRecipeSyncPayload(Map<ResourceLocation, ResourceLocation> recipes) {
    public static SpawnEggRecipeSyncPayload decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, ResourceLocation> recipes = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            recipes.put(buf.readResourceLocation(), buf.readResourceLocation());
        }
        return new SpawnEggRecipeSyncPayload(Map.copyOf(recipes));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(recipes.size());
        recipes.forEach((drop, egg) -> {
            buf.writeResourceLocation(drop);
            buf.writeResourceLocation(egg);
        });
    }
}
