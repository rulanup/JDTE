package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT2BE;
import com.direwolf20.justdirethings.common.blockentities.BlockPlacerT2BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT2BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT2BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT2BE;
import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT2BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT2BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blockentities.ParadoxMachineBE;
import com.direwolf20.justdirethings.common.blockentities.SensorT2BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockBreakerT2BE.class, BlockPlacerT2BE.class, BlockSwapperT2BE.class, ClickerT2BE.class, DropperT2BE.class, EnergyTransmitterBE.class, FluidCollectorT2BE.class, FluidPlacerT2BE.class, GeneratorFluidT1BE.class, GeneratorT1BE.class, ParadoxMachineBE.class, SensorT2BE.class})
public abstract class EnergyCostMixin {
    @Inject(method = "getStandardEnergyCost", at = @At("RETURN"), cancellable = true)
    private void jdte$adjustEnergyCost(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(UpgradeHelper.adjustEnergyCost((BaseMachineBE) (Object) this, cir.getReturnValue()));
    }
}
