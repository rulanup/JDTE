package com.jdte.common.integrations.curios;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Predicate;

final class AdvancedEnergyTransmitterCuriosIntegration {
    private AdvancedEnergyTransmitterCuriosIntegration() {
    }

    static void collect(ServerPlayer player, Predicate<ItemStack> visitor) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (var stacksHandler : handler.getCurios().values()) {
                var stacks = stacksHandler.getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (!stack.isEmpty() && !visitor.test(stack)) {
                        return;
                    }
                }
            }
        });
    }
}