package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GreenhouseMatrixControlPayload(BlockPos blockPos, int action, boolean value) implements CustomPacketPayload {
    public static final Type<GreenhouseMatrixControlPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "greenhouse_matrix_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GreenhouseMatrixControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, GreenhouseMatrixControlPayload::blockPos,
            ByteBufCodecs.VAR_INT, GreenhouseMatrixControlPayload::action,
            ByteBufCodecs.BOOL, GreenhouseMatrixControlPayload::value,
            GreenhouseMatrixControlPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
