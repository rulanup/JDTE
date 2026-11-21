package com.jdte.common.items.machinesettings;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

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

    static void write(CompoundTag copiedData, int schemaVersion, Identifier machineType, int tickSpeed,
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
        if (!(copiedData.get(ROOT_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return Optional.empty();
        }

        CompoundTag settings = copiedData.getCompoundOrEmpty(ROOT_KEY);
        if (!(settings.get(SCHEMA_VERSION_KEY) instanceof net.minecraft.nbt.IntTag)
                || !(settings.get(MACHINE_TYPE_KEY) instanceof net.minecraft.nbt.StringTag)
                || !(settings.get(TICK_SPEED_KEY) instanceof net.minecraft.nbt.IntTag)
                || !(settings.get(DIRECTION_KEY) instanceof net.minecraft.nbt.IntTag)
                || !(settings.get(CUSTOM_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return Optional.empty();
        }

        int schemaVersion = settings.getIntOr(SCHEMA_VERSION_KEY, 0);
        if (schemaVersion != MachineSettingsSnapshot.CURRENT_SCHEMA) {
            return Optional.empty();
        }

        Identifier machineType = Identifier.tryParse(settings.getStringOr(MACHINE_TYPE_KEY, ""));
        if (machineType == null) {
            return Optional.empty();
        }

        int tickSpeed = settings.getIntOr(TICK_SPEED_KEY, 0);
        if (tickSpeed <= 0) {
            return Optional.empty();
        }

        int direction = settings.getIntOr(DIRECTION_KEY, 0);
        if (!isValidDirection(direction)) {
            return Optional.empty();
        }

        return Optional.of(new MachineSettingsSnapshot(
                schemaVersion,
                machineType,
                tickSpeed,
                direction,
                settings.getCompoundOrEmpty(CUSTOM_KEY).copy()));
    }

    static boolean isValidDirection(int direction) {
        return direction >= 0 && direction < Direction.values().length;
    }

    static boolean isInRange(int value, int minimum, int maximum) {
        return value >= minimum && value <= maximum;
    }

    static Optional<Boolean> readBoolean(CompoundTag tag, String key) {
        if (!(tag.get(key) instanceof net.minecraft.nbt.ByteTag)) {
            return Optional.empty();
        }

        byte value = tag.getByteOr(key, (byte) 0);
        if (value == 0) {
            return Optional.of(false);
        }
        if (value == 1) {
            return Optional.of(true);
        }
        return Optional.empty();
    }

    static <E extends Enum<E>> void writeEnum(CompoundTag tag, String key, E value) {
        if (value != null) {
            tag.putString(key, value.name());
        }
    }

    static <E extends Enum<E>> Optional<E> readEnum(CompoundTag tag, String key, Class<E> enumType) {
        if (!(tag.get(key) instanceof net.minecraft.nbt.StringTag)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Enum.valueOf(enumType, tag.getStringOr(key, "")));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
