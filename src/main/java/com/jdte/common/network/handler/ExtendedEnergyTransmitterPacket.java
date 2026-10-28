package com.jdte.common.network.handler;

import com.jdte.common.blockentities.ExtendedEnergyTransmitterBE;
import com.jdte.common.containers.ExtendedEnergyTransmitterContainer;
import com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload;
import com.jdte.common.network.PacketContext;

public final class ExtendedEnergyTransmitterPacket {
    private ExtendedEnergyTransmitterPacket() {
    }

    public static void handle(ExtendedEnergyTransmitterSettingPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ExtendedEnergyTransmitterContainer container
                    && container.baseMachineBE instanceof ExtendedEnergyTransmitterBE transmitter
                    && container.stillValid(context.player()) && !transmitter.isRemoved()
                    && context.player().level().getBlockEntity(transmitter.getBlockPos())==transmitter) {
                transmitter.setEnergyTransmitterSettings(payload.showParticles());
            }
        });
    }
}
