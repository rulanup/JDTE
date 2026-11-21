package com.jdte.common.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GreenhouseCropResolverTest implements com.jdte.test.WorldRegistryTest {

    @Test
    void genericFallbackResolvesOrdinaryCropWithoutLevel() {
        GreenhouseCropDefinition definition = GreenhouseCropResolver.findGeneric(
                new ItemStack(Items.WHEAT_SEEDS));

        assertNotNull(definition);
        assertEquals(Items.WHEAT_SEEDS, definition.outputs().getFirst().getItem());
        assertEquals("minecraft:wheat", definition.displayBlock().toString());
    }
}
