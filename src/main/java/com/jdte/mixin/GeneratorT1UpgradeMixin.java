package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeneratorT1BE.class)
public abstract class GeneratorT1UpgradeMixin {
    @Shadow public int maxBurn;
    @Shadow public int burnRemaining;
    @Shadow public int feRemaining;
    @Shadow int fuelBurnMultiplier;
    @Shadow public abstract int insertEnergy(int power, boolean simulate);
    @Shadow public abstract int fePerTick();
    @Shadow public abstract int getFePerFuelTick();
    @Shadow public abstract int getBurnSpeedMultiplier();
    @Shadow public abstract net.neoforged.neoforge.items.ItemStackHandler getMachineHandler();

    @Inject(method = "doBurn", at = @At("HEAD"), cancellable = true)
    private void jdte$generatorUpgradeBurn(CallbackInfo ci) {
        if (!UpgradeHelper.hasGeneratorUpgrade((GeneratorT1BE) (Object) this)) {
            return;
        }

        if (feRemaining > 0) {
            ci.cancel();
            return;
        }
        maxBurn = 0;
        burnRemaining = 0;
        if (insertEnergy(fePerTick(), true) <= 0) {
            ci.cancel();
            return;
        }
        ItemStack fuelStack = getMachineHandler().getStackInSlot(0);
        if (fuelStack.isEmpty()) {
            ci.cancel();
            return;
        }
        int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING);
        if (burnTime <= 0) {
            ci.cancel();
            return;
        }
        if (!fuelStack.hasCraftingRemainingItem() && fuelStack.getCount() < 2) {
            ci.cancel();
            return;
        }

        int oldMultiplier = fuelBurnMultiplier;
        if (fuelStack.getItem() instanceof Coal_T1 direCoal) {
            fuelBurnMultiplier = direCoal.getBurnSpeedMultiplier();
        } else if (fuelStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CoalBlock_T1 coalBlock) {
            fuelBurnMultiplier = coalBlock.getBurnSpeedMultiplier();
        } else if (fuelStack.getItem() instanceof FuelCanister) {
            fuelBurnMultiplier = FuelCanister.getBurnSpeedMultiplier(fuelStack);
        } else {
            fuelBurnMultiplier = 1;
        }
        if (fuelBurnMultiplier != oldMultiplier) {
            ((GeneratorT1BE) (Object) this).markDirtyClient();
        }
        if (fuelStack.hasCraftingRemainingItem()) {
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            if (remaining.getItem() instanceof FuelCanister) {
                FuelCanister.decrementFuel(remaining);
            }
            getMachineHandler().setStackInSlot(0, remaining);
        } else {
            fuelStack.shrink(2);
        }

        feRemaining = burnTime * getFePerFuelTick() * 3;
        maxBurn = (int) (Math.floor(burnTime) / getBurnSpeedMultiplier());
        burnRemaining = maxBurn + 1;
        ci.cancel();
    }

    @Inject(method = "getFEPerTick", at = @At("RETURN"), cancellable = true)
    private void jdte$generatorUpgradeOutput(CallbackInfoReturnable<Integer> cir) {
        if (UpgradeHelper.hasGeneratorUpgrade((GeneratorT1BE) (Object) this)) {
            cir.setReturnValue(Config.GENERATOR_T1_FE_PER_TICK.get() * 3);
        }
    }
}
