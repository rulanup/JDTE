package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AutoIoConfigPayload(BlockPos blockPos, int sideMask, boolean request) implements CustomPacketPayload {
    public static final Type<AutoIoConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "auto_io_config"));

    @Override
    public Type<AutoIoConfigPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, AutoIoConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AutoIoConfigPayload::blockPos,
            ByteBufCodecs.INT, AutoIoConfigPayload::sideMask,
            ByteBufCodecs.BOOL, AutoIoConfigPayload::request,
            AutoIoConfigPayload::new
    );
}
