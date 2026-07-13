package com.jdte.mixin;

import com.jdte.common.blockentities.EntitySuppressorManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Shadow protected ClientLevel level;

    @Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void jdte$suppressParticle(Particle particle, CallbackInfo ci) {
        Vec3 position = particle.getPos();
        if (EntitySuppressorManager.shouldSuppressParticle(level, position.x, position.y, position.z)) {
            ci.cancel();
        }
    }
}
