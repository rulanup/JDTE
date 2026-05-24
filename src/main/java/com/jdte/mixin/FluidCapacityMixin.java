package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT2BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT2BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FluidCollectorT1BE.class, FluidCollectorT2BE.class, FluidPlacerT1BE.class, FluidPlacerT2BE.class, GeneratorFluidT1BE.class, ParadoxMachineBE.class})
public abstract class FluidCapacityMixin {
    @Inject(method = "getMaxMB", at = @At("RETURN"), cancellable = true)
    private void jdte$adjustMaxMb(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.adjustFluidCapacity((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
