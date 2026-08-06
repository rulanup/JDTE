package com.jdte.common.network.handler;

import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.network.data.RangeBlockerSyncPayload;
import com.jdte.common.network.PacketContext;
import net.minecraft.client.Minecraft;

public final class RangeBlockerSyncPacket {
    private RangeBlockerSyncPacket() {}

    public static void handle(RangeBlockerSyncPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getBlockEntity(payload.blockPos()) instanceof RangeBlockerBE blocker) {
                blocker.applyClientSync(payload.mode(), payload.target(), payload.blacklist(),
                        payload.active(), payload.area());
            }
        });
    }
}
