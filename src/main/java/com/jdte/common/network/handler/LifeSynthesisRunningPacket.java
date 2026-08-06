package com.jdte.common.network.handler;

import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.network.data.LifeSynthesisRunningPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class LifeSynthesisRunningPacket {
    private LifeSynthesisRunningPacket() {}

    public static void handle(LifeSynthesisRunningPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.blockPos()) instanceof LifeSynthesisVatBE vat) {
                vat.applyClientSync(payload.running());
            }
        });
    }
}
