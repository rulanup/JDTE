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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.Optional;

public class InfusionRecipe implements MachineRecipe {
    private final Identifier id;
    private final Ingredient input;
    private final int inputCount;
    private final FluidStackTemplate fluidInput;
    private final ItemStackTemplate output;
    private final int energyCost;

    public InfusionRecipe(Identifier id, Ingredient input, int inputCount, FluidStack fluidInput, ItemStack output, int energyCost) {
        this(id, input, inputCount, FluidStackTemplate.fromNonEmptyStack(fluidInput), ItemStackTemplate.fromNonEmptyStack(output), energyCost);
    }

    private InfusionRecipe(Identifier id, Ingredient input, int inputCount, FluidStackTemplate fluidInput, ItemStackTemplate output, int energyCost) {
        this.id = id;
        this.input = input;
        this.inputCount = Math.max(1, inputCount);
        this.fluidInput = fluidInput;
        this.output = output;
        this.energyCost = energyCost;
    }

    public InfusionRecipe(Identifier id, ItemStack legacyInput, FluidStack fluidInput, ItemStack output, int energyCost) {
        this(id, Ingredient.of(legacyInput.getItem()), legacyInput.getCount(), fluidInput, output, energyCost);
    }

    public boolean matches(ItemStack stack, FluidStack fluid) {
        return input.test(stack)
                && stack.getCount() >= inputCount
                && fluidInput.fluid().value().isSame(fluid.getFluid())
                && fluid.getAmount() >= fluidInput.amount();
    }

    public Ingredient getIngredient() {
        return input;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getInput() {
        return input.items().findFirst().map(item -> new ItemStack(item, inputCount)).orElse(ItemStack.EMPTY);
    }

    public FluidStack getFluidInput() {
        return fluidInput.create();
    }

    public ItemStack getOutput() {
        return output.create();
    }

    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean matches(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input) {
        return output.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<InfusionRecipe> getType() {
        return JDTERecipes.INFUSION_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<InfusionRecipe> getSerializer() {
        return JDTERecipes.INFUSION_RECIPE_SERIALIZER.get();
    }

    public record InputSpec(Ingredient ingredient, int count) {
        public static final Codec<InputSpec> CODEC = Codec.either(
                BioFactoryInput.CODEC,
                Codec.either(
                        Ingredient.CODEC,
                        ItemStackTemplate.CODEC
                )
        ).xmap(
                either -> either.map(
                        bf -> new InputSpec(bf.ingredient(), Math.max(1, bf.count())),
                        inner -> inner.map(
                                ing -> new InputSpec(ing, 1),
                                stack -> new InputSpec(Ingredient.of(stack.item().value()), stack.count())
                        )
                ),
                spec -> com.mojang.datafixers.util.Either.left(new BioFactoryInput(spec.ingredient(), spec.count()))
        );
    }

    public static class Serializer {
        public static RecipeSerializer<InfusionRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("id", Identifier.fromNamespaceAndPath("jdte", "infusion")).forGetter(r -> r.id),
                InputSpec.CODEC.fieldOf("input").forGetter(r -> new InputSpec(r.input, r.inputCount)),
                Codec.INT.optionalFieldOf("count").forGetter(r -> Optional.of(r.inputCount)),
                FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(r -> r.fluidInput),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.output),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(InfusionRecipe::getEnergyCost)
        ).apply(instance, (id, inputSpec, optCount, fluid, output, energy) -> {
            int count = optCount.orElse(inputSpec.count());
            return new InfusionRecipe(id, inputSpec.ingredient(), count, fluid, output, energy);
        }));

        public static MapCodec<InfusionRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
            return StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, InfusionRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
            ByteBufCodecs.VAR_INT.encode(buf, recipe.inputCount);
            FluidStackTemplate.STREAM_CODEC.encode(buf, recipe.fluidInput);
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
            ByteBufCodecs.INT.encode(buf, recipe.energyCost);
        }

        private static InfusionRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            int inputCount = ByteBufCodecs.VAR_INT.decode(buf);
            var fluid = FluidStackTemplate.STREAM_CODEC.decode(buf);
            var output = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int energyCost = ByteBufCodecs.INT.decode(buf);
            return new InfusionRecipe(Identifier.parse("network"), input, inputCount, fluid, output, energyCost);
        }
    }
}
