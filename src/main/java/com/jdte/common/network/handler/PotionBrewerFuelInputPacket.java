package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.network.data.PotionBrewerFuelInputPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PotionBrewerFuelInputPacket {
    private PotionBrewerFuelInputPacket() {
    }

    public static void handle(PotionBrewerFuelInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof BaseMachineContainer machineContainer)
                    || !(machineContainer.baseMachineBE instanceof AdvancedPotionBrewerBE brewer)
                    || !brewer.getBlockPos().equals(payload.blockPos())) {
                return;
            }
            brewer.setFuelInputEnabled(payload.enabled());
        });
    }
}
