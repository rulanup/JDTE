package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WrenchAreaAdjustResultPayload(double radius, double maxRadius) implements CustomPacketPayload {
    public static final Type<WrenchAreaAdjustResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "wrench_area_adjust_result"));

    @Override
    public Type<WrenchAreaAdjustResultPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, WrenchAreaAdjustResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, WrenchAreaAdjustResultPayload::radius,
            ByteBufCodecs.DOUBLE, WrenchAreaAdjustResultPayload::maxRadius,
            WrenchAreaAdjustResultPayload::new
    );
}
