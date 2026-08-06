package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickerT1BE.class)
public abstract class ClickerFluidMixin {
    @Inject(method = "tickServer", at = @At("HEAD"))
    private void jdte$fillFluidItem(CallbackInfo ci) {
        UpgradeHelper.syncClickerFluidTank((BaseMachineBE) (Object) this);
        UpgradeHelper.fillClickerItemFromTank((BaseMachineBE) (Object) this);
    }
}
