package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.network.data.AdvancedEnergyTransmitterBindingPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AdvancedEnergyTransmitterBindingPacket {
    private AdvancedEnergyTransmitterBindingPacket() {
    }

    public static void handle(AdvancedEnergyTransmitterBindingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof BaseMachineContainer menu)
                    || !(menu.baseMachineBE instanceof AdvancedEnergyTransmitterBE transmitter)
                    || !transmitter.getBlockPos().equals(payload.blockPos())) {
                return;
            }
            AdvancedEnergyTransmitterBE.BindingResult result = transmitter.togglePlayerBinding(player);
            player.displayClientMessage(Component.translatable(switch (result) {
                case BOUND -> "message.jdte.energy_transmitter.player_bound";
                case UNBOUND -> "message.jdte.energy_transmitter.player_unbound";
                case DENIED -> "message.jdte.energy_transmitter.binding_denied";
            }), true);
        });
    }
}