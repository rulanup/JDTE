package com.jdte.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnEggEntityDataTest {

    @Test
    void copiesEntityDataWithoutSharingMutableTag() {
        ItemStack stack = new ItemStack(Items.STICK);
        CompoundTag source = new CompoundTag();
        source.putString("id", "productivebees:configurable_bee");
        source.putString("type", "productivebees:life_fluid");
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(source));

        Optional<CompoundTag> copy = SpawnEggEntityData.copy(stack);

        assertTrue(copy.isPresent());
        assertNotSame(source, copy.get());
        assertEquals("productivebees:life_fluid", copy.get().getString("type"));
        copy.get().putString("type", "minecraft:bee");
        assertEquals("productivebees:life_fluid", stack.get(DataComponents.ENTITY_DATA).copyTag()
                .getString("type"));
    }

    @Test
    void missingEntityDataIsEmpty() {
        Optional<CompoundTag> copy = SpawnEggEntityData.copy(
                new ItemStack(Items.STICK));

        assertFalse(copy.isPresent());
    }
}
