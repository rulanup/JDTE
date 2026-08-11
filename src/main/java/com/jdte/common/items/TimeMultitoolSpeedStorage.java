package com.jdte.common.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

final class TimeMultitoolSpeedStorage {
    private TimeMultitoolSpeedStorage() {
    }

    static TimeMultitoolSpeedMode get(ItemStack stack, DataComponentType<Integer> component) {
        return TimeMultitoolSpeedMode.fromStoredIndex(stack.getOrDefault(component, 0));
    }

    static void set(ItemStack stack, DataComponentType<Integer> component, TimeMultitoolSpeedMode mode) {
        stack.set(component, mode.ordinal());
    }
}
