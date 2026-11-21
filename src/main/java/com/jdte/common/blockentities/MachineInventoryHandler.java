package com.jdte.common.blockentities;

import com.jdte.common.utils.MachineDropDataHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

class MachineInventoryHandler extends ItemStacksResourceHandler {
    private static final MapCodec<CompoundTag> RAW_ENTRY = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private final List<CompoundTag> unresolvedItems = new ArrayList<>();

    MachineInventoryHandler(int slots) {
        super(slots);
    }

    ItemStack copyStack(int slot) {
        return getResource(slot).toStack(getAmountAsInt(slot));
    }

    void setStack(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.isEmpty() ? 0 : stack.getCount());
    }

    boolean insertBatch(int firstSlot, int endSlot, List<ItemStack> drops, TransactionContext parent) {
        var output = RangedResourceHandler.of(this, firstSlot, endSlot);
        try (var batch = Transaction.open(parent)) {
            for (ItemStack stack : drops) {
                if (!stack.isEmpty() && ResourceHandlerUtil.insertStacking(output, ItemResource.of(stack),
                        stack.getCount(), batch) != stack.getCount()) return false;
            }
            batch.commit();
            return true;
        }
    }

    /** Machine production may consume ingredients that its public transfer handler retains. */
    int extractForProduction(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        return super.extract(slot, resource, amount, transaction);
    }

    @Override public void serialize(ValueOutput output) {
        for (int slot = 0; slot < size(); slot++) {
            if (getAmountAsInt(slot) > OversizedItemStackHandlerSerialization.MAX_CODEC_COUNT) {
                OversizedItemStackHandlerSerialization.serialize(this, output);
                return;
            }
        }
        super.serialize(output);
        writeUnresolvedItems(output);
    }

    void writeUnresolvedItems(ValueOutput output) {
        if (!unresolvedItems.isEmpty()) {
            output.store(MachineDropDataHelper.UNRESOLVED_ITEMS, CompoundTag.CODEC.listOf(), unresolvedItems);
        }
    }

    @Override public void deserialize(ValueInput input) {
        unresolvedItems.clear();
        boolean legacy = input.getInt("Size").isPresent();
        String contents = legacy ? "Items" : "stacks";
        var restored = input.keySet().contains(contents)
                ? NonNullList.withSize(size(), ItemStack.EMPTY) : copyToList();
        if (input.keySet().contains(contents)) {
            int index = 0;
            for (ValueInput entry : input.childrenListOrEmpty(contents)) {
                restoreEntry(restored, entry, legacy ? entry.getIntOr("Slot", -1) : index);
                index++;
            }
        }
        for (ValueInput entry : input.childrenListOrEmpty(MachineDropDataHelper.UNRESOLVED_ITEMS)) {
            restoreEntry(restored, entry, entry.getIntOr("Slot", -1));
        }
        setStacks(restored);
    }

    private void restoreEntry(List<ItemStack> restored, ValueInput entry, int slot) {
        if (slot < 0 || slot >= size()) return;
        Identifier id = entry.getString("id").map(Identifier::tryParse).orElse(null);
        if (id == null) return;
        if (restored.get(slot).isEmpty() && BuiltInRegistries.ITEM.containsKey(id)) {
            ItemStack stack = OversizedItemStackHandlerSerialization.deserializeStack(entry);
            if (!stack.isEmpty()) {
                restored.set(slot, stack);
                return;
            }
        }
        // Keep the original components and oversized count until the source mod is installed again.
        entry.read(RAW_ENTRY).ifPresent(raw -> {
            CompoundTag saved = raw.copy();
            if (!saved.contains("Slot")) saved.putInt("Slot", slot);
            unresolvedItems.add(saved);
        });
    }
}
