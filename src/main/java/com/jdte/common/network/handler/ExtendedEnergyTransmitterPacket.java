package com.jdte.common.network.handler;

import com.jdte.common.blockentities.ExtendedEnergyTransmitterBE;
import com.jdte.common.containers.ExtendedEnergyTransmitterContainer;
import com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ExtendedEnergyTransmitterPacket {
    private ExtendedEnergyTransmitterPacket() {
    }

    public static void handle(ExtendedEnergyTransmitterSettingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ExtendedEnergyTransmitterContainer container
                    && container.baseMachineBE instanceof ExtendedEnergyTransmitterBE transmitter) {
                transmitter.setEnergyTransmitterSettings(payload.showParticles());
            }
        });
    }
}
