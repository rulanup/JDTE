package com.jdte.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/** Accesses the entity data stored on a spawn egg without exposing the stack's mutable component. */
public final class SpawnEggEntityData {
    private SpawnEggEntityData() { }

    public static Optional<CompoundTag> copy(ItemStack stack) {
        CustomData entityData = stack.get(DataComponents.ENTITY_DATA);
        return entityData == null ? Optional.empty() : Optional.of(entityData.copyTag());
    }
}
