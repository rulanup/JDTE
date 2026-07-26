package com.jdte.common.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactoryTransformTest {

    private static FactoryTransform transform(Vec3i size, int turns, BlockPos sourceOrigin, BlockPos targetOrigin) {
        return new FactoryTransform(size, turns, sourceOrigin, targetOrigin, null, null);
    }

    @Test
    void quarterTurnsNormalizedIntoRange() {
        assertEquals(Rotation.CLOCKWISE_90,
                transform(new Vec3i(1, 1, 1), 5, BlockPos.ZERO, BlockPos.ZERO).rotation());
        assertEquals(Rotation.COUNTERCLOCKWISE_90,
                transform(new Vec3i(1, 1, 1), -1, BlockPos.ZERO, BlockPos.ZERO).rotation());
    }

    @Test
    void rotatedSizeSwapsXZOnOddTurns() {
        Vec3i size = new Vec3i(3, 2, 4);
        assertEquals(size, transform(size, 0, BlockPos.ZERO, BlockPos.ZERO).rotatedSize());
        assertEquals(new Vec3i(4, 2, 3), transform(size, 1, BlockPos.ZERO, BlockPos.ZERO).rotatedSize());
        assertEquals(size, transform(size, 2, BlockPos.ZERO, BlockPos.ZERO).rotatedSize());
        assertEquals(new Vec3i(4, 2, 3), transform(size, 3, BlockPos.ZERO, BlockPos.ZERO).rotatedSize());
    }

    @Test
    void positionRotationPerQuarterTurn() {
        Vec3i size = new Vec3i(3, 2, 4);
        BlockPos rel = new BlockPos(2, 1, 3);
        assertEquals(rel, transform(size, 0, BlockPos.ZERO, BlockPos.ZERO).position(rel));
        assertEquals(new BlockPos(0, 1, 2), transform(size, 1, BlockPos.ZERO, BlockPos.ZERO).position(rel));
        assertEquals(new BlockPos(0, 1, 0), transform(size, 2, BlockPos.ZERO, BlockPos.ZERO).position(rel));
        assertEquals(new BlockPos(3, 1, 0), transform(size, 3, BlockPos.ZERO, BlockPos.ZERO).position(rel));
    }

    @Test
    void positionRotationIsBijectiveWithinBounds() {
        Vec3i size = new Vec3i(3, 1, 4);
        FactoryTransform t = transform(size, 1, BlockPos.ZERO, BlockPos.ZERO);
        Vec3i rotated = t.rotatedSize();
        boolean[][] seen = new boolean[rotated.getX()][rotated.getZ()];
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                BlockPos out = t.position(new BlockPos(x, 0, z));
                seen[out.getX()][out.getZ()] = true;
            }
        }
        for (boolean[] row : seen) {
            for (boolean cell : row) {
                assertEquals(true, cell);
            }
        }
    }

    @Test
    void pointRotationMatchesContinuousCoordinates() {
        Vec3i size = new Vec3i(3, 2, 4);
        Vec3 rel = new Vec3(2.5, 1.0, 3.5);
        assertEquals(new Vec3(0.5, 1.0, 2.5), transform(size, 1, BlockPos.ZERO, BlockPos.ZERO).point(rel));
        assertEquals(new Vec3(0.5, 1.0, 0.5), transform(size, 2, BlockPos.ZERO, BlockPos.ZERO).point(rel));
        assertEquals(new Vec3(3.5, 1.0, 0.5), transform(size, 3, BlockPos.ZERO, BlockPos.ZERO).point(rel));
    }

    @Test
    void blockEntityDataWritesTargetCoordinates() {
        FactoryTransform t = transform(new Vec3i(3, 3, 3), 0, new BlockPos(10, 0, 10), new BlockPos(100, 0, 100));
        CompoundTag original = new CompoundTag();
        original.putInt("x", 10);
        original.putInt("y", 0);
        original.putInt("z", 10);
        original.putString("custom", "value");

        CompoundTag result = t.blockEntityData(original, new BlockPos(100, 0, 100), false);

        assertEquals(100, result.getInt("x"));
        assertEquals(0, result.getInt("y"));
        assertEquals(100, result.getInt("z"));
        assertEquals("value", result.getString("custom"));
        assertEquals(10, original.getInt("x"));
    }

    @Test
    void remapRelocatesInternalLongPositionLinks() {
        FactoryTransform t = transform(new Vec3i(3, 3, 3), 0, new BlockPos(10, 0, 10), new BlockPos(100, 0, 100));
        CompoundTag original = new CompoundTag();
        original.putLong("linkedpos", new BlockPos(11, 0, 12).asLong());
        original.putLong("externalpos", new BlockPos(50, 0, 50).asLong());

        CompoundTag result = t.blockEntityData(original, new BlockPos(100, 0, 100), true);

        assertEquals(new BlockPos(101, 0, 102), BlockPos.of(result.getLong("linkedpos")));
        assertEquals(new BlockPos(50, 0, 50), BlockPos.of(result.getLong("externalpos")));
    }

    @Test
    void remapRelocatesIntArrayPositionsAndNestedCompounds() {
        FactoryTransform t = transform(new Vec3i(3, 3, 3), 0, new BlockPos(10, 0, 10), new BlockPos(100, 0, 100));
        CompoundTag original = new CompoundTag();
        original.putIntArray("controllerpos", new int[]{12, 2, 10});
        CompoundTag nested = new CompoundTag();
        nested.putInt("x", 11);
        nested.putInt("y", 1);
        nested.putInt("z", 11);
        original.put("masterpos", nested);

        CompoundTag result = t.blockEntityData(original, new BlockPos(100, 0, 100), true);

        int[] remapped = result.getIntArray("controllerpos");
        assertEquals(102, remapped[0]);
        assertEquals(2, remapped[1]);
        assertEquals(100, remapped[2]);
        CompoundTag nestedResult = result.getCompound("masterpos");
        assertEquals(101, nestedResult.getInt("x"));
        assertEquals(1, nestedResult.getInt("y"));
        assertEquals(101, nestedResult.getInt("z"));
    }

    @Test
    void remapRotatesDirectionNames() {
        FactoryTransform t = transform(new Vec3i(3, 3, 3), 1, new BlockPos(10, 0, 10), new BlockPos(100, 0, 100));
        CompoundTag original = new CompoundTag();
        original.putString("facing", "north");
        original.putLong("linkedpos", new BlockPos(10, 0, 10).asLong());

        CompoundTag result = t.blockEntityData(original, new BlockPos(100, 0, 100), true);

        assertEquals("east", result.getString("facing"));
    }

    @Test
    void entityDataTransformsPositionRotationAndPassengers() {
        FactoryTransform t = transform(new Vec3i(4, 4, 4), 1, new BlockPos(10, 0, 10), new BlockPos(100, 0, 100));
        CompoundTag entity = new CompoundTag();
        entity.put("Pos", doubles(11.5, 0.0, 12.5));
        entity.put("Rotation", floats(45.0F, 10.0F));
        CompoundTag passenger = new CompoundTag();
        passenger.put("Pos", doubles(11.5, 1.0, 12.5));
        passenger.put("Rotation", floats(0.0F, 0.0F));
        ListTag passengers = new ListTag();
        passengers.add(passenger);
        entity.put("Passengers", passengers);

        CompoundTag result = t.entityData(entity, false);

        ListTag pos = result.getList("Pos", Tag.TAG_DOUBLE);
        assertEquals(new Vec3(101.5, 0.0, 101.5),
                new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2)));
        assertEquals(135.0F, result.getList("Rotation", Tag.TAG_FLOAT).getFloat(0));
        CompoundTag passengerResult = result.getList("Passengers", Tag.TAG_COMPOUND).getCompound(0);
        ListTag passengerPos = passengerResult.getList("Pos", Tag.TAG_DOUBLE);
        assertEquals(1.0, passengerPos.getDouble(1));
        assertEquals(90.0F, passengerResult.getList("Rotation", Tag.TAG_FLOAT).getFloat(0));
    }

    private static ListTag doubles(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(x));
        list.add(DoubleTag.valueOf(y));
        list.add(DoubleTag.valueOf(z));
        return list;
    }

    private static ListTag floats(float yaw, float pitch) {
        ListTag list = new ListTag();
        list.add(FloatTag.valueOf(yaw));
        list.add(FloatTag.valueOf(pitch));
        return list;
    }
}
