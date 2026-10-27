package com.jdte.common.blockentities;

import com.jdte.common.recipes.GreenhouseCropResolver;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.CraftingPatternData;
import com.jdte.common.items.EssenceConversionUpgradeItem;
import com.jdte.common.upgrades.UpgradeHelper;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;

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

    public static List<CraftingPatternData> patterns(BaseMachineBE machine) {
        var handler = UpgradeHelper.getUpgradeHandler(machine);
        if (handler == null) return List.of();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.getItem() instanceof EssenceConversionUpgradeItem) return EssenceConversionUpgradeItem.patterns(stack);
        }
        return List.of();
    }

    static List<ItemStack> convert(ServerLevel level, List<ItemStack> drops, BaseMachineBE machine) {
        return planConversion(level, drops, machine, rotation(machine)).drops();
    }

    static ConversionPlan planConversion(ServerLevel level, List<ItemStack> drops, BaseMachineBE machine, int[] rotation) {
        List<CraftingPatternData> patterns = patterns(machine);
        if (patterns.isEmpty()) return new ConversionPlan(convert(level, drops), rotation.clone());
        var plan = GreenhousePatternConversionHelper.convert(level, drops, patterns, rotation);
        List<ItemStack> converted = new ArrayList<>();
        for (ItemStack drop : plan.drops()) {
            List<ItemStack> results = GreenhousePatternConversionHelper.references(patterns, drop, true)
                    ? List.of(drop) : convert(level, List.of(drop));
            for (ItemStack result : results) addAmount(converted, result, result.getCount());
        }
        return new ConversionPlan(converted, plan.rotation());
    }

    private static int[] rotation(BaseMachineBE machine) {
        if (machine instanceof GreenhouseBE greenhouse) return greenhouse.getEssencePatternRotation();
        if (machine instanceof LargeGreenhouseBE greenhouse) return greenhouse.getEssencePatternRotation();
        return new int[0];
    }

    private static void commitRotation(BaseMachineBE machine, int[] rotation) {
        if (machine instanceof GreenhouseBE greenhouse) greenhouse.setEssencePatternRotation(rotation);
        else if (machine instanceof LargeGreenhouseBE greenhouse) greenhouse.setEssencePatternRotation(rotation);
    }

    record ConversionPlan(List<ItemStack> drops, int[] rotation) {}

    public static boolean shouldRetainForConversion(ServerLevel level, BaseMachineBE machine, ItemStack stack) {
        if (!(machine instanceof GreenhouseBE || machine instanceof LargeGreenhouseBE)
                || !UpgradeHelper.hasEssenceConversionUpgrade(machine)) return false;
        List<CraftingPatternData> patterns = patterns(machine);
        if (GreenhousePatternConversionHelper.references(patterns, stack, false)) return true;
        if (GreenhousePatternConversionHelper.references(patterns, stack, true)) return false;
        return findConversion(level, stack).isPresent();
    }

    public static void prepareOutput(ServerLevel level, BaseMachineBE machine) {
        if (machine instanceof GreenhouseBE greenhouse) greenhouse.prepareOutputConversion();
        else if (machine instanceof LargeGreenhouseBE greenhouse) greenhouse.prepareOutputConversion();
    }

    static ItemStack getConversionResult(ServerLevel level, ItemStack essence, BaseMachineBE machine) {
        for (CraftingPatternData pattern : patterns(machine)) {
            if (pattern.resolve(level).isPresent()
                    && GreenhousePatternConversionHelper.references(List.of(pattern), essence, false)) return pattern.result().copy();
        }
        return getConversionResult(level, essence);
    }

    static boolean convertStored(ServerLevel level, IItemHandler handler, BaseMachineBE machine) {
        if (patterns(machine).isEmpty()) return convertStored(level, handler);
        List<ItemStack> before = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) addAmount(before, stack, stack.getCount());
        }
        int[] rotation = rotation(machine);
        ConversionPlan plan = planConversion(level, before, machine, rotation);
        List<ItemStack> after = plan.drops();
        boolean same = before.size() == after.size();
        if (same) for (ItemStack stack : before) {
            if (GreenhousePatternConversionHelper.count(before, stack) != GreenhousePatternConversionHelper.count(after, stack)) {
                same = false;
                break;
            }
        }
        if (same) {
            commitRotation(machine, plan.rotation());
            return !java.util.Arrays.equals(rotation, plan.rotation());
        }
        if (!GreenhouseCapacityLedger.empty(handler).canFit(after, 1)) return false;
        for (ItemStack stack : before) extractAll(handler, stack);
        if (!insertAll(handler, after)) {
            for (int slot = 0; slot < handler.getSlots(); slot++) extractAll(handler, handler.getStackInSlot(slot).copy());
            if (!insertAll(handler, before)) throw new IllegalStateException("Could not restore greenhouse conversion inputs");
            throw new IllegalStateException("Recorded greenhouse crafting exceeded reserved capacity");
        }
        commitRotation(machine, plan.rotation());
        return true;
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

    static void addAmount(List<ItemStack> stacks, ItemStack template, long amount) {
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
        if (template.isEmpty()) return;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler instanceof IItemHandlerModifiable modifiable
                    && ItemStack.isSameItemSameComponents(handler.getStackInSlot(slot), template)) {
                modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                continue;
            }
            while (true) {
                ItemStack before = handler.getStackInSlot(slot);
                if (!ItemStack.isSameItemSameComponents(before, template)) break;

                int beforeCount = before.getCount();
                ItemStack extracted = handler.extractItem(slot, beforeCount, false);
                ItemStack after = handler.getStackInSlot(slot);
                if (extracted.isEmpty()
                        || ItemStack.isSameItemSameComponents(after, template) && after.getCount() >= beforeCount) {
                    break;
                }
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
