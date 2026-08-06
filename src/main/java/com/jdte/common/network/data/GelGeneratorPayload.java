package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GelGeneratorPayload(boolean autoBalanceInputs) implements CustomPacketPayload {
    public static final Type<GelGeneratorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "gel_generator"));

    @Override
    public Type<GelGeneratorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, GelGeneratorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GelGeneratorPayload::autoBalanceInputs,
            GelGeneratorPayload::new
    );
}
