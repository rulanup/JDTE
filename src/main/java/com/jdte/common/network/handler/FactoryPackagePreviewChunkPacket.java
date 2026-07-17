package com.jdte.common.network.handler;

import com.jdte.client.FactoryPackagePreviewClient;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FactoryPackagePreviewChunkPacket {
    private FactoryPackagePreviewChunkPacket() {}

    public static void handle(FactoryPackagePreviewChunkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> FactoryPackagePreviewClient.acceptChunk(payload));
    }
}
