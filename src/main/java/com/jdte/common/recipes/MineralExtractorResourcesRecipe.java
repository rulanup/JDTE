package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Settings recipe that selects the mineral extractor's two distinct resource fluids. */
public record MineralExtractorResourcesRecipe(
        Identifier fortuneFluid,
        Identifier accelerationFluid
) implements MachineRecipe {
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
    public ItemStack assemble(CraftingInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<MineralExtractorResourcesRecipe> getType() {
        return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<MineralExtractorResourcesRecipe> getSerializer() {
        return JDTERecipes.MINERAL_EXTRACTOR_RESOURCES_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static RecipeSerializer<MineralExtractorResourcesRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<UnvalidatedResources> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("fortune_fluid").forGetter(UnvalidatedResources::fortuneFluid),
                Identifier.CODEC.fieldOf("acceleration_fluid").forGetter(UnvalidatedResources::accelerationFluid)
        ).apply(instance, UnvalidatedResources::new));
        private static final MapCodec<MineralExtractorResourcesRecipe> CODEC = RAW_CODEC.flatXmap(
                resources -> validated(resources.fortuneFluid(), resources.accelerationFluid()),
                recipe -> DataResult.success(new UnvalidatedResources(recipe.fortuneFluid(), recipe.accelerationFluid())));

        private static DataResult<MineralExtractorResourcesRecipe> validated(Identifier fortuneFluid,
                                                                               Identifier accelerationFluid) {
            String fortuneError = sourceFluidError("fortune_fluid", fortuneFluid);
            if (fortuneError != null) {
                return DataResult.error(() -> fortuneError);
            }
            String accelerationError = sourceFluidError("acceleration_fluid", accelerationFluid);
            if (accelerationError != null) {
                return DataResult.error(() -> accelerationError);
            }
            if (fortuneFluid.equals(accelerationFluid)) {
                return DataResult.error(() -> "fortune_fluid and acceleration_fluid must differ");
            }
            return DataResult.success(new MineralExtractorResourcesRecipe(fortuneFluid, accelerationFluid));
        }

        private static String sourceFluidError(String field, Identifier id) {
            if (!BuiltInRegistries.FLUID.containsKey(id)) {
                return "Unknown " + field + ": " + id;
            }
            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            if (fluid == Fluids.EMPTY) {
                return field + " cannot be empty: " + id;
            }
            if (!fluid.defaultFluidState().isSource()) {
                return field + " must reference a source fluid: " + id;
            }
            return null;
        }

        public static MapCodec<MineralExtractorResourcesRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, MineralExtractorResourcesRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public MineralExtractorResourcesRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return validated(Identifier.STREAM_CODEC.decode(buffer), Identifier.STREAM_CODEC.decode(buffer))
                            .getOrThrow();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MineralExtractorResourcesRecipe recipe) {
                    Identifier.STREAM_CODEC.encode(buffer, recipe.fortuneFluid());
                    Identifier.STREAM_CODEC.encode(buffer, recipe.accelerationFluid());
                }
            };
        }

        private record UnvalidatedResources(Identifier fortuneFluid, Identifier accelerationFluid) {
        }
    }
}
