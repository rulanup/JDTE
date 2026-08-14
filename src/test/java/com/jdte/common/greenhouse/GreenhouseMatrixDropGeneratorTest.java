package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixDropGeneratorTest {
    @Test
    void scalesDeterministicOutputsWithoutPerHarvestCalls() {
        GreenhouseCropDefinition definition = new GreenhouseCropDefinition(
                List.of(new ItemStack(Items.WHEAT)),
                ResourceLocation.withDefaultNamespace("wheat"),
                ResourceLocation.withDefaultNamespace("wheat"), false, 4_096,
                GreenhouseRecipe.DEFAULT_FLUID, 10);
        GreenhouseMatrixProductionProfile profile = profile(definition, 2);

        GreenhouseMatrixDropGenerator.Result result = GreenhouseMatrixDropGenerator.generate(
                null, profile, 10L, 8, RandomSource.create(1L));

        assertEquals(0, result.dynamicCalls());
        assertEquals(1, result.drops().size());
        assertTrue(result.drops().getFirst().stack().is(Items.WHEAT));
        assertEquals(12L, result.drops().getFirst().amount());
        assertEquals(GreenhouseRecipe.DEFAULT_FLUID, profile.definition().fluid());
    }

    private static GreenhouseMatrixProductionProfile profile(GreenhouseCropDefinition definition, int fortune) {
        return new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 1,
                GreenhouseMatrixProductionProfile.definitionKey(definition), 1L,
                1, 1, fortune, false, false, 10, 1, 0, 0,
                false, false, definition.growthWork(), 512L, definition, BlockPos.ZERO);
    }
}
