package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/** The Forge 1.20.1 NBT/byte-buffer form of the JDTE infusion recipe. */
public class InfusionRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final ItemStack input;
    private final FluidStack fluidInput;
    private final ItemStack output;
    private final int energyCost;

    public InfusionRecipe(ResourceLocation id, ItemStack input, FluidStack fluidInput, ItemStack output, int energyCost) {
        this.id = id;
        this.input = input;
        this.fluidInput = fluidInput;
        this.output = output;
        this.energyCost = energyCost;
    }

    public boolean matches(ItemStack stack, FluidStack fluid) {
        return ItemStack.isSameItemSameTags(input, stack)
                && stack.getCount() >= input.getCount()
                && fluidInput.getFluid().isSame(fluid.getFluid())
                && fluid.getAmount() >= fluidInput.getAmount();
    }

    public ItemStack getInput() { return input.copy(); }
    public FluidStack getFluidInput() { return fluidInput.copy(); }
    public ItemStack getOutput() { return output.copy(); }
    public int getEnergyCost() { return energyCost; }

    @Override public boolean matches(CraftingContainer input, Level level) { return false; }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeType<?> getType() { return JDTERecipes.INFUSION_RECIPE_TYPE.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.INFUSION_RECIPE_SERIALIZER.get(); }
    @Override public CraftingBookCategory category() { return CraftingBookCategory.MISC; }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        @Override
        public InfusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ResourceLocation storedId = json.has("id")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "id")) : recipeId;
            return new InfusionRecipe(storedId, itemStack(GsonHelper.getAsJsonObject(json, "input")),
                    fluidStack(GsonHelper.getAsJsonObject(json, "fluid")),
                    itemStack(GsonHelper.getAsJsonObject(json, "output")),
                    GsonHelper.getAsInt(json, "energy"));
        }

        @Override
        public InfusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ItemStack input = buffer.readItem();
            FluidStack fluid = new FluidStack(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()), buffer.readVarInt());
            ItemStack output = buffer.readItem();
            return new InfusionRecipe(recipeId, input, fluid, output, buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, InfusionRecipe recipe) {
            buffer.writeItem(recipe.input);
            buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.fluidInput.getFluid()));
            buffer.writeVarInt(recipe.fluidInput.getAmount());
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.energyCost);
        }

        static ItemStack itemStack(JsonObject json) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(json, "id")));
            if (item == null) throw new IllegalArgumentException("Unknown item in JDTE recipe: " + json);
            return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
        }

        static FluidStack fluidStack(JsonObject json) {
            return new FluidStack(BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(json, "id"))),
                    GsonHelper.getAsInt(json, "amount"));
        }
    }
}
