package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 生命合成舱运行状态轻量包：仅在配方状态翻转时发送一次，驱动客户端光束渲染。 */
public record LifeSynthesisRunningPayload(BlockPos blockPos, boolean running) implements CustomPacketPayload {
    public static final Type<LifeSynthesisRunningPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "life_synthesis_running"));

    @Override
    public Type<LifeSynthesisRunningPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, LifeSynthesisRunningPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, LifeSynthesisRunningPayload::blockPos,
            ByteBufCodecs.BOOL, LifeSynthesisRunningPayload::running,
            LifeSynthesisRunningPayload::new
    );
}
