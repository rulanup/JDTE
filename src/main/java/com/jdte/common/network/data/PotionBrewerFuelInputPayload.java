package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PotionBrewerFuelInputPayload(BlockPos blockPos, boolean enabled) implements CustomPacketPayload {
    public static final Type<PotionBrewerFuelInputPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "potion_brewer_fuel_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionBrewerFuelInputPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PotionBrewerFuelInputPayload::blockPos,
            ByteBufCodecs.BOOL, PotionBrewerFuelInputPayload::enabled,
            PotionBrewerFuelInputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
