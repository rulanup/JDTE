package com.jdte.common.network.handler;

import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ExtendedExperienceHolderSettingsPacket {
    private ExtendedExperienceHolderSettingsPacket() {
    }

    public static void handle(ExtendedExperienceHolderSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof ExtendedExperienceHolderContainer container)
                    || !(container.baseMachineBE instanceof ExtendedExperienceHolderBE experienceHolder)
                    || !container.getBlockPos().equals(payload.blockPos())
                    || !experienceHolder.getBlockPos().equals(payload.blockPos())) {
                return;
            }

            experienceHolder.changeSettings(
                    context.player(), payload.targetExp(), payload.ownerOnly(), payload.collectExp(),
                    payload.showParticles());
        });
    }
}
