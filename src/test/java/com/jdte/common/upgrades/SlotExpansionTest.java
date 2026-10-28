package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SlotExpansionTest {
    private record Stack(String id, int count, Map<String,String> tag) {
        boolean empty() { return count == 0; }
        Stack copy() { return new Stack(id, count, new HashMap<>(tag)); }
    }
    private Stack empty() { return new Stack("air", 0, new HashMap<>()); }
    private List<Stack> destination(int size) {
        var result = new ArrayList<Stack>(); for (int i=0;i<size;i++) result.add(empty()); return result;
    }
    @Test void fourSlotsExpandToEightWithTheSamePositionsAndTags() {
        var source=List.of(new Stack("capacity",1,Map.of("owner","a")),empty(),empty(),new Stack("read",1,Map.of("link","b")));
        var expanded=SlotExpansion.plan(source,destination(8),Stack::empty,Stack::copy);
        assertEquals(8,expanded.size());assertEquals(source,expanded.subList(0,4));
        for(int i=4;i<8;i++)assertTrue(expanded.get(i).empty());
    }
    @Test void resultHasNoSharedMutableNbtWithSourceOrDestination() {
        var source=List.of(new Stack("card",1,new HashMap<>(Map.of("owner","a"))));var target=destination(8);
        var result=SlotExpansion.plan(source,target,Stack::empty,Stack::copy);
        result.get(0).tag().put("owner","b");result.get(7).tag().put("new","x");
        assertEquals("a",source.get(0).tag().get("owner"));assertTrue(target.get(7).tag().isEmpty());
    }
    @Test void occupiedInactiveTargetIsRejectedWithoutMutatingEitherSide() {
        var source=List.of(new Stack("first",1,new HashMap<>()));var target=destination(8);target.set(7,new Stack("hidden",1,new HashMap<>()));
        assertThrows(IllegalArgumentException.class,()->SlotExpansion.plan(source,target,Stack::empty,Stack::copy));
        assertEquals("first",source.get(0).id());assertEquals("hidden",target.get(7).id());
    }
    @Test void shrinkingIsRejectedBeforeAnyMutation() {
        var source=destination(8);assertThrows(IllegalArgumentException.class,()->SlotExpansion.plan(source,destination(4),Stack::empty,Stack::copy));assertEquals(8,source.size());
    }
    @Test void emptyUpgradePresetsRemainEmptyAndKeepEightSlots() {
        var result=SlotExpansion.plan(destination(4),destination(8),Stack::empty,Stack::copy);assertEquals(8,result.size());assertTrue(result.stream().allMatch(Stack::empty));
    }
}
