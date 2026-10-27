package com.jdte.common.integrations.ae2;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
/** Forge item tags replacing the source AE wireless target and extraction components. */
public final class AEWirelessLinkData {
 private AEWirelessLinkData(){}
 public static GlobalPos get(ItemStack stack){
  if(!stack.hasTag()||!stack.getTag().contains("JDTEWirelessLink",Tag.TAG_COMPOUND))return null;
  var tag=stack.getTag().getCompound("JDTEWirelessLink");var dim=ResourceLocation.tryParse(tag.getString("Dimension"));
  return dim!=null&&tag.contains("Position",Tag.TAG_LONG)?GlobalPos.of(ResourceKey.create(Registries.DIMENSION,dim),BlockPos.of(tag.getLong("Position"))):null;
 }
 public static void set(ItemStack stack,GlobalPos target){
  if(target==null){if(stack.hasTag())stack.getTag().remove("JDTEWirelessLink");return;}
  var tag=new CompoundTag();tag.putString("Dimension",target.dimension().location().toString());tag.putLong("Position",target.pos().asLong());stack.getOrCreateTag().put("JDTEWirelessLink",tag);
 }
 public static boolean extractionEnabled(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean("JDTEAEExtractionEnabled");}
 public static void setExtractionEnabled(ItemStack stack){stack.getOrCreateTag().putBoolean("JDTEAEExtractionEnabled",true);}
}
