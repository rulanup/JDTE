package com.jdte.mixin;

import com.jdte.common.manager.EntityAccelerationManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityAccelerationLivingEntityMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void jdte$cancelTravelDuringAcceleration(Vec3 travelVector, CallbackInfo ci) {
        if (EntityAccelerationManager.isSuppressingAi()) {
            ci.cancel();
        }
    }

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void jdte$cancelPushEntitiesDuringAcceleration(CallbackInfo ci) {
        if (EntityAccelerationManager.isSuppressingAi()) {
            ci.cancel();
        }
    }
}
