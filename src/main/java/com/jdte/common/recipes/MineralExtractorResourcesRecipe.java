package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Settings recipe that selects the mineral extractor's two distinct resource fluids. */
public record MineralExtractorResourcesRecipe(
        ResourceLocation fortuneFluid,
        ResourceLocation accelerationFluid
) implements Recipe<CraftingInput> {
    public MineralExtractorResourcesRecipe {
        if (fortuneFluid.equals(accelerationFluid)) {
            throw new IllegalArgumentException("fortune_fluid and acceleration_fluid must differ");
        }
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
        return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<MineralExtractorResourcesRecipe> {
        private static final MapCodec<UnvalidatedResources> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("fortune_fluid").forGetter(UnvalidatedResources::fortuneFluid),
                ResourceLocation.CODEC.fieldOf("acceleration_fluid").forGetter(UnvalidatedResources::accelerationFluid)
        ).apply(instance, UnvalidatedResources::new));
        private static final MapCodec<MineralExtractorResourcesRecipe> CODEC = RAW_CODEC.flatXmap(
                resources -> validated(resources.fortuneFluid(), resources.accelerationFluid()),
                recipe -> DataResult.success(new UnvalidatedResources(recipe.fortuneFluid(), recipe.accelerationFluid())));

        private static DataResult<MineralExtractorResourcesRecipe> validated(ResourceLocation fortuneFluid,
                                                                               ResourceLocation accelerationFluid) {
            if (fortuneFluid.equals(accelerationFluid)) {
                return DataResult.error(() -> "fortune_fluid and acceleration_fluid must differ");
            }
            return DataResult.success(new MineralExtractorResourcesRecipe(fortuneFluid, accelerationFluid));
        }

        @Override
        public MapCodec<MineralExtractorResourcesRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MineralExtractorResourcesRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public MineralExtractorResourcesRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return validated(ResourceLocation.STREAM_CODEC.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer))
                            .getOrThrow();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MineralExtractorResourcesRecipe recipe) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, recipe.fortuneFluid());
                    ResourceLocation.STREAM_CODEC.encode(buffer, recipe.accelerationFluid());
                }
            };
        }

        private record UnvalidatedResources(ResourceLocation fortuneFluid, ResourceLocation accelerationFluid) {
        }
    }
}
