package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AutoIoConfigSyncPayload(BlockPos blockPos, int inputMask, int outputMask) implements CustomPacketPayload {
    public static final Type<AutoIoConfigSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JDTE.MODID, "auto_io_config_sync"));

    @Override
    public Type<AutoIoConfigSyncPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, AutoIoConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AutoIoConfigSyncPayload::blockPos,
            ByteBufCodecs.INT, AutoIoConfigSyncPayload::inputMask,
            ByteBufCodecs.INT, AutoIoConfigSyncPayload::outputMask,
            AutoIoConfigSyncPayload::new
    );
}
