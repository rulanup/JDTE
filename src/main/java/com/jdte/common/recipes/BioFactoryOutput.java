package com.jdte.common.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record BioFactoryOutput(ItemStack stack, float chance) {
    public static final Codec<BioFactoryOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(BioFactoryOutput::stack),
            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(BioFactoryOutput::chance)
    ).apply(instance, BioFactoryOutput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BioFactoryOutput> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, BioFactoryOutput::stack,
            ByteBufCodecs.FLOAT, BioFactoryOutput::chance,
            BioFactoryOutput::new);

    public ItemStack roll(RandomSource random, double multiplier) {
        if (stack.isEmpty() || random.nextFloat() >= chance) return ItemStack.EMPTY;
        double scaled = stack.getCount() * Math.max(0.0D, multiplier);
        int count = (int) Math.floor(scaled);
        if (random.nextDouble() < scaled - count) count++;
        return count <= 0 ? ItemStack.EMPTY : stack.copyWithCount(count);
    }
}
