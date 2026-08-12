package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixAutoCraftingCatalogTest {
    @Test
    void preservesPageAndSlotOrderAndMarksOnlyNonEmptyInvalidPatterns() {
        List<ItemStack> first = emptyPage();
        first.set(0, new ItemStack(Items.PAPER));
        first.set(3, new ItemStack(Items.STICK));
        List<ItemStack> second = emptyPage();
        second.set(15, new ItemStack(Items.PAPER));

        GreenhouseMatrixAutoCraftingCatalog.Snapshot snapshot = GreenhouseMatrixAutoCraftingCatalog.build(
                List.of(
                        new GreenhouseMatrixAutoCraftingCatalog.PageInput(new BlockPos(1, 2, 3), first),
                        new GreenhouseMatrixAutoCraftingCatalog.PageInput(new BlockPos(4, 5, 6), second)),
                7L,
                (stack, sourceIndex) -> stack.is(Items.PAPER)
                        ? Optional.of(new GreenhouseMatrixCraftingRecipe(
                                new ItemStack(Items.WHEAT), 1L, new ItemStack(Items.RED_DYE), 1L, sourceIndex))
                        : Optional.empty());

        assertEquals(2, snapshot.pageCount());
        assertEquals(1 << 3, snapshot.invalidMask(0));
        assertEquals(0, snapshot.invalidMask(1));
        assertEquals(List.of(0, 31), snapshot.recipes().stream()
                .map(GreenhouseMatrixCraftingRecipe::sourceIndex).toList());
        assertEquals(7L, snapshot.recipeGeneration());
    }

    private static List<ItemStack> emptyPage() {
        return new ArrayList<>(java.util.Collections.nCopies(16, ItemStack.EMPTY));
    }
}
