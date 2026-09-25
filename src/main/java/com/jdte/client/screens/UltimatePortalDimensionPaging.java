package com.jdte.client.screens;

import java.util.List;

public final class UltimatePortalDimensionPaging {
    public static final int ITEMS_PER_PAGE = 8;

    private UltimatePortalDimensionPaging() {
    }

    public static int pageCount(int itemCount) {
        return Math.max(1, (Math.max(0, itemCount) + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    public static int clampPage(int page, int itemCount) {
        return Math.max(0, Math.min(page, pageCount(itemCount) - 1));
    }

    public static <T> List<T> pageItems(List<T> items, int page) {
        int safePage = clampPage(page, items.size());
        int from = safePage * ITEMS_PER_PAGE;
        int to = Math.min(items.size(), from + ITEMS_PER_PAGE);
        return List.copyOf(items.subList(from, to));
    }
}
