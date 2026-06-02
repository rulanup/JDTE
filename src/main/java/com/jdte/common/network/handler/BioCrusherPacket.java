package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.network.data.BioCrusherPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BioCrusherPacket {
    private static final BioCrusherPacket INSTANCE = new BioCrusherPacket();

    public static BioCrusherPacket get() {
        return INSTANCE;
    }

    public void handle(BioCrusherPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof BaseMachineContainer machineContainer && machineContainer.baseMachineBE instanceof BioCrusherBE crusher) {
                crusher.setMode(payload.mode());
            }
        });
    }
}
