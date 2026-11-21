package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record LootFabricatorRecipe(Ingredient input,
                                   List<BioFactoryOutput> outputs,
                                   Optional<Identifier> entityType,
                                   int lifeFluid,
                                   int timeFluid,
                                   int energy,
                                   int processTicks) implements MachineRecipe {

    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return input.size() > 0 && matches(input.getItem(0));
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.getFirst().stack().copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<LootFabricatorRecipe> getType() {
        return JDTERecipes.LOOT_FABRICATOR_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<LootFabricatorRecipe> getSerializer() {
        return JDTERecipes.LOOT_FABRICATOR_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer {
        public static RecipeSerializer<LootFabricatorRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<LootFabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input").forGetter(LootFabricatorRecipe::input),
                BioFactoryOutput.CODEC.listOf().fieldOf("outputs").forGetter(LootFabricatorRecipe::outputs),
                Identifier.CODEC.optionalFieldOf("entity_type").forGetter(LootFabricatorRecipe::entityType),
                Codec.INT.optionalFieldOf("life_fluid", 20).forGetter(LootFabricatorRecipe::lifeFluid),
                Codec.INT.optionalFieldOf("time_fluid", 1).forGetter(LootFabricatorRecipe::timeFluid),
                Codec.INT.optionalFieldOf("energy", 5000).forGetter(LootFabricatorRecipe::energy),
                Codec.INT.optionalFieldOf("process_ticks", 20).forGetter(LootFabricatorRecipe::processTicks)
        ).apply(instance, LootFabricatorRecipe::new));

        public static MapCodec<LootFabricatorRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, LootFabricatorRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public LootFabricatorRecipe decode(RegistryFriendlyByteBuf buf) {
                    Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    List<BioFactoryOutput> outputs = BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                    Optional<Identifier> entityType = ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buf);
                    int lifeFluid = ByteBufCodecs.VAR_INT.decode(buf);
                    int timeFluid = ByteBufCodecs.VAR_INT.decode(buf);
                    int energy = ByteBufCodecs.VAR_INT.decode(buf);
                    int processTicks = ByteBufCodecs.VAR_INT.decode(buf);
                    return new LootFabricatorRecipe(input, outputs, entityType, lifeFluid, timeFluid, energy, processTicks);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, LootFabricatorRecipe recipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
                    BioFactoryOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.outputs());
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, recipe.entityType());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.lifeFluid());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.timeFluid());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.energy());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.processTicks());
                }
            };
        }
    }
}
