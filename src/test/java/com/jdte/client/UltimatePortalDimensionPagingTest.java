package com.jdte.client;

import com.jdte.client.screens.UltimatePortalDimensionPaging;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UltimatePortalDimensionPagingTest {
    @Test
    void dimensionPagingSplitsLargeListsIntoEightItemPages() {
        List<String> dimensions = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");

        assertEquals(2, UltimatePortalDimensionPaging.pageCount(dimensions.size()));
        assertEquals(0, UltimatePortalDimensionPaging.clampPage(-1, dimensions.size()));
        assertEquals(1, UltimatePortalDimensionPaging.clampPage(99, dimensions.size()));
        assertEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h"),
                UltimatePortalDimensionPaging.pageItems(dimensions, 0));
        assertEquals(List.of("i", "j"), UltimatePortalDimensionPaging.pageItems(dimensions, 1));
    }
}
