package com.jdte.common.utils;

import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/** Adapts entity filters to JDT 1.20.1's item-stack filter API. */
public final class EntityFilterHelper {
    private EntityFilterHelper() {
    }

    public static boolean matches(FilterableBE filterable, Entity entity) {
        SpawnEggItem egg = SpawnEggItem.byId(entity.getType());
        return egg != null ? filterable.isStackValidFilter(new ItemStack(egg))
                : !filterable.getFilterData().allowlist;
    }
}
