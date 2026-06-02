package com.jdte.common.network;

import com.jdte.JDTE;
import com.jdte.common.network.data.BioCrusherPayload;
import com.jdte.common.network.data.GelGeneratorPayload;
import com.jdte.common.network.data.LifeExtractorPayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.common.network.handler.BioCrusherPacket;
import com.jdte.common.network.handler.GelGeneratorPacket;
import com.jdte.common.network.handler.LifeExtractorPacket;
import com.jdte.common.network.handler.TimeAcceleratorPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class JDTEPacketHandler {
    public static void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(JDTE.MODID);
        registrar.playToServer(TimeAcceleratorPayload.TYPE, TimeAcceleratorPayload.STREAM_CODEC, TimeAcceleratorPacket.get()::handle);
        registrar.playToServer(GelGeneratorPayload.TYPE, GelGeneratorPayload.STREAM_CODEC, GelGeneratorPacket.get()::handle);
        registrar.playToServer(LifeExtractorPayload.TYPE, LifeExtractorPayload.STREAM_CODEC, LifeExtractorPacket.get()::handle);
        registrar.playToServer(BioCrusherPayload.TYPE, BioCrusherPayload.STREAM_CODEC, BioCrusherPacket.get()::handle);
    }
}
