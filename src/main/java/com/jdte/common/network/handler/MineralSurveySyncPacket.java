package com.jdte.common.network.handler;

import com.jdte.common.network.data.MineralSurveySyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MineralSurveySyncPacket {
    private MineralSurveySyncPacket() { }

    public static void handle(MineralSurveySyncPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(MineralSurveySyncPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> com.jdte.client.MineralSurveyClientCache.set(payload.surveys()));
        }
    }
}
