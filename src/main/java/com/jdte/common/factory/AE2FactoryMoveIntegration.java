package com.jdte.common.factory;

import appeng.api.movable.BlockEntityMoveStrategies;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

final class AE2FactoryMoveIntegration {
    private static final String CABLE_BUS_ID = "ae2:cable_bus";

    private AE2FactoryMoveIntegration() {}

    static BlockState rotateState(BlockState state, Rotation rotation) {
        BlockState rotated = state.rotate(rotation);

        IOrientationStrategy strategy = IOrientationStrategy.get(state);
        BlockOrientation orientation = BlockOrientation.get(strategy, state);
        rotated = strategy.setOrientation(rotated,
                rotation.rotate(orientation.getSide(RelativeSide.FRONT)),
                rotation.rotate(orientation.getSide(RelativeSide.TOP)));

        if (state.hasProperty(PatternProviderBlock.PUSH_DIRECTION)) {
            PushDirection pushDirection = state.getValue(PatternProviderBlock.PUSH_DIRECTION);
            Direction direction = pushDirection.getDirection();
            if (direction != null) {
                rotated = rotated.setValue(PatternProviderBlock.PUSH_DIRECTION,
                        PushDirection.fromDirection(rotation.rotate(direction)));
            }
        }
        return rotated;
    }

    static CompoundTag rotateMoveData(CompoundTag data, Rotation rotation) {
        if (rotation == Rotation.NONE || !CABLE_BUS_ID.equals(data.getString("id"))) return data;
        rotateDirectionalTags(data, rotation, "", false);
        rotateDirectionalTags(data, rotation, "facade", true);
        return data;
    }

    private static void rotateDirectionalTags(CompoundTag data, Rotation rotation, String prefix,
                                              boolean capitalizeDirection) {
        Map<Direction, Tag> saved = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            String key = directionalKey(prefix, direction, capitalizeDirection);
            Tag value = data.get(key);
            if (value != null) saved.put(direction, value.copy());
            data.remove(key);
        }
        saved.forEach((direction, value) -> data.put(
                directionalKey(prefix, rotation.rotate(direction), capitalizeDirection), value));
    }

    private static String directionalKey(String prefix, Direction direction, boolean capitalizeDirection) {
        String name = direction.getSerializedName();
        if (capitalizeDirection) name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return prefix + name;
    }

    static CompoundTag beginMove(BlockEntity blockEntity, HolderLookup.Provider registries) {
        return BlockEntityMoveStrategies.get(blockEntity).beginMove(blockEntity, registries);
    }

    static boolean completeMove(BlockState state, CompoundTag data, Level level, BlockPos pos) {
        BlockEntity sourceRepresentation = BlockEntity.loadStatic(pos, state, data.copy(), level.registryAccess());
        if (sourceRepresentation == null) return false;
        boolean moved = BlockEntityMoveStrategies.get(sourceRepresentation)
                .completeMove(sourceRepresentation, state, data, level, pos);
        BlockEntity restored = level.getBlockEntity(pos);
        if (moved && restored != null) restored.setChanged();
        return moved;
    }
}
