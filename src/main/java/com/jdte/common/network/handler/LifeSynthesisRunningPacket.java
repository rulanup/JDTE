package com.jdte.common.network.handler;

import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.network.data.LifeSynthesisRunningPayload;
import com.jdte.common.network.PacketContext;
import net.minecraft.client.Minecraft;

public final class LifeSynthesisRunningPacket {
    private LifeSynthesisRunningPacket() {}

    public static void handle(LifeSynthesisRunningPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getBlockEntity(payload.blockPos()) instanceof LifeSynthesisVatBE vat) {
                vat.applyClientSync(payload.running());
            }
        });
    }
}
