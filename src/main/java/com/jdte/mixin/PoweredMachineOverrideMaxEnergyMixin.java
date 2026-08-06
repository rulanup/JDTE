package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnergyTransmitterBE.class, GeneratorFluidT1BE.class, GeneratorT1BE.class, ParadoxMachineBE.class})
public abstract class PoweredMachineOverrideMaxEnergyMixin {
    @Inject(method = "getMaxEnergy", at = @At("RETURN"), cancellable = true)
    private void jdte$adjustMaxEnergy(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.adjustEnergyCapacity((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
