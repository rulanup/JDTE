package com.jdte.mixin;

import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.setup.Config;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.GeneratorUpgradeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GeneratorT1BE.class, remap = false)
public abstract class GeneratorT1UpgradeMixin {
    @Shadow public int maxBurn;
    @Shadow public int burnRemaining;
    @Shadow public int feRemaining;
    @Shadow int fuelBurnMultiplier;
    @Shadow public abstract int insertEnergy(int power, boolean simulate);
    @Shadow public abstract int fePerTick();
    @Shadow public abstract int getFePerFuelTick();
    @Shadow public abstract int getBurnSpeedMultiplier();

    @Inject(remap = false, method = "doBurn", at = @At("HEAD"), cancellable = true)
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
        GeneratorT1BE generator = (GeneratorT1BE) (Object) this;
        ItemStack fuelStack = generator.getMachineHandler().getStackInSlot(0);
        if (fuelStack.isEmpty()) {
            ci.cancel();
            return;
        }
        int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
        if (burnTime <= 0) {
            ci.cancel();
            return;
        }
        if (!fuelStack.hasCraftingRemainingItem() && fuelStack.getCount() < 2) {
            ci.cancel();
            return;
        }

        if (fuelStack.getItem() instanceof com.direwolf20.justdirethings.common.items.FuelCanister) {
            int minimum = fuelStack.getItem() instanceof com.jdte.common.items.LargeFuelCanisterItem
                    ? com.jdte.common.items.LargeFuelCanisterItem.getMinimumFuelConsumed()
                    : Config.FUEL_CANISTER_MINIMUM_TICKS_CONSUMED.get();
            if ((long) com.direwolf20.justdirethings.common.items.FuelCanister.getFuelLevel(fuelStack) < 2L * minimum) { ci.cancel(); return; }
        }
        int energyBudget = com.jdte.common.items.FuelCanisterMath.energyBudget(burnTime, getFePerFuelTick(), 3);
        if (energyBudget == 0) { ci.cancel(); return; }
        int oldMultiplier = fuelBurnMultiplier;
        fuelBurnMultiplier = GeneratorUpgradeHelper.burnSpeedMultiplier(fuelStack);
        if (fuelBurnMultiplier != oldMultiplier) {
            ((GeneratorT1BE) (Object) this).markDirtyClient();
        }
        GeneratorUpgradeHelper.consumeFuel(generator.getMachineHandler(), fuelStack);

        feRemaining = energyBudget;
        maxBurn = com.jdte.common.items.FuelCanisterMath.safeBurnCounter(burnTime, getBurnSpeedMultiplier());
        burnRemaining = maxBurn + 1;
        ci.cancel();
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method="doBurn", at=@At(value="INVOKE",target="Lcom/direwolf20/justdirethings/common/items/FuelCanister;getBurnSpeedMultiplier(Lnet/minecraft/world/item/ItemStack;)I"),remap=false)
    private int jdte$largeFuelSpeed(ItemStack stack) { return com.jdte.common.items.PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(stack); }

    @Inject(remap = false, method = "getFEPerTick", at = @At("RETURN"), cancellable = true)
    private void jdte$generatorUpgradeOutput(CallbackInfoReturnable<Integer> cir) {
        if (UpgradeHelper.hasGeneratorUpgrade((GeneratorT1BE) (Object) this)) {
            cir.setReturnValue(com.jdte.common.items.FuelCanisterMath.rate(Config.GENERATOR_T1_FE_PER_TICK.get(), 3));
        }
    }
}
