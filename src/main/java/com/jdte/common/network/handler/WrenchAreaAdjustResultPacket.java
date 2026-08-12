package com.jdte.common.network.handler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.jdte.common.network.data.WrenchAreaAdjustResultPayload;

public class WrenchAreaAdjustResultPacket {
    private static final WrenchAreaAdjustResultPacket INSTANCE = new WrenchAreaAdjustResultPacket();

    public static WrenchAreaAdjustResultPacket get() {
        return INSTANCE;
    }

    public void handle(WrenchAreaAdjustResultPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(payload, context);
        }
    }

    private static final class ClientHandler {
        private static void handle(WrenchAreaAdjustResultPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player == null) return;
                minecraft.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.jdte.wrench_area",
                                String.format("%.0f", payload.radius()),
                                String.format("%.0f", payload.maxRadius()))
                                .withStyle(net.minecraft.ChatFormatting.GREEN),
                        true
                );
                minecraft.player.playSound(net.minecraft.sounds.SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
            });
        }
    }
}
