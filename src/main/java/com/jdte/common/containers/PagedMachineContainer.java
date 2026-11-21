package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.mixin.AbstractContainerMenuInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

abstract class PagedMachineContainer extends BaseMachineContainer {
    protected PagedMachineContainer(MenuType<?> type, int id, Inventory inventory, BlockPos pos) {
        super(type, id, inventory, pos);
    }

    @Override public void clicked(int index, int button, ContainerInput input, Player player) {
        if (index >= 0 && index < slots.size() && slots.get(index) instanceof PagedMachineSlot slot) {
            ItemStack stored = slot.getItem();
            int limit = stored.getMaxStackSize();
            if (stored.getCount() > limit && slot.mayPickup(player)) {
                // Vanilla repeats quick-move and whole-stack throws, and swaps the full slot into the hotbar.
                if (input == ContainerInput.QUICK_MOVE && (button == 0 || button == 1)) {
                    ItemStack batch = stored.copyWithCount(limit);
                    if (((AbstractContainerMenuInvoker) this)
                            .jdte$invokeMoveItemStackTo(batch, slots.size() - 36, slots.size(), true)) {
                        slot.onTake(player, slot.remove(limit - batch.getCount()));
                    }
                    return;
                }
                if (input == ContainerInput.SWAP && ((button >= 0 && button < 9) || button == 40)) {
                    if (player.getInventory().getItem(button).isEmpty()) {
                        player.getInventory().setItem(button, slot.safeTake(limit, limit, player));
                    }
                    return;
                }
                if (input == ContainerInput.THROW && button == 1 && getCarried().isEmpty()) {
                    if (player.canDropItems()) {
                        ItemStack dropped = slot.safeTake(limit, limit, player);
                        player.drop(dropped, true);
                        player.handleCreativeModeItemDrop(dropped);
                    }
                    return;
                }
            }
        }
        super.clicked(index, button, input, player);
    }
}
