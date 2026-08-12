package com.jdte.common.greenhouse;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixOutputBufferCraftingTest {
    @Test
    void atomicallyReplacesLongInputCountsWithLongOutputs() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 3_000_000_000L, false);

        boolean changed = buffer.applyCraftingBatch(List.of(
                new GreenhouseMatrixOutputBuffer.Transformation(
                        new ItemStack(Items.WHEAT), 3_000_000_000L,
                        new ItemStack(Items.RED_DYE), 9_000_000_000L)));

        assertTrue(changed);
        assertEquals(0L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(9_000_000_000L, buffer.count(new ItemStack(Items.RED_DYE)));
        assertEquals(9_000_000_000L, buffer.totalCount());
    }

    @Test
    void leavesBufferUntouchedWhenCombinedTransformationsOverConsumeOneInput() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 5L, false);

        boolean changed = buffer.applyCraftingBatch(List.of(
                transformation(Items.RED_DYE, 3L),
                transformation(Items.BLUE_DYE, 3L)));

        assertFalse(changed);
        assertEquals(5L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(0L, buffer.count(new ItemStack(Items.RED_DYE)));
        assertEquals(1, buffer.distinctTypes());
    }

    @Test
    void leavesBufferUntouchedWhenOutputWouldExceedTypeLimit() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(1, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 4L, false);

        boolean changed = buffer.applyCraftingBatch(List.of(
                new GreenhouseMatrixOutputBuffer.Transformation(
                        new ItemStack(Items.WHEAT), 2L,
                        new ItemStack(Items.RED_DYE), 2L)));

        assertFalse(changed);
        assertEquals(4L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(0L, buffer.count(new ItemStack(Items.RED_DYE)));
    }

    @Test
    void freesAnInputTypeBeforeCheckingTheOutputTypeLimit() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(1, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 4L, false);

        boolean changed = buffer.applyCraftingBatch(List.of(
                new GreenhouseMatrixOutputBuffer.Transformation(
                        new ItemStack(Items.WHEAT), 4L,
                        new ItemStack(Items.RED_DYE), 2L)));

        assertTrue(changed);
        assertEquals(1, buffer.distinctTypes());
        assertEquals(2L, buffer.count(new ItemStack(Items.RED_DYE)));
    }

    @Test
    void leavesBufferUntouchedWhenOutputWouldExceedTotalCapacity() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, 10L);
        buffer.insert(new ItemStack(Items.WHEAT), 5L, false);

        boolean changed = buffer.applyCraftingBatch(List.of(
                new GreenhouseMatrixOutputBuffer.Transformation(
                        new ItemStack(Items.WHEAT), 1L,
                        new ItemStack(Items.RED_DYE), 7L)));

        assertFalse(changed);
        assertEquals(5L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(0L, buffer.count(new ItemStack(Items.RED_DYE)));
    }

    private static GreenhouseMatrixOutputBuffer.Transformation transformation(net.minecraft.world.item.Item output,
                                                                               long inputAmount) {
        return new GreenhouseMatrixOutputBuffer.Transformation(
                new ItemStack(Items.WHEAT), inputAmount, new ItemStack(output), 1L);
    }
}
