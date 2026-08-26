package com.jdte.common.network.handler;

import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.network.data.ExtendedExperienceHolderPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ExtendedExperienceHolderPacket {
    private ExtendedExperienceHolderPacket() {
    }

    public static void handle(ExtendedExperienceHolderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof ExtendedExperienceHolderContainer container)
                    || !(container.baseMachineBE instanceof ExtendedExperienceHolderBE experienceHolder)
                    || !container.getBlockPos().equals(payload.blockPos())
                    || !experienceHolder.getBlockPos().equals(payload.blockPos())) {
                return;
            }

            if (payload.add()) {
                experienceHolder.storeExp(context.player(), payload.levels());
            } else {
                experienceHolder.extractExp(context.player(), payload.levels());
            }
        });
    }
}
