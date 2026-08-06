package com.jdte.common.network.handler;

import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.network.data.EntitySuppressorSyncPayload;
import com.jdte.common.network.PacketContext;
import net.minecraft.client.Minecraft;

public final class EntitySuppressorSyncPacket {
    private EntitySuppressorSyncPacket() {}

    public static void handle(EntitySuppressorSyncPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getBlockEntity(payload.blockPos()) instanceof EntitySuppressorBE suppressor) {
                suppressor.applyClientSync(payload.mode(), payload.target(), payload.blacklist(),
                        payload.particleActive(), payload.entitySuppressionActive(),
                        payload.renderingSuppressionActive(), payload.area());
            }
        });
    }
}
