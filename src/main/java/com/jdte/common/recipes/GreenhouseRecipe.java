package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
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

import java.util.Optional;

public record GreenhouseRecipe(Ingredient seed, java.util.List<ItemStack> outputs, ResourceLocation displayBlock,
                               Optional<ResourceLocation> harvestBlock, boolean useLootTable,
                               int growthWork, int timeFluid) implements Recipe<CraftingInput> {
    public boolean matchesSeed(ItemStack stack) {
        return seed.test(stack);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return input.size() > 0 && matchesSeed(input.getItem(0));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.GREENHOUSE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.GREENHOUSE_RECIPE_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<GreenhouseRecipe> {
        private static final MapCodec<GreenhouseRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("seed").forGetter(GreenhouseRecipe::seed),
                ItemStack.CODEC.listOf().fieldOf("outputs").forGetter(GreenhouseRecipe::outputs),
                ResourceLocation.CODEC.fieldOf("display_block").forGetter(GreenhouseRecipe::displayBlock),
                ResourceLocation.CODEC.optionalFieldOf("harvest_block").forGetter(GreenhouseRecipe::harvestBlock),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("use_loot_table", true).forGetter(GreenhouseRecipe::useLootTable),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("growth_work").forGetter(GreenhouseRecipe::growthWork),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("time_fluid").forGetter(GreenhouseRecipe::timeFluid)
        ).apply(instance, GreenhouseRecipe::new));

        @Override
        public MapCodec<GreenhouseRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GreenhouseRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public GreenhouseRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new GreenhouseRecipe(
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                            ItemStack.LIST_STREAM_CODEC.decode(buffer),
                            ResourceLocation.STREAM_CODEC.decode(buffer),
                            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer),
                            ByteBufCodecs.BOOL.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GreenhouseRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.seed());
                    ItemStack.LIST_STREAM_CODEC.encode(buffer, recipe.outputs());
                    ResourceLocation.STREAM_CODEC.encode(buffer, recipe.displayBlock());
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, recipe.harvestBlock());
                    ByteBufCodecs.BOOL.encode(buffer, recipe.useLootTable());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.growthWork());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.timeFluid());
                }
            };
        }
    }
}
