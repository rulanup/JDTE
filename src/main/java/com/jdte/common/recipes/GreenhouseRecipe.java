package com.jdte.common.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record GreenhouseRecipe(Ingredient seed, List<ItemStack> outputs, ResourceLocation displayBlock,
                               Optional<ResourceLocation> harvestBlock, boolean useLootTable,
                               int growthWork, int timeFluid, ResourceLocation id) implements Recipe<CraftingContainer> {
    public boolean matchesSeed(ItemStack stack) { return seed.test(stack); }
    @Override public boolean matches(CraftingContainer input, Level level) { return input.getContainerSize() > 0 && matchesSeed(input.getItem(0)); }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) { return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.GREENHOUSE_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return JDTERecipes.GREENHOUSE_RECIPE_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<GreenhouseRecipe> {
        @Override
        public GreenhouseRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient seed = Ingredient.fromJson(json.get("seed"));
            JsonArray outputJson = GsonHelper.getAsJsonArray(json, "outputs");
            List<ItemStack> outputs = new ArrayList<>(outputJson.size());
            outputJson.forEach(element -> outputs.add(InfusionRecipe.Serializer.itemStack(element.getAsJsonObject())));
            Optional<ResourceLocation> harvest = json.has("harvest_block")
                    ? Optional.of(new ResourceLocation(GsonHelper.getAsString(json, "harvest_block"))) : Optional.empty();
            return new GreenhouseRecipe(seed, List.copyOf(outputs),
                    new ResourceLocation(GsonHelper.getAsString(json, "display_block")), harvest,
                    GsonHelper.getAsBoolean(json, "use_loot_table", true),
                    GsonHelper.getAsInt(json, "growth_work"), GsonHelper.getAsInt(json, "time_fluid"), recipeId);
        }

        @Override
        public GreenhouseRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient seed = Ingredient.fromNetwork(buffer);
            int outputCount = buffer.readVarInt();
            List<ItemStack> outputs = new ArrayList<>(outputCount);
            for (int index = 0; index < outputCount; index++) outputs.add(buffer.readItem());
            ResourceLocation display = buffer.readResourceLocation();
            Optional<ResourceLocation> harvest = buffer.readBoolean() ? Optional.of(buffer.readResourceLocation()) : Optional.empty();
            return new GreenhouseRecipe(seed, List.copyOf(outputs), display, harvest, buffer.readBoolean(),
                    buffer.readVarInt(), buffer.readVarInt(), recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GreenhouseRecipe recipe) {
            recipe.seed.toNetwork(buffer);
            buffer.writeVarInt(recipe.outputs.size());
            recipe.outputs.forEach(buffer::writeItem);
            buffer.writeResourceLocation(recipe.displayBlock);
            buffer.writeBoolean(recipe.harvestBlock.isPresent());
            recipe.harvestBlock.ifPresent(buffer::writeResourceLocation);
            buffer.writeBoolean(recipe.useLootTable);
            buffer.writeVarInt(recipe.growthWork);
            buffer.writeVarInt(recipe.timeFluid);
        }
    }
}
