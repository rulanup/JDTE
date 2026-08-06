package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.network.data.FactoryPackerStartPayload;
import net.minecraft.server.level.ServerPlayer;
import com.jdte.common.network.PacketContext;

public final class FactoryPackerStartPacket {
    private FactoryPackerStartPacket() {}

    public static void handle(FactoryPackerStartPayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof BaseMachineContainer menu
                    && menu.baseMachineBE instanceof FactoryPackerBE packer) {
                player.sendSystemMessage(packer.startOperation(player), true);
            }
        });
    }
}
