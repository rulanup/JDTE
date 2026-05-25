package com.jdte.mixin;

import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FilterBasicHandler.class)
public interface FilterBasicHandlerAccessor {
    @Mutable
    @Accessor("stacks")
    void jdte$setSize(int newSize);
}
