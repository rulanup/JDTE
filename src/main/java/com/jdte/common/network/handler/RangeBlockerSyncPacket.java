package com.jdte.common.network.handler;

import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.network.data.RangeBlockerSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RangeBlockerSyncPacket {
    private RangeBlockerSyncPacket() {}

    public static void handle(RangeBlockerSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.blockPos()) instanceof RangeBlockerBE blocker) {
                blocker.applyClientSync(payload.mode(), payload.blacklist(), payload.active(), payload.area());
            }
        });
    }
}
