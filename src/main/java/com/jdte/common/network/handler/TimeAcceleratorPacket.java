package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimeAcceleratorPacket {
    private static final TimeAcceleratorPacket INSTANCE = new TimeAcceleratorPacket();

    public static TimeAcceleratorPacket get() {
        return INSTANCE;
    }

    public void handle(TimeAcceleratorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof BaseMachineContainer machineContainer && machineContainer.baseMachineBE instanceof AdvancedTimeAcceleratorBE accelerator) {
                accelerator.setMultiplier(payload.multiplier());
            }
        });
    }
}
