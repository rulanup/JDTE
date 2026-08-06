package com.jdte.client;

import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.network.data.FactoryPackageRotatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FactoryPackageScrollHandler {
    private FactoryPackageScrollHandler() {}

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !JDTEKeyMappings.WRENCH_AREA_MODIFIER.isDown()) {
            return;
        }
        ItemStack held = FactoryPackageItem.isFilled(minecraft.player.getMainHandItem())
                ? minecraft.player.getMainHandItem() : minecraft.player.getOffhandItem();
        if (!FactoryPackageItem.isFilled(held)) return;
        int delta = event.getScrollDeltaY() > 0 ? 1 : event.getScrollDeltaY() < 0 ? -1 : 0;
        if (delta == 0) return;
        FactoryPackageItem.rotate(held, delta);
        PacketDistributor.sendToServer(new FactoryPackageRotatePayload(delta));
        event.setCanceled(true);
    }
}
