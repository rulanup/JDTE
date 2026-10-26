package com.jdte.common.recipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import net.minecraft.world.level.material.Fluids;
public record MineralExtractorResourcesRecipe(ResourceLocation id, ResourceLocation fortuneFluid,
        ResourceLocation accelerationFluid) implements Recipe<CraftingContainer> {
    public MineralExtractorResourcesRecipe {
        if (fortuneFluid.equals(accelerationFluid)) throw new IllegalArgumentException("Resource fluids must differ");
        for (ResourceLocation fluid : java.util.List.of(fortuneFluid, accelerationFluid)) {
            if (!BuiltInRegistries.FLUID.containsKey(fluid) || BuiltInRegistries.FLUID.get(fluid) == Fluids.EMPTY
                    || !BuiltInRegistries.FLUID.get(fluid).defaultFluidState().isSource())
                throw new IllegalArgumentException("Expected registered source fluid: " + fluid);
        }
    }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean matches(CraftingContainer input, Level level) { return false; }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) { return ItemStack.EMPTY; }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return false; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeType<?> getType() { return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_TYPE.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_SERIALIZER.get(); }
    public static final class Serializer implements RecipeSerializer<MineralExtractorResourcesRecipe> {
        @Override public MineralExtractorResourcesRecipe fromJson(ResourceLocation id, JsonObject json) {
            try { return new MineralExtractorResourcesRecipe(id, new ResourceLocation(GsonHelper.getAsString(json,"fortune_fluid")),
                    new ResourceLocation(GsonHelper.getAsString(json,"acceleration_fluid"))); }
            catch (IllegalArgumentException e) { throw new JsonParseException("Invalid mineral resources recipe " + id, e); }
        }
        @Override public MineralExtractorResourcesRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MineralExtractorResourcesRecipe(id, buf.readResourceLocation(), buf.readResourceLocation());
        }
        @Override public void toNetwork(FriendlyByteBuf buf, MineralExtractorResourcesRecipe recipe) {
            buf.writeResourceLocation(recipe.fortuneFluid()); buf.writeResourceLocation(recipe.accelerationFluid());
        }
    }
}
