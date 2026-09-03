package com.jdte.common.items.machinesettings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record MachineSettingsSnapshot(
        int schemaVersion,
        ResourceLocation machineType,
        int tickSpeed,
        int direction,
        CompoundTag custom) {
    public static final int CURRENT_SCHEMA = 1;

    public static void write(CompoundTag copiedData, ResourceLocation machineType, int tickSpeed, int direction,
                             CompoundTag custom) {
        MachineSettingsCodecSupport.write(copiedData, CURRENT_SCHEMA, machineType, tickSpeed, direction, custom);
    }

    public static Optional<MachineSettingsSnapshot> read(CompoundTag copiedData) {
        return MachineSettingsCodecSupport.read(copiedData);
    }
}
