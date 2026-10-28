package com.jdte.common.recipes;
import com.google.gson.JsonObject;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Source fuel-canister upgrade; four occupied ingot slots consume four actual items. */
public final class LargePortableContainerRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    public LargePortableContainerRecipe(ResourceLocation id){this.id=id;}
    @Override public boolean matches(CraftingContainer input,Level level){
        int source=0,ingots=0,catalyst=0;
        for(int i=0;i<input.getContainerSize();i++){var s=input.getItem(i);if(s.isEmpty())continue;var item=BuiltInRegistries.ITEM.getKey(s.getItem());
            if(item.equals(new ResourceLocation("justdirethings","fuel_canister"))&&s.getCount()==1)source++;
            else if(item.equals(new ResourceLocation("justdirethings","eclipsealloy_ingot")))ingots++;
            else if(s.is(JDTEItems.TIME_FLUID_CATALYST.get()))catalyst++;else return false;
        }return source==1&&ingots==4&&catalyst==1;
    }
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess registry){
        if(!matches(input,null))return ItemStack.EMPTY;
        for(int i=0;i<input.getContainerSize();i++)if(BuiltInRegistries.ITEM.getKey(input.getItem(i).getItem()).equals(new ResourceLocation("justdirethings","fuel_canister"))){var data=input.getItem(i).save(new net.minecraft.nbt.CompoundTag());data.putString("id",BuiltInRegistries.ITEM.getKey(JDTEItems.LARGE_FUEL_CANISTER.get()).toString());data.putByte("Count",(byte)1);return ItemStack.of(data);}
        return ItemStack.EMPTY;
    }
    @Override public NonNullList<Ingredient> getIngredients(){var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.get(new ResourceLocation("justdirethings","fuel_canister"))));for(int i=0;i<4;i++)ingredients.add(Ingredient.of(BuiltInRegistries.ITEM.get(new ResourceLocation("justdirethings","eclipsealloy_ingot"))));ingredients.add(Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get()));return ingredients;}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer input){return NonNullList.withSize(input.getContainerSize(),ItemStack.EMPTY);}
    @Override public boolean canCraftInDimensions(int w,int h){return w*h>=6;}
    @Override public ItemStack getResultItem(RegistryAccess registry){return new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());}
    @Override public ResourceLocation getId(){return id;}
    @Override public RecipeSerializer<?> getSerializer(){return JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_SERIALIZER.get();}
    @Override public CraftingBookCategory category(){return CraftingBookCategory.MISC;}
    public static final class Serializer implements RecipeSerializer<LargePortableContainerRecipe>{
        @Override public LargePortableContainerRecipe fromJson(ResourceLocation id,JsonObject json){if(!json.get("source_item").getAsString().equals("justdirethings:fuel_canister")||!json.get("result").getAsString().equals("jdte:large_fuel_canister"))throw new IllegalArgumentException("Unported portable-container pair");return new LargePortableContainerRecipe(id);}
        @Override public LargePortableContainerRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf){return new LargePortableContainerRecipe(id);}
        @Override public void toNetwork(FriendlyByteBuf buf,LargePortableContainerRecipe recipe){}
    }
}
