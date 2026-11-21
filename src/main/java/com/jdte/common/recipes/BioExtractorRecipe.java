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

import java.util.List;
import java.util.Optional;

public record BioExtractorRecipe(Identifier entityType,
                                 Optional<FluidStackTemplate> fluidTemplate,
                                 List<BioFactoryOutput> outputItems,
                                 int energy,
                                 int cooldown,
                                 float damage) implements MachineRecipe {
    public Optional<FluidStack> outputFluid() {
        return fluidTemplate.map(FluidStackTemplate::create);
    }

    public boolean matches(EntityType<?> type) {
        return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.getFirst().stack().copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<BioExtractorRecipe> getType() {
        return JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<BioExtractorRecipe> getSerializer() {
        return JDTERecipes.BIO_EXTRACTOR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static RecipeSerializer<BioExtractorRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<BioExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("entity").forGetter(BioExtractorRecipe::entityType),
                FluidStackTemplate.CODEC.optionalFieldOf("output_fluid").forGetter(BioExtractorRecipe::fluidTemplate),
                BioFactoryOutput.CODEC.listOf().optionalFieldOf("output_items", List.of()).forGetter(BioExtractorRecipe::outputItems),
                Codec.INT.optionalFieldOf("energy", 1000).forGetter(BioExtractorRecipe::energy),
                Codec.INT.optionalFieldOf("cooldown", 600).forGetter(BioExtractorRecipe::cooldown),
                Codec.FLOAT.optionalFieldOf("damage", 0.0f).forGetter(BioExtractorRecipe::damage)
        ).apply(instance, BioExtractorRecipe::new));

        public static MapCodec<BioExtractorRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, BioExtractorRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public BioExtractorRecipe decode(RegistryFriendlyByteBuf buf) {
                    Identifier entityType = Identifier.STREAM_CODEC.decode(buf);
                    var outputFluid = ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC).decode(buf);
                    List<BioFactoryOutput> outputItems = BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                    int energy = ByteBufCodecs.VAR_INT.decode(buf);
                    int cooldown = ByteBufCodecs.VAR_INT.decode(buf);
                    float damage = ByteBufCodecs.FLOAT.decode(buf);
                    return new BioExtractorRecipe(entityType, outputFluid, outputItems, energy, cooldown, damage);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BioExtractorRecipe recipe) {
                    Identifier.STREAM_CODEC.encode(buf, recipe.entityType());
                    ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC).encode(buf, recipe.fluidTemplate());
                    BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.outputItems());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.energy());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.cooldown());
                    ByteBufCodecs.FLOAT.encode(buf, recipe.damage());
                }
            };
        }
    }
}
