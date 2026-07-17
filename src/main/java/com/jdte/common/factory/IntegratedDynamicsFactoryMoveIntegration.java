package com.jdte.common.factory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Rotation;

final class IntegratedDynamicsFactoryMoveIntegration {
    private static final String[] DIRECTION_MAPS = {
            "connected", "forceDisconnected", "redstoneLevels", "redstoneInputs", "redstoneStrong",
            "lastRedstonePulses", "scheduledPulseRemaining", "lightLevels"
    };

    private IntegratedDynamicsFactoryMoveIntegration() {}

    static void rotateMoveData(CompoundTag data, Rotation rotation) {
        rotateParts(data.getCompound("partContainer"), rotation);
        for (String name : DIRECTION_MAPS) rotateDirectionMap(data.getCompound(name), rotation);
    }

    private static void rotateParts(CompoundTag partContainer, Rotation rotation) {
        ListTag parts = partContainer.getList("parts", Tag.TAG_COMPOUND);
        for (int index = 0; index < parts.size(); index++) {
            CompoundTag part = parts.getCompound(index);
            if (part.contains("__side", Tag.TAG_STRING)) {
                Direction side = Direction.byName(part.getString("__side"));
                if (side != null) part.putString("__side", rotation.rotate(side).getSerializedName());
            }
            if (part.contains("targetSide", Tag.TAG_ANY_NUMERIC)) {
                Direction targetSide = directionByOrdinal(part.getInt("targetSide"));
                if (targetSide != null) part.putInt("targetSide", rotation.rotate(targetSide).ordinal());
            }
        }
    }

    private static void rotateDirectionMap(CompoundTag mapTag, Rotation rotation) {
        ListTag entries = mapTag.getList("map", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (!entry.contains("key", Tag.TAG_ANY_NUMERIC)) continue;
            Direction direction = directionByOrdinal(entry.getInt("key"));
            if (direction != null) entry.putInt("key", rotation.rotate(direction).ordinal());
        }
    }

    private static Direction directionByOrdinal(int ordinal) {
        Direction[] directions = Direction.values();
        return ordinal >= 0 && ordinal < directions.length ? directions[ordinal] : null;
    }
}
