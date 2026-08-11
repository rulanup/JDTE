package com.jdte.common.network.handler;

import com.jdte.common.containers.GreenhouseMatrixContainer;
import com.jdte.common.network.data.GreenhouseMatrixPatternPagePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class GreenhouseMatrixPatternPagePacket {
    private GreenhouseMatrixPatternPagePacket() {
    }

    public static void handle(GreenhouseMatrixPatternPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof GreenhouseMatrixContainer menu)
                    || !GreenhouseMatrixPatternPageRequestValidator.matches(
                    menu.containerId, menu.getPos(), payload)) {
                return;
            }
            menu.setAutoCraftingPage(payload.page());
        });
    }
}
