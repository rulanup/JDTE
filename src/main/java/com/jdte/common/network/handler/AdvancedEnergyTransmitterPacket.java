package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.network.data.AdvancedEnergyTransmitterPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AdvancedEnergyTransmitterPacket {
    private AdvancedEnergyTransmitterPacket() {
    }

    public static void handle(AdvancedEnergyTransmitterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof BaseMachineContainer menu)
                    || !(menu.baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter)
                    || !transmitter.getBlockPos().equals(payload.blockPos())) {
                return;
            }
            transmitter.setShowParticles(payload.showParticles());
        });
    }
}