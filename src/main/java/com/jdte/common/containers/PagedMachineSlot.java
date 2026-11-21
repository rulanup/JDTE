package com.jdte.common.containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

abstract class PagedMachineSlot extends ResourceHandlerSlot {
    PagedMachineSlot(ItemStacksResourceHandler handler, int slot, int x, int y) {
        super(handler, handler::set, slot, x, y);
    }

    protected abstract boolean isActiveSlot();

    @Override public java.util.Optional<ItemStack> tryRemove(int amount, int maxAmount, Player player) {
        ItemStack stored = getItem();
        if (stored.getCount() <= stored.getMaxStackSize()) return super.tryRemove(amount, maxAmount, player);
        if (!mayPickup(player)) return java.util.Optional.empty();
        int count = Math.min(Math.min(amount, maxAmount), stored.getMaxStackSize());
        return count > 0 ? java.util.Optional.of(remove(count)) : java.util.Optional.empty();
    }

    @Override protected ItemStack getStackCopy() {
        return isActiveSlot() ? super.getStackCopy() : ItemStack.EMPTY;
    }
    @Override protected void setStackCopy(ItemStack stack) {
        if (isActiveSlot()) super.setStackCopy(stack);
    }
    @Override public int getMaxStackSize() { return isActiveSlot() ? super.getMaxStackSize() : 0; }
    @Override public int getMaxStackSize(ItemStack stack) {
        return isActiveSlot() ? super.getMaxStackSize(stack) : 0;
    }
    @Override public boolean mayPickup(Player player) { return isActiveSlot() && super.mayPickup(player); }
    @Override public boolean mayPlace(ItemStack stack) { return isActiveSlot() && super.mayPlace(stack); }
}
