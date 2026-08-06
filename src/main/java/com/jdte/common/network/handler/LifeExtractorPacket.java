package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.network.data.LifeExtractorPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LifeExtractorPacket {
    private static final LifeExtractorPacket INSTANCE = new LifeExtractorPacket();

    public static LifeExtractorPacket get() {
        return INSTANCE;
    }

    public void handle(LifeExtractorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof BaseMachineContainer machineContainer && machineContainer.baseMachineBE instanceof LifeExtractorBE extractor) {
                extractor.setMode(payload.mode());
            }
        });
    }
}
