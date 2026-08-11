package com.jdte.common.greenhouse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GreenhouseMatrixProductionGroupTest {
    @Test
    void mergesThreeThousandIdenticalLanesIntoOneGroup() {
        GreenhouseMatrixProductionProfile profile = profile(new ItemStack(Items.WHEAT_SEEDS), 64, 1, 0);
        Map<GreenhouseMatrixProductionProfile, GreenhouseMatrixProductionGroup> groups = new HashMap<>();

        IntStream.range(0, 3_000).forEach(ignored ->
                groups.computeIfAbsent(profile, GreenhouseMatrixProductionGroup::new).addUnit());

        assertEquals(1, groups.size());
        assertEquals(3_000, groups.get(profile).units());
    }

    @Test
    void doesNotMergeDifferentComponentsCountsOrMachineSettings() {
        ItemStack ordinary = new ItemStack(Items.WHEAT_SEEDS);
        ItemStack named = ordinary.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("different"));

        assertNotEquals(profile(ordinary, 64, 1, 0), profile(named, 64, 1, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 32, 1, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 64, 2, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 64, 1, 1));
    }

    private static GreenhouseMatrixProductionProfile profile(ItemStack seed, int templates,
                                                              int selectedMultiplier, int fortune) {
        return new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                seed, templates, "minecraft:wheat", 7L,
                selectedMultiplier, 1, fortune, false, false,
                10, 1, 2, 1, true, true, 4_096, 512L);
    }
}
