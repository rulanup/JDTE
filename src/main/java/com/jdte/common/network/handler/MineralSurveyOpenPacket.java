package com.jdte.common.network.handler;

import com.jdte.common.network.data.MineralSurveyOpenPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MineralSurveyOpenPacket {
    private MineralSurveyOpenPacket() {
    }

    public static void handle(MineralSurveyOpenPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(MineralSurveyOpenPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> net.minecraft.client.Minecraft.getInstance().setScreen(
                    new com.jdte.client.screens.MineralSurveyScreen(payload.survey())));
        }
    }
}