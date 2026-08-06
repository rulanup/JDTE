package com.jdte.common.jei.potionbrewer;

import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.utils.InfusionFluidHelper;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public record PotionBrewerJeiRecipe(
        ItemStack inputStack,
        List<List<ItemStack>> ingredientStacks,
        FluidStack fluidStack,
        ItemStack outputStack,
        int energyCost
) {
    public ResourceLocation id() {
        String key = stackKey(inputStack) + '|' + ingredientStacks.stream()
                .map(step -> step.stream().map(PotionBrewerJeiRecipe::stackKey).toList().toString())
                .toList() + '|' + fluidStack + '|' + stackKey(outputStack);
        return ResourceLocation.fromNamespaceAndPath("jdte", "jei/potion_brewer/" + Integer.toUnsignedString(key.hashCode(), 16));
    }
    private static final int BOTTLE_COUNT = AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT;
    private static final int MAX_INGREDIENT_STEPS = 1 + AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT;
    private static final int WATER_FLUID_AMOUNT = InfusionFluidHelper.BOTTLE_FLUID_AMOUNT * BOTTLE_COUNT;

    public static List<PotionBrewerJeiRecipe> getRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return List.of();
        }

        PotionBrewing brewing = minecraft.level.potionBrewing();
        Map<String, PotionBrewerJeiRecipe> recipes = new LinkedHashMap<>();
        Set<String> completeChainOutputs = new HashSet<>();
        List<Transition> transitions = getTransitions(brewing);
        Map<String, List<Transition>> transitionsByInput = getTransitionsByInput(transitions);

        addCompleteChainRecipes(recipes, completeChainOutputs, transitionsByInput);
        addFallbackBrewingRecipes(recipes, completeChainOutputs, transitions);
        return List.copyOf(recipes.values());
    }

    public static List<ItemStack> getMachines() {
        return List.of(new ItemStack(JDTEBlocks.ADVANCED_POTION_BREWER.get()));
    }

    public boolean hasIngredientInput() {
        return !ingredientStacks.isEmpty();
    }

    public boolean hasFluidInput() {
        return !fluidStack.isEmpty();
    }

    private static void addCompleteChainRecipes(Map<String, PotionBrewerJeiRecipe> recipes,
                                                Set<String> completeChainOutputs,
                                                Map<String, List<Transition>> transitionsByInput) {
        ItemStack waterBottle = single(PotionContents.createItemStack(Items.POTION, Potions.WATER));
        addCompleteChainRecipe(recipes, completeChainOutputs, List.of(), waterBottle);

        Queue<ChainNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new ChainNode(waterBottle, List.of()));
        visited.add(stackKey(waterBottle));

        while (!queue.isEmpty()) {
            ChainNode node = queue.remove();
            if (node.ingredients().size() >= MAX_INGREDIENT_STEPS) {
                continue;
            }

            for (Transition transition : transitionsByInput.getOrDefault(stackKey(node.stack()), List.of())) {
                String outputKey = stackKey(transition.output());
                if (!visited.add(outputKey)) {
                    continue;
                }

                List<List<ItemStack>> nextIngredients = appendIngredientOptions(node.ingredients(), transition.ingredients());
                addCompleteChainRecipe(recipes, completeChainOutputs, nextIngredients, transition.output());
                queue.add(new ChainNode(transition.output(), nextIngredients));
            }
        }
    }

    private static void addFallbackBrewingRecipes(Map<String, PotionBrewerJeiRecipe> recipes,
                                                  Set<String> completeChainOutputs,
                                                  List<Transition> transitions) {
        for (Transition transition : transitions) {
            if (completeChainOutputs.contains(stackKey(transition.output()))) {
                continue;
            }
            addRecipe(recipes, transition.input(), List.of(transition.ingredients()), FluidStack.EMPTY, transition.output());
        }
    }

    private static List<Transition> getTransitions(PotionBrewing brewing) {
        List<ItemStack> inputs = getCandidateInputs(brewing);
        List<ItemStack> ingredients = getCandidateIngredients(brewing);
        Map<String, TransitionBuilder> transitions = new LinkedHashMap<>();

        for (ItemStack input : inputs) {
            for (ItemStack ingredient : ingredients) {
                if (!brewing.hasMix(input, ingredient)) {
                    continue;
                }
                ItemStack output = single(brewing.mix(ingredient, input.copy()));
                if (output.isEmpty() || ItemStack.isSameItemSameComponents(input, output)) {
                    continue;
                }

                String key = stackKey(input) + ">" + stackKey(output);
                transitions.computeIfAbsent(key, ignored -> new TransitionBuilder(input, output))
                        .addIngredient(ingredient);
            }
        }

        return transitions.values().stream()
                .map(TransitionBuilder::build)
                .toList();
    }

    private static Map<String, List<Transition>> getTransitionsByInput(List<Transition> transitions) {
        Map<String, List<Transition>> byInput = new LinkedHashMap<>();
        for (Transition transition : transitions) {
            byInput.computeIfAbsent(stackKey(transition.input()), ignored -> new ArrayList<>())
                    .add(transition);
        }
        return byInput;
    }

    private static List<ItemStack> getCandidateInputs(PotionBrewing brewing) {
        Map<String, ItemStack> inputs = new LinkedHashMap<>();

        addPotionContainerInputs(inputs, brewing, Items.POTION);
        addPotionContainerInputs(inputs, brewing, Items.SPLASH_POTION);
        addPotionContainerInputs(inputs, brewing, Items.LINGERING_POTION);

        for (Item item : sortedItems()) {
            ItemStack stack = single(new ItemStack(item));
            if (!stack.isEmpty() && brewing.isInput(stack)) {
                addStack(inputs, stack);
            }
        }

        for (IBrewingRecipe recipe : brewing.getRecipes()) {
            if (recipe instanceof BrewingRecipe simpleRecipe) {
                addIngredientStacks(inputs, simpleRecipe.getInput());
            }
        }

        return List.copyOf(inputs.values());
    }

    private static void addPotionContainerInputs(Map<String, ItemStack> inputs, PotionBrewing brewing, Item containerItem) {
        for (Holder<Potion> potion : sortedPotions()) {
            ItemStack stack = single(PotionContents.createItemStack(containerItem, potion));
            if (!stack.isEmpty() && brewing.isInput(stack)) {
                addStack(inputs, stack);
            }
        }
    }

    private static List<ItemStack> getCandidateIngredients(PotionBrewing brewing) {
        Map<String, ItemStack> ingredients = new LinkedHashMap<>();

        for (Item item : sortedItems()) {
            ItemStack stack = single(new ItemStack(item));
            if (!stack.isEmpty() && brewing.isIngredient(stack)) {
                addStack(ingredients, stack);
            }
        }

        for (IBrewingRecipe recipe : brewing.getRecipes()) {
            if (recipe instanceof BrewingRecipe simpleRecipe) {
                addIngredientStacks(ingredients, simpleRecipe.getIngredient());
            }
        }

        return List.copyOf(ingredients.values());
    }

    private static void addIngredientStacks(Map<String, ItemStack> stacks, Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            ItemStack singleStack = single(stack);
            if (!singleStack.isEmpty()) {
                addStack(stacks, singleStack);
            }
        }
    }

    private static List<Item> sortedItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()))
                .toList();
    }

    private static List<Holder.Reference<Potion>> sortedPotions() {
        return BuiltInRegistries.POTION.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .toList();
    }

    private static void addCompleteChainRecipe(Map<String, PotionBrewerJeiRecipe> recipes,
                                               Set<String> completeChainOutputs,
                                               List<List<ItemStack>> ingredients,
                                               ItemStack output) {
        addRecipe(recipes,
                new ItemStack(Items.GLASS_BOTTLE),
                ingredients,
                new FluidStack(Fluids.WATER, WATER_FLUID_AMOUNT),
                output);
        completeChainOutputs.add(stackKey(output));
    }

    private static void addRecipe(Map<String, PotionBrewerJeiRecipe> recipes,
                                  ItemStack input, List<List<ItemStack>> ingredients, FluidStack fluid, ItemStack output) {
        ItemStack normalizedInput = single(input);
        List<List<ItemStack>> normalizedIngredients = normalizeIngredientOptions(ingredients);
        ItemStack normalizedOutput = single(output);
        FluidStack normalizedFluid = fluid.copy();
        if (normalizedInput.isEmpty() || normalizedOutput.isEmpty()) {
            return;
        }
        if (normalizedIngredients.isEmpty() && normalizedFluid.isEmpty()) {
            return;
        }

        PotionBrewerJeiRecipe recipe = new PotionBrewerJeiRecipe(
                normalizedInput,
                normalizedIngredients,
                normalizedFluid,
                normalizedOutput,
                AdvancedPotionBrewerBE.BASE_ENERGY_COST
        );
        recipes.putIfAbsent(recipeKey(recipe), recipe);
    }

    private static void addStack(Map<String, ItemStack> stacks, ItemStack stack) {
        ItemStack normalized = single(stack);
        if (!normalized.isEmpty()) {
            stacks.putIfAbsent(stackKey(normalized), normalized);
        }
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy.is(Items.AIR) ? ItemStack.EMPTY : copy;
    }

    private static List<List<ItemStack>> appendIngredientOptions(List<List<ItemStack>> ingredients, List<ItemStack> ingredientOptions) {
        List<List<ItemStack>> next = new ArrayList<>(ingredients.size() + 1);
        for (List<ItemStack> stackOptions : ingredients) {
            next.add(normalizeIngredients(stackOptions));
        }
        next.add(normalizeIngredients(ingredientOptions));
        return List.copyOf(next);
    }

    private static List<ItemStack> normalizeIngredients(List<ItemStack> ingredients) {
        List<ItemStack> normalized = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            ItemStack stack = single(ingredient);
            if (!stack.isEmpty()) {
                normalized.add(stack);
            }
        }
        return List.copyOf(normalized);
    }

    private static List<List<ItemStack>> normalizeIngredientOptions(List<List<ItemStack>> ingredients) {
        List<List<ItemStack>> normalized = new ArrayList<>();
        for (List<ItemStack> ingredientOptions : ingredients) {
            List<ItemStack> options = normalizeIngredients(ingredientOptions);
            if (!options.isEmpty()) {
                normalized.add(options);
            }
        }
        return List.copyOf(normalized);
    }

    private static String recipeKey(PotionBrewerJeiRecipe recipe) {
        return stackKey(recipe.inputStack)
                + "|" + ingredientListKey(recipe.ingredientStacks)
                + "|" + fluidKey(recipe.fluidStack)
                + "|" + stackKey(recipe.outputStack);
    }

    private static String ingredientListKey(List<List<ItemStack>> ingredients) {
        StringBuilder key = new StringBuilder();
        for (List<ItemStack> ingredientOptions : ingredients) {
            if (!key.isEmpty()) {
                key.append(",");
            }
            key.append("[");
            key.append(ingredientOptions.stream().map(PotionBrewerJeiRecipe::stackKey).reduce((left, right) -> left + ";" + right).orElse(""));
            key.append("]");
        }
        return key.toString();
    }

    private static String stackKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "@" + stack.getCount() + "@" + stack.getComponents();
    }

    private static String fluidKey(FluidStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        return BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "@" + stack.getAmount();
    }

    private record ChainNode(ItemStack stack, List<List<ItemStack>> ingredients) {
    }

    private record Transition(ItemStack input, ItemStack output, List<ItemStack> ingredients) {
    }

    private static class TransitionBuilder {
        private final ItemStack input;
        private final ItemStack output;
        private final Map<String, ItemStack> ingredients = new LinkedHashMap<>();

        private TransitionBuilder(ItemStack input, ItemStack output) {
            this.input = single(input);
            this.output = single(output);
        }

        private void addIngredient(ItemStack ingredient) {
            ItemStack stack = single(ingredient);
            if (!stack.isEmpty()) {
                ingredients.putIfAbsent(stackKey(stack), stack);
            }
        }

        private Transition build() {
            return new Transition(input, output, List.copyOf(ingredients.values()));
        }
    }
}
