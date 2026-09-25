package com.jdte.common.items;
import com.jdte.common.manager.AEExtractionPlayerManager;

import com.jdte.setup.JDTEDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AEExtractionPlayerServiceTest {
    @Test
    void markedStackIsCollectedOnceByIdentity() {
        ItemStack marked = marked(new ItemStack(Items.STICK));
        assertEquals(List.of(marked), AEExtractionPlayerManager.collectDistinct(
                List.of(marked, ItemStack.EMPTY, marked), List.of(marked)));
    }

    @Test
    void unmarkedIsIgnoredAndEqualContentsDifferentInstancesRemain() {
        ItemStack first = marked(new ItemStack(Items.STICK));
        ItemStack second = marked(new ItemStack(Items.STICK));
        assertEquals(List.of(first, second), AEExtractionPlayerManager.collectDistinct(
                List.of(new ItemStack(Items.STICK), first), List.of(second)));
    }

    private static ItemStack marked(ItemStack stack) {
        stack.set(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), true);
        return stack;
    }
}
