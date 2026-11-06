package com.jdte.common.items;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import java.util.*;

/** Forge NBT replacement for the source's recipe-validated nine-ingredient component. */
public record CraftingPatternData(ResourceLocation recipeId,List<ItemStack> ingredients,ItemStack result) {
    public CraftingPatternData {
        if(recipeId==null||ingredients.size()!=9||result.isEmpty())throw new IllegalArgumentException("Invalid crafting pattern");
        ingredients=ingredients.stream().map(s->s.isEmpty()?ItemStack.EMPTY:s.copyWithCount(1)).toList();result=result.copy();
    }
    public CraftingContainer input(){return input(ingredients);}
    private static CraftingContainer input(List<ItemStack> grid){
        var menu=new AbstractContainerMenu(null,0){
            @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
            @Override public boolean stillValid(Player p){return false;}
        };
        var input=new TransientCraftingContainer(menu,3,3);
        for(int i=0;i<9;i++)input.setItem(i,grid.get(i).copyWithCount(1));return input;
    }
    public static Optional<CraftingPatternData> capture(Level level,List<ItemStack> grid){
        if(grid.size()!=9)return Optional.empty();
        try{
            var input=input(grid);
            return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).flatMap(recipe->{
                if(recipe.isSpecial())return Optional.empty();var output=recipe.assemble(input,level.registryAccess());
                if(output.isEmpty()||!output.isItemEnabled(level.enabledFeatures()))return Optional.empty();
                recipe.getRemainingItems(input);return Optional.of(new CraftingPatternData(recipe.getId(),grid,output));
            });
        }catch(RuntimeException failure){LogUtils.getLogger().debug("Crafting pattern rejected an incompatible recipe",failure);return Optional.empty();}
    }
    public Optional<CraftingRecipe> resolve(Level level){
        try{
            var recipe=level.getRecipeManager().byKey(recipeId).orElse(null);
            if(!(recipe instanceof CraftingRecipe crafting)||crafting.isSpecial())return Optional.empty();
            var input=input();if(!crafting.matches(input,level))return Optional.empty();
            var actual=crafting.assemble(input,level.registryAccess());crafting.getRemainingItems(input);
            return ItemStack.matches(result,actual)&&actual.isItemEnabled(level.enabledFeatures())?Optional.of(crafting):Optional.empty();
        }catch(RuntimeException failure){LogUtils.getLogger().debug("Crafting pattern {} rejected an incompatible recipe",recipeId,failure);return Optional.empty();}
    }
    public CompoundTag save(){
        var tag=new CompoundTag();tag.putString("Recipe",recipeId.toString());var list=new ListTag();
        for(var stack:ingredients)list.add(stack.save(new CompoundTag()));tag.put("Ingredients",list);
        tag.put("Result",result.copyWithCount(1).save(new CompoundTag()));tag.putInt("ResultCount",result.getCount());return tag;
    }
    public static Optional<CraftingPatternData> load(CompoundTag tag){
        var id=ResourceLocation.tryParse(tag.getString("Recipe"));var grid=tag.getList("Ingredients",Tag.TAG_COMPOUND);
        if(id==null||grid.size()!=9||!tag.contains("ResultCount",Tag.TAG_INT)||tag.getInt("ResultCount")<=0)return Optional.empty();
        var output=ItemStack.of(tag.getCompound("Result"));if(output.isEmpty())return Optional.empty();
        var ingredients=new ArrayList<ItemStack>();for(int i=0;i<9;i++)ingredients.add(ItemStack.of(grid.getCompound(i)));
        return Optional.of(new CraftingPatternData(id,ingredients,output.copyWithCount(tag.getInt("ResultCount"))));
    }
    @Override public boolean equals(Object value){
        if(this==value)return true;if(!(value instanceof CraftingPatternData other)||!recipeId.equals(other.recipeId)||!ItemStack.matches(result,other.result))return false;
        for(int i=0;i<9;i++)if(!ItemStack.matches(ingredients.get(i),other.ingredients.get(i)))return false;return true;
    }
    @Override public int hashCode(){int hash=recipeId.hashCode();for(var s:ingredients)hash=31*hash+Objects.hash(s.getItem(),s.getTag(),s.getCount());return 31*hash+Objects.hash(result.getItem(),result.getTag(),result.getCount());}
}
