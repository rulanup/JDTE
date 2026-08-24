package com.jdte.common.items;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStackComponentTransactionTest {
    private static final DataComponentType<Integer> FLUID =
            DataComponentType.<Integer>builder().persistent(Codec.INT).build();
    private static final DataComponentType<Integer> ENERGY =
            DataComponentType.<Integer>builder().persistent(Codec.INT).build();

    @Test
    void partialDrainAndRefundFailureCannotChangeTheLiveStack() {
        ItemStack live = stackWithResources(100, 200);

        var prepared = ItemStackComponentTransaction.prepare(live, staged -> {
            staged.set(FLUID, 93);
            staged.set(ENERGY, 189);
            staged.set(FLUID, 94);
            return false;
        });

        assertTrue(prepared.isEmpty());
        assertEquals(100, live.get(FLUID));
        assertEquals(200, live.get(ENERGY));
    }

    @Test
    void rejectedFinalCommitCannotOverwriteChangedLiveResources() {
        ItemStack live = stackWithResources(100, 200);
        ItemStackComponentTransaction transaction = ItemStackComponentTransaction.prepare(live, staged -> {
            staged.set(FLUID, 93);
            staged.set(ENERGY, 189);
            return true;
        }).orElseThrow();
        live.set(FLUID, 99);

        assertFalse(transaction.commit(live));
        assertEquals(99, live.get(FLUID));
        assertEquals(200, live.get(ENERGY));
    }

    @Test
    void acceptedFinalCommitAppliesBothPreparedResourceChanges() {
        ItemStack live = stackWithResources(100, 200);
        ItemStackComponentTransaction transaction = ItemStackComponentTransaction.prepare(live, staged -> {
            staged.set(FLUID, 93);
            staged.set(ENERGY, 189);
            return true;
        }).orElseThrow();

        assertTrue(transaction.commit(live));
        assertEquals(93, live.get(FLUID));
        assertEquals(189, live.get(ENERGY));
    }

    private static ItemStack stackWithResources(int fluid, int energy) {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(FLUID, fluid);
        stack.set(ENERGY, energy);
        return stack;
    }
}
