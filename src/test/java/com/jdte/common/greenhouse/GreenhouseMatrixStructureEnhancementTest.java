package com.jdte.common.greenhouse;

import com.jdte.common.blocks.GreenhouseMatrixStructure;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixStructureEnhancementTest {
    @Test
    void rejectsOnlyTheSeedAndAutomaticCraftingCombination() {
        EnumMap<GreenhouseMatrixEnhancement, Integer> counts = counts();
        counts.put(GreenhouseMatrixEnhancement.SEED_CONVERSION, 1);
        counts.put(GreenhouseMatrixEnhancement.AUTO_CRAFTING, 2);
        assertTrue(GreenhouseMatrixStructure.hasCraftingEnhancementConflict(counts));

        counts.put(GreenhouseMatrixEnhancement.SEED_CONVERSION, 0);
        assertFalse(GreenhouseMatrixStructure.hasCraftingEnhancementConflict(counts));
        counts.put(GreenhouseMatrixEnhancement.SEED_CONVERSION, 1);
        counts.put(GreenhouseMatrixEnhancement.AUTO_CRAFTING, 0);
        assertFalse(GreenhouseMatrixStructure.hasCraftingEnhancementConflict(counts));
    }

    @Test
    void sortsAndDeduplicatesPatternPagesByYThenZThenX() {
        BlockPos y1z0x3 = new BlockPos(3, 1, 0);
        BlockPos y0z4x2 = new BlockPos(2, 0, 4);
        BlockPos y0z4x1 = new BlockPos(1, 0, 4);

        List<BlockPos> sorted = GreenhouseMatrixStructure.sortAutoCraftingPages(List.of(
                y1z0x3, y0z4x2, y0z4x1, y0z4x2));

        assertEquals(List.of(y0z4x1, y0z4x2, y1z0x3), sorted);
    }

    private static EnumMap<GreenhouseMatrixEnhancement, Integer> counts() {
        EnumMap<GreenhouseMatrixEnhancement, Integer> result = new EnumMap<>(GreenhouseMatrixEnhancement.class);
        for (GreenhouseMatrixEnhancement enhancement : GreenhouseMatrixEnhancement.values()) {
            result.put(enhancement, 0);
        }
        return result;
    }
}
