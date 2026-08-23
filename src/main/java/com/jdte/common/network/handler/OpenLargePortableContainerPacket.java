package com.jdte.common.network.handler;

import com.jdte.common.containers.LargePortableContainerMenus;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenLargePortableContainerPacket {
    private OpenLargePortableContainerPacket() {
    }

    public static void handle(OpenLargePortableContainerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                LargePortableContainerMenus.openFromCurios(player, payload.containerKind());
            }
        });
    }
}
