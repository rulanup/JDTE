package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GeneratorFluidT1BE.class, remap = false)
public abstract class GeneratorFluidUpgradeMixin {
    @Inject(remap = false, method = "doGenerate", at = @At("HEAD"), cancellable = true)
    private void jdte$generatorUpgradeFluid(CallbackInfo ci) {
        GeneratorFluidT1BE generator = (GeneratorFluidT1BE) (Object) this;
        if (!UpgradeHelper.hasGeneratorUpgrade(generator)) {
            return;
        }
        if (!generator.isActiveRedstone() || generator.getFluidStack().isEmpty()) {
            ci.cancel();
            return;
        }
        int generated = generator.getFePerFuelTick() * 3;
        if (generated == 0 || generator.insertEnergy(generated, true) != generated) {
            ci.cancel();
            return;
        }
        FluidStack extracted = generator.getFluidTank().drain(2, IFluidHandler.FluidAction.EXECUTE);
        if (extracted.getAmount() < 2) {
            ci.cancel();
            return;
        }
        generator.insertEnergy(generated, false);
        generator.setChanged();
        ci.cancel();
    }

    @Inject(remap = false, method = "getFEOutputPerTick", at = @At("RETURN"), cancellable = true)
    private void jdte$generatorUpgradeOutput(CallbackInfoReturnable<Integer> cir) {
        if (UpgradeHelper.hasGeneratorUpgrade((GeneratorFluidT1BE) (Object) this)) {
            cir.setReturnValue(cir.getReturnValue() * 3);
        }
    }
}
