package com.jdte.common.greenhouse;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixAutoCraftingSettlementTest {
    @Test
    void usesLongCompleteRoundsAndRotatesRemainderPatterns() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 4_000_000_002L, false);
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(Items.WHEAT, 2L, Items.RED_DYE, 6L, 0),
                recipe(Items.WHEAT, 2L, Items.BLUE_DYE, 1L, 1));

        GreenhouseMatrixAutoCraftingProcessor.Result result =
                GreenhouseMatrixAutoCraftingProcessor.process(buffer, recipes, 0);

        assertTrue(result.changed());
        assertEquals(1_000_000_001L * 6L, buffer.count(new ItemStack(Items.RED_DYE)));
        assertEquals(1_000_000_000L, buffer.count(new ItemStack(Items.BLUE_DYE)));
        assertEquals(1, result.nextCursor());
    }

    @Test
    void doesNotRecursivelyCraftOutputsCreatedInTheSameSettlement() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 2L, false);
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(Items.WHEAT, 1L, Items.CARROT, 1L, 0),
                recipe(Items.CARROT, 1L, Items.POTATO, 1L, 1));

        GreenhouseMatrixAutoCraftingProcessor.process(buffer, recipes, 0);

        assertEquals(2L, buffer.count(new ItemStack(Items.CARROT)));
        assertEquals(0L, buffer.count(new ItemStack(Items.POTATO)));
    }

    @Test
    void canCraftAnInputThatAlreadyExistedBeforeThisSettlement() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 2L, false);
        buffer.insert(new ItemStack(Items.CARROT), 1L, false);
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(Items.WHEAT, 1L, Items.CARROT, 1L, 0),
                recipe(Items.CARROT, 1L, Items.POTATO, 1L, 1));

        GreenhouseMatrixAutoCraftingProcessor.process(buffer, recipes, 0);

        assertEquals(2L, buffer.count(new ItemStack(Items.CARROT)));
        assertEquals(1L, buffer.count(new ItemStack(Items.POTATO)));
    }

    @Test
    void leavesInputAndCursorUntouchedWhenTheOutputCannotFit() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(1, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), 3L, false);

        GreenhouseMatrixAutoCraftingProcessor.Result result = GreenhouseMatrixAutoCraftingProcessor.process(
                buffer, List.of(recipe(Items.WHEAT, 2L, Items.RED_DYE, 1L, 0)), 7);

        assertFalse(result.changed());
        assertEquals(3L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(7, result.nextCursor());
    }

    @Test
    void craftsOnlyTheLargestBatchWhoseNetGrowthFitsTheRemainingCapacity() {
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, 10L);
        buffer.insert(new ItemStack(Items.WHEAT), 8L, false);

        GreenhouseMatrixAutoCraftingProcessor.Result result = GreenhouseMatrixAutoCraftingProcessor.process(
                buffer, List.of(recipe(Items.WHEAT, 1L, Items.RED_DYE, 2L, 0)), 0);

        assertTrue(result.changed());
        assertEquals(6L, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(4L, buffer.count(new ItemStack(Items.RED_DYE)));
        assertEquals(10L, buffer.totalCount());
    }

    @Test
    void fillsLongMaxInsteadOfOverflowingThePlannedOutput() {
        long essence = 6_000_000_000_000_000_000L;
        long growthCapacity = Long.MAX_VALUE - essence;
        GreenhouseMatrixOutputBuffer buffer = new GreenhouseMatrixOutputBuffer(8, Long.MAX_VALUE);
        buffer.insert(new ItemStack(Items.WHEAT), essence, false);

        GreenhouseMatrixAutoCraftingProcessor.Result result = GreenhouseMatrixAutoCraftingProcessor.process(
                buffer, List.of(recipe(Items.WHEAT, 1L, Items.RED_DYE, 2L, 0)), 0);

        assertTrue(result.changed());
        assertEquals(essence - growthCapacity, buffer.count(new ItemStack(Items.WHEAT)));
        assertEquals(growthCapacity * 2L, buffer.count(new ItemStack(Items.RED_DYE)));
        assertEquals(Long.MAX_VALUE, buffer.totalCount());
    }

    private static GreenhouseMatrixCraftingRecipe recipe(Item input, long inputCount,
                                                          Item output, long outputCount, int sourceIndex) {
        return new GreenhouseMatrixCraftingRecipe(
                new ItemStack(input), inputCount, new ItemStack(output), outputCount, sourceIndex);
    }
}
