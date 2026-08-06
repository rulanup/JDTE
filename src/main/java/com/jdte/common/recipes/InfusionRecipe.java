package com.jdte.common.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class InfusionRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final ItemStack input;
    private final FluidStack fluidInput;
    private final ItemStack output;
    private final int energyCost;

    public InfusionRecipe(ResourceLocation id, ItemStack input, FluidStack fluidInput, ItemStack output, int energyCost) {
        this.id = id;
        this.input = input;
        this.fluidInput = fluidInput;
        this.output = output;
        this.energyCost = energyCost;
    }

    public boolean matches(ItemStack stack, FluidStack fluid) {
        return ItemStack.isSameItemSameComponents(input, stack)
                && stack.getCount() >= input.getCount()
                && fluidInput.getFluid().isSame(fluid.getFluid())
                && fluid.getAmount() >= fluidInput.getAmount();
    }

    public ItemStack getInput() {
        return input;
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
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

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        private static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(r -> r.id),
                ItemStack.CODEC.fieldOf("input").forGetter(InfusionRecipe::getInput),
                FluidStack.CODEC.fieldOf("fluid").forGetter(InfusionRecipe::getFluidInput),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(InfusionRecipe::getEnergyCost)
        ).apply(instance, InfusionRecipe::new));

        @Override
        public MapCodec<InfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private void toNetwork(RegistryFriendlyByteBuf buf, InfusionRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buf, recipe.input);
            ByteBufCodecs.holderRegistry(Registries.FLUID).encode(buf, recipe.fluidInput.getFluid().builtInRegistryHolder());
            ByteBufCodecs.INT.encode(buf, recipe.fluidInput.getAmount());
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
            ByteBufCodecs.INT.encode(buf, recipe.energyCost);
        }

        private InfusionRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            ItemStack input = ItemStack.STREAM_CODEC.decode(buf);
            var fluidHolder = ByteBufCodecs.holderRegistry(Registries.FLUID).decode(buf);
            int fluidAmount = ByteBufCodecs.INT.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            int energyCost = ByteBufCodecs.INT.decode(buf);
            return new InfusionRecipe(ResourceLocation.parse("network"), input, new FluidStack(fluidHolder.value(), fluidAmount), output, energyCost);
        }
    }
}
