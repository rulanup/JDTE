package com.jdte.common.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Set;

public record FactoryTransform(Vec3i sourceSize, int quarterTurns, BlockPos sourceOrigin, BlockPos targetOrigin,
                               ResourceLocation sourceDimension, ResourceLocation targetDimension) {
    private static final Set<String> POSITION_NAMES = Set.of(
            "pos", "position", "target", "controller", "controllerpos", "controller_pos", "link", "linkedpos",
            "linked_pos", "main", "master", "masterpos", "master_pos", "origin", "location", "boundpos",
            "bound_pos");

    public FactoryTransform {
        quarterTurns = Math.floorMod(quarterTurns, 4);
    }

    public Vec3i rotatedSize() {
        return quarterTurns % 2 == 0 ? sourceSize
                : new Vec3i(sourceSize.getZ(), sourceSize.getY(), sourceSize.getX());
    }

    public BlockPos position(BlockPos relative) {
        return switch (quarterTurns) {
            case 1 -> new BlockPos(sourceSize.getZ() - 1 - relative.getZ(), relative.getY(), relative.getX());
            case 2 -> new BlockPos(sourceSize.getX() - 1 - relative.getX(), relative.getY(),
                    sourceSize.getZ() - 1 - relative.getZ());
            case 3 -> new BlockPos(relative.getZ(), relative.getY(), sourceSize.getX() - 1 - relative.getX());
            default -> relative;
        };
    }

    public Vec3 point(Vec3 relative) {
        return switch (quarterTurns) {
            case 1 -> new Vec3(sourceSize.getZ() - relative.z, relative.y, relative.x);
            case 2 -> new Vec3(sourceSize.getX() - relative.x, relative.y, sourceSize.getZ() - relative.z);
            case 3 -> new Vec3(relative.z, relative.y, sourceSize.getX() - relative.x);
            default -> relative;
        };
    }

    public BlockState state(BlockState state) {
        return FactoryBlockEntityMoveSupport.rotateState(state, rotation());
    }

    public Rotation rotation() {
        return switch (quarterTurns) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public CompoundTag blockEntityData(CompoundTag original, BlockPos targetPos, boolean remapLinks) {
        CompoundTag result = original.copy();
        FactoryBlockEntityMoveSupport.rotateMoveData(result, rotation());
        result.putInt("x", targetPos.getX());
        result.putInt("y", targetPos.getY());
        result.putInt("z", targetPos.getZ());
        if (remapLinks) remapCompound(result, "", true);
        return result;
    }

    public CompoundTag entityData(CompoundTag original, boolean remapLinks) {
        CompoundTag result = original.copy();
        transformEntityTree(result, remapLinks);
        return result;
    }

    private void transformEntityTree(CompoundTag entity, boolean remapLinks) {
        ListTag pos = entity.getList("Pos", Tag.TAG_DOUBLE);
        if (pos.size() == 3) {
            Vec3 absolute = new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
            Vec3 transformed = targetPoint(absolute);
            entity.put("Pos", newDoubleList(transformed.x, transformed.y, transformed.z));
        }
        ListTag rotation = entity.getList("Rotation", Tag.TAG_FLOAT);
        if (rotation.size() >= 2) {
            float yaw = rotation.getFloat(0) + quarterTurns * 90.0F;
            entity.put("Rotation", newFloatList(yaw, rotation.getFloat(1)));
        }
        if (remapLinks) remapCompound(entity, "", false);
        ListTag passengers = entity.getList("Passengers", Tag.TAG_COMPOUND);
        for (int index = 0; index < passengers.size(); index++) {
            transformEntityTree(passengers.getCompound(index), remapLinks);
        }
    }

    private Vec3 targetPoint(Vec3 absolute) {
        Vec3 local = absolute.subtract(sourceOrigin.getX(), sourceOrigin.getY(), sourceOrigin.getZ());
        Vec3 rotated = point(local);
        return rotated.add(targetOrigin.getX(), targetOrigin.getY(), targetOrigin.getZ());
    }

    private boolean remapCompound(CompoundTag compound, String parentName, boolean root) {
        boolean changedPosition = false;
        for (String key : Set.copyOf(compound.getAllKeys())) {
            String normalized = key.toLowerCase(Locale.ROOT);
            if (normalized.equals("passengers")) continue;
            if (compound.contains(key, Tag.TAG_COMPOUND)) {
                changedPosition |= remapCompound(compound.getCompound(key), normalized, false);
            } else if (compound.contains(key, Tag.TAG_LIST)) {
                ListTag list = compound.getList(key, Tag.TAG_COMPOUND);
                for (int index = 0; index < list.size(); index++) {
                    changedPosition |= remapCompound(list.getCompound(index), normalized, false);
                }
            } else if (isPositionName(normalized) && compound.contains(key, Tag.TAG_LONG)) {
                BlockPos original = BlockPos.of(compound.getLong(key));
                BlockPos moved = relocateIfInternal(original);
                if (!moved.equals(original)) {
                    compound.putLong(key, moved.asLong());
                    changedPosition = true;
                }
            } else if (isPositionName(normalized) && compound.contains(key, Tag.TAG_INT_ARRAY)) {
                int[] value = compound.getIntArray(key);
                if (value.length == 3) {
                    BlockPos original = new BlockPos(value[0], value[1], value[2]);
                    BlockPos moved = relocateIfInternal(original);
                    if (!moved.equals(original)) {
                        compound.putIntArray(key, new int[]{moved.getX(), moved.getY(), moved.getZ()});
                        changedPosition = true;
                    }
                }
            } else if (isDirectionName(normalized) && compound.contains(key, Tag.TAG_STRING)) {
                Direction direction = Direction.byName(compound.getString(key));
                if (direction != null) compound.putString(key, rotation().rotate(direction).getName());
            } else if (isDirectionName(normalized) && compound.contains(key, Tag.TAG_ANY_NUMERIC)) {
                Direction direction = Direction.from3DDataValue(compound.getInt(key));
                compound.putInt(key, rotation().rotate(direction).get3DDataValue());
            }
        }

        if (!root && isPositionName(parentName)
                && compound.contains("x", Tag.TAG_ANY_NUMERIC)
                && compound.contains("y", Tag.TAG_ANY_NUMERIC)
                && compound.contains("z", Tag.TAG_ANY_NUMERIC)) {
            BlockPos original = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
            BlockPos moved = relocateIfInternal(original);
            if (!moved.equals(original)) {
                compound.putInt("x", moved.getX());
                compound.putInt("y", moved.getY());
                compound.putInt("z", moved.getZ());
                changedPosition = true;
            }
        }
        if (changedPosition && sourceDimension != null && targetDimension != null) {
            for (String key : Set.copyOf(compound.getAllKeys())) {
                String normalized = key.toLowerCase(Locale.ROOT);
                if ((normalized.contains("dimension") || normalized.equals("dim"))
                        && compound.contains(key, Tag.TAG_STRING)
                        && sourceDimension.toString().equals(compound.getString(key))) {
                    compound.putString(key, targetDimension.toString());
                }
            }
        }
        return changedPosition;
    }

    private BlockPos relocateIfInternal(BlockPos absolute) {
        BlockPos max = sourceOrigin.offset(sourceSize).offset(-1, -1, -1);
        if (absolute.getX() < sourceOrigin.getX() || absolute.getX() > max.getX()
                || absolute.getY() < sourceOrigin.getY() || absolute.getY() > max.getY()
                || absolute.getZ() < sourceOrigin.getZ() || absolute.getZ() > max.getZ()) return absolute;
        return targetOrigin.offset(position(absolute.subtract(sourceOrigin)));
    }

    private static boolean isPositionName(String name) {
        if (POSITION_NAMES.contains(name)) return true;
        return name.endsWith("pos") || name.endsWith("position") || name.endsWith("target")
                || name.endsWith("origin") || name.endsWith("location");
    }

    private static boolean isDirectionName(String name) {
        return name.equals("facing") || name.equals("direction") || name.endsWith("_facing")
                || name.endsWith("_direction");
    }

    private static ListTag newDoubleList(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(net.minecraft.nbt.DoubleTag.valueOf(x));
        list.add(net.minecraft.nbt.DoubleTag.valueOf(y));
        list.add(net.minecraft.nbt.DoubleTag.valueOf(z));
        return list;
    }

    private static ListTag newFloatList(float yaw, float pitch) {
        ListTag list = new ListTag();
        list.add(net.minecraft.nbt.FloatTag.valueOf(yaw));
        list.add(net.minecraft.nbt.FloatTag.valueOf(pitch));
        return list;
    }
}
