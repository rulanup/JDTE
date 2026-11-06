package com.jdte.mixin;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.jdte.common.items.LargePotionCanisterData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=PotionCanister.class,remap=false)
public abstract class PotionCanisterCapacityMixin {
    @Inject(method="getPotionAmount",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$amount(ItemStack stack,CallbackInfoReturnable<Integer> cir){if(LargePotionCanisterData.isLarge(stack))cir.setReturnValue(LargePotionCanisterData.amount(stack));}
    @Inject(method="setPotionAmount",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$setAmount(ItemStack stack,int amount,CallbackInfo ci){if(LargePotionCanisterData.isLarge(stack)){LargePotionCanisterData.setAmount(stack,amount);ci.cancel();}}
    @Inject(method="reducePotionAmount",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$reduce(ItemStack stack,int amount,CallbackInfo ci){if(LargePotionCanisterData.isLarge(stack)){if(amount>=0)LargePotionCanisterData.setAmount(stack,(long)LargePotionCanisterData.amount(stack)-amount);ci.cancel();}}
    @Inject(method="addPotionAmount",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$add(ItemStack stack,int amount,CallbackInfo ci){if(LargePotionCanisterData.isLarge(stack)){if(amount>=0)LargePotionCanisterData.setAmount(stack,(long)LargePotionCanisterData.amount(stack)+amount);ci.cancel();}}
    @Inject(method="getStoredPotion",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$baseType(ItemStack stack,CallbackInfoReturnable<net.minecraft.world.item.alchemy.Potion> cir){if(LargePotionCanisterData.isLarge(stack))cir.setReturnValue(LargePotionCanisterData.base(stack));}
    @Inject(method="getPotionColor",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$fullColor(ItemStack stack,CallbackInfoReturnable<Integer> cir){if(LargePotionCanisterData.isLarge(stack))cir.setReturnValue(LargePotionCanisterData.color(stack));}

}
