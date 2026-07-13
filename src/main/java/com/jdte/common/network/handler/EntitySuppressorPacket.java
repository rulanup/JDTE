package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.network.data.EntitySuppressorPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class EntitySuppressorPacket {
    private EntitySuppressorPacket() {}
    public static void handle(EntitySuppressorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BaseMachineContainer menu
                    && menu.baseMachineBE instanceof EntitySuppressorBE suppressor) {
                suppressor.setSettings(payload.mode(), payload.target(), payload.blacklist());
            }
        });
    }
}
