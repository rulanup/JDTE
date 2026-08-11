package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GreenhouseMatrixPatternPagePayload(BlockPos blockPos, int containerId, int page)
        implements CustomPacketPayload {
    public static final Type<GreenhouseMatrixPatternPagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "greenhouse_matrix_pattern_page"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GreenhouseMatrixPatternPagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, GreenhouseMatrixPatternPagePayload::blockPos,
                    ByteBufCodecs.VAR_INT, GreenhouseMatrixPatternPagePayload::containerId,
                    ByteBufCodecs.VAR_INT, GreenhouseMatrixPatternPagePayload::page,
                    GreenhouseMatrixPatternPagePayload::new);

    @Override
    public Type<GreenhouseMatrixPatternPagePayload> type() {
        return TYPE;
    }
}
