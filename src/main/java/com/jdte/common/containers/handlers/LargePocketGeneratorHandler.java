package com.jdte.common.containers.handlers;

import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.common.items.PortableFuelBurnSpeedHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import java.util.Objects;

/** One canonical native FuelInventory view shared by menus, fuel consumption and item automation. */
public final class LargePocketGeneratorHandler extends ItemStackHandler {
    public static final String KEY = "FuelInventory";
    private final ItemStack owner;
    private final boolean authoritative;
    private CompoundTag observed;
    private boolean refreshing;
    public LargePocketGeneratorHandler(ItemStack owner, boolean authoritative) { super(1); this.owner = owner; this.authoritative = authoritative; refresh(); }
    private boolean live() { return !authoritative || LargePocketGeneratorItem.canMutate(owner); }
    private void refresh() {
        if (refreshing) return;
        var data = owner.hasTag() ? owner.getTag().getCompound(KEY) : new CompoundTag();
        if (Objects.equals(data, observed)) return;
        var bounded = data.copy(); bounded.putInt("Size", 1); refreshing = true;
        try { super.deserializeNBT(bounded); } finally { refreshing = false; }
        observed = data.copy();
    }
    @Override protected void onContentsChanged(int slot) {
        if (refreshing || !live()) return;
        var data = super.serializeNBT(); owner.getOrCreateTag().put(KEY, data); observed = data.copy();
    }
    public void commitSlotChange() { refresh(); onContentsChanged(0); }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && PortableFuelBurnSpeedHelper.resolveBurnTime(stack) > 0; }
    @Override protected int getStackLimit(int slot, ItemStack stack) { return stack.hasCraftingRemainingItem() ? 1 : Math.min(64, stack.getMaxStackSize()); }
    @Override public ItemStack getStackInSlot(int slot) { refresh(); return super.getStackInSlot(slot); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { refresh(); return live() ? super.insertItem(slot, stack.copy(), simulate) : stack; }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { refresh(); return live() ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY; }
    // Menu synchronization/removal may set a non-fuel remainder. Insert validation is separate.
    @Override public void setStackInSlot(int slot, ItemStack stack) { refresh(); if (live()) super.setStackInSlot(slot, stack.copy()); }
    @Override public void deserializeNBT(CompoundTag nbt) {
        if (owner == null) { super.deserializeNBT(nbt); return; }
        refresh(); if (!live()) return;
        var bounded = nbt.copy(); bounded.putInt("Size", 1); refreshing = true;
        try { super.deserializeNBT(bounded); } finally { refreshing = false; }
        onContentsChanged(0);
    }
    @Override public CompoundTag serializeNBT() { refresh(); return super.serializeNBT(); }
}
