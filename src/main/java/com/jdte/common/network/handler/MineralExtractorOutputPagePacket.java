package com.jdte.common.network.handler;

import com.jdte.common.containers.MineralExtractorContainer;
import com.jdte.common.network.data.MineralExtractorOutputPagePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MineralExtractorOutputPagePacket {
    private MineralExtractorOutputPagePacket() {
    }

    public static void handle(MineralExtractorOutputPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MineralExtractorContainer container) {
                container.setOutputPage(payload.page());
            }
        });
    }
}