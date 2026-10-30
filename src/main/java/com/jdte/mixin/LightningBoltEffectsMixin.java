package com.jdte.mixin;

import com.jdte.common.entities.MachineLightningEffects;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightningBolt.class)
public abstract class LightningBoltEffectsMixin implements MachineLightningEffects {
    @Unique
    private static final EntityDataAccessor<Boolean> jdte$RENDER_LIGHTNING =
            SynchedEntityData.defineId(LightningBolt.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void jdte$defineLightningEffects(CallbackInfo ci) {
        ((LightningBolt)(Object)this).getEntityData().define(jdte$RENDER_LIGHTNING, true);
    }

    @Override
    public boolean jdte$isLightningRenderEnabled() {
        return ((LightningBolt) (Object) this).getEntityData().get(jdte$RENDER_LIGHTNING);
    }

    @Override
    public void jdte$setLightningRenderEnabled(boolean enabled) {
        ((LightningBolt) (Object) this).getEntityData().set(jdte$RENDER_LIGHTNING, enabled);
    }

    @Inject(method = "shouldRenderAtSqrDistance", at = @At("HEAD"), cancellable = true)
    private void jdte$hideLightning(double distance, CallbackInfoReturnable<Boolean> cir) {
        if (!jdte$isLightningRenderEnabled()) cir.setReturnValue(false);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setSkyFlashTime(I)V"), require = 1)
    private void jdte$controlSkyFlash(Level level, int duration) {
        if (jdte$isLightningRenderEnabled()) level.setSkyFlashTime(duration);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"), require = 2)
    private void jdte$controlLightningSound(Level level, double x, double y, double z,
                                           SoundEvent sound, SoundSource source, float volume,
                                           float pitch, boolean distanceDelay) {
        if (!((LightningBolt) (Object) this).isSilent()) {
            level.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
        }
    }
}
