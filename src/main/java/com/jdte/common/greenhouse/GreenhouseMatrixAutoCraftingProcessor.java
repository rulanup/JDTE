package com.jdte.common.greenhouse;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Applies one non-recursive automatic-crafting settlement against a long-count output buffer. */
public final class GreenhouseMatrixAutoCraftingProcessor {
    private GreenhouseMatrixAutoCraftingProcessor() {
    }

    public static Result process(GreenhouseMatrixOutputBuffer buffer,
                                 List<GreenhouseMatrixCraftingRecipe> recipes,
                                 int cursor) {
        if (buffer == null || recipes == null || recipes.isEmpty()) return new Result(false, cursor);
        Map<StackKey, Long> startingAmounts = new LinkedHashMap<>();
        for (GreenhouseMatrixDropGenerator.Drop drop : buffer.snapshotDrops()) {
            startingAmounts.put(StackKey.of(drop.stack()), drop.amount());
        }
        Map<StackKey, List<GreenhouseMatrixCraftingRecipe>> groups = new LinkedHashMap<>();
        for (GreenhouseMatrixCraftingRecipe recipe : recipes) {
            groups.computeIfAbsent(StackKey.of(recipe.input()), ignored -> new ArrayList<>()).add(recipe);
        }

        boolean changed = false;
        int nextCursor = cursor;
        for (Map.Entry<StackKey, List<GreenhouseMatrixCraftingRecipe>> entry : groups.entrySet()) {
            long available = startingAmounts.getOrDefault(entry.getKey(), 0L);
            if (available <= 0L) continue;
            GreenhouseMatrixCraftingPlanner.Plan plan = GreenhouseMatrixCraftingPlanner.plan(
                    available, buffer.remainingCapacity(), entry.getValue(), nextCursor);
            if (!plan.valid()) continue;
            List<GreenhouseMatrixOutputBuffer.Transformation> transformations = new ArrayList<>();
            for (GreenhouseMatrixCraftingPlanner.Allocation allocation : plan.allocations()) {
                if (allocation.crafts() <= 0L) continue;
                transformations.add(new GreenhouseMatrixOutputBuffer.Transformation(
                        allocation.recipe().input(), allocation.consumedInput(),
                        allocation.recipe().output(), allocation.producedOutput()));
            }
            if (transformations.isEmpty() || !buffer.applyCraftingBatch(transformations)) continue;
            changed = true;
            nextCursor = plan.nextCursor();
        }
        return new Result(changed, nextCursor);
    }

    public record Result(boolean changed, int nextCursor) {
    }

    private record StackKey(Item item, DataComponentPatch components) {
        private static StackKey of(ItemStack stack) {
            return new StackKey(stack.getItem(), stack.getComponentsPatch());
        }
    }
}
