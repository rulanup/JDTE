package com.jdte.common.integrations.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import com.jdte.common.greenhouse.GreenhouseMatrixCraftingRecipe;
import com.jdte.common.greenhouse.GreenhouseMatrixPatternRecipeValidator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** AE2-specific implementation kept behind {@code GreenhouseMatrixPatternSupport}. */
public final class GreenhouseMatrixAE2PatternDecoder {
    private static final int GRID_SIZE = 9;

    private GreenhouseMatrixAE2PatternDecoder() {
    }

    public static boolean isEncodedPattern(ItemStack stack) {
        return stack != null && !stack.isEmpty() && PatternDetailsHelper.isEncodedPattern(stack);
    }

    public static Optional<GreenhouseMatrixCraftingRecipe> decode(ItemStack stack, Level level, int sourceIndex) {
        try {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, level);
            if (!(details instanceof AECraftingPattern pattern)) return Optional.empty();
            List<GenericStack> sparseInputs = pattern.getSparseInputs();
            if (sparseInputs.size() != GRID_SIZE) return Optional.empty();

            List<ItemStack> grid = new ArrayList<>(GRID_SIZE);
            for (GenericStack generic : sparseInputs) {
                if (generic == null) {
                    grid.add(ItemStack.EMPTY);
                    continue;
                }
                if (generic.amount() <= 0L || !(generic.what() instanceof AEItemKey itemKey)) return Optional.empty();
                grid.add(itemKey.toStack());
            }
            CraftingInput input = CraftingInput.ofPositioned(3, 3, grid).input();
            ItemStack actualOutput = pattern.assemble(input, level);
            NonNullList<ItemStack> remaining = pattern.getRemainingItems(input);
            GenericStack primary = pattern.getPrimaryOutput();
            if (primary == null || primary.amount() <= 0L || primary.amount() > Integer.MAX_VALUE
                    || !(primary.what() instanceof AEItemKey outputKey)) {
                return Optional.empty();
            }
            ItemStack claimedOutput = outputKey.toStack((int) primary.amount());
            return GreenhouseMatrixPatternRecipeValidator.validate(
                    grid, claimedOutput, actualOutput, remaining, sourceIndex);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
