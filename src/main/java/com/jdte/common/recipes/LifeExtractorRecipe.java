package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public record LifeExtractorRecipe(Identifier entityType,
                                  FluidStackTemplate fluidTemplate,
                                  int energy) implements MachineRecipe {
    public LifeExtractorRecipe(Identifier entityType, FluidStack fluid, int energy) {
        this(entityType, FluidStackTemplate.fromNonEmptyStack(fluid), energy);
    }

    public FluidStack outputFluid() { return fluidTemplate.create(); }

    public boolean matches(EntityType<?> type) {
        return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
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
    public RecipeType<LifeExtractorRecipe> getType() {
        return JDTERecipes.LIFE_EXTRACTOR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<LifeExtractorRecipe> getSerializer() {
        return JDTERecipes.LIFE_EXTRACTOR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static RecipeSerializer<LifeExtractorRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<LifeExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("entity").forGetter(LifeExtractorRecipe::entityType),
                FluidStackTemplate.CODEC.fieldOf("output_fluid").forGetter(LifeExtractorRecipe::fluidTemplate),
                Codec.INT.optionalFieldOf("energy", 300).forGetter(LifeExtractorRecipe::energy)
        ).apply(instance, LifeExtractorRecipe::new));

        public static MapCodec<LifeExtractorRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, LifeExtractorRecipe> streamCodec() {
            return StreamCodec.composite(
                    Identifier.STREAM_CODEC, LifeExtractorRecipe::entityType,
                    FluidStackTemplate.STREAM_CODEC, LifeExtractorRecipe::fluidTemplate,
                    ByteBufCodecs.VAR_INT, LifeExtractorRecipe::energy,
                    LifeExtractorRecipe::new
            );
        }
    }
}
