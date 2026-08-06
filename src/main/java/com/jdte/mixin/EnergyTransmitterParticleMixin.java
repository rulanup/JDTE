package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.jdte.common.blockentities.EntitySuppressorManager;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnergyTransmitterBE.class)
public abstract class EnergyTransmitterParticleMixin {
    @Inject(method = "doParticles", at = @At("HEAD"), cancellable = true)
    private void jdte$suppressEnergyTransferParticles(BlockPos source, BlockPos target, CallbackInfo ci) {
        EnergyTransmitterBE transmitter = (EnergyTransmitterBE) (Object) this;
        if (transmitter.getLevel() != null
                && (EntitySuppressorManager.shouldSuppressParticle(
                        transmitter.getLevel(), source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5)
                    || EntitySuppressorManager.shouldSuppressParticle(
                        transmitter.getLevel(), target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5))) {
            ci.cancel();
        }
    }
}
