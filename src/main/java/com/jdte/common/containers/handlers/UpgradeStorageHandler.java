package com.jdte.common.containers.handlers;

import com.jdte.common.items.UpgradeStorageItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/** Live, item-backed Forge replacement for the source's ComponentItemHandler. */
public class UpgradeStorageHandler extends ItemStackHandler {
    public static final int SLOTS = 20;
    public static final String NBT_KEY = "JDTEUpgradeStorage";
    private final ItemStack owner;
    private final int fixedSlots;
    private CompoundTag lastSaved;
    private boolean refreshing;

    public UpgradeStorageHandler(ItemStack owner) {
        this(owner, SLOTS);
    }

    protected UpgradeStorageHandler(ItemStack owner, int slots) {
        super(slots);
        this.fixedSlots = slots;
        if (!(owner.getItem() instanceof UpgradeStorageItem)) throw new IllegalArgumentException("Expected an Upgrade Storage item");
        this.owner = owner;
        refresh();
    }

    private void refresh() {
        if (refreshing) return;
        CompoundTag data = owner.hasTag() ? owner.getTag().getCompound(NBT_KEY) : new CompoundTag();
        if (data.equals(lastSaved)) return;
        CompoundTag bounded = data.copy();
        bounded.putInt("Size", fixedSlots); // Saved NBT must not resize this fixed inventory.
        refreshing = true;
        try {
            super.deserializeNBT(bounded);
        } finally { refreshing = false; }
        lastSaved = data.copy();
    }

    public void save() {
        CompoundTag data = super.serializeNBT();
        owner.getOrCreateTag().put(NBT_KEY, data);
        lastSaved = data.copy();
    }

    @Override public int getSlotLimit(int slot) { return 64; }
    @Override public int getStackLimit(int slot, ItemStack stack) { return 64; }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.isEmpty() || UpgradeStorageItem.isAllowedUpgrade(stack); }
    @Override public ItemStack getStackInSlot(int slot) { refresh(); return super.getStackInSlot(slot); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        refresh(); return super.insertItem(slot, stack, simulate);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        refresh(); return super.extractItem(slot, amount, simulate);
    }
    @Override public void setStackInSlot(int slot, ItemStack stack) {
        refresh();
        if (!isItemValid(slot, stack) || stack.getCount() > getSlotLimit(slot)) throw new IllegalArgumentException("Invalid Upgrade Storage contents");
        super.setStackInSlot(slot, stack);
    }
    @Override protected void onContentsChanged(int slot) { if (!refreshing) save(); }
}
