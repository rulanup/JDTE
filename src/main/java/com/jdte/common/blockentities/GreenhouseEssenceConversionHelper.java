package com.jdte.common.blockentities;

import com.jdte.common.recipes.MachineRecipeHelper;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.CraftingPatternData;
import com.jdte.common.items.EssenceConversionUpgradeItem;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Resolves unambiguous, essence-only crafting recipes and applies their ratios to greenhouse output. */
public final class GreenhouseEssenceConversionHelper {
    private static final TagKey<Item> MYSTICAL_ESSENCES = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath("mysticalagriculture", "essences"));
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

    static boolean convertStored(ServerLevel level, GreenhouseOutputInventory handler, BaseMachineBE machine) {
        if (patterns(machine).isEmpty()) return convertStored(level, handler);
        List<ItemStack> before = new ArrayList<>();
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemStack stack = handler.copyStack(slot);
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
        if (!handler.replace(before, after)) return false;
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

    static boolean convertStored(ServerLevel level, GreenhouseOutputInventory outputHandler) {
        List<ItemStack> essenceTypes = new ArrayList<>();
        for (int slot = 0; slot < outputHandler.size(); slot++) {
            ItemStack stack = outputHandler.copyStack(slot);
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
            changed |= outputHandler.replace(List.of(essence), replacement);
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
        for (var holder : MachineRecipeHelper.recipes(level).byType(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            if (!usesEssence(recipe, essence)) continue;
            if (++matchingRecipes > 1) return Optional.empty();
            candidate = analyzeRecipe(level, recipe, essence);
        }
        return matchingRecipes == 1 ? Optional.ofNullable(candidate) : Optional.empty();
    }

    private static boolean usesEssence(CraftingRecipe recipe, ItemStack essence) {
        for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
            if (ingredient.test(essence)) return true;
        }
        return false;
    }

    private static Conversion analyzeRecipe(ServerLevel level, CraftingRecipe recipe, ItemStack essence) {
        PlacementInfo placement = recipe.placementInfo();
        if (placement.isImpossibleToPlace()) return null;
        for (Ingredient ingredient : placement.ingredients()) {
            var choices = ingredient.items().limit(2).toList();
            if (choices.size() != 1 || !ingredient.test(essence)
                    || !ItemStack.isSameItemSameComponents(new ItemStack(choices.getFirst()), essence)) return null;
        }
        var indices = placement.slotsToIngredientIndex();
        if (indices.isEmpty() || indices.size() > 9) return null;
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : Math.min(3, indices.size());
        int height = Math.ceilDiv(indices.size(), width);
        List<ItemStack> grid = new ArrayList<>(width * height);
        for (int slot = 0; slot < width * height; slot++) {
            grid.add(slot < indices.size() && indices.getInt(slot) != PlacementInfo.EMPTY_SLOT
                    ? essence.copyWithCount(1) : ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(width, height, grid);
        if (!recipe.matches(input, level)) return null;
        ItemStack result = recipe.assemble(input);
        if (result.isEmpty() || ItemStack.isSameItemSameComponents(result, essence)) return null;
        return new Conversion(input.ingredientCount(), result);
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

    private static long count(GreenhouseOutputInventory handler, ItemStack template) {
        long count = 0L;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemStack stack = handler.copyStack(slot);
            if (ItemStack.isSameItemSameComponents(stack, template)) count += stack.getCount();
        }
        return count;
    }


    private record StackKey(Item item, DataComponentPatch components) {
    }

    private record Conversion(int essenceCount, ItemStack result) {
    }
}
