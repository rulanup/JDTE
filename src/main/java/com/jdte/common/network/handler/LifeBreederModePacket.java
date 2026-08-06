package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.network.data.LifeBreederModePayload;
import com.jdte.common.network.PacketContext;

public final class LifeBreederModePacket {
    private LifeBreederModePacket() { }
    public static void handle(LifeBreederModePayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BaseMachineContainer menu
                    && menu.baseMachineBE instanceof LifeBreederBE breeder) {
                breeder.setMode(payload.mode());
            }
        });
    }
}
