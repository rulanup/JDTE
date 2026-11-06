package com.jdte.common.items;

import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.effect.MobEffectInstance;

/** Forge native bows retain their non-potion abilities; this source-equivalent branch handles full PotionContents. */
public final class LargePotionBowIntegration {
    private LargePotionBowIntegration(){}
    public static boolean hasLarge(ItemStack bow){for(int i=0;i<2;i++)if(LargePotionCanisterData.isLarge(BaseBow.getPotionCanister(bow,i)))return true;return false;}
    public static void apply(BaseBow bow,ItemStack stack,JustDireArrow arrow,Player player){
        if(player.level().isClientSide||bow.noPotionAbilitiesActive(stack))return;
        int cost=0;for(var ability:new Ability[]{Ability.POTIONARROW,Ability.SPLASH,Ability.LINGERING})if(bow.canUseAbilityAndDurability(stack,ability))cost+=ability==Ability.LINGERING?50:25;
        if(cost<=0)return;boolean applied=false;
        for(int slot=0;slot<2;slot++){
            var canister=BaseBow.getPotionCanister(stack,slot);if(!(canister.getItem() instanceof PotionCanister)||PotionCanister.getPotionAmount(canister)<cost)continue;
            java.util.List<MobEffectInstance> effects;
            if(LargePotionCanisterData.isLarge(canister)){
                if(!LargePotionCanisterData.validContents(LargePotionCanisterData.contents(canister)))continue;
                effects=LargePotionCanisterData.effects(canister);
            }else{
                var potion=PotionCanister.getStoredPotion(canister);if(potion==Potions.EMPTY)continue;effects=potion.getEffects();
            }
            for(var effect:effects){arrow.addEffect(new MobEffectInstance(effect));applied=true;}
            PotionCanister.reducePotionAmount(canister,cost);BaseBow.setPotionCanister(stack,canister,slot);
        }
        if(applied){
            if(bow.canUseAbilityAndDurability(stack,Ability.POTIONARROW)){arrow.setPotionArrow(true);Helpers.damageTool(stack,player,Ability.POTIONARROW);}
            if(bow.canUseAbilityAndDurability(stack,Ability.SPLASH)){arrow.setSplash(true);Helpers.damageTool(stack,player,Ability.SPLASH);}
            if(bow.canUseAbilityAndDurability(stack,Ability.LINGERING)){arrow.setLingering(true);Helpers.damageTool(stack,player,Ability.LINGERING);}
        }
    }
}
