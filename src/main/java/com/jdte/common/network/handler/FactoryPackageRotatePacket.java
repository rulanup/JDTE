package com.jdte.common.network.handler;

import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.network.data.FactoryPackageRotatePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.jdte.common.network.PacketContext;

public final class FactoryPackageRotatePacket {
    private FactoryPackageRotatePacket() {}

    public static void handle(FactoryPackageRotatePayload payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack = FactoryPackageItem.isFilled(player.getMainHandItem())
                    ? player.getMainHandItem() : player.getOffhandItem();
            if (!FactoryPackageItem.isFilled(stack) || payload.delta() == 0) return;
            FactoryPackageItem.rotate(stack, Integer.signum(payload.delta()));
            player.getInventory().setChanged();
            player.displayClientMessage(Component.translatable("message.jdte.factory_package.rotated",
                    FactoryPackageItem.getRotation(stack) * 90), true);
        });
    }
}
