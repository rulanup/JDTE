package com.jdte.common.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record BioFactoryOutput(ItemStackTemplate template, float chance) {
    public BioFactoryOutput(ItemStack stack, float chance) {
        this(ItemStackTemplate.fromNonEmptyStack(stack), chance);
    }

    public ItemStack stack() { return template.create(); }

    public static final Codec<BioFactoryOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(BioFactoryOutput::template),
            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(BioFactoryOutput::chance)
    ).apply(instance, BioFactoryOutput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BioFactoryOutput> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, BioFactoryOutput::template,
            ByteBufCodecs.FLOAT, BioFactoryOutput::chance,
            BioFactoryOutput::new);

    public ItemStack roll(RandomSource random, double multiplier) {
        if (random.nextFloat() >= chance) return ItemStack.EMPTY;
        double scaled = template.count() * Math.max(0.0D, multiplier);
        int count = (int) Math.floor(scaled);
        if (random.nextDouble() < scaled - count) count++;
        return count <= 0 ? ItemStack.EMPTY : template.withCount(count).create();
    }
}
