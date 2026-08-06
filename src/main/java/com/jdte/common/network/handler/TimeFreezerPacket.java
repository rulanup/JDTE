package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.network.data.TimeFreezerPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimeFreezerPacket {
    private static final TimeFreezerPacket INSTANCE = new TimeFreezerPacket();

    public static TimeFreezerPacket get() {
        return INSTANCE;
    }

    public void handle(TimeFreezerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container instanceof BaseMachineContainer machineContainer && machineContainer.baseMachineBE instanceof TimeFreezerBE freezer) {
                freezer.setTimeFreezeEnabled(payload.timeFreezeEnabled());
                freezer.setWeatherFreezeEnabled(payload.weatherFreezeEnabled());
            }
        });
    }
}
