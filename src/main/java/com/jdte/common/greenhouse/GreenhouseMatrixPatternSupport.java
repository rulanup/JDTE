package com.jdte.common.greenhouse;

import com.jdte.common.integrations.ae2.GreenhouseMatrixAE2PatternDecoder;
import com.jdte.common.recipes.GreenhouseCropResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.Optional;

/** Optional-mod-safe facade for matrix pattern decoding. */
public final class GreenhouseMatrixPatternSupport {
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2");

    private GreenhouseMatrixPatternSupport() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    public static boolean isEncodedPattern(ItemStack stack) {
        return AVAILABLE && GreenhouseMatrixAE2PatternDecoder.isEncodedPattern(stack);
    }

    public static Optional<GreenhouseMatrixCraftingRecipe> decode(ItemStack stack, Level level, int sourceIndex) {
        if (!AVAILABLE || stack == null || stack.isEmpty() || level == null) return Optional.empty();
        return GreenhouseMatrixAE2PatternDecoder.decode(stack, level, sourceIndex);
    }

    public static long recipeGeneration() {
        return GreenhouseCropResolver.cacheGeneration();
    }
}
