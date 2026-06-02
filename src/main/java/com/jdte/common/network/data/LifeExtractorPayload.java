package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LifeExtractorPayload(int mode) implements CustomPacketPayload {
    public static final Type<LifeExtractorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "life_extractor"));

    @Override
    public Type<LifeExtractorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, LifeExtractorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LifeExtractorPayload::mode,
            LifeExtractorPayload::new
    );
}
