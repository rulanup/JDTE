package com.jdte.common.content;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentSelectionTest {
    @Test
    void parsesBareIdsUsingTheJdteNamespace() {
        ContentSelection selection = ContentSelection.parse("greenhouse");

        assertEquals(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse"), selection.id());
        assertTrue(selection.matches(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse/wheat")));
        assertFalse(selection.matches(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse2")));
    }

    @Test
    void preservesNamespacedIds() {
        ContentSelection selection = ContentSelection.parse("minecraft:oak_log");

        assertEquals(ResourceLocation.fromNamespaceAndPath("minecraft", "oak_log"), selection.id());
        assertTrue(selection.matches(ResourceLocation.fromNamespaceAndPath("minecraft", "oak_log")));
        assertFalse(selection.matches(ResourceLocation.fromNamespaceAndPath("minecraft", "oak_planks")));
    }

    @Test
    void rejectsInvalidIds() {
        assertThrows(IllegalArgumentException.class, () -> ContentSelection.parse("bad id"));
    }
}
