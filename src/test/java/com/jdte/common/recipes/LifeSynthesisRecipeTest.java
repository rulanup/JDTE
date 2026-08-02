package com.jdte.common.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifeSynthesisRecipeTest {

    private static LifeSynthesisRecipe recipe(LifeSynthesisRecipe.InputSlot... inputs) {
        return new LifeSynthesisRecipe(List.of(inputs), List.of(), FluidStack.EMPTY, 100, 10, "plant");
    }

    private static LifeSynthesisRecipe.InputSlot diamondSlot(int count) {
        return new LifeSynthesisRecipe.InputSlot(Ingredient.of(Items.DIAMOND), count);
    }

    private static LifeSynthesisRecipe.InputSlot emeraldSlot(int count) {
        return new LifeSynthesisRecipe.InputSlot(Ingredient.of(Items.EMERALD), count);
    }

    private static List<ItemStack> slots(ItemStack... stacks) {
        return new ArrayList<>(List.of(stacks));
    }

    @Test
    void consumeStrictConsumesExactAmounts() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3));
        List<ItemStack> inputs = slots(new ItemStack(Items.DIAMOND, 3));

        assertTrue(recipe.consumeStrict(inputs));
        assertTrue(inputs.getFirst().isEmpty());
    }

    /** 回归测试：数量不足时必须整体拒绝，不得部分扣减后仍产出整批。 */
    @Test
    void consumeStrictRejectsInsufficientCountsWithoutPartialConsume() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3));
        List<ItemStack> inputs = slots(new ItemStack(Items.DIAMOND, 2));

        assertFalse(recipe.consumeStrict(inputs));
        assertEquals(2, inputs.getFirst().getCount());
    }

    @Test
    void consumeStrictSpreadsAcrossMultipleSlots() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(5));
        List<ItemStack> inputs = slots(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.DIAMOND, 2));

        assertTrue(recipe.consumeStrict(inputs));
        assertTrue(inputs.get(0).isEmpty());
        assertTrue(inputs.get(1).isEmpty());
    }

    /** 任一输入行不满足时整体回滚，先匹配成功的行也不得扣减。 */
    @Test
    void consumeStrictRejectsWhenAnyRowUnavailableWithoutPartialConsume() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3), emeraldSlot(3));
        List<ItemStack> inputs = slots(new ItemStack(Items.DIAMOND, 6));

        assertFalse(recipe.consumeStrict(inputs));
        assertEquals(6, inputs.getFirst().getCount());
    }

    /** 同一输入槽不得被多行重复扣减（槽独占语义）。 */
    @Test
    void consumeStrictRejectsSharedSlotAcrossRows() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3), diamondSlot(3));
        List<ItemStack> inputs = slots(new ItemStack(Items.DIAMOND, 6));

        assertFalse(recipe.consumeStrict(inputs));
        assertEquals(6, inputs.getFirst().getCount());
    }

    @Test
    void matchesSlotsAcceptsSufficientCounts() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3));
        assertTrue(recipe.matchesSlots(slots(new ItemStack(Items.DIAMOND, 4))));
    }

    @Test
    void matchesSlotsRejectsInsufficientCounts() {
        LifeSynthesisRecipe recipe = recipe(diamondSlot(3));
        assertFalse(recipe.matchesSlots(slots(new ItemStack(Items.DIAMOND, 2))));
    }
}
