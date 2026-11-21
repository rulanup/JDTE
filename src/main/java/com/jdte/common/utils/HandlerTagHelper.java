package com.jdte.common.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class HandlerTagHelper {
    private HandlerTagHelper() {}

    public static CompoundTag write(ValueIOSerializable handler, HolderLookup.Provider registries) {
        var problems = new ProblemReporter.Collector();
        var output = TagValueOutput.createWithContext(problems, registries);
        handler.serialize(output);
        if (!problems.isEmpty()) throw new IllegalStateException(problems.getReport());
        return output.buildResult();
    }

    public static void read(ValueIOSerializable handler, HolderLookup.Provider registries, CompoundTag tag) {
        var problems = new ProblemReporter.Collector();
        handler.deserialize(TagValueInput.create(problems, registries, tag));
        if (!problems.isEmpty()) throw new IllegalArgumentException(problems.getReport());
    }

    /** Old NeoForge inventories wrote Slot as an int. ItemStackWithSlot writes an unsigned byte. */
    public static boolean matchesItemHandlerTag(CompoundTag saved, CompoundTag decoded) {
        if (saved.equals(decoded)) return true;
        var normalized = saved.copy();
        for (var tag : normalized.getListOrEmpty("Items")) {
            if (tag instanceof CompoundTag entry && entry.get("Slot") instanceof net.minecraft.nbt.IntTag) {
                int slot = entry.getIntOr("Slot", -1);
                if (slot < 0 || slot > 255) return false;
                entry.putByte("Slot", (byte) slot);
            }
        }
        return normalized.equals(decoded);
    }
}
