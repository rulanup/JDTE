package com.jdte.common.factory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Rotation;

final class IntegratedDynamicsFactoryMoveIntegration {
    private static final String[] DIRECTION_MAPS = {
            "connected", "forceDisconnected", "redstoneLevels", "redstoneInputs", "redstoneStrong",
            "lastRedstonePulses", "scheduledPulseRemaining", "lightLevels"
    };

    private IntegratedDynamicsFactoryMoveIntegration() {}

    static void rotateMoveData(CompoundTag data, Rotation rotation) {
        rotateParts(data.getCompoundOrEmpty("partContainer"), rotation);
        for (String name : DIRECTION_MAPS) rotateDirectionMap(data.getCompoundOrEmpty(name), rotation);
    }

    private static void rotateParts(CompoundTag partContainer, Rotation rotation) {
        ListTag parts = partContainer.getListOrEmpty("parts");
        for (int index = 0; index < parts.size(); index++) {
            CompoundTag part = parts.getCompoundOrEmpty(index);
            if ((part.get("__side") instanceof net.minecraft.nbt.StringTag)) {
                Direction side = Direction.byName(part.getStringOr("__side", ""));
                if (side != null) part.putString("__side", rotation.rotate(side).getSerializedName());
            }
            if (part.get("targetSide") instanceof net.minecraft.nbt.NumericTag) {
                Direction targetSide = directionByOrdinal(part.getIntOr("targetSide", 0));
                if (targetSide != null) part.putInt("targetSide", rotation.rotate(targetSide).ordinal());
            }
        }
    }

    private static void rotateDirectionMap(CompoundTag mapTag, Rotation rotation) {
        ListTag entries = mapTag.getListOrEmpty("map");
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompoundOrEmpty(index);
            if (!(entry.get("key") instanceof net.minecraft.nbt.NumericTag)) continue;
            Direction direction = directionByOrdinal(entry.getIntOr("key", 0));
            if (direction != null) entry.putInt("key", rotation.rotate(direction).ordinal());
        }
    }

    private static Direction directionByOrdinal(int ordinal) {
        Direction[] directions = Direction.values();
        return ordinal >= 0 && ordinal < directions.length ? directions[ordinal] : null;
    }
}
