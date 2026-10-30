package com.jdte.smoke;
import com.jdte.common.items.*;
import com.jdte.common.containers.handlers.LargePotionCanisterHandler;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.effect.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import java.util.List;
final class PotionProbeItems {
 static ItemStack potion(int n){var s=PotionUtils.setPotion(new ItemStack(Items.POTION,n),Potions.SWIFTNESS);PotionUtils.setCustomEffects(s,List.of(new MobEffectInstance(MobEffects.REGENERATION,321,2,true,false,false)));s.getOrCreateTag().putInt("CustomPotionColor",0x12ab34);return s;}
 static LargePotionCanisterHandler handler(ItemStack s){return (LargePotionCanisterHandler)s.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);}
 static ItemStack filled(){var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());for(int i=0;i<4;i++){if(!handler(s).insertItem(0,potion(4),false).isEmpty())throw new IllegalStateException("fill rejected");var bottles=handler(s).extractItem(0,4,false);if(!bottles.is(Items.GLASS_BOTTLE)||bottles.getCount()!=4)throw new IllegalStateException("bottle ledger");}return s;}
 static ItemStack bow(){var s=new ItemStack(Registration.EclipseAlloyBow.get());ToggleableTool.setSetting(s,"enabled",true);for(var a:new Ability[]{Ability.POTIONARROW,Ability.SPLASH,Ability.LINGERING}){s.getOrCreateTag().putBoolean("upgrade_"+a.getName(),true);ToggleableTool.setSetting(s,a.getName(),true);}s.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(Integer.MAX_VALUE,false);return s;}
}
