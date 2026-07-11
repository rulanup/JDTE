package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WrenchAreaAdjustPayload(BlockPos blockPos, int delta) implements CustomPacketPayload {
    public static final Type<WrenchAreaAdjustPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "wrench_area_adjust"));

    @Override
    public Type<WrenchAreaAdjustPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, WrenchAreaAdjustPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WrenchAreaAdjustPayload::blockPos,
            ByteBufCodecs.INT, WrenchAreaAdjustPayload::delta,
            WrenchAreaAdjustPayload::new
    );
}
