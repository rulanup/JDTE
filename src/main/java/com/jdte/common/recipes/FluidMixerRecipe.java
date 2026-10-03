package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

/**
 * 流体混合器配方：两种流体输入 + 可选物品催化剂 → 流体输出。
 * 物品催化剂在混合过程中被消耗。
 */
public record FluidMixerRecipe(FluidStack fluidInputA, FluidStack fluidInputB,
                               Optional<InputItem> itemInput, FluidStack output,
                               int processTicks, int energy) implements Recipe<CraftingInput> {

    /**
     * 检查两个流体（不考虑数量）和可选物品是否匹配此配方。
     * 流体顺序无关：A/B 可以互换。
     */
    public boolean matchesFluids(FluidStack tankA, FluidStack tankB, ItemStack catalyst) {
        boolean fluidsMatch = (tankA.is(fluidInputA.getFluid()) && tankB.is(fluidInputB.getFluid()))
                || (tankA.is(fluidInputB.getFluid()) && tankB.is(fluidInputA.getFluid()));
        if (!fluidsMatch) return false;
        if (itemInput.isPresent()) {
            InputItem input = itemInput.get();
            return input.ingredient().test(catalyst) && catalyst.getCount() >= input.count();
        }
        return true;
    }

    /**
     * 检查两个流体是否有足够的数量。流体顺序由 {@link #matchesFluids} 确定后传入。
     */
    public boolean hasEnoughFluids(FluidStack tankA, FluidStack tankB) {
        if (tankA.is(fluidInputA.getFluid()) && tankB.is(fluidInputB.getFluid())) {
            return tankA.getAmount() >= fluidInputA.getAmount()
                    && tankB.getAmount() >= fluidInputB.getAmount();
        }
        // 反向
        return tankA.getAmount() >= fluidInputB.getAmount()
                && tankB.getAmount() >= fluidInputA.getAmount();
    }

    /** 根据实际罐A的流体返回对应的消耗量。 */
    public int getDrainA(FluidStack tankA) {
        return tankA.is(fluidInputA.getFluid()) ? fluidInputA.getAmount() : fluidInputB.getAmount();
    }

    /** 根据实际罐B的流体返回对应的消耗量。 */
    public int getDrainB(FluidStack tankB) {
        return tankB.is(fluidInputB.getFluid()) ? fluidInputB.getAmount() : fluidInputA.getAmount();
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
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.FLUID_MIXER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.FLUID_MIXER_RECIPE_TYPE.get();
    }

    public record InputItem(Ingredient ingredient, int count) {
        public static final Codec<InputItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(InputItem::ingredient),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(InputItem::count)
        ).apply(instance, InputItem::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InputItem> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, InputItem::ingredient,
                ByteBufCodecs.VAR_INT, InputItem::count,
                InputItem::new);
    }

    public static final class Serializer implements RecipeSerializer<FluidMixerRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> FLUID_STACK_CODEC = StreamCodec.of(
                (buf, stack) -> {
                    ByteBufCodecs.holderRegistry(Registries.FLUID).encode(buf, stack.getFluid().builtInRegistryHolder());
                    ByteBufCodecs.INT.encode(buf, stack.getAmount());
                },
                buf -> new FluidStack(ByteBufCodecs.holderRegistry(Registries.FLUID).decode(buf).value(),
                        ByteBufCodecs.INT.decode(buf)));

        private static final MapCodec<FluidMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStack.CODEC.fieldOf("fluid_input_a").forGetter(FluidMixerRecipe::fluidInputA),
                FluidStack.CODEC.fieldOf("fluid_input_b").forGetter(FluidMixerRecipe::fluidInputB),
                InputItem.CODEC.optionalFieldOf("item_input").forGetter(FluidMixerRecipe::itemInput),
                FluidStack.CODEC.fieldOf("output").forGetter(FluidMixerRecipe::output),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("process_ticks").forGetter(FluidMixerRecipe::processTicks),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(FluidMixerRecipe::energy)
        ).apply(instance, FluidMixerRecipe::new));

        @Override
        public MapCodec<FluidMixerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidMixerRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public FluidMixerRecipe decode(RegistryFriendlyByteBuf buffer) {
                    FluidStack a = FLUID_STACK_CODEC.decode(buffer);
                    FluidStack b = FLUID_STACK_CODEC.decode(buffer);
                    boolean hasItem = ByteBufCodecs.BOOL.decode(buffer);
                    Optional<InputItem> item = hasItem
                            ? Optional.of(InputItem.STREAM_CODEC.decode(buffer))
                            : Optional.empty();
                    FluidStack out = FLUID_STACK_CODEC.decode(buffer);
                    int ticks = ByteBufCodecs.VAR_INT.decode(buffer);
                    int energy = ByteBufCodecs.VAR_INT.decode(buffer);
                    return new FluidMixerRecipe(a, b, item, out, ticks, energy);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, FluidMixerRecipe recipe) {
                    FLUID_STACK_CODEC.encode(buffer, recipe.fluidInputA());
                    FLUID_STACK_CODEC.encode(buffer, recipe.fluidInputB());
                    ByteBufCodecs.BOOL.encode(buffer, recipe.itemInput().isPresent());
                    recipe.itemInput().ifPresent(item -> InputItem.STREAM_CODEC.encode(buffer, item));
                    FLUID_STACK_CODEC.encode(buffer, recipe.output());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.processTicks());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.energy());
                }
            };
        }
    }
}
