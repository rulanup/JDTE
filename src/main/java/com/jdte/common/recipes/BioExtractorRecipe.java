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

import java.util.List;
import java.util.Optional;

public record BioExtractorRecipe(ResourceLocation entityType,
                                 Optional<FluidStack> outputFluid,
                                 List<BioFactoryOutput> outputItems,
                                 int energy,
                                 int cooldown,
                                 float damage) implements Recipe<CraftingInput> {

    public boolean matches(EntityType<?> type) {
        return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.getFirst().stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.getFirst().stack().copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.BIO_EXTRACTOR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<BioExtractorRecipe> {
        private static final MapCodec<BioExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entity").forGetter(BioExtractorRecipe::entityType),
                FluidStack.CODEC.optionalFieldOf("output_fluid").forGetter(BioExtractorRecipe::outputFluid),
                BioFactoryOutput.CODEC.listOf().optionalFieldOf("output_items", List.of()).forGetter(BioExtractorRecipe::outputItems),
                Codec.INT.optionalFieldOf("energy", 1000).forGetter(BioExtractorRecipe::energy),
                Codec.INT.optionalFieldOf("cooldown", 600).forGetter(BioExtractorRecipe::cooldown),
                Codec.FLOAT.optionalFieldOf("damage", 0.0f).forGetter(BioExtractorRecipe::damage)
        ).apply(instance, BioExtractorRecipe::new));

        @Override
        public MapCodec<BioExtractorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BioExtractorRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public BioExtractorRecipe decode(RegistryFriendlyByteBuf buf) {
                    ResourceLocation entityType = ResourceLocation.STREAM_CODEC.decode(buf);
                    Optional<FluidStack> outputFluid = ByteBufCodecs.optional(FluidStack.STREAM_CODEC).decode(buf);
                    List<BioFactoryOutput> outputItems = BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                    int energy = ByteBufCodecs.VAR_INT.decode(buf);
                    int cooldown = ByteBufCodecs.VAR_INT.decode(buf);
                    float damage = ByteBufCodecs.FLOAT.decode(buf);
                    return new BioExtractorRecipe(entityType, outputFluid, outputItems, energy, cooldown, damage);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BioExtractorRecipe recipe) {
                    ResourceLocation.STREAM_CODEC.encode(buf, recipe.entityType());
                    ByteBufCodecs.optional(FluidStack.STREAM_CODEC).encode(buf, recipe.outputFluid());
                    BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.outputItems());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.energy());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.cooldown());
                    ByteBufCodecs.FLOAT.encode(buf, recipe.damage());
                }
            };
        }
    }
}
