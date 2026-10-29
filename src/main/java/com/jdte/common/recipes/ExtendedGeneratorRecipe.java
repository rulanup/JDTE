package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Same shaped ingredients as source, preserving packed machine state instead of deleting it. */
public final class ExtendedGeneratorRecipe implements CraftingRecipe {
    private final ShapedRecipe delegate;
    public ExtendedGeneratorRecipe(ShapedRecipe delegate) { this.delegate=delegate; }
    private ItemStack source(CraftingContainer input) {
        for(int i=0;i<input.getContainerSize();i++) {
            var stack=input.getItem(i);
            if(stack.is(com.jdte.setup.JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_TIME_ACCELERATOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_LIFE_EXTRACTOR.get()) || stack.is(com.jdte.setup.JDTEItems.ADVANCED_BIO_EXTRACTOR.get()) || stack.is(com.direwolf20.justdirethings.setup.Registration.GeneratorT1.get().asItem())
                    || stack.is(com.direwolf20.justdirethings.setup.Registration.GeneratorFluidT1.get().asItem()))return stack;
        }
        return ItemStack.EMPTY;
    }
    @Override public boolean matches(CraftingContainer input,Level level) {
        if(!delegate.matches(input,level))return false;
        // Occupied inactive destination storage cannot be silently overwritten.
        var caps=(source(input).hasTag()?source(input).getTag():new CompoundTag()).getCompound("JustDiresBEData").getCompound("ForgeCaps").getCompound("jdte:machine_data");
        var old=source(input);var data=old.hasTag()?old.getTag().getCompound("JustDiresBEData"):new CompoundTag();
        if(old.is(com.jdte.setup.JDTEItems.ADVANCED_LIFE_EXTRACTOR.get()) && data.getDouble("pendingLifeFluid")!=0)return false;
        return caps.getCompound("extended_upgrade_handler").getList("Items",Tag.TAG_COMPOUND).isEmpty();
    }
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess access) {
        var result=delegate.assemble(input,access);var old=source(input);
        if(old.hasTag()) {
            result.setTag(old.getTag().copy());
            if(result.getTag().contains("JustDiresBEData",Tag.TAG_COMPOUND)) {
                var data=result.getTag().getCompound("JustDiresBEData");
                var all=data.getCompound("ForgeCaps");var caps=all.getCompound("jdte:machine_data");
                if(!result.is(com.jdte.setup.JDTEItems.ADVANCED_BIO_EXTRACTOR.get()) && !result.is(com.jdte.setup.JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get())) {
                    var expanded=caps.getCompound("upgrade_handler").copy();expanded.putInt("Size",8);
                    caps.put("extended_upgrade_handler",expanded);
                    var empty=new CompoundTag();empty.putInt("Size",4);caps.put("upgrade_handler",empty);
                }
                all.put("jdte:machine_data",caps);data.put("ForgeCaps",all);
                data.putString("id",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            }
        }
        return result;
    }
    @Override public boolean canCraftInDimensions(int w,int h) { return delegate.canCraftInDimensions(w,h); }
    @Override public ItemStack getResultItem(RegistryAccess access) { return delegate.getResultItem(access); }
    @Override public ResourceLocation getId() { return delegate.getId(); }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.EXTENDED_GENERATOR_RECIPE_SERIALIZER.get(); }
    @Override public CraftingBookCategory category() { return delegate.category(); }
    @Override public String getGroup() { return delegate.getGroup(); }
    @Override public NonNullList<Ingredient> getIngredients() { return delegate.getIngredients(); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) { return NonNullList.withSize(input.getContainerSize(),ItemStack.EMPTY); }
    public static final class Serializer implements RecipeSerializer<ExtendedGeneratorRecipe> {
        @Override public ExtendedGeneratorRecipe fromJson(ResourceLocation id,JsonObject json) { return new ExtendedGeneratorRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id,json)); }
        @Override public ExtendedGeneratorRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf) { return new ExtendedGeneratorRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id,buf)); }
        @Override public void toNetwork(FriendlyByteBuf buf,ExtendedGeneratorRecipe recipe) { RecipeSerializer.SHAPED_RECIPE.toNetwork(buf,recipe.delegate); }
    }
}
