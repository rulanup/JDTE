package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.*;

/** Forge representation of PotionContents: native amount/base ID plus complete vanilla potion NBT. */
public final class LargePotionCanisterData {
    public static final String CONTENTS="JDTEPotionContents", AMOUNT="PotionAmt", BASE="StoredPotionId";
    private LargePotionCanisterData(){}
    public static boolean isLarge(ItemStack stack){return stack.getItem() instanceof LargePotionCanisterItem;}
    public static int amount(ItemStack stack){return stack.hasTag()?PotionCanisterMath.boundedAmount(stack.getTag().getInt(AMOUNT)):0;}
    public static CompoundTag fromPotion(ItemStack potion){
        var result=new CompoundTag();var tag=potion.getTag();if(tag==null)return result;
        for(String key:List.of("Potion","CustomPotionEffects","CustomPotionColor"))if(tag.contains(key))result.put(key,tag.get(key).copy());
        return normalize(result);
    }
    public static CompoundTag contents(ItemStack stack){
        if(!stack.hasTag())return new CompoundTag();var tag=stack.getTag();
        if(tag.contains(CONTENTS))return tag.contains(CONTENTS,Tag.TAG_COMPOUND)?normalize(tag.getCompound(CONTENTS)):new CompoundTag();
        var result=new CompoundTag();if(tag.contains(BASE,Tag.TAG_STRING))result.putString("Potion",tag.getString(BASE));
        // Legacy native data used only a base potion; preserve custom extensions if present.
        for(String key:List.of("CustomPotionEffects","CustomPotionColor"))if(tag.contains(key))result.put(key,tag.get(key).copy());
        return normalize(result);
    }
    private static CompoundTag normalize(CompoundTag input){
        var result=input.copy();
        if(result.contains("Potion",Tag.TAG_STRING)){var id=ResourceLocation.tryParse(result.getString("Potion"));if(id!=null)result.putString("Potion",id.toString());}
        if(result.contains("CustomPotionEffects",Tag.TAG_LIST)){var raw=(ListTag)result.get("CustomPotionEffects");if(raw.isEmpty())result.remove("CustomPotionEffects");else if(raw.getElementType()==Tag.TAG_COMPOUND){var canonical=new ListTag();for(var entry:raw){var effect=MobEffectInstance.load((CompoundTag)entry);if(effect==null)return result;canonical.add(effect.save(new CompoundTag()));}result.put("CustomPotionEffects",canonical);}}
        return result;
    }
    public static boolean validContents(CompoundTag contents){
        boolean base=false;
        if(contents.contains("Potion")){
            if(!contents.contains("Potion",Tag.TAG_STRING))return false;
            var id=ResourceLocation.tryParse(contents.getString("Potion"));
            if(id==null||!BuiltInRegistries.POTION.containsKey(id))return false;
            base=BuiltInRegistries.POTION.get(id)!=Potions.EMPTY;
        }
        if(contents.contains("CustomPotionColor")&&!contents.contains("CustomPotionColor",Tag.TAG_INT))return false;
        if(contents.contains("CustomPotionEffects")){
            if(!contents.contains("CustomPotionEffects",Tag.TAG_LIST))return false;
            var list=(ListTag)contents.get("CustomPotionEffects");if(!list.isEmpty()&&list.getElementType()!=Tag.TAG_COMPOUND)return false;
            var effects=contents.getList("CustomPotionEffects",Tag.TAG_COMPOUND);
            for(var raw:effects)if(MobEffectInstance.load((CompoundTag)raw)==null)return false;
            if(!effects.isEmpty())return true;
        }
        return base;
    }
    public static ItemStack asPotion(CompoundTag contents){var potion=new ItemStack(Items.POTION);potion.setTag(contents.copy());return potion;}
    public static List<MobEffectInstance> effects(ItemStack stack){return validContents(contents(stack))?PotionUtils.getMobEffects(asPotion(contents(stack))):List.of();}
    public static int color(ItemStack stack){return validContents(contents(stack))?PotionUtils.getColor(asPotion(contents(stack))):-1;}
    public static Potion base(ItemStack stack){return validContents(contents(stack))?PotionUtils.getPotion(contents(stack)):Potions.EMPTY;}
    public static void setAmount(ItemStack stack,long value){
        int amount=PotionCanisterMath.boundedAmount(value);var tag=stack.getOrCreateTag();tag.putInt(AMOUNT,amount);
        if(amount==0){tag.remove(CONTENTS);tag.remove(BASE);tag.remove("CustomPotionEffects");tag.remove("CustomPotionColor");}
    }
    public static boolean tryFillBatch(ItemStack stack,ItemStack input){
        if(!isLarge(stack)||stack.getCount()!=1||stack.hasTag()&&stack.getTag().contains(AMOUNT)&&!stack.getTag().contains(AMOUNT,Tag.TAG_INT)||!(input.getItem() instanceof PotionItem)||!PotionCanisterMath.canFill(rawAmount(stack),input.getCount()))return false;
        var incoming=fromPotion(input);var existing=contents(stack);
        if(!validContents(incoming)||rawAmount(stack)>0&&(!validContents(existing)||!existing.equals(incoming)))return false;
        // Stage a complete new compound; no caller input or container is changed before validation.
        var next=stack.hasTag()?stack.getTag().copy():new CompoundTag();
        next.put(CONTENTS,incoming.copy());
        if(incoming.contains("Potion"))next.putString(BASE,incoming.getString("Potion"));else next.remove(BASE);
        next.putInt(AMOUNT,rawAmount(stack)+PotionCanisterMath.BATCH);
        stack.setTag(next);input.shrink(PotionCanisterMath.POTIONS_PER_BATCH);return true;
    }
    private static int rawAmount(ItemStack stack){return stack.hasTag()?stack.getTag().getInt(AMOUNT):0;}
}
