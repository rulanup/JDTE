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

import java.util.List;

/**
 * 生命合成舱配方：若干培养基（物品）+ 养分流体 -> 生命流体。
 * 同一输入槽只匹配一个物品槽，避免不同输入行重复扣减同一槽。
 */
public record LifeSynthesisRecipe(List<InputSlot> inputs, List<FluidStack> fluidInputs,
                                  FluidStack output, int processTicks, int energy,
                                  String tier) implements Recipe<CraftingInput> {

    /** 当前实现只支持单养分罐，取第一种养分流体。 */
    public FluidStack nutrient() {
        return fluidInputs.isEmpty() ? FluidStack.EMPTY : fluidInputs.getFirst();
    }

    public boolean matchesSlots(List<ItemStack> slots) {
        boolean[] used = new boolean[slots.size()];
        for (InputSlot slot : inputs) {
            int needed = slot.count();
            for (int i = 0; i < slots.size() && needed > 0; i++) {
                if (!used[i] && slot.ingredient().test(slots.get(i))) {
                    used[i] = true;
                    needed -= Math.min(needed, slots.get(i).getCount());
                }
            }
            if (needed > 0) return false;
        }
        return true;
    }

    /**
     * 严格匹配并扣减一份配方所需培养基。
     * 所有输入行都满足数量时才实际扣减；任一不足则整体放弃，不产生部分扣减。
     */
    public boolean consumeStrict(List<ItemStack> slots) {
        boolean[] used = new boolean[slots.size()];
        int[] take = new int[slots.size()];
        for (InputSlot slot : inputs) {
            int needed = slot.count();
            for (int i = 0; i < slots.size() && needed > 0; i++) {
                if (used[i] || !slot.ingredient().test(slots.get(i))) continue;
                used[i] = true;
                int t = Math.min(needed, slots.get(i).getCount());
                take[i] = t;
                needed -= t;
            }
            if (needed > 0) return false;
        }
        for (int i = 0; i < take.length; i++) {
            if (take[i] > 0) slots.get(i).shrink(take[i]);
        }
        return true;
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
        return JDTERecipes.LIFE_SYNTHESIS_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.LIFE_SYNTHESIS_RECIPE_TYPE.get();
    }

    public record InputSlot(Ingredient ingredient, int count) {
        public static final Codec<InputSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(InputSlot::ingredient),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(InputSlot::count)
        ).apply(instance, InputSlot::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InputSlot> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, InputSlot::ingredient,
                ByteBufCodecs.VAR_INT, InputSlot::count,
                InputSlot::new);
    }

    public static final class Serializer implements RecipeSerializer<LifeSynthesisRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> FLUID_STACK_CODEC = StreamCodec.of(
                (buf, stack) -> {
                    ByteBufCodecs.holderRegistry(Registries.FLUID).encode(buf, stack.getFluid().builtInRegistryHolder());
                    ByteBufCodecs.INT.encode(buf, stack.getAmount());
                },
                buf -> new FluidStack(ByteBufCodecs.holderRegistry(Registries.FLUID).decode(buf).value(),
                        ByteBufCodecs.INT.decode(buf)));

        private static final MapCodec<LifeSynthesisRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                InputSlot.CODEC.listOf().fieldOf("inputs").forGetter(LifeSynthesisRecipe::inputs),
                FluidStack.CODEC.listOf().fieldOf("fluid_inputs").forGetter(LifeSynthesisRecipe::fluidInputs),
                FluidStack.CODEC.fieldOf("output").forGetter(LifeSynthesisRecipe::output),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("process_ticks").forGetter(LifeSynthesisRecipe::processTicks),
                net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("energy").forGetter(LifeSynthesisRecipe::energy),
                net.minecraft.util.ExtraCodecs.NON_EMPTY_STRING.fieldOf("tier").forGetter(LifeSynthesisRecipe::tier)
        ).apply(instance, LifeSynthesisRecipe::new));

        @Override
        public MapCodec<LifeSynthesisRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LifeSynthesisRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public LifeSynthesisRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new LifeSynthesisRecipe(
                            InputSlot.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                            FLUID_STACK_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                            FLUID_STACK_CODEC.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.VAR_INT.decode(buffer),
                            ByteBufCodecs.STRING_UTF8.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, LifeSynthesisRecipe recipe) {
                    InputSlot.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.inputs());
                    FLUID_STACK_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fluidInputs());
                    FLUID_STACK_CODEC.encode(buffer, recipe.output());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.processTicks());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.energy());
                    ByteBufCodecs.STRING_UTF8.encode(buffer, recipe.tier());
                }
            };
        }
    }
}