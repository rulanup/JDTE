package com.jdte.common.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

public record LifeSynthesisRecipe(List<InputSlot> inputs, List<FluidStack> fluidInputs,
                                  FluidStack output, int processTicks, int energy,
                                  String tier, ResourceLocation id) implements Recipe<CraftingContainer> {
    public FluidStack nutrient() { return fluidInputs.isEmpty() ? FluidStack.EMPTY : fluidInputs.get(0); }

    public boolean matchesSlots(List<ItemStack> slots) {
        return matchesStrict(inputs, slots,
                (input, stack) -> input.ingredient().test(stack), InputSlot::count, ItemStack::getCount);
    }

    public boolean consumeStrict(List<ItemStack> slots) {
        return consumeStrict(inputs, slots,
                (input, stack) -> input.ingredient().test(stack), InputSlot::count, ItemStack::getCount,
                (stack, amount) -> stack.shrink(amount));
    }

    static <R, S> boolean matchesStrict(List<R> requirements, List<S> slots,
                                        BiPredicate<R, S> matches, ToIntFunction<R> requiredCount,
                                        ToIntFunction<S> slotCount) {
        return allocateStrict(requirements, slots, matches, requiredCount, slotCount) != null;
    }

    static <R, S> boolean consumeStrict(List<R> requirements, List<S> slots,
                                        BiPredicate<R, S> matches, ToIntFunction<R> requiredCount,
                                        ToIntFunction<S> slotCount, BiConsumer<S, Integer> consume) {
        int[] allocations = allocateStrict(requirements, slots, matches, requiredCount, slotCount);
        if (allocations == null) {
            return false;
        }
        for (int index = 0; index < allocations.length; index++) {
            if (allocations[index] > 0) {
                consume.accept(slots.get(index), allocations[index]);
            }
        }
        return true;
    }

    private static <R, S> int[] allocateStrict(List<R> requirements, List<S> slots,
                                                 BiPredicate<R, S> matches, ToIntFunction<R> requiredCount,
                                                 ToIntFunction<S> slotCount) {
        boolean[] used = new boolean[slots.size()];
        int[] allocations = new int[slots.size()];
        for (R requirement : requirements) {
            int needed = Math.max(0, requiredCount.applyAsInt(requirement));
            for (int index = 0; index < slots.size() && needed > 0; index++) {
                S stack = slots.get(index);
                if (used[index] || !matches.test(requirement, stack)) {
                    continue;
                }
                used[index] = true;
                int amount = Math.min(needed, Math.max(0, slotCount.applyAsInt(stack)));
                allocations[index] = amount;
                needed -= amount;
            }
            if (needed > 0) {
                return null;
            }
        }
        return allocations;
    }

    @Override public boolean matches(CraftingContainer input, Level level) { return false; }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.LIFE_SYNTHESIS_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get(); }

    public record InputSlot(Ingredient ingredient, int count) {
        static InputSlot fromJson(JsonObject json) {
            return new InputSlot(Ingredient.fromJson(json.get("ingredient")), GsonHelper.getAsInt(json, "count"));
        }
        static InputSlot fromNetwork(FriendlyByteBuf buffer) {
            return new InputSlot(Ingredient.fromNetwork(buffer), buffer.readVarInt());
        }
        void toNetwork(FriendlyByteBuf buffer) {
            ingredient.toNetwork(buffer);
            buffer.writeVarInt(count);
        }
    }

    public static final class Serializer implements RecipeSerializer<LifeSynthesisRecipe> {
        @Override
        public LifeSynthesisRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            List<InputSlot> inputs = new ArrayList<>();
            for (var value : GsonHelper.getAsJsonArray(json, "inputs")) inputs.add(InputSlot.fromJson(value.getAsJsonObject()));
            List<FluidStack> fluidInputs = fluidList(GsonHelper.getAsJsonArray(json, "fluid_inputs"));
            return new LifeSynthesisRecipe(List.copyOf(inputs), List.copyOf(fluidInputs),
                    InfusionRecipe.Serializer.fluidStack(GsonHelper.getAsJsonObject(json, "output")),
                    GsonHelper.getAsInt(json, "process_ticks"), GsonHelper.getAsInt(json, "energy"),
                    GsonHelper.getAsString(json, "tier"), recipeId);
        }

        @Override
        public LifeSynthesisRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int inputCount = buffer.readVarInt();
            List<InputSlot> inputs = new ArrayList<>(inputCount);
            for (int index = 0; index < inputCount; index++) inputs.add(InputSlot.fromNetwork(buffer));
            int fluidCount = buffer.readVarInt();
            List<FluidStack> fluids = new ArrayList<>(fluidCount);
            for (int index = 0; index < fluidCount; index++) fluids.add(readFluid(buffer));
            return new LifeSynthesisRecipe(List.copyOf(inputs), List.copyOf(fluids), readFluid(buffer),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(), recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LifeSynthesisRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(input -> input.toNetwork(buffer));
            buffer.writeVarInt(recipe.fluidInputs.size());
            recipe.fluidInputs.forEach(fluid -> writeFluid(buffer, fluid));
            writeFluid(buffer, recipe.output);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energy);
            buffer.writeUtf(recipe.tier);
        }

        private static List<FluidStack> fluidList(JsonArray json) {
            List<FluidStack> fluids = new ArrayList<>(json.size());
            json.forEach(value -> fluids.add(InfusionRecipe.Serializer.fluidStack(value.getAsJsonObject())));
            return fluids;
        }
        private static FluidStack readFluid(FriendlyByteBuf buffer) {
            return new FluidStack(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()), buffer.readVarInt());
        }
        private static void writeFluid(FriendlyByteBuf buffer, FluidStack fluid) {
            buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
            buffer.writeVarInt(fluid.getAmount());
        }
    }
}
