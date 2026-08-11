package com.jdte.common.blockentities;

import com.jdte.common.recipes.GreenhouseCropResolver;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Resolves unambiguous, essence-only crafting recipes and applies their ratios to greenhouse output. */
public final class GreenhouseEssenceConversionHelper {
    private static final TagKey<Item> MYSTICAL_ESSENCES = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "essences"));
    private static final Map<StackKey, Optional<Conversion>> CONVERSIONS = new HashMap<>();
    private static long cachedGeneration = Long.MIN_VALUE;

    private GreenhouseEssenceConversionHelper() {
    }

    public static List<ItemStack> convert(ServerLevel level, List<ItemStack> drops) {
        if (drops.isEmpty()) return drops;

        List<ItemStack> mergedDrops = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) addAmount(mergedDrops, drop, drop.getCount());

        List<ItemStack> convertedDrops = new ArrayList<>(mergedDrops.size());
        for (ItemStack drop : mergedDrops) {
            Optional<Conversion> conversion = findConversion(level, drop);
            if (conversion.isEmpty()) {
                addAmount(convertedDrops, drop, drop.getCount());
                continue;
            }

            Conversion recipe = conversion.get();
            int crafts = drop.getCount() / recipe.essenceCount();
            int remainder = drop.getCount() % recipe.essenceCount();
            if (crafts > 0) {
                addAmount(convertedDrops, recipe.result(), (long) crafts * recipe.result().getCount());
            }
            if (remainder > 0) addAmount(convertedDrops, drop, remainder);
        }
        return convertedDrops;
    }

    public static List<ItemStack> replaceSeeds(List<ItemStack> drops, ItemStack plantedSeed, ItemStack essence) {
        if (drops.isEmpty() || plantedSeed.isEmpty() || essence.isEmpty()
                || !essence.is(MYSTICAL_ESSENCES)
                || ItemStack.isSameItemSameComponents(plantedSeed, essence)) {
            return drops;
        }

        List<ItemStack> replaced = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            ItemStack output = ItemStack.isSameItemSameComponents(drop, plantedSeed) ? essence : drop;
            addAmount(replaced, output, drop.getCount());
        }
        return replaced;
    }

    static ItemStack getConversionResult(ServerLevel level, ItemStack essence) {
        return findConversion(level, essence).map(conversion -> conversion.result().copy()).orElse(ItemStack.EMPTY);
    }

    static boolean convertStored(ServerLevel level, IItemHandler outputHandler) {
        List<ItemStack> essenceTypes = new ArrayList<>();
        for (int slot = 0; slot < outputHandler.getSlots(); slot++) {
            ItemStack stack = outputHandler.getStackInSlot(slot);
            if (stack.isEmpty() || essenceTypes.stream()
                    .anyMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack))) continue;
            if (findConversion(level, stack).isPresent()) essenceTypes.add(stack.copyWithCount(1));
        }

        boolean changed = false;
        for (ItemStack essence : essenceTypes) {
            Conversion conversion = findConversion(level, essence).orElse(null);
            if (conversion == null) continue;
            long stored = count(outputHandler, essence);
            long crafts = stored / conversion.essenceCount();
            if (crafts <= 0) continue;

            List<ItemStack> replacement = new ArrayList<>(2);
            addAmount(replacement, conversion.result(), crafts * conversion.result().getCount());
            addAmount(replacement, essence, stored % conversion.essenceCount());
            GreenhouseCapacityLedger capacity = GreenhouseCapacityLedger.captureWithout(outputHandler, essence);
            if (!capacity.canFit(replacement, 1)) continue;

            extractAll(outputHandler, essence);
            if (!insertAll(outputHandler, replacement)) {
                throw new IllegalStateException("Simulated Greenhouse essence conversion did not fit");
            }
            changed = true;
        }
        return changed;
    }

    private static Optional<Conversion> findConversion(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || !stack.is(MYSTICAL_ESSENCES)) return Optional.empty();
        refreshCacheGeneration();
        StackKey key = new StackKey(stack.getItem(), stack.getComponentsPatch());
        return CONVERSIONS.computeIfAbsent(key, ignored -> resolveConversion(level, stack));
    }

    private static void refreshCacheGeneration() {
        long generation = GreenhouseCropResolver.cacheGeneration();
        if (cachedGeneration == generation) return;
        cachedGeneration = generation;
        CONVERSIONS.clear();
    }

    private static Optional<Conversion> resolveConversion(ServerLevel level, ItemStack essence) {
        Conversion candidate = null;
        int matchingRecipes = 0;
        for (var holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            if (!usesEssence(recipe, essence)) continue;
            if (++matchingRecipes > 1) return Optional.empty();
            candidate = analyzeRecipe(level, recipe, essence);
        }
        return matchingRecipes == 1 ? Optional.ofNullable(candidate) : Optional.empty();
    }

    private static boolean usesEssence(CraftingRecipe recipe, ItemStack essence) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty() && ingredient.test(essence)) return true;
        }
        return false;
    }

    private static Conversion analyzeRecipe(ServerLevel level, CraftingRecipe recipe, ItemStack essence) {
        int essenceCount = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            ItemStack[] choices = ingredient.getItems();
            if (choices.length != 1 || !ItemStack.isSameItemSameComponents(choices[0], essence)) return null;
            essenceCount++;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (essenceCount <= 0 || result.isEmpty() || ItemStack.isSameItemSameComponents(result, essence)) return null;
        return new Conversion(essenceCount, result.copy());
    }

    private static void addAmount(List<ItemStack> stacks, ItemStack template, long amount) {
        if (template.isEmpty() || amount <= 0) return;
        for (ItemStack existing : stacks) {
            if (amount <= 0) return;
            if (!ItemStack.isSameItemSameComponents(existing, template)) continue;
            int accepted = (int) Math.min(amount, Integer.MAX_VALUE - (long) existing.getCount());
            existing.grow(accepted);
            amount -= accepted;
        }
        while (amount > 0) {
            int count = (int) Math.min(amount, Integer.MAX_VALUE);
            stacks.add(template.copyWithCount(count));
            amount -= count;
        }
    }

    private static long count(IItemHandler handler, ItemStack template) {
        long count = 0L;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (ItemStack.isSameItemSameComponents(stack, template)) count += stack.getCount();
        }
        return count;
    }

    private static void extractAll(IItemHandler handler, ItemStack template) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                handler.extractItem(slot, stack.getCount(), false);
            }
        }
    }

    private static boolean insertAll(IItemHandler handler, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            long remaining = stack.getCount();
            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                ItemStack existing = handler.getStackInSlot(slot);
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                    remaining -= insert(handler, slot, stack, remaining);
                }
            }
            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                if (handler.getStackInSlot(slot).isEmpty()) {
                    remaining -= insert(handler, slot, stack, remaining);
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private static int insert(IItemHandler handler, int slot, ItemStack template, long amount) {
        int offered = (int) Math.min(amount, handler.getSlotLimit(slot));
        ItemStack remainder = handler.insertItem(slot, template.copyWithCount(offered), false);
        return offered - remainder.getCount();
    }

    private record StackKey(Item item, DataComponentPatch components) {
    }

    private record Conversion(int essenceCount, ItemStack result) {
    }
}
