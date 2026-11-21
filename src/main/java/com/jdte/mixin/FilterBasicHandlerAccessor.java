package com.jdte.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StacksResourceHandler.class)
public interface FilterBasicHandlerAccessor {
    @Invoker("setStacks")
    void jdte$setStacks(NonNullList<ItemStack> stacks);
}
