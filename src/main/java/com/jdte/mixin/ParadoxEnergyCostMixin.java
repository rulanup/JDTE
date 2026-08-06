package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParadoxMachineBE.class)
public abstract class ParadoxEnergyCostMixin {
    @Inject(method = "getBlockEnergyCost", at = @At("RETURN"), cancellable = true)
    private void jdte$adjustBlockEnergyCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.adjustEnergyCost((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "getEntityEnergyCost", at = @At("RETURN"), cancellable = true)
    private void jdte$adjustEntityEnergyCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.adjustEnergyCost((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
