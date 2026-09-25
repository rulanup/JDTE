package com.jdte.common.blockentities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** Holds one completed Bio Factory cycle until all outputs can be committed. */
final class BioFactoryPendingOutput {
    private static final String ITEMS_TAG = "items";
    private static final String FLUID_TAG = "fluid";
    private static final String ENERGY_COST_TAG = "energyCost";
    private static final String TIME_COST_TAG = "timeCost";
    private static final String LIFE_COST_TAG = "lifeCost";
    private static final String PROCESS_COST_TAG = "processCost";
    private static final String CREATIVE_TAG = "creative";
    private static final String INPUT_SLOTS_TAG = "inputSlots";
    private static final String INPUT_COUNTS_TAG = "inputCounts";

    private final List<ItemStack> items;
    private final FluidStack fluid;
    private final int energyCost;
    private final int timeCost;
    private final int lifeCost;
    private final int processCost;
    private final boolean creative;
    private final int[] inputSlots;
    private final int[] inputCounts;

    private BioFactoryPendingOutput(List<ItemStack> items, FluidStack fluid, int energyCost, int timeCost,
                                    int lifeCost, int processCost, boolean creative, int[] inputSlots,
                                    int[] inputCounts) {
        this.items = items;
        this.fluid = fluid;
        this.energyCost = energyCost;
        this.timeCost = timeCost;
        this.lifeCost = lifeCost;
        this.processCost = processCost;
        this.creative = creative;
        this.inputSlots = inputSlots;
        this.inputCounts = inputCounts;
    }

    static BioFactoryPendingOutput of(List<ItemStack> items, FluidStack fluid, int energyCost, int timeCost,
                                      int lifeCost, int processCost, boolean creative, int[] inputSlots,
                                      int[] inputCounts) {
        List<ItemStack> copies = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            if (!item.isEmpty()) copies.add(item.copy());
        }
        return new BioFactoryPendingOutput(copies, fluid.copy(), energyCost, timeCost, lifeCost, processCost,
                creative, inputSlots == null ? null : inputSlots.clone(),
                inputCounts == null ? null : inputCounts.clone());
    }

    static BioFactoryPendingOutput load(CompoundTag tag, HolderLookup.Provider provider) {
        List<ItemStack> items = new ArrayList<>();
        ListTag itemTags = tag.getList(ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < itemTags.size(); i++) {
            ItemStack item = ItemStack.parse(provider, itemTags.getCompound(i)).orElse(ItemStack.EMPTY);
            if (!item.isEmpty()) items.add(item);
        }
        FluidStack fluid = tag.contains(FLUID_TAG)
                ? FluidStack.parse(provider, tag.getCompound(FLUID_TAG)).orElse(FluidStack.EMPTY)
                : FluidStack.EMPTY;
        int[] inputSlots = readInts(tag, INPUT_SLOTS_TAG);
        int[] inputCounts = readInts(tag, INPUT_COUNTS_TAG);
        return new BioFactoryPendingOutput(items, fluid, tag.getInt(ENERGY_COST_TAG), tag.getInt(TIME_COST_TAG),
                tag.getInt(LIFE_COST_TAG), tag.getInt(PROCESS_COST_TAG), tag.getBoolean(CREATIVE_TAG),
                inputSlots.length == 0 ? null : inputSlots, inputCounts.length == 0 ? null : inputCounts);
    }

    CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag itemTags = new ListTag();
        for (ItemStack item : items) itemTags.add(item.save(provider));
        tag.put(ITEMS_TAG, itemTags);
        if (!fluid.isEmpty()) tag.put(FLUID_TAG, fluid.save(provider));
        tag.putInt(ENERGY_COST_TAG, energyCost);
        tag.putInt(TIME_COST_TAG, timeCost);
        tag.putInt(LIFE_COST_TAG, lifeCost);
        tag.putInt(PROCESS_COST_TAG, processCost);
        tag.putBoolean(CREATIVE_TAG, creative);
        writeInts(tag, INPUT_SLOTS_TAG, inputSlots);
        writeInts(tag, INPUT_COUNTS_TAG, inputCounts);
        return tag;
    }

    private static int[] readInts(CompoundTag tag, String key) {
        return tag.contains(key) ? tag.getIntArray(key) : new int[0];
    }

    private static void writeInts(CompoundTag tag, String key, int[] values) {
        if (values != null) tag.putIntArray(key, values);
    }

    int energyCost() { return energyCost; }
    int timeCost() { return timeCost; }
    int lifeCost() { return lifeCost; }
    int processCost() { return processCost; }
    boolean creative() { return creative; }
    int[] inputSlots() { return inputSlots; }
    int[] inputCounts() { return inputCounts; }

    List<ItemStack> items() {
        return items;
    }

    FluidStack fluid() {
        return fluid;
    }
}
