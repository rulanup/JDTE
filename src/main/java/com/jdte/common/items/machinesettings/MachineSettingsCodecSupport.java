package com.jdte.common.items.machinesettings;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

final class MachineSettingsCodecSupport {
    static final String ROOT_KEY = "jdteMachineSettings";
    static final String SCHEMA_VERSION_KEY = "schemaVersion";
    static final String MACHINE_TYPE_KEY = "machineType";
    static final String TICK_SPEED_KEY = "tickSpeed";
    static final String DIRECTION_KEY = "direction";
    static final String CUSTOM_KEY = "custom";

    private MachineSettingsCodecSupport() {
    }

    static void write(CompoundTag copiedData, int schemaVersion, ResourceLocation machineType, int tickSpeed,
                      int direction, CompoundTag custom) {
        CompoundTag settings = new CompoundTag();
        settings.putInt(SCHEMA_VERSION_KEY, schemaVersion);
        settings.putString(MACHINE_TYPE_KEY, machineType.toString());
        settings.putInt(TICK_SPEED_KEY, tickSpeed);
        settings.putInt(DIRECTION_KEY, direction);
        settings.put(CUSTOM_KEY, custom.copy());
        copiedData.put(ROOT_KEY, settings);
    }

    static Optional<MachineSettingsSnapshot> read(CompoundTag copiedData) {
        if (!copiedData.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag settings = copiedData.getCompound(ROOT_KEY);
        if (!settings.contains(SCHEMA_VERSION_KEY, Tag.TAG_INT)
                || !settings.contains(MACHINE_TYPE_KEY, Tag.TAG_STRING)
                || !settings.contains(TICK_SPEED_KEY, Tag.TAG_INT)
                || !settings.contains(DIRECTION_KEY, Tag.TAG_INT)
                || !settings.contains(CUSTOM_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        int schemaVersion = settings.getInt(SCHEMA_VERSION_KEY);
        if (schemaVersion != MachineSettingsSnapshot.CURRENT_SCHEMA) {
            return Optional.empty();
        }

        ResourceLocation machineType = ResourceLocation.tryParse(settings.getString(MACHINE_TYPE_KEY));
        if (machineType == null) {
            return Optional.empty();
        }

        int tickSpeed = settings.getInt(TICK_SPEED_KEY);
        if (tickSpeed <= 0) {
            return Optional.empty();
        }

        int direction = settings.getInt(DIRECTION_KEY);
        if (!isValidDirection(direction)) {
            return Optional.empty();
        }

        return Optional.of(new MachineSettingsSnapshot(
                schemaVersion,
                machineType,
                tickSpeed,
                direction,
                settings.getCompound(CUSTOM_KEY).copy()));
    }

    static boolean isValidDirection(int direction) {
        return direction >= 0 && direction < Direction.values().length;
    }
}
