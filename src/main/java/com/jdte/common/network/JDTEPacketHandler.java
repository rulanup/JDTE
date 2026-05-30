package com.jdte.common.network;

import com.jdte.JDTE;
import com.jdte.common.network.data.GelGeneratorPayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.network.handler.GelGeneratorPacket;
import com.jdte.common.network.handler.TimeAcceleratorPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class JDTEPacketHandler {
    public static void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(JDTE.MODID);
        registrar.playToServer(TimeAcceleratorPayload.TYPE, TimeAcceleratorPayload.STREAM_CODEC, TimeAcceleratorPacket.get()::handle);
        registrar.playToServer(GelGeneratorPayload.TYPE, GelGeneratorPayload.STREAM_CODEC, GelGeneratorPacket.get()::handle);
    }
}
