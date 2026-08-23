package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class LargePortableContainerClientEvents {
    private LargePortableContainerClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (JDTEKeyMappings.LARGE_POCKET_GENERATOR.consumeClick()) {
            PacketDistributor.sendToServer(
                    new OpenLargePortableContainerPayload(
                            OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR));
        }
        if (JDTEKeyMappings.LARGE_POTION_CANISTER.consumeClick()) {
            PacketDistributor.sendToServer(
                    new OpenLargePortableContainerPayload(
                            OpenLargePortableContainerPayload.ContainerKind.LARGE_POTION_CANISTER));
        }
        if (JDTEKeyMappings.LARGE_FUEL_CANISTER.consumeClick()) {
            PacketDistributor.sendToServer(
                    new OpenLargePortableContainerPayload(
                            OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER));
        }
    }
}
