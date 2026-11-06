package com.jdte.mixin;

import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import com.jdte.common.items.LargePotionBowIntegration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=BaseBow.class,remap=false)
public abstract class LargePotionBowMixin {
    @Inject(method="setupArrowAbilities",at=@At(value="INVOKE",target="Lcom/direwolf20/justdirethings/common/items/tools/basetools/BaseBow;noPotionAbilitiesActive(Lnet/minecraft/world/item/ItemStack;)Z"),cancellable=true,remap=false)
    private void jdte$fullPotionContents(ItemStack stack,JustDireArrow arrow,Player player,CallbackInfo ci){
        if(LargePotionBowIntegration.hasLarge(stack)){LargePotionBowIntegration.apply((BaseBow)(Object)this,stack,arrow,player);ci.cancel();}
    }
}
