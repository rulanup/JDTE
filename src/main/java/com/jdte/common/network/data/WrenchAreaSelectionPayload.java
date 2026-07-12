package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WrenchAreaSelectionPayload(BlockPos machinePos, BlockPos firstCorner, BlockPos secondCorner)
        implements CustomPacketPayload {
    public static final Type<WrenchAreaSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "wrench_area_selection"));

    public static final StreamCodec<FriendlyByteBuf, WrenchAreaSelectionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WrenchAreaSelectionPayload::machinePos,
            BlockPos.STREAM_CODEC, WrenchAreaSelectionPayload::firstCorner,
            BlockPos.STREAM_CODEC, WrenchAreaSelectionPayload::secondCorner,
            WrenchAreaSelectionPayload::new
    );

    @Override
    public Type<WrenchAreaSelectionPayload> type() {
        return TYPE;
    }
}
