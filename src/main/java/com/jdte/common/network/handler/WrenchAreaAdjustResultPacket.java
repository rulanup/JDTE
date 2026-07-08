package com.jdte.common.network.handler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.jdte.common.network.data.WrenchAreaAdjustResultPayload;

public class WrenchAreaAdjustResultPacket {
    private static final WrenchAreaAdjustResultPacket INSTANCE = new WrenchAreaAdjustResultPacket();

    public static WrenchAreaAdjustResultPacket get() {
        return INSTANCE;
    }

    public void handle(WrenchAreaAdjustResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return;
            minecraft.player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_area",
                            String.format("%.0f", payload.radius()),
                            String.format("%.0f", payload.maxRadius()))
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
            minecraft.player.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
        });
    }
}
