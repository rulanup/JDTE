package com.jdte.common.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

public final class LegacyMachineAttachmentHelper {
    private static final Set<String> ENERGY = Set.of("justdirethings:energystorage_machines",
            "justdirethings:energystorage_generators", "justdirethings:energystorage_transmitters");
    private static final Set<String> FLUID = Set.of("justdirethings:machine_fluid_handler",
            "justdirethings:generator_fluid_handler", "justdirethings:paradox_fluid_handler");
    private static final Set<String> INVENTORY = Set.of("justdirethings:handler", "justdirethings:machine_handler",
            "justdirethings:generator_item_handler", "justdirethings:generator_fluid_item_handler",
            "justdirethings:handler_item_collector", "justdirethings:anysize_filter_handler");

    private LegacyMachineAttachmentHelper() {}

    public static CompoundTag upgrade(CompoundTag original) {
        CompoundTag result = original;
        for (var entry : original.entrySet()) {
            CompoundTag replacement = convert(entry.getKey(), entry.getValue());
            if (replacement == null) continue;
            if (result == original) result = original.copy();
            result.put(entry.getKey(), replacement);
        }
        return result;
    }

    private static CompoundTag convert(String key, Tag value) {
        if (ENERGY.contains(key) && value instanceof IntTag energy) {
            var replacement = new CompoundTag();
            replacement.putInt("energy", energy.intValue());
            return replacement;
        }
        if (!(value instanceof CompoundTag storage) || storage.contains("stacks")) return null;
        var stacks = new ListTag();
        if (FLUID.contains(key) && storage.get("Fluid") instanceof CompoundTag fluid) {
            stacks.add(fluid.copy());
        } else if (INVENTORY.contains(key) && storage.get("Items") instanceof ListTag items) {
            int slots = storage.getIntOr("Size", -1);
            if (slots < 0 || slots > 65536) return null;
            for (int i = 0; i < slots; i++) stacks.add(new CompoundTag());
            for (Tag item : items) {
                if (!(item instanceof CompoundTag stack)) continue;
                int slot = stack.getIntOr("Slot", -1);
                if (slot < 0 || slot >= slots) continue;
                var copy = stack.copy();
                copy.remove("Slot");
                stacks.set(slot, copy);
            }
        } else {
            return null;
        }
        var replacement = storage.copy();
        replacement.put("stacks", stacks);
        return replacement;
    }
}
