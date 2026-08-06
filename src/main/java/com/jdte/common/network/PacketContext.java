package com.jdte.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * Small compatibility facade for JDTE's packet handlers while they move from
 * NeoForge payload contexts to Forge's SimpleChannel context.
 */
public final class PacketContext {
    private final NetworkEvent.Context delegate;

    public PacketContext(NetworkEvent.Context delegate) {
        this.delegate = delegate;
    }

    public void enqueueWork(Runnable work) {
        delegate.enqueueWork(work);
    }

    public Player player() {
        ServerPlayer sender = delegate.getSender();
        if (sender == null) {
            throw new IllegalStateException("A client-bound packet does not have a sending player");
        }
        return sender;
    }
}
