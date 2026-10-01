package com.jdte.common.integrations.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Predicate;

final class AdvancedUpgradeStorageCuriosIntegration {
    private AdvancedUpgradeStorageCuriosIntegration() {
    }

    static ItemStack find(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (var stacksHandler : handler.getCurios().values()) {
                        var stacks = stacksHandler.getStacks();
                        for (int slot = 0; slot < stacks.getSlots(); slot++) {
                            ItemStack stack = stacks.getStackInSlot(slot);
                            if (!stack.isEmpty() && predicate.test(stack)) {
                                return stack;
                            }
                        }
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }
}
