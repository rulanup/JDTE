package com.jdte.common.greenhouse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreativeGreenhouseOutputCatalogTest {
    @Test
    void catalogDeduplicatesOnlyStacksWithTheSameItemAndComponents() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(4);
        ItemStack ordinary = new ItemStack(Items.WHEAT);
        ItemStack duplicate = ordinary.copyWithCount(32);
        ItemStack named = ordinary.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("creative crop"));

        assertEquals(CreativeGreenhouseOutputCatalog.ReplaceResult.REPLACED,
                catalog.replaceCatalog(List.of(ordinary, duplicate, ItemStack.EMPTY, named)));
        assertEquals(2, catalog.distinctTypes());
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
        assertEquals(Long.MAX_VALUE, catalog.amountAt(1));
        assertTrue(catalog.prototypeAt(0).is(Items.WHEAT));
        assertEquals(Component.literal("creative crop"),
                catalog.prototypeAt(1).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void simulatedAndExecutedExtractionReturnRequestedVirtualCountsWithoutDepletion() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(2);
        catalog.replaceCatalog(List.of(new ItemStack(Items.WHEAT)));
        IItemHandler items = catalog.itemView();

        assertEquals(64, items.getStackInSlot(0).getCount());
        assertEquals(1_000, items.extractItem(0, 1_000, true).getCount());
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
        assertEquals(32, items.extractItem(0, 32, false).getCount());
        assertEquals(64, items.extractItem(0, 64, false).getCount());
        assertEquals(Integer.MAX_VALUE, items.getSlotLimit(0));
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
        assertEquals(1, catalog.distinctTypes());
    }

    @Test
    void virtualExtractionCanReturnTheLargestItemStackCountWithoutDepletion() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(1);
        catalog.replaceCatalog(List.of(new ItemStack(Items.WHEAT)));

        ItemStack extracted = catalog.itemView().extractItem(0, Integer.MAX_VALUE, false);

        assertEquals(Integer.MAX_VALUE, extracted.getCount());
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
    }

    @Test
    void outputViewRejectsInsertionWithoutChangingTheCatalog() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(2);
        catalog.replaceCatalog(List.of(new ItemStack(Items.WHEAT)));
        IItemHandler items = catalog.itemView();
        ItemStack offered = new ItemStack(Items.CARROT, 17);

        ItemStack rejected = items.insertItem(0, offered, false);

        assertTrue(rejected.is(Items.CARROT));
        assertEquals(17, rejected.getCount());
        assertFalse(items.isItemValid(0, offered));
        assertEquals(1, catalog.distinctTypes());
        assertTrue(catalog.prototypeAt(0).is(Items.WHEAT));
    }

    @Test
    void overflowingReplacementLeavesThePreviousCatalogUntouched() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(2);
        catalog.replaceCatalog(List.of(new ItemStack(Items.WHEAT)));

        assertEquals(CreativeGreenhouseOutputCatalog.ReplaceResult.DISTINCT_TYPE_LIMIT_EXCEEDED,
                catalog.replaceCatalog(List.of(
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.POTATO),
                        new ItemStack(Items.BEETROOT))));
        assertEquals(1, catalog.distinctTypes());
        assertTrue(catalog.prototypeAt(0).is(Items.WHEAT));
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
    }

    @Test
    void successfulReplacementRemovesProductsThatAreNoLongerPresent() {
        CreativeGreenhouseOutputCatalog catalog = new CreativeGreenhouseOutputCatalog(3);
        catalog.replaceCatalog(List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.CARROT)));

        assertEquals(CreativeGreenhouseOutputCatalog.ReplaceResult.REPLACED,
                catalog.replaceCatalog(List.of(ItemStack.EMPTY, new ItemStack(Items.POTATO))));
        assertEquals(1, catalog.distinctTypes());
        assertTrue(catalog.prototypeAt(0).is(Items.POTATO));
        assertEquals(Long.MAX_VALUE, catalog.amountAt(0));
    }
}
