package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public record GreenhouseRecipe(Ingredient seed, java.util.List<ItemStackTemplate> outputTemplates, Identifier displayBlock,
                               Optional<Identifier> harvestBlock, boolean useLootTable,
                               int growthWork, Identifier fluid, int timeFluid) implements MachineRecipe {
    public static final Identifier DEFAULT_FLUID =
            Identifier.fromNamespaceAndPath("justdirethings", "time_fluid_source");

    public boolean matchesSeed(ItemStack stack) {
        return seed.test(stack);
    }

    public java.util.List<ItemStack> outputs() {
        return outputTemplates.stream().map(ItemStackTemplate::create).toList();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return input.size() > 0 && matchesSeed(input.getItem(0));
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return outputTemplates.isEmpty() ? ItemStack.EMPTY : outputTemplates.getFirst().create();
    }

    @Override
    public RecipeSerializer<GreenhouseRecipe> getSerializer() {
        return JDTERecipes.GREENHOUSE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<GreenhouseRecipe> getType() {
        return JDTERecipes.GREENHOUSE_RECIPE_TYPE.get();
    }

    public static final class Serializer {
        public static RecipeSerializer<GreenhouseRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final Codec<Identifier> SOURCE_FLUID_ID_CODEC =
                Identifier.CODEC.validate(Serializer::validateSourceFluid);
        private static final MapCodec<GreenhouseRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("seed").forGetter(GreenhouseRecipe::seed),
                ItemStackTemplate.CODEC.listOf().fieldOf("outputs").forGetter(GreenhouseRecipe::outputTemplates),
                Identifier.CODEC.fieldOf("display_block").forGetter(GreenhouseRecipe::displayBlock),
                Identifier.CODEC.optionalFieldOf("harvest_block").forGetter(GreenhouseRecipe::harvestBlock),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("use_loot_table", true).forGetter(GreenhouseRecipe::useLootTable),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("growth_work").forGetter(GreenhouseRecipe::growthWork),
                SOURCE_FLUID_ID_CODEC.optionalFieldOf("fluid", DEFAULT_FLUID).forGetter(GreenhouseRecipe::fluid),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("time_fluid").forGetter(GreenhouseRecipe::timeFluid)
        ).apply(instance, GreenhouseRecipe::new));

        private static DataResult<Identifier> validateSourceFluid(Identifier id) {
            if (!BuiltInRegistries.FLUID.containsKey(id)) {
                return DataResult.error(() -> "Unknown greenhouse recipe fluid: " + id);
            }
            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            if (fluid == Fluids.EMPTY) {
                return DataResult.error(() -> "Greenhouse recipe fluid cannot be empty: " + id);
            }
            if (!fluid.defaultFluidState().isSource()) {
                return DataResult.error(() -> "Greenhouse recipe fluid must be a source fluid: " + id);
            }
            return DataResult.success(id);
        }

        public static MapCodec<GreenhouseRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, GreenhouseRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public GreenhouseRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new GreenhouseRecipe(
                            Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                            Identifier.STREAM_CODEC.decode(buffer),
                            ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buffer),
                            ByteBufCodecs.BOOL.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            Identifier.STREAM_CODEC.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GreenhouseRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.seed());
                    ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.outputTemplates());
                    Identifier.STREAM_CODEC.encode(buffer, recipe.displayBlock());
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buffer, recipe.harvestBlock());
                    ByteBufCodecs.BOOL.encode(buffer, recipe.useLootTable());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.growthWork());
                    Identifier.STREAM_CODEC.encode(buffer, recipe.fluid());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.timeFluid());
                }
            };
        }
    }
}
