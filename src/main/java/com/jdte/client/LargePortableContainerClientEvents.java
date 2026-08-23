package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.common.network.data.OpenLargePortableContainerPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class LargePortableContainerClientEvents {
    private LargePortableContainerClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        for (OpenLargePortableContainerPayload payload : collectOpenPayloads(
                minecraft.player != null,
                minecraft.screen != null,
                JDTEKeyMappings.LARGE_POCKET_GENERATOR::consumeClick,
                JDTEKeyMappings.LARGE_POTION_CANISTER::consumeClick,
                JDTEKeyMappings.LARGE_FUEL_CANISTER::consumeClick)) {
            PacketDistributor.sendToServer(payload);
        }
    }

    public static List<OpenLargePortableContainerPayload> collectOpenPayloads(boolean hasPlayer,
                                                                              boolean screenOpen,
                                                                              BooleanSupplier generatorKey,
                                                                              BooleanSupplier potionKey,
                                                                              BooleanSupplier fuelKey) {
        if (!hasPlayer || screenOpen) {
            return List.of();
        }
        List<OpenLargePortableContainerPayload> payloads = new ArrayList<>(3);
        if (generatorKey.getAsBoolean()) {
            payloads.add(new OpenLargePortableContainerPayload(
                    OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR));
        }
        if (potionKey.getAsBoolean()) {
            payloads.add(new OpenLargePortableContainerPayload(
                    OpenLargePortableContainerPayload.ContainerKind.LARGE_POTION_CANISTER));
        }
        if (fuelKey.getAsBoolean()) {
            payloads.add(new OpenLargePortableContainerPayload(
                    OpenLargePortableContainerPayload.ContainerKind.LARGE_FUEL_CANISTER));
        }
        return payloads;
    }
}
