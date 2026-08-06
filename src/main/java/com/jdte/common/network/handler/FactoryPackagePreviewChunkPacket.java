package com.jdte.common.network.handler;

import com.jdte.client.FactoryPackagePreviewClient;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import com.jdte.common.network.PacketContext;

public final class FactoryPackagePreviewChunkPacket {
    private FactoryPackagePreviewChunkPacket() {}

    public static void handle(FactoryPackagePreviewChunkPayload payload, PacketContext context) {
        context.enqueueWork(() -> FactoryPackagePreviewClient.acceptChunk(payload));
    }
}
