package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.network.PacketContext;
import com.jdte.common.network.data.TimeFreezerPayload;

public final class TimeFreezerPacket {
    private static final TimeFreezerPacket INSTANCE = new TimeFreezerPacket();

    public static TimeFreezerPacket get() {
        return INSTANCE;
    }

    private TimeFreezerPacket() {
    }

    public static void handle(TimeFreezerPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BaseMachineContainer menu
                    && menu.baseMachineBE instanceof TimeFreezerBE freezer) {
                freezer.setTimeFreezeEnabled(payload.timeFreezeEnabled());
                freezer.setWeatherFreezeEnabled(payload.weatherFreezeEnabled());
            }
        });
    }
}
