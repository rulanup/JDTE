package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public record LifeExtractorRecipe(ResourceLocation entityType,
                                  FluidStack outputFluid,
                                  int energy) implements Recipe<CraftingInput> {

    public boolean matches(EntityType<?> type) {
        return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.LIFE_EXTRACTOR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.LIFE_EXTRACTOR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<LifeExtractorRecipe> {
        private static final MapCodec<LifeExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entity").forGetter(LifeExtractorRecipe::entityType),
                FluidStack.CODEC.fieldOf("output_fluid").forGetter(LifeExtractorRecipe::outputFluid),
                Codec.INT.optionalFieldOf("energy", 300).forGetter(LifeExtractorRecipe::energy)
        ).apply(instance, LifeExtractorRecipe::new));

        @Override
        public MapCodec<LifeExtractorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LifeExtractorRecipe> streamCodec() {
            return StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, LifeExtractorRecipe::entityType,
                    FluidStack.STREAM_CODEC, LifeExtractorRecipe::outputFluid,
                    ByteBufCodecs.VAR_INT, LifeExtractorRecipe::energy,
                    LifeExtractorRecipe::new
            );
        }
    }
}
