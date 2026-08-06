package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record BioFactoryRecipe(Ingredient specimen, List<BioFactoryInput> inputs,
                               List<BioFactoryOutput> outputs,
                               Optional<ResourceLocation> processFluid, int processFluidAmount,
                               Optional<ResourceLocation> outputFluid, int outputFluidAmount,
                               int processTicks, int energy) implements Recipe<CraftingInput> {
    public int[] findMatchingSlots(ItemStack specimenStack, List<ItemStack> inputStacks) {
        if (!specimen.test(specimenStack) || inputs.size() > inputStacks.size()) return null;
        int[] assignment = new int[inputs.size()];
        java.util.Arrays.fill(assignment, -1);
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

    @Override public boolean matches(CraftingInput input, Level level) {
        if (input.size() < 1) return false;
        List<ItemStack> stacks = new java.util.ArrayList<>();
        for (int slot = 1; slot < input.size(); slot++) stacks.add(input.getItem(slot));
        return findMatchingSlots(input.getItem(0), stacks) != null;
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().stack().copy();
    }
    @Override public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1 + inputs.size();
    }
    @Override public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().stack().copy();
    }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.BIO_FACTORY_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<BioFactoryRecipe> {
        private static final MapCodec<BioFactoryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("specimen").forGetter(BioFactoryRecipe::specimen),
                Ingredient.CODEC.optionalFieldOf("food").forGetter(recipe -> Optional.empty()),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("food_count", 0)
                        .forGetter(recipe -> 0),
                BioFactoryInput.CODEC.listOf().optionalFieldOf("inputs", List.of())
                        .forGetter(BioFactoryRecipe::inputs),
                BioFactoryOutput.CODEC.listOf().fieldOf("outputs").forGetter(BioFactoryRecipe::outputs),
                ResourceLocation.CODEC.optionalFieldOf("process_fluid").forGetter(BioFactoryRecipe::processFluid),
                Codec.INT.optionalFieldOf("process_fluid_amount", 0).forGetter(BioFactoryRecipe::processFluidAmount),
                ResourceLocation.CODEC.optionalFieldOf("output_fluid").forGetter(BioFactoryRecipe::outputFluid),
                Codec.INT.optionalFieldOf("output_fluid_amount", 0).forGetter(BioFactoryRecipe::outputFluidAmount),
                Codec.INT.optionalFieldOf("process_ticks", 600).forGetter(BioFactoryRecipe::processTicks),
                Codec.INT.optionalFieldOf("energy", 1000).forGetter(BioFactoryRecipe::energy)
        ).apply(instance, Serializer::create));

        private static BioFactoryRecipe create(Ingredient specimen, Optional<Ingredient> legacyFood,
                                               int legacyFoodCount, List<BioFactoryInput> inputs,
                                               List<BioFactoryOutput> outputs,
                                               Optional<ResourceLocation> processFluid, int processFluidAmount,
                                               Optional<ResourceLocation> outputFluid, int outputFluidAmount,
                                               int processTicks, int energy) {
            List<BioFactoryInput> resolvedInputs = inputs.isEmpty() && legacyFood.isPresent()
                    ? List.of(new BioFactoryInput(legacyFood.get(), legacyFoodCount)) : List.copyOf(inputs);
            if (resolvedInputs.size() > 3) throw new IllegalArgumentException("Bio Factory recipes support at most 3 inputs");
            return new BioFactoryRecipe(specimen, resolvedInputs, outputs, processFluid, processFluidAmount,
                    outputFluid, outputFluidAmount, processTicks, energy);
        }

        @Override public MapCodec<BioFactoryRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, BioFactoryRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override public BioFactoryRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new BioFactoryRecipe(
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                            BioFactoryInput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                            BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer));
                }

                @Override public void encode(RegistryFriendlyByteBuf buffer, BioFactoryRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.specimen());
                    BioFactoryInput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.inputs());
                    BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.outputs());
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, recipe.processFluid());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.processFluidAmount());
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, recipe.outputFluid());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.outputFluidAmount());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.processTicks());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.energy());
                }
            };
        }
    }
}
