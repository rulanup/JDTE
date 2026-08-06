package com.jdte.mixin;

import com.jdte.common.blockentities.EntitySuppressorManager;
import com.jdte.common.blockentities.RangeBlockerManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores NeoForge's generic entity-tick hooks on Forge 1.20.1. */
@Mixin(Entity.class)
public abstract class EntityTickMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void jdte$beforeTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        RangeBlockerManager.onEntityTickPre(entity);
        if (EntitySuppressorManager.shouldSuppressEntityTick(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void jdte$afterTick(CallbackInfo ci) {
        RangeBlockerManager.onEntityTickPost((Entity) (Object) this);
    }
}
