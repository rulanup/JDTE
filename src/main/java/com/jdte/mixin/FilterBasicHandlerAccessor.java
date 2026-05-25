package com.jdte.mixin;

import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackHandler.class)
public interface FilterBasicHandlerAccessor {
    @Mutable
    @Accessor("stacks")
    void jdte$setStacks(net.minecraft.world.item.ItemStack[] stacks);
}
