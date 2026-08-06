package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record BioFactoryOutput(ItemStack stack, float chance) {
    public static BioFactoryOutput fromJson(JsonObject json) {
        return new BioFactoryOutput(InfusionRecipe.Serializer.itemStack(GsonHelper.getAsJsonObject(json, "item")),
                GsonHelper.getAsFloat(json, "chance", 1.0F));
    }

    public static BioFactoryOutput fromNetwork(FriendlyByteBuf buffer) {
        return new BioFactoryOutput(buffer.readItem(), buffer.readFloat());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeItem(stack);
        buffer.writeFloat(chance);
    }

    public ItemStack roll(RandomSource random, double multiplier) {
        if (stack.isEmpty() || random.nextFloat() >= chance) return ItemStack.EMPTY;
        double scaled = stack.getCount() * Math.max(0.0D, multiplier);
        int count = (int) Math.floor(scaled);
        if (random.nextDouble() < scaled - count) count++;
        return count <= 0 ? ItemStack.EMPTY : stack.copyWithCount(count);
    }
}
