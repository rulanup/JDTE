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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record BioFactoryRecipe(Ingredient specimen, List<BioFactoryInput> inputs, List<BioFactoryOutput> outputs,
                               Optional<ResourceLocation> processFluid, int processFluidAmount,
                               Optional<ResourceLocation> outputFluid, int outputFluidAmount,
                               int processTicks, int energy, ResourceLocation id) implements Recipe<CraftingContainer> {
    public int[] findMatchingSlots(ItemStack specimenStack, List<ItemStack> inputStacks) {
        if (!specimen.test(specimenStack) || inputs.size() > inputStacks.size()) return null;
        int[] assignment = new int[inputs.size()];
        Arrays.fill(assignment, -1);
        return assignInput(0, inputStacks, assignment, new boolean[inputStacks.size()]) ? assignment : null;
    }

    private boolean assignInput(int inputIndex, List<ItemStack> stacks, int[] assignment, boolean[] used) {
        if (inputIndex >= inputs.size()) return true;
        BioFactoryInput input = inputs.get(inputIndex);
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (used[slot] || !input.ingredient().test(stack) || stack.getCount() < input.count()) continue;
            used[slot] = true;
            assignment[inputIndex] = slot;
            if (assignInput(inputIndex + 1, stacks, assignment, used)) return true;
            used[slot] = false;
            assignment[inputIndex] = -1;
        }
        return false;
    }

    @Override public boolean matches(CraftingContainer input, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 1; slot < input.getContainerSize(); slot++) stacks.add(input.getItem(slot));
        return input.getContainerSize() > 0 && findMatchingSlots(input.getItem(0), stacks) != null;
    }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) { return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1 + inputs.size(); }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.BIO_FACTORY_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<BioFactoryRecipe> {
        @Override
        public BioFactoryRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient specimen = Ingredient.fromJson(json.get("specimen"));
            List<BioFactoryInput> inputs = new ArrayList<>();
            if (json.has("inputs")) {
                for (var value : GsonHelper.getAsJsonArray(json, "inputs")) inputs.add(BioFactoryInput.fromJson(value.getAsJsonObject()));
            } else if (json.has("food")) {
                inputs.add(new BioFactoryInput(Ingredient.fromJson(json.get("food")), GsonHelper.getAsInt(json, "food_count", 1)));
            }
            if (inputs.size() > 3) throw new IllegalArgumentException("Bio Factory recipes support at most 3 inputs: " + recipeId);
            List<BioFactoryOutput> outputs = new ArrayList<>();
            for (var value : GsonHelper.getAsJsonArray(json, "outputs")) outputs.add(BioFactoryOutput.fromJson(value.getAsJsonObject()));
            return new BioFactoryRecipe(specimen, List.copyOf(inputs), List.copyOf(outputs), optionalId(json, "process_fluid"),
                    GsonHelper.getAsInt(json, "process_fluid_amount", 0), optionalId(json, "output_fluid"),
                    GsonHelper.getAsInt(json, "output_fluid_amount", 0), GsonHelper.getAsInt(json, "process_ticks", 600),
                    GsonHelper.getAsInt(json, "energy", 1000), recipeId);
        }

        @Override
        public BioFactoryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient specimen = Ingredient.fromNetwork(buffer);
            List<BioFactoryInput> inputs = readList(buffer, BioFactoryInput::fromNetwork);
            List<BioFactoryOutput> outputs = readList(buffer, BioFactoryOutput::fromNetwork);
            Optional<ResourceLocation> processFluid = readOptionalId(buffer);
            int processFluidAmount = buffer.readVarInt();
            Optional<ResourceLocation> outputFluid = readOptionalId(buffer);
            return new BioFactoryRecipe(specimen, inputs, outputs, processFluid, processFluidAmount, outputFluid,
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BioFactoryRecipe recipe) {
            recipe.specimen.toNetwork(buffer);
            writeList(buffer, recipe.inputs, BioFactoryInput::toNetwork);
            writeList(buffer, recipe.outputs, BioFactoryOutput::toNetwork);
            writeOptionalId(buffer, recipe.processFluid);
            buffer.writeVarInt(recipe.processFluidAmount);
            writeOptionalId(buffer, recipe.outputFluid);
            buffer.writeVarInt(recipe.outputFluidAmount);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energy);
        }

        private static Optional<ResourceLocation> optionalId(JsonObject json, String key) {
            return json.has(key) ? Optional.of(new ResourceLocation(GsonHelper.getAsString(json, key))) : Optional.empty();
        }
        private static Optional<ResourceLocation> readOptionalId(FriendlyByteBuf buffer) {
            return buffer.readBoolean() ? Optional.of(buffer.readResourceLocation()) : Optional.empty();
        }
        private static void writeOptionalId(FriendlyByteBuf buffer, Optional<ResourceLocation> id) {
            buffer.writeBoolean(id.isPresent());
            id.ifPresent(buffer::writeResourceLocation);
        }
        private static <T> List<T> readList(FriendlyByteBuf buffer, java.util.function.Function<FriendlyByteBuf, T> reader) {
            int size = buffer.readVarInt();
            List<T> values = new ArrayList<>(size);
            for (int index = 0; index < size; index++) values.add(reader.apply(buffer));
            return List.copyOf(values);
        }
        private static <T> void writeList(FriendlyByteBuf buffer, List<T> values,
                                          java.util.function.BiConsumer<T, FriendlyByteBuf> writer) {
            buffer.writeVarInt(values.size());
            values.forEach(value -> writer.accept(value, buffer));
        }
    }
}
