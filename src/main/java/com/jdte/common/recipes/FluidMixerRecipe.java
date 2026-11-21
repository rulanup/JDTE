package com.jdte.common.recipes;

import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.Optional;

/**
 * 流体混合器配方：一种或两种流体输入 + 可选物品催化剂 → 流体输出。
 * 物品催化剂在混合过程中被消耗。
 */
public record FluidMixerRecipe(FluidStackTemplate inputA, Optional<FluidStackTemplate> inputB,
                               Optional<InputItem> itemInput, FluidStackTemplate outputTemplate,
                               int processTicks, int energy) implements MachineRecipe {
    public FluidMixerRecipe(FluidStack a, FluidStack b, Optional<InputItem> item, FluidStack output, int ticks, int energy) {
        this(FluidStackTemplate.fromNonEmptyStack(a),
                b.isEmpty() ? Optional.empty() : Optional.of(FluidStackTemplate.fromNonEmptyStack(b)),
                item, FluidStackTemplate.fromNonEmptyStack(output), ticks, energy);
    }

    public FluidStack fluidInputA() { return inputA.create(); }
    public FluidStack fluidInputB() { return inputB.map(FluidStackTemplate::create).orElse(FluidStack.EMPTY); }
    public FluidStack output() { return outputTemplate.create(); }

    private boolean matchesB(FluidStack stack) {
        return inputB.map(template -> stack.is(template.fluid())).orElseGet(stack::isEmpty);
    }

    private int amountB() { return inputB.map(FluidStackTemplate::amount).orElse(0); }

    /**
     * 检查两个流体（不考虑数量）和可选物品是否匹配此配方。
     * 流体顺序无关：A/B 可以互换。
     */
    public boolean matchesFluids(FluidStack tankA, FluidStack tankB, ItemStack catalyst) {
        boolean fluidsMatch = (tankA.is(inputA.fluid()) && matchesB(tankB))
                || (matchesB(tankA) && tankB.is(inputA.fluid()));
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
        if (tankA.is(inputA.fluid()) && matchesB(tankB)) {
            return tankA.getAmount() >= inputA.amount()
                    && tankB.getAmount() >= amountB();
        }
        // 反向
        return tankA.getAmount() >= amountB()
                && tankB.getAmount() >= inputA.amount();
    }

    /** 根据实际罐A的流体返回对应的消耗量。 */
    public int getDrainA(FluidStack tankA) {
        return tankA.is(inputA.fluid()) ? inputA.amount() : amountB();
    }

    /** 根据实际罐B的流体返回对应的消耗量。 */
    public int getDrainB(FluidStack tankB) {
        return matchesB(tankB) ? amountB() : inputA.amount();
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
    public RecipeSerializer<FluidMixerRecipe> getSerializer() {
        return JDTERecipes.FLUID_MIXER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<FluidMixerRecipe> getType() {
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

    public static final class Serializer {
        public static RecipeSerializer<FluidMixerRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<FluidMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStackTemplate.CODEC.fieldOf("fluid_input_a").forGetter(FluidMixerRecipe::inputA),
                FluidStackTemplate.CODEC.optionalFieldOf("fluid_input_b").forGetter(FluidMixerRecipe::inputB),
                InputItem.CODEC.optionalFieldOf("item_input").forGetter(FluidMixerRecipe::itemInput),
                FluidStackTemplate.CODEC.fieldOf("output").forGetter(FluidMixerRecipe::outputTemplate),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("process_ticks").forGetter(FluidMixerRecipe::processTicks),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(FluidMixerRecipe::energy)
        ).apply(instance, FluidMixerRecipe::new));

        public static MapCodec<FluidMixerRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, FluidMixerRecipe> streamCodec() {
            return StreamCodec.composite(
                    FluidStackTemplate.STREAM_CODEC, FluidMixerRecipe::inputA,
                    ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC), FluidMixerRecipe::inputB,
                    ByteBufCodecs.optional(InputItem.STREAM_CODEC), FluidMixerRecipe::itemInput,
                    FluidStackTemplate.STREAM_CODEC, FluidMixerRecipe::outputTemplate,
                    ByteBufCodecs.VAR_INT, FluidMixerRecipe::processTicks,
                    ByteBufCodecs.VAR_INT, FluidMixerRecipe::energy,
                    FluidMixerRecipe::new);
        }
    }
}
