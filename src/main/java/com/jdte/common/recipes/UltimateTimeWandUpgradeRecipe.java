package com.jdte.common.recipes;
import com.google.gson.JsonObject;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.setup.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
/** One wand + one or both distinct upgrade cards, copying all existing resources/settings. */
public final class UltimateTimeWandUpgradeRecipe extends CustomRecipe {
 public UltimateTimeWandUpgradeRecipe(ResourceLocation id){super(id,CraftingBookCategory.EQUIPMENT);}
 @Override public boolean matches(CraftingContainer input,Level level){ItemStack wand=ItemStack.EMPTY;boolean entity=false,overclock=false;
  for(int i=0;i<input.getContainerSize();i++){var s=input.getItem(i);if(s.isEmpty())continue;
   if(s.is(JDTEItems.ULTIMATE_TIME_WAND.get())){if(!wand.isEmpty()||s.getCount()!=1)return false;wand=s;}
   else if(s.is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get())){if(entity)return false;entity=true;}
   else if(s.is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())){if(overclock)return false;overclock=true;}else return false;
  }
  return !wand.isEmpty()&&(entity||overclock)&&(!entity||!UltimateTimeWandItem.hasEntityAcceleration(wand))&&(!overclock||!UltimateTimeWandItem.hasUltimateOverclock(wand));
 }
 @Override public ItemStack assemble(CraftingContainer input,RegistryAccess access){if(!matches(input,null))return ItemStack.EMPTY;ItemStack result=ItemStack.EMPTY;boolean entity=false,overclock=false;for(int i=0;i<input.getContainerSize();i++){var s=input.getItem(i);if(s.is(JDTEItems.ULTIMATE_TIME_WAND.get()))result=s.copyWithCount(1);entity|=s.is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());overclock|=s.is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get());}if(entity)UltimateTimeWandItem.setEntityAcceleration(result,true);if(overclock)UltimateTimeWandItem.setUltimateOverclock(result,true);return result;}
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=2;}
 @Override public RecipeSerializer<?> getSerializer(){return JDTERecipes.ULTIMATE_TIME_WAND_UPGRADE_RECIPE_SERIALIZER.get();}
 public static final class Serializer implements RecipeSerializer<UltimateTimeWandUpgradeRecipe>{public UltimateTimeWandUpgradeRecipe fromJson(ResourceLocation id,JsonObject json){return new UltimateTimeWandUpgradeRecipe(id);}public UltimateTimeWandUpgradeRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf b){return new UltimateTimeWandUpgradeRecipe(id);}public void toNetwork(FriendlyByteBuf b,UltimateTimeWandUpgradeRecipe r){}}
}
