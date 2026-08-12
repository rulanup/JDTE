package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Objects;
import java.util.function.Predicate;

/** Fixed one-page inventory for encoded patterns. */
public final class GreenhouseMatrixPatternItemHandler extends ItemStackHandler {
    public static final int SLOTS = 16;
    private final Predicate<ItemStack> validator;
    private final Runnable changeListener;
    private boolean loading;

    public GreenhouseMatrixPatternItemHandler(Predicate<ItemStack> validator, Runnable changeListener) {
        super(SLOTS);
        this.validator = Objects.requireNonNull(validator);
        this.changeListener = Objects.requireNonNull(changeListener);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < SLOTS && stack != null && !stack.isEmpty() && validator.test(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!loading) changeListener.run();
    }

    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        loading = true;
        try {
            deserializeNBT(provider, tag);
        } finally {
            loading = false;
        }
    }
}
