package com.jdte.common.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidMixerRecipeSingleFluidTest {
    private final FluidMixerRecipe recipe = new FluidMixerRecipe(
            new FluidStack(Fluids.WATER, 1_000),
            FluidStack.EMPTY,
            Optional.of(new FluidMixerRecipe.InputItem(Ingredient.of(Items.STICK), 1)),
            new FluidStack(Fluids.LAVA, 250),
            100,
            5_000);
    private final ItemStack catalyst = new ItemStack(Items.STICK);

    @Test
    void matchesSingleFluidInputInEitherTankOnlyWhenOtherTankIsEmpty() {
        FluidStack water = new FluidStack(Fluids.WATER, 1_000);

        assertTrue(recipe.matchesFluids(water, FluidStack.EMPTY, catalyst));
        assertTrue(recipe.matchesFluids(FluidStack.EMPTY, water, catalyst));
        assertFalse(recipe.matchesFluids(water, new FluidStack(Fluids.LAVA, 1_000), catalyst));
    }

    @Test
    void drainsOnlyTheTankContainingTheSingleFluidInput() {
        FluidStack waterInA = new FluidStack(Fluids.WATER, 1_000);
        FluidStack waterInB = new FluidStack(Fluids.WATER, 1_000);

        assertTrue(recipe.hasEnoughFluids(waterInA, FluidStack.EMPTY));
        assertEquals(1_000, recipe.getDrainA(waterInA));
        assertEquals(0, recipe.getDrainB(FluidStack.EMPTY));
        assertTrue(recipe.hasEnoughFluids(FluidStack.EMPTY, waterInB));
        assertEquals(0, recipe.getDrainA(FluidStack.EMPTY));
        assertEquals(1_000, recipe.getDrainB(waterInB));
    }
}
