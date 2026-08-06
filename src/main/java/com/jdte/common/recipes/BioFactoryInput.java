package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

public record BioFactoryInput(Ingredient ingredient, int count) {
    public static BioFactoryInput fromJson(JsonObject json) {
        return new BioFactoryInput(Ingredient.fromJson(json.get("ingredient")), GsonHelper.getAsInt(json, "count", 1));
    }

    public static BioFactoryInput fromNetwork(FriendlyByteBuf buffer) {
        return new BioFactoryInput(Ingredient.fromNetwork(buffer), buffer.readVarInt());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeVarInt(count);
    }
}
