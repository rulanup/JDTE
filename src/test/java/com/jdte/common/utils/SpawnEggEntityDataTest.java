package com.jdte.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnEggEntityDataTest implements com.jdte.test.WorldRegistryTest {

    @org.junit.jupiter.api.BeforeAll
    static void bindDefaultComponents() {
        com.jdte.test.TestWorldRegistries.load();
    }

    @Test
    void copiesEntityDataWithoutSharingMutableTag() {
        ItemStack stack = new ItemStack(Items.STICK);
        CompoundTag source = new CompoundTag();
        source.putString("type", "productivebees:life_fluid");
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.BEE, source));

        Optional<CompoundTag> copy = SpawnEggEntityData.copy(stack);

        assertTrue(copy.isPresent());
        assertNotSame(source, copy.get());
        assertEquals("minecraft:bee", copy.get().getStringOr("id", ""));
        assertEquals("productivebees:life_fluid", copy.get().getStringOr("type", ""));
        copy.get().putString("type", "minecraft:bee");
        assertEquals("productivebees:life_fluid", stack.get(DataComponents.ENTITY_DATA).copyTagWithoutId()
                .getStringOr("type", ""));
    }

    @Test
    void missingEntityDataIsEmpty() {
        Optional<CompoundTag> copy = SpawnEggEntityData.copy(
                new ItemStack(Items.STICK));

        assertFalse(copy.isPresent());
    }
}
