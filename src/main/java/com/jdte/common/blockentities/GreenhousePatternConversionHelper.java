package com.jdte.common.blockentities;

import com.jdte.common.items.CraftingPatternData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Batch crafting against exact recorded ingredients, including vanilla crafting remainders. */
final class GreenhousePatternConversionHelper {
    private GreenhousePatternConversionHelper() {}

    static Result convert(ServerLevel level, List<ItemStack> drops, List<CraftingPatternData> patterns, int[] rotation) {
        List<ItemStack> working = new ArrayList<>();
        for (ItemStack drop : drops) GreenhouseEssenceConversionHelper.addAmount(working, drop, drop.getCount());
        int safeLen = Math.max(patterns.size(), rotation != null ? rotation.length : 0);
        int[] safeRotation = new int[safeLen];
        if (rotation != null) {
            System.arraycopy(rotation, 0, safeRotation, 0, Math.min(rotation.length, safeLen));
        }
        int[] nextRotation = safeRotation.clone();
        List<RecipePlan> recipes = new ArrayList<>();
        for (int index = 0; index < patterns.size(); index++) {
            CraftingPatternData pattern = patterns.get(index);
            var resolved = pattern.resolve(level).orElse(null);
            if (resolved == null) continue;
            List<ItemStack> requirements = new ArrayList<>();
            for (ItemStack ingredient : pattern.ingredients()) {
                GreenhouseEssenceConversionHelper.addAmount(requirements, ingredient, ingredient.getCount());
            }
            if (requirements.isEmpty()) continue;
            var remainders = resolved.value().getRemainingItems(pattern.input());
            recipes.add(new RecipePlan(index, requirements, pattern.result(), List.copyOf(remainders)));
        }
        for (List<RecipePlan> group : groups(recipes)) {
            group.sort(java.util.Comparator.comparingInt(RecipePlan::index));
            int key = group.getFirst().index();
            int start = Math.floorMod(safeRotation[key], Math.max(1, patterns.size()));
            int first = 0;
            for (int index = 1; index < group.size(); index++) {
                if (Math.floorMod(group.get(index).index() - start, patterns.size())
                        < Math.floorMod(group.get(first).index() - start, patterns.size())) first = index;
            }
            java.util.Collections.rotate(group, -first);
            List<ItemStack> cycle = new ArrayList<>();
            for (RecipePlan recipe : group) for (ItemStack required : recipe.requirements()) {
                GreenhouseEssenceConversionHelper.addAmount(cycle, required, required.getCount());
            }
            long rounds = craftsAvailable(working, cycle);
            if (rounds > 0) {
                for (RecipePlan recipe : group) apply(working, recipe, rounds);
                nextRotation[key] = group.getFirst().index();
            }
            // At most one incomplete round. Waiting for the next shared recipe prevents starvation.
            for (int index = 0; index < group.size(); index++) {
                RecipePlan recipe = group.get(index);
                if (craftsAvailable(working, recipe.requirements()) <= 0) break;
                apply(working, recipe, 1);
                nextRotation[key] = group.get((index + 1) % group.size()).index();
            }
        }
        working.removeIf(ItemStack::isEmpty);
        return new Result(working, nextRotation);
    }

    private static List<List<RecipePlan>> groups(List<RecipePlan> recipes) {
        List<List<RecipePlan>> groups = new ArrayList<>();
        for (RecipePlan recipe : recipes) {
            List<RecipePlan> connected = new ArrayList<>();
            connected.add(recipe);
            boolean merged;
            do {
                merged = false;
                for (int index = groups.size() - 1; index >= 0; index--) {
                    List<RecipePlan> group = groups.get(index);
                    if (connected.stream().anyMatch(left -> group.stream().anyMatch(right -> sharesInput(left, right)))) {
                        connected.addAll(group);
                        groups.remove(index);
                        merged = true;
                    }
                }
            } while (merged);
            groups.add(connected);
        }
        return groups;
    }

    private static boolean sharesInput(RecipePlan left, RecipePlan right) {
        return left.requirements().stream().anyMatch(a -> right.requirements().stream()
                .anyMatch(b -> ItemStack.isSameItemSameComponents(a, b)));
    }

    private static long craftsAvailable(List<ItemStack> working, List<ItemStack> requirements) {
        long crafts = Long.MAX_VALUE;
        for (ItemStack required : requirements) crafts = Math.min(crafts, count(working, required) / required.getCount());
        return requirements.isEmpty() ? 0 : crafts;
    }

    private static void apply(List<ItemStack> working, RecipePlan recipe, long crafts) {
        for (ItemStack required : recipe.requirements()) remove(working, required, crafts * required.getCount());
        GreenhouseEssenceConversionHelper.addAmount(working, recipe.result(), crafts * recipe.result().getCount());
        for (ItemStack remainder : recipe.remainders()) {
            GreenhouseEssenceConversionHelper.addAmount(working, remainder, crafts * remainder.getCount());
        }
        working.removeIf(ItemStack::isEmpty);
    }

    record Result(List<ItemStack> drops, int[] rotation) {}
    private record RecipePlan(int index, List<ItemStack> requirements, ItemStack result, List<ItemStack> remainders) {}

    static boolean references(List<CraftingPatternData> patterns, ItemStack stack, boolean includeResults) {
        for (CraftingPatternData pattern : patterns) {
            if (includeResults && ItemStack.isSameItemSameComponents(pattern.result(), stack)) return true;
            for (ItemStack ingredient : pattern.ingredients()) {
                if (!ingredient.isEmpty() && ItemStack.isSameItemSameComponents(ingredient, stack)) return true;
            }
        }
        return false;
    }

    static long count(List<ItemStack> stacks, ItemStack template) {
        long count = 0;
        for (ItemStack stack : stacks) if (ItemStack.isSameItemSameComponents(stack, template)) count += stack.getCount();
        return count;
    }

    private static void remove(List<ItemStack> stacks, ItemStack template, long amount) {
        for (ItemStack stack : stacks) {
            if (amount <= 0) return;
            if (!ItemStack.isSameItemSameComponents(stack, template)) continue;
            int removed = (int) Math.min(amount, stack.getCount());
            stack.shrink(removed);
            amount -= removed;
        }
        if (amount != 0) throw new IllegalStateException("Recorded crafting ingredients changed during conversion");
    }
}
