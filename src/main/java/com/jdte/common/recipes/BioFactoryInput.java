package com.jdte.common.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public record BioFactoryInput(Ingredient ingredient, int count) {
    public static final Codec<BioFactoryInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(BioFactoryInput::ingredient),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("count", 0).forGetter(BioFactoryInput::count)
    ).apply(instance, BioFactoryInput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BioFactoryInput> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, BioFactoryInput::ingredient,
            ByteBufCodecs.VAR_INT, BioFactoryInput::count,
            BioFactoryInput::new);
}
