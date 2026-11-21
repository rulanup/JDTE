package com.jdte.common.blockentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreenhouseEssenceConversionHelperTest implements com.jdte.test.WorldRegistryTest {
    @Test
    void extractsEveryChunkFromAnOversizedGreenhouseOutputStack() {
        var handler = new MachineInventoryHandler(1) {
            @Override protected int getCapacity(int slot, ItemResource resource) { return 2_048; }
        };
        var output = new GreenhouseOutputInventory(handler, 0, 1);
        ItemStack essence = new ItemStack(Items.WHEAT, 256);
        handler.setStack(0, essence);

        assertFalse(output.replace(List.of(essence), List.of(new ItemStack(Items.BREAD, 2_049))));
        assertTrue(ItemStack.matches(essence, output.copyStack(0)));
        assertTrue(output.replace(List.of(essence), List.of(new ItemStack(Items.BREAD, 128))));
        assertTrue(ItemStack.matches(new ItemStack(Items.BREAD, 128), output.copyStack(0)));
    }
}
