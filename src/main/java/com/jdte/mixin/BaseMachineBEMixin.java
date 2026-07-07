package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoTransferHelper;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMachineBE.class)
public abstract class BaseMachineBEMixin {
    @Shadow protected int tickSpeed;
    @Shadow protected int operationTicks;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void jdte$syncUpgradeCapacities(CallbackInfo ci) {
        BaseMachineBE machine = (BaseMachineBE) (Object) this;
        UpgradeHelper.syncCapacities(machine);
        AutoIoTransferHelper.tick(machine);
    }

    @Inject(method = "handleTicks", at = @At("HEAD"), cancellable = true)
    private void jdte$handleEffectiveTicks(CallbackInfo ci) {
        int effectiveTickSpeed = UpgradeHelper.getEffectiveTickSpeed((BaseMachineBE) (Object) this, tickSpeed);
        if (operationTicks <= 0) {
            operationTicks = effectiveTickSpeed;
        }
        operationTicks--;
        ci.cancel();
    }

    @Inject(method = "getTickSpeed", at = @At("RETURN"), cancellable = true)
    private void jdte$getEffectiveTickSpeed(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.getEffectiveTickSpeed((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
