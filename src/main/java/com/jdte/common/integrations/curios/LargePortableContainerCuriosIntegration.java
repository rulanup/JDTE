package com.jdte.common.integrations.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public final class LargePortableContainerCuriosIntegration {
    private LargePortableContainerCuriosIntegration() {
    }

    public static Optional<ItemStack> resolveStack(Player player, String slotId) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(slotId))
                .filter(handler -> handler.getSlots() > 0)
                .map(handler -> handler.getStacks().getStackInSlot(0));
    }
}
