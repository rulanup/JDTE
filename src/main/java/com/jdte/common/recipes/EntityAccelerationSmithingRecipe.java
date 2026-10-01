package com.jdte.common.recipes;

import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public final class EntityAccelerationSmithingRecipe implements SmithingRecipe {
    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get())
                && input.template().getCount() == 1
                && input.base().getCount() == 1
                && isBaseIngredient(input.base())
                && input.addition().isEmpty();
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        if (!matches(input, null)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = input.base().copyWithCount(1);
        result.set(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), true);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        stack.set(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), true);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.ENTITY_ACCELERATION_SMITHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(JDTEItems.ULTIMATE_TIME_WAND.get())
                && !stack.getOrDefault(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), false);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return false;
    }

    public static final class Serializer implements RecipeSerializer<EntityAccelerationSmithingRecipe> {
        private static final EntityAccelerationSmithingRecipe INSTANCE = new EntityAccelerationSmithingRecipe();
        private static final MapCodec<EntityAccelerationSmithingRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, EntityAccelerationSmithingRecipe> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<EntityAccelerationSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityAccelerationSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
