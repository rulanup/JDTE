package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.network.data.GelGeneratorPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GelGeneratorPacket {
    private static final GelGeneratorPacket INSTANCE = new GelGeneratorPacket();

    public static GelGeneratorPacket get() {
        return INSTANCE;
    }

    public void handle(GelGeneratorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof BaseMachineContainer machineContainer && machineContainer.baseMachineBE instanceof GelGeneratorBE gelGenerator) {
                gelGenerator.setAutoBalanceInputs(payload.autoBalanceInputs());
            }
        });
    }
}
