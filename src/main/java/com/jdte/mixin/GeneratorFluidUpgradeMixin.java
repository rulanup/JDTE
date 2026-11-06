package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.GeneratorFuelBatch;
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
        int generated = GeneratorFuelBatch.energyOutput(generator.getFePerFuelTick());
        FluidStack available = generator.getFluidTank().drain(GeneratorFuelBatch.FLUID_COST, IFluidHandler.FluidAction.SIMULATE);
        if (!GeneratorFuelBatch.canCommit(available.getAmount(), generated,
                generated > 0 ? generator.insertEnergy(generated, true) : 0)) {
            ci.cancel();
            return;
        }
        FluidStack previousFluid = generator.getFluidStack().copy();
        int previousEnergy = generator.getEnergyStorage().getEnergyStored();
        FluidStack extracted = generator.getFluidTank().drain(GeneratorFuelBatch.FLUID_COST, IFluidHandler.FluidAction.EXECUTE);
        if (extracted.getAmount() != GeneratorFuelBatch.FLUID_COST
                || generator.insertEnergy(generated, false) != generated) {
            // Both buffers belong to this machine. Restore them if a commit is unexpectedly short.
            generator.getFluidTank().setFluid(previousFluid);
            generator.getEnergyStorage().setEnergy(previousEnergy);
            ci.cancel();
            return;
        }
        generator.setChanged();
        ci.cancel();
    }

    @Inject(remap = false, method = "getFEOutputPerTick", at = @At("RETURN"), cancellable = true)
    private void jdte$generatorUpgradeOutput(CallbackInfoReturnable<Integer> cir) {
        if (UpgradeHelper.hasGeneratorUpgrade((GeneratorFluidT1BE) (Object) this)) {
            cir.setReturnValue(com.jdte.common.items.FuelCanisterMath.rate(cir.getReturnValue(), 3));
        }
    }
}
