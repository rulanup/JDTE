package com.jdte.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Optional;

/** Accesses the entity data stored on a spawn egg without exposing the stack's mutable component. */
public final class SpawnEggEntityData {
    private SpawnEggEntityData() { }

    public static Optional<CompoundTag> copy(ItemStack stack) {
        var entityData = stack.get(DataComponents.ENTITY_DATA);
        if (entityData == null) return Optional.empty();
        CompoundTag tag = entityData.copyTagWithoutId();
        tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityData.type()).toString());
        return Optional.of(tag);
    }
}
