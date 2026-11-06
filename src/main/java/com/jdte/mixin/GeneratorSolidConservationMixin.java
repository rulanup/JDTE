package com.jdte.mixin;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.jdte.common.items.FuelCanisterMath;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native Forge discards requested (rather than accepted) FE when its buffer is full. Preserve purchased credit. */
@Mixin(value=GeneratorT1BE.class,remap=false)
public abstract class GeneratorSolidConservationMixin {
    @Shadow public int feRemaining;
    @Shadow public int burnRemaining;
    @Shadow int fuelBurnMultiplier;
    @Shadow public int maxBurn;
    @Inject(method="doGenerate",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$preserveCredit(CallbackInfo ci){
        var owner=(GeneratorT1BE)(Object)this;
        if(owner.isActiveRedstone()&&feRemaining==0)owner.doBurn();
        int budget=Math.max(0,owner.fePerTick());
        if(feRemaining>0&&budget>0){
            int accepted=Math.max(0,Math.min(Math.min(budget,feRemaining),owner.insertEnergy(Math.min(budget,feRemaining),false)));
            feRemaining-=accepted;if(accepted>0)burnRemaining=Math.max(0,burnRemaining-1);
            if(owner.isActiveRedstone()&&feRemaining==0){
                owner.doBurn();int remaining=Math.min(budget-accepted,feRemaining);
                if(remaining>0){int extra=Math.max(0,Math.min(remaining,owner.insertEnergy(remaining,false)));feRemaining-=extra;if(extra>0)burnRemaining=Math.max(0,burnRemaining-1);}
            }
            if(accepted>0)owner.setChanged();
        }
        ci.cancel();
    }
    @Inject(method="fePerTick",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$checkedRate(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir){var owner=(GeneratorT1BE)(Object)this;cir.setReturnValue(FuelCanisterMath.rate(owner.getFePerFuelTick(),owner.getBurnSpeedMultiplier()));}
    @Inject(method="getBurnSpeedMultiplier",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$checkedSpeed(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1,FuelCanisterMath.rate(com.direwolf20.justdirethings.setup.Config.GENERATOR_T1_BURN_SPEED_MULTIPLIER.get(),fuelBurnMultiplier)));
    }
    @Inject(method="doBurn",at=@At("TAIL"),remap=false)
    private void jdte$checkedCounter(CallbackInfo ci) {
        if (maxBurn == Integer.MAX_VALUE) { maxBurn=Integer.MAX_VALUE-1;burnRemaining=Integer.MAX_VALUE; }
    }
    @Redirect(method="doBurn",at=@At(value="INVOKE",target="Lnet/minecraftforge/common/ForgeHooks;getBurnTime(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/RecipeType;)I"),remap=false)
    private int jdte$checkedBurn(ItemStack stack,RecipeType<?> type){int ticks=net.minecraftforge.common.ForgeHooks.getBurnTime(stack,type);return FuelCanisterMath.energyBudget(ticks,((GeneratorT1BE)(Object)this).getFePerFuelTick(),1)>0?ticks:0;}
}
