package com.jdte.common.recipes;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifeSynthesisRecipeTest {
    private record Requirement(String item, int count) {
    }

    private static final class Slot {
        private final String item;
        private int count;

        private Slot(String item, int count) {
            this.item = item;
            this.count = count;
        }

        private int count() {
            return count;
        }

        private void shrink(int amount) {
            count -= amount;
        }
    }

    private static Requirement diamondSlot(int count) {
        return new Requirement("diamond", count);
    }

    private static Requirement emeraldSlot(int count) {
        return new Requirement("emerald", count);
    }

    private static List<Slot> slots(Slot... stacks) {
        return new ArrayList<>(List.of(stacks));
    }

    private static Slot stack(String item, int count) {
        return new Slot(item, count);
    }

    private static boolean consume(List<Requirement> requirements, List<Slot> slots) {
        return LifeSynthesisRecipe.consumeStrict(requirements, slots,
                (requirement, slot) -> requirement.item().equals(slot.item), Requirement::count, Slot::count, Slot::shrink);
    }

    private static boolean matches(List<Requirement> requirements, List<Slot> slots) {
        return LifeSynthesisRecipe.matchesStrict(requirements, slots,
                (requirement, slot) -> requirement.item().equals(slot.item), Requirement::count, Slot::count);
    }

    @Test
    void consumeStrictConsumesExactAmounts() {
        List<Slot> inputs = slots(stack("diamond", 3));

        assertTrue(consume(List.of(diamondSlot(3)), inputs));
        assertEquals(0, inputs.get(0).count());
    }

    /** 回归测试：数量不足时必须整体拒绝，不得部分扣减后仍产出整批。 */
    @Test
    void consumeStrictRejectsInsufficientCountsWithoutPartialConsume() {
        List<Slot> inputs = slots(stack("diamond", 2));

        assertFalse(consume(List.of(diamondSlot(3)), inputs));
        assertEquals(2, inputs.get(0).count());
    }

    @Test
    void consumeStrictSpreadsAcrossMultipleSlots() {
        List<Slot> inputs = slots(stack("diamond", 3), stack("diamond", 2));

        assertTrue(consume(List.of(diamondSlot(5)), inputs));
        assertEquals(0, inputs.get(0).count());
        assertEquals(0, inputs.get(1).count());
    }

    /** 任一输入行不满足时整体回滚，先匹配成功的行也不得扣减。 */
    @Test
    void consumeStrictRejectsWhenAnyRowUnavailableWithoutPartialConsume() {
        List<Slot> inputs = slots(stack("diamond", 6));

        assertFalse(consume(List.of(diamondSlot(3), emeraldSlot(3)), inputs));
        assertEquals(6, inputs.get(0).count());
    }

    /** 同一输入槽不得被多行重复扣减（槽独占语义）。 */
    @Test
    void consumeStrictRejectsSharedSlotAcrossRows() {
        List<Slot> inputs = slots(stack("diamond", 6));

        assertFalse(consume(List.of(diamondSlot(3), diamondSlot(3)), inputs));
        assertEquals(6, inputs.get(0).count());
    }

    @Test
    void matchesSlotsAcceptsSufficientCounts() {
        assertTrue(matches(List.of(diamondSlot(3)), slots(stack("diamond", 4))));
    }

    @Test
    void matchesSlotsRejectsInsufficientCounts() {
        assertFalse(matches(List.of(diamondSlot(3)), slots(stack("diamond", 2))));
    }
}
