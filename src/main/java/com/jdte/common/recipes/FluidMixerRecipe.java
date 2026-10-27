package com.jdte.common.recipes;

import com.google.gson.*;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import java.util.Optional;

/** Forge form of the source fluid recipe; item assembly is empty because the result is fluid. */
public record FluidMixerRecipe(FluidStack fluidInputA,FluidStack fluidInputB,Optional<InputItem> itemInput,
                               FluidStack output,int processTicks,int energy,ResourceLocation id) implements Recipe<CraftingContainer> {
    public FluidMixerRecipe {
        if(fluidInputA.isEmpty() || output.isEmpty() || processTicks<=0 || energy<=0)throw new IllegalArgumentException("Invalid fluid mixer recipe: "+id);
        fluidInputA=fluidInputA.copy();fluidInputB=fluidInputB.copy();output=output.copy();
    }
    private boolean direct(FluidStack a,FluidStack b) { return a.getFluid()==fluidInputA.getFluid() && b.getFluid()==fluidInputB.getFluid(); }
    public boolean matchesFluids(FluidStack a,FluidStack b,ItemStack catalyst) {
        if(!direct(a,b) && !(a.getFluid()==fluidInputB.getFluid() && b.getFluid()==fluidInputA.getFluid()))return false;
        return itemInput.isEmpty() || itemInput.get().ingredient().test(catalyst) && catalyst.getCount()>=itemInput.get().count();
    }
    public boolean hasEnoughFluids(FluidStack a,FluidStack b) { return a.getAmount()>=getDrainA(a) && b.getAmount()>=getDrainB(b); }
    public int getDrainA(FluidStack a) { return a.getFluid()==fluidInputA.getFluid()?fluidInputA.getAmount():fluidInputB.getAmount(); }
    public int getDrainB(FluidStack b) { return b.getFluid()==fluidInputB.getFluid()?fluidInputB.getAmount():fluidInputA.getAmount(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.FLUID_MIXER_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get(); }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean matches(CraftingContainer input,Level level) { return false; }
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int x,int y) { return false; }
    public record InputItem(Ingredient ingredient,int count) {
        public InputItem { if(ingredient.isEmpty() || count<=0)throw new IllegalArgumentException("Invalid mixer catalyst"); }
    }
    public static final class Serializer implements RecipeSerializer<FluidMixerRecipe> {
        private static FluidStack fluid(JsonObject json) {
            var key=ResourceLocation.tryParse(GsonHelper.getAsString(json,"id"));
            int amount=GsonHelper.getAsInt(json,"amount");
            if(key==null || !BuiltInRegistries.FLUID.containsKey(key) || amount<=0)throw new JsonSyntaxException("Invalid mixer fluid: "+json);
            var stack=new FluidStack(BuiltInRegistries.FLUID.get(key),amount);
            if(stack.isEmpty())throw new JsonSyntaxException("Empty mixer fluid: "+json);return stack;
        }
        @Override public FluidMixerRecipe fromJson(ResourceLocation id,JsonObject json) {
            Optional<InputItem> item=Optional.empty();
            if(json.has("item_input")){var input=GsonHelper.getAsJsonObject(json,"item_input");item=Optional.of(new InputItem(Ingredient.fromJson(input.get("ingredient")),GsonHelper.getAsInt(input,"count")));}
            return new FluidMixerRecipe(fluid(GsonHelper.getAsJsonObject(json,"fluid_input_a")),json.has("fluid_input_b")?fluid(GsonHelper.getAsJsonObject(json,"fluid_input_b")):FluidStack.EMPTY,
                    item,fluid(GsonHelper.getAsJsonObject(json,"output")),GsonHelper.getAsInt(json,"process_ticks"),GsonHelper.getAsInt(json,"energy"),id);
        }
        @Override public FluidMixerRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buffer) {
            var a=buffer.readFluidStack();var b=buffer.readFluidStack();Optional<InputItem> item=buffer.readBoolean()?Optional.of(new InputItem(Ingredient.fromNetwork(buffer),buffer.readVarInt())):Optional.empty();
            return new FluidMixerRecipe(a,b,item,buffer.readFluidStack(),buffer.readVarInt(),buffer.readVarInt(),id);
        }
        @Override public void toNetwork(FriendlyByteBuf buffer,FluidMixerRecipe recipe) {
            buffer.writeFluidStack(recipe.fluidInputA());buffer.writeFluidStack(recipe.fluidInputB());buffer.writeBoolean(recipe.itemInput().isPresent());
            recipe.itemInput().ifPresent(item->{item.ingredient().toNetwork(buffer);buffer.writeVarInt(item.count());});
            buffer.writeFluidStack(recipe.output());buffer.writeVarInt(recipe.processTicks());buffer.writeVarInt(recipe.energy());
        }
    }
}
