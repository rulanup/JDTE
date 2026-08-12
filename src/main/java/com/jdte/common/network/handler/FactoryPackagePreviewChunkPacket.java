package com.jdte.common.network.handler;

import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FactoryPackagePreviewChunkPacket {
    private FactoryPackagePreviewChunkPacket() {}

    public static void handle(FactoryPackagePreviewChunkPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(FactoryPackagePreviewChunkPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> com.jdte.client.FactoryPackagePreviewClient.acceptChunk(payload));
        }
    }
}
