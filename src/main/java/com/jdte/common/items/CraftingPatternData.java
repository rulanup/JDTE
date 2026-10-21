package com.jdte.common.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/** A server-validated crafting recipe and its exact nine ghost ingredients. */
public record CraftingPatternData(ResourceLocation recipeId, List<ItemStack> ingredients, ItemStack result) {
    private static final Codec<List<ItemStack>> GRID_CODEC = ItemStack.OPTIONAL_CODEC.listOf().validate(
            grid -> grid.size() == 9 ? DataResult.success(grid) : DataResult.error(() -> "Expected nine pattern slots"));
    public static final Codec<CraftingPatternData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("recipe").forGetter(CraftingPatternData::recipeId),
            GRID_CODEC.fieldOf("ingredients").forGetter(CraftingPatternData::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(CraftingPatternData::result)
    ).apply(instance, CraftingPatternData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingPatternData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public CraftingPatternData {
        if (ingredients.size() != 9 || result.isEmpty()) throw new IllegalArgumentException("Invalid crafting pattern");
        ingredients = ingredients.stream().map(stack -> stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1)).toList();
        result = result.copy();
    }

    public CraftingInput input() {
        return CraftingInput.of(3, 3, ingredients.stream().map(ItemStack::copy).toList());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CraftingPatternData pattern) || !recipeId.equals(pattern.recipeId)
                || !ItemStack.matches(result, pattern.result)) return false;
        for (int slot = 0; slot < 9; slot++) {
            if (!ItemStack.matches(ingredients.get(slot), pattern.ingredients.get(slot))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 31 * recipeId.hashCode() + ItemStack.hashItemAndComponents(result);
        hash = 31 * hash + result.getCount();
        for (ItemStack ingredient : ingredients) {
            hash = 31 * hash + ItemStack.hashItemAndComponents(ingredient);
            hash = 31 * hash + ingredient.getCount();
        }
        return hash;
    }

    public static Optional<CraftingPatternData> capture(Level level, List<ItemStack> grid) {
        if (grid.size() != 9) return Optional.empty();
        try {
            CraftingInput input = CraftingInput.of(3, 3, grid.stream()
                    .map(stack -> stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1)).toList());
            return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).flatMap(holder -> {
                if (holder.value().isSpecial()) return Optional.empty();
                ItemStack result = holder.value().assemble(input, level.registryAccess());
                if (result.isEmpty() || !result.isItemEnabled(level.enabledFeatures())) return Optional.empty();
                holder.value().getRemainingItems(input);
                return Optional.of(new CraftingPatternData(holder.id(), grid, result));
            });
        } catch (RuntimeException incompatibleRecipe) {
            return Optional.empty();
        }
    }

    public Optional<RecipeHolder<CraftingRecipe>> resolve(Level level) {
        try {
            var holder = level.getRecipeManager().byKey(recipeId).orElse(null);
            if (holder == null || !(holder.value() instanceof CraftingRecipe recipe) || recipe.isSpecial()) return Optional.empty();
            CraftingInput input = input();
            if (!recipe.matches(input, level)) return Optional.empty();
            ItemStack actual = recipe.assemble(input, level.registryAccess());
            recipe.getRemainingItems(input);
            return ItemStack.matches(result, actual) && actual.isItemEnabled(level.enabledFeatures())
                    ? Optional.of(new RecipeHolder<>(recipeId, recipe)) : Optional.empty();
        } catch (RuntimeException incompatibleRecipe) {
            return Optional.empty();
        }
    }
}
