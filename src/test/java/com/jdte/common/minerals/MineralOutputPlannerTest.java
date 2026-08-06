package com.jdte.common.minerals;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralOutputPlannerTest {
    private static final ResourceLocation IRON = id("iron_ore");
    private static final ResourceLocation GOLD = id("gold_ore");
    private static final ResourceLocation LIMITED = id("limited_ore");

    @Test
    void selectsPartialBatchWhenBacklogExceedsEmptyInventory() {
        List<MineralOutputPlanner.SlotState> slots = emptySlots(16, 64);

        long cycles = MineralOutputPlanner.findMaxFitting(2_048L,
                candidate -> MineralOutputPlanner.plan(slots, Map.of(IRON, candidate), Map.of(IRON, 64)));

        assertEquals(1_024L, cycles);
    }

    @Test
    void reportsNoFittingWorkOnlyWhenInventoryHasNoSpace() {
        List<MineralOutputPlanner.SlotState> slots = List.of(
                new MineralOutputPlanner.SlotState(GOLD, 64, 64));

        long cycles = MineralOutputPlanner.findMaxFitting(100L,
                candidate -> MineralOutputPlanner.plan(slots, Map.of(IRON, candidate), Map.of(IRON, 64)));

        assertEquals(0L, cycles);
    }

    @Test
    void fillsMatchingStacksBeforeUsingEmptySlots() {
        List<MineralOutputPlanner.SlotState> slots = List.of(
                new MineralOutputPlanner.SlotState(IRON, 60, 64),
                MineralOutputPlanner.SlotState.empty(64));

        MineralOutputPlanner.Plan plan = MineralOutputPlanner.plan(
                slots, Map.of(IRON, 10L), Map.of(IRON, 64));

        assertTrue(plan.fits());
        assertEquals(64, plan.slots().get(0).count());
        assertEquals(6, plan.slots().get(1).count());
    }

    @Test
    void respectsMultipleMineralsAndLowItemStackLimits() {
        List<MineralOutputPlanner.SlotState> slots = emptySlots(3, 64);
        Map<ResourceLocation, Long> amounts = new LinkedHashMap<>();
        amounts.put(IRON, 64L);
        amounts.put(LIMITED, 32L);

        MineralOutputPlanner.Plan plan = MineralOutputPlanner.plan(
                slots, amounts, Map.of(IRON, 64, LIMITED, 16));

        assertTrue(plan.fits());
        assertEquals(List.of(64, 16, 16), plan.slots().stream()
                .map(MineralOutputPlanner.SlotState::count).toList());
        assertFalse(MineralOutputPlanner.plan(
                slots, Map.of(LIMITED, 49L), Map.of(LIMITED, 16)).fits());
    }

    @Test
    void failedPlanLeavesOriginalSnapshotUntouched() {
        List<MineralOutputPlanner.SlotState> slots = List.of(
                new MineralOutputPlanner.SlotState(IRON, 63, 64));

        MineralOutputPlanner.Plan plan = MineralOutputPlanner.plan(
                slots, Map.of(IRON, 2L), Map.of(IRON, 64));

        assertFalse(plan.fits());
        assertEquals(63, plan.slots().getFirst().count());
        assertEquals(63, slots.getFirst().count());
    }

    private static List<MineralOutputPlanner.SlotState> emptySlots(int count, int limit) {
        List<MineralOutputPlanner.SlotState> slots = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) slots.add(MineralOutputPlanner.SlotState.empty(limit));
        return List.copyOf(slots);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("test", path);
    }
}