package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.network.data.RangeBlockerPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RangeBlockerPacket {
    private RangeBlockerPacket() {}

    public static void handle(RangeBlockerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BaseMachineContainer menu
                    && menu.baseMachineBE instanceof RangeBlockerBE blocker) {
                blocker.setSettings(payload.mode(), payload.target(), payload.blacklist());
            }
        });
    }
}
