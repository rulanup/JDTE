package com.jdte.ae.mixin;

import appeng.hooks.ticking.TickHandler;
import com.jdte.ae.AeVirtualTickContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TickHandler.class, remap = false)
public abstract class TickHandlerMixin {
    @Inject(
            method = "getCurrentTick",
            at = @At("RETURN"),
            cancellable = true,
            require = 1,
            remap = false)
    private void jdteAe$overrideCurrentTick(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(AeVirtualTickContext.override(cir.getReturnValue()));
    }
}
