package com.jdte.common.blockentities;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    static BioFactoryPendingOutput load(ValueInput tag) {
        List<ItemStack> items = new ArrayList<>();
        for (ValueInput entry : tag.childrenListOrEmpty(ITEMS_TAG)) {
            ItemStack item = OversizedItemStackHandlerSerialization.deserializeStack(entry);
            if (!item.isEmpty()) items.add(item);
        }
        FluidStack fluid = tag.read(FLUID_TAG, FluidStack.CODEC)
                .orElse(FluidStack.EMPTY);
        int[] inputSlots = readInts(tag, INPUT_SLOTS_TAG);
        int[] inputCounts = readInts(tag, INPUT_COUNTS_TAG);
        return new BioFactoryPendingOutput(items, fluid, tag.getIntOr(ENERGY_COST_TAG, 0), tag.getIntOr(TIME_COST_TAG, 0),
                tag.getIntOr(LIFE_COST_TAG, 0), tag.getIntOr(PROCESS_COST_TAG, 0), tag.getBooleanOr(CREATIVE_TAG, false),
                inputSlots.length == 0 ? null : inputSlots, inputCounts.length == 0 ? null : inputCounts);
    }

    void save(ValueOutput tag) {
        var itemTags = tag.childrenList(ITEMS_TAG);
        for (ItemStack item : items) OversizedItemStackHandlerSerialization.serializeStack(item, itemTags.addChild());
        if (!fluid.isEmpty()) tag.store(FLUID_TAG, FluidStack.CODEC, fluid);
        tag.putInt(ENERGY_COST_TAG, energyCost);
        tag.putInt(TIME_COST_TAG, timeCost);
        tag.putInt(LIFE_COST_TAG, lifeCost);
        tag.putInt(PROCESS_COST_TAG, processCost);
        tag.putBoolean(CREATIVE_TAG, creative);
        writeInts(tag, INPUT_SLOTS_TAG, inputSlots);
        writeInts(tag, INPUT_COUNTS_TAG, inputCounts);
    }

    private static int[] readInts(ValueInput tag, String key) {
        return tag.getIntArray(key).orElseGet(() -> new int[0]);
    }

    private static void writeInts(ValueOutput tag, String key, int[] values) {
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
