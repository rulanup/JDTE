package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class InfusionRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final FluidStack fluidInput;
    private final ItemStack output;
    private final int energyCost;

    public InfusionRecipe(ResourceLocation id, Ingredient input, int inputCount, FluidStack fluidInput, ItemStack output, int energyCost) {
        this.id = id;
        this.input = input;
        this.inputCount = Math.max(1, inputCount);
        this.fluidInput = fluidInput;
        this.output = output;
        this.energyCost = energyCost;
    }

    public InfusionRecipe(ResourceLocation id, ItemStack legacyInput, FluidStack fluidInput, ItemStack output, int energyCost) {
        this(id, Ingredient.of(legacyInput.getItem()), legacyInput.getCount(), fluidInput, output, energyCost);
    }

    public boolean matches(ItemStack stack, FluidStack fluid) {
        return input.test(stack)
                && stack.getCount() >= inputCount
                && fluidInput.getFluid().isSame(fluid.getFluid())
                && fluid.getAmount() >= fluidInput.getAmount();
    }

    public Ingredient getIngredient() {
        return input;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getInput() {
        ItemStack[] items = input.getItems();
        if (items.length > 0) {
            ItemStack stack = items[0].copy();
            stack.setCount(inputCount);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public FluidStack getFluidInput() {
        return fluidInput;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean matches(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, net.minecraft.core.HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.INFUSION_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.INFUSION_RECIPE_SERIALIZER.get();
    }

    @Override
    public net.minecraft.world.item.crafting.CraftingBookCategory category() {
        return net.minecraft.world.item.crafting.CraftingBookCategory.MISC;
    }

    public record InputSpec(Ingredient ingredient, int count) {
        public static final Codec<InputSpec> CODEC = Codec.either(
                BioFactoryInput.CODEC,
                Codec.either(
                        Ingredient.CODEC,
                        ItemStack.CODEC
                )
        ).xmap(
                either -> either.map(
                        bf -> new InputSpec(bf.ingredient(), Math.max(1, bf.count())),
                        inner -> inner.map(
                                ing -> new InputSpec(ing, 1),
                                stack -> new InputSpec(Ingredient.of(stack.getItem()), Math.max(1, stack.getCount()))
                        )
                ),
                spec -> com.mojang.datafixers.util.Either.left(new BioFactoryInput(spec.ingredient(), spec.count()))
        );
    }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        private static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("id", ResourceLocation.fromNamespaceAndPath("jdte", "infusion")).forGetter(r -> r.id),
                InputSpec.CODEC.fieldOf("input").forGetter(r -> new InputSpec(r.input, r.inputCount)),
                Codec.INT.optionalFieldOf("count").forGetter(r -> Optional.of(r.inputCount)),
                FluidStack.CODEC.fieldOf("fluid").forGetter(InfusionRecipe::getFluidInput),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(InfusionRecipe::getEnergyCost)
        ).apply(instance, (id, inputSpec, optCount, fluid, output, energy) -> {
            int count = optCount.orElse(inputSpec.count());
            return new InfusionRecipe(id, inputSpec.ingredient(), count, fluid, output, energy);
        }));

        @Override
        public MapCodec<InfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private void toNetwork(RegistryFriendlyByteBuf buf, InfusionRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
            ByteBufCodecs.VAR_INT.encode(buf, recipe.inputCount);
            FluidStack.STREAM_CODEC.encode(buf, recipe.fluidInput);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
            ByteBufCodecs.INT.encode(buf, recipe.energyCost);
        }

        private InfusionRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            int inputCount = ByteBufCodecs.VAR_INT.decode(buf);
            FluidStack fluid = FluidStack.STREAM_CODEC.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int energyCost = ByteBufCodecs.INT.decode(buf);
            return new InfusionRecipe(ResourceLocation.parse("network"), input, inputCount, fluid, output, energyCost);
        }
    }
}
