package com.jdte.common.greenhouse;

import com.jdte.common.blockentities.GreenhouseMatrixAutoCraftingBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Immutable decoded view of all pattern pages in stable structure order. */
public final class GreenhouseMatrixAutoCraftingCatalog {
    private static final int SLOTS_PER_PAGE = 16;

    private GreenhouseMatrixAutoCraftingCatalog() {
    }

    public static Snapshot capture(Level level, List<BlockPos> positions) {
        List<PageInput> pages = new ArrayList<>(positions.size());
        for (BlockPos pos : positions) {
            List<ItemStack> stacks = new ArrayList<>(SLOTS_PER_PAGE);
            if (level.getBlockEntity(pos) instanceof GreenhouseMatrixAutoCraftingBE page) {
                for (int slot = 0; slot < SLOTS_PER_PAGE; slot++) {
                    stacks.add(page.patterns().getStackInSlot(slot).copy());
                }
            } else {
                for (int slot = 0; slot < SLOTS_PER_PAGE; slot++) stacks.add(ItemStack.EMPTY);
            }
            pages.add(new PageInput(pos, stacks));
        }
        return build(pages, GreenhouseMatrixPatternSupport.recipeGeneration(),
                (stack, sourceIndex) -> GreenhouseMatrixPatternSupport.decode(stack, level, sourceIndex));
    }

    public static Snapshot build(List<PageInput> pages, long recipeGeneration, PatternDecoder decoder) {
        List<PageInput> safePages = pages == null ? List.of() : List.copyOf(pages);
        List<Integer> invalidMasks = new ArrayList<>(safePages.size());
        List<GreenhouseMatrixCraftingRecipe> recipes = new ArrayList<>();
        for (int pageIndex = 0; pageIndex < safePages.size(); pageIndex++) {
            PageInput page = safePages.get(pageIndex);
            int invalidMask = 0;
            for (int slot = 0; slot < SLOTS_PER_PAGE; slot++) {
                ItemStack stack = page.patterns().get(slot);
                if (stack.isEmpty()) continue;
                int sourceIndex = pageIndex * SLOTS_PER_PAGE + slot;
                Optional<GreenhouseMatrixCraftingRecipe> decoded = decoder.decode(stack.copy(), sourceIndex);
                if (decoded.isPresent()) recipes.add(decoded.orElseThrow());
                else invalidMask |= 1 << slot;
            }
            invalidMasks.add(invalidMask);
        }
        return new Snapshot(safePages, List.copyOf(recipes), List.copyOf(invalidMasks), recipeGeneration);
    }

    @FunctionalInterface
    public interface PatternDecoder {
        Optional<GreenhouseMatrixCraftingRecipe> decode(ItemStack stack, int sourceIndex);
    }

    public record PageInput(BlockPos position, List<ItemStack> patterns) {
        public PageInput {
            if (position == null) throw new IllegalArgumentException("position must not be null");
            if (patterns == null || patterns.size() != SLOTS_PER_PAGE) {
                throw new IllegalArgumentException("a pattern page must contain exactly 16 slots");
            }
            position = position.immutable();
            List<ItemStack> copies = new ArrayList<>(SLOTS_PER_PAGE);
            for (ItemStack pattern : patterns) copies.add(pattern == null ? ItemStack.EMPTY : pattern.copy());
            patterns = List.copyOf(copies);
        }
    }

    public record Snapshot(List<PageInput> pages, List<GreenhouseMatrixCraftingRecipe> recipes,
                           List<Integer> invalidMasks, long recipeGeneration) {
        public int pageCount() {
            return pages.size();
        }

        public int invalidMask(int page) {
            return page >= 0 && page < invalidMasks.size() ? invalidMasks.get(page) : 0;
        }

        public static Snapshot empty(long generation) {
            return new Snapshot(List.of(), List.of(), List.of(), generation);
        }
    }
}
