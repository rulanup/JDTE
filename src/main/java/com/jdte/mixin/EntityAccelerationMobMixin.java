package com.jdte.mixin;

import com.jdte.common.manager.EntityAccelerationManager;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class EntityAccelerationMobMixin {
    @Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
    private void jdte$suppressAiDuringAcceleration(CallbackInfoReturnable<Boolean> cir) {
        if (EntityAccelerationManager.isSuppressingAi()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void jdte$cancelServerAiStep(CallbackInfo ci) {
        if (EntityAccelerationManager.isSuppressingAi()) {
            ci.cancel();
        }
    }
}
