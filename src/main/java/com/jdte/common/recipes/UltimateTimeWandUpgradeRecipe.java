package com.jdte.common.recipes;

import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class UltimateTimeWandUpgradeRecipe extends CustomRecipe {
    public UltimateTimeWandUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean foundWand = false;
        boolean foundUpgrade = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(JDTEItems.ULTIMATE_TIME_WAND.get())) {
                if (foundWand || stack.getOrDefault(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), false)) {
                    return false;
                }
                foundWand = true;
            } else if (stack.is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get())) {
                if (foundUpgrade) {
                    return false;
                }
                foundUpgrade = true;
            } else {
                return false;
            }
        }

        return foundWand && foundUpgrade;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack wand = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(JDTEItems.ULTIMATE_TIME_WAND.get())) {
                wand = stack;
                break;
            }
        }
        if (wand.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = wand.copyWithCount(1);
        result.set(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), true);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.ULTIMATE_TIME_WAND_UPGRADE_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<UltimateTimeWandUpgradeRecipe> {
        private static final UltimateTimeWandUpgradeRecipe INSTANCE =
                new UltimateTimeWandUpgradeRecipe(CraftingBookCategory.EQUIPMENT);
        private static final MapCodec<UltimateTimeWandUpgradeRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, UltimateTimeWandUpgradeRecipe> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<UltimateTimeWandUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UltimateTimeWandUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
