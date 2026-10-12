package com.jdte.common.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecorativeFumoBlockTest {
    @Test
    void placementFacesPlayerHorizontally() {
        assertEquals(Direction.SOUTH, DecorativeFumoBlock.placementFacing(Direction.NORTH));
        assertEquals(Direction.WEST, DecorativeFumoBlock.placementFacing(Direction.EAST));
        assertEquals(Direction.NORTH, DecorativeFumoBlock.placementFacing(Direction.SOUTH));
        assertEquals(Direction.EAST, DecorativeFumoBlock.placementFacing(Direction.WEST));
    }

    @Test
    void shapeUsesSuppliedModelBounds() {
        assertBounds(DecorativeFumoBlock.shapeFor(Direction.NORTH).bounds(),
                3.9 / 16, 0, 3.8 / 16, 12.2 / 16, 13.6 / 16, 14.7 / 16);
    }

    @Test
    void shapeRotatesForAllHorizontalDirections() {
        assertBounds(DecorativeFumoBlock.shapeFor(Direction.NORTH).bounds(),
                3.9 / 16, 0, 3.8 / 16, 12.2 / 16, 13.6 / 16, 14.7 / 16);
        assertBounds(DecorativeFumoBlock.shapeFor(Direction.EAST).bounds(),
                1.3 / 16, 0, 3.9 / 16, 12.2 / 16, 13.6 / 16, 12.2 / 16);
        assertBounds(DecorativeFumoBlock.shapeFor(Direction.SOUTH).bounds(),
                3.8 / 16, 0, 1.3 / 16, 12.7 / 16, 13.6 / 16, 12.2 / 16);
        assertBounds(DecorativeFumoBlock.shapeFor(Direction.WEST).bounds(),
                3.8 / 16, 0, 3.8 / 16, 14.7 / 16, 13.6 / 16, 12.1 / 16);
    }

    private static void assertBounds(AABB bounds, double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ) {
        assertEquals(minX, bounds.minX, 1.0E-6);
        assertEquals(minY, bounds.minY, 1.0E-6);
        assertEquals(minZ, bounds.minZ, 1.0E-6);
        assertEquals(maxX, bounds.maxX, 1.0E-6);
        assertEquals(maxY, bounds.maxY, 1.0E-6);
        assertEquals(maxZ, bounds.maxZ, 1.0E-6);
    }
}