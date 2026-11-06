package com.jdte.common.recipes;
import com.google.gson.JsonObject;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.setup.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
/** Real vanilla smithing menu/JEI layout; original template/base/catalyst counts are exactly one. */
public final class EntityAccelerationSmithingRecipe extends SmithingTransformRecipe {
 public EntityAccelerationSmithingRecipe(ResourceLocation id){super(id,Ingredient.of(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()),Ingredient.of(JDTEItems.ULTIMATE_TIME_WAND.get()),Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get()),result());}
 private static ItemStack result(){var result=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());UltimateTimeWandItem.setEntityAcceleration(result,true);return result;}
 @Override public boolean matches(Container input,Level level){return input.getContainerSize()>=3&&input.getItem(0).getCount()==1&&input.getItem(1).getCount()==1&&input.getItem(2).getCount()==1&&isTemplateIngredient(input.getItem(0))&&isBaseIngredient(input.getItem(1))&&isAdditionIngredient(input.getItem(2));}
 @Override public ItemStack assemble(Container input,RegistryAccess access){if(!matches(input,null))return ItemStack.EMPTY;var result=input.getItem(1).copyWithCount(1);UltimateTimeWandItem.setEntityAcceleration(result,true);return result;}
 @Override public boolean isTemplateIngredient(ItemStack s){return s.is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());}
 @Override public boolean isBaseIngredient(ItemStack s){return s.is(JDTEItems.ULTIMATE_TIME_WAND.get())&&!UltimateTimeWandItem.hasEntityAcceleration(s);}
 @Override public boolean isAdditionIngredient(ItemStack s){return s.is(JDTEItems.TIME_FLUID_CATALYST.get());}
 @Override public RecipeSerializer<?> getSerializer(){return JDTERecipes.ENTITY_ACCELERATION_SMITHING_RECIPE_SERIALIZER.get();}
 public static final class Serializer implements RecipeSerializer<EntityAccelerationSmithingRecipe>{public EntityAccelerationSmithingRecipe fromJson(ResourceLocation id,JsonObject j){return new EntityAccelerationSmithingRecipe(id);}public EntityAccelerationSmithingRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf b){return new EntityAccelerationSmithingRecipe(id);}public void toNetwork(FriendlyByteBuf b,EntityAccelerationSmithingRecipe r){}}
}
