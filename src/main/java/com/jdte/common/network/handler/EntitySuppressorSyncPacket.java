package com.jdte.common.network.handler;

import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.network.data.EntitySuppressorSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class EntitySuppressorSyncPacket {
    private EntitySuppressorSyncPacket() {}

    public static void handle(EntitySuppressorSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.blockPos()) instanceof EntitySuppressorBE suppressor) {
                suppressor.applyClientSync(payload.mode(), payload.target(), payload.blacklist(),
                        payload.particleActive(), payload.entitySuppressionActive(), payload.area());
            }
        });
    }
}
