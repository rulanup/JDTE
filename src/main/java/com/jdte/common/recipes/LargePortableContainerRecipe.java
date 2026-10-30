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

/** Source portable upgrades; four occupied ingot slots consume four actual items in Forge crafting. */
public final class LargePortableContainerRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final Item source,result;
    public LargePortableContainerRecipe(ResourceLocation id){this(id,item("justdirethings:fuel_canister"),JDTEItems.LARGE_FUEL_CANISTER.get());}
    public LargePortableContainerRecipe(ResourceLocation id,Item source,Item result){
        if(!(source==item("justdirethings:fuel_canister")&&result==JDTEItems.LARGE_FUEL_CANISTER.get()
                ||source==item("justdirethings:potion_canister")&&result==JDTEItems.LARGE_POTION_CANISTER.get()))throw new IllegalArgumentException("Unported portable-container pair");
        this.id=id;this.source=source;this.result=result;
    }
    private static Item item(String id){var key=ResourceLocation.tryParse(id);if(key==null||!BuiltInRegistries.ITEM.containsKey(key))throw new IllegalArgumentException("Unknown portable item "+id);return BuiltInRegistries.ITEM.get(key);}
    @Override public boolean matches(CraftingContainer input,Level level){
        int sources=0,ingots=0,catalysts=0;
        for(int i=0;i<input.getContainerSize();i++){var s=input.getItem(i);if(s.isEmpty())continue;
            if(s.is(source)&&s.getCount()==1)sources++;
            else if(s.is(item("justdirethings:eclipsealloy_ingot")))ingots++;
            else if(s.is(JDTEItems.TIME_FLUID_CATALYST.get()))catalysts++;else return false;
        }return sources==1&&ingots==4&&catalysts==1;
    }
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess registry){
        if(!matches(input,null))return ItemStack.EMPTY;
        for(int i=0;i<input.getContainerSize();i++)if(input.getItem(i).is(source)){
            var data=input.getItem(i).save(new net.minecraft.nbt.CompoundTag());data.putString("id",BuiltInRegistries.ITEM.getKey(result).toString());data.putByte("Count",(byte)1);return ItemStack.of(data);
        }return ItemStack.EMPTY;
    }
    @Override public NonNullList<Ingredient> getIngredients(){var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(source));for(int i=0;i<4;i++)ingredients.add(Ingredient.of(item("justdirethings:eclipsealloy_ingot")));ingredients.add(Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get()));return ingredients;}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer input){return NonNullList.withSize(input.getContainerSize(),ItemStack.EMPTY);}
    @Override public boolean canCraftInDimensions(int w,int h){return w*h>=6;}
    @Override public ItemStack getResultItem(RegistryAccess registry){return new ItemStack(result);}
    @Override public ResourceLocation getId(){return id;}
    @Override public RecipeSerializer<?> getSerializer(){return JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_SERIALIZER.get();}
    @Override public CraftingBookCategory category(){return CraftingBookCategory.MISC;}
    public static final class Serializer implements RecipeSerializer<LargePortableContainerRecipe>{
        @Override public LargePortableContainerRecipe fromJson(ResourceLocation id,JsonObject json){return new LargePortableContainerRecipe(id,item(json.get("source_item").getAsString()),item(json.get("result").getAsString()));}
        @Override public LargePortableContainerRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf){return new LargePortableContainerRecipe(id,item(buf.readResourceLocation().toString()),item(buf.readResourceLocation().toString()));}
        @Override public void toNetwork(FriendlyByteBuf buf,LargePortableContainerRecipe recipe){buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.source));buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.result));}
    }
}
