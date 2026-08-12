package com.jdte.common.blockentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseEssenceConversionHelperTest {
    @Test
    void extractsEveryChunkFromAnOversizedGreenhouseOutputStack() throws Exception {
        ItemStackHandler output = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 2_048;
            }

            @Override
            protected int getStackLimit(int slot, ItemStack stack) {
                return getSlotLimit(slot);
            }
        };
        ItemStack essence = new ItemStack(Items.WHEAT, 256);
        output.setStackInSlot(0, essence.copy());

        Method extractAll = GreenhouseEssenceConversionHelper.class
                .getDeclaredMethod("extractAll", IItemHandler.class, ItemStack.class);
        extractAll.setAccessible(true);
        extractAll.invoke(null, output, essence.copyWithCount(1));

        assertTrue(output.getStackInSlot(0).isEmpty());
    }
}
