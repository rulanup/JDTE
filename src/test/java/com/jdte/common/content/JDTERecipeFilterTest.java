package com.jdte.common.content;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDTERecipeFilterTest {
    @Test
    void filterRemovesDisabledIdsBeforeRecipeDecoding() {
        JDTEContentControl control = new JDTEContentControl(
                List.of(ContentSelection.parse("greenhouse")),
                List.of(ContentSelection.parse("minecraft:unused")),
                true, true, true);
        Map<ResourceLocation, com.google.gson.JsonElement> recipes = new LinkedHashMap<>();
        recipes.put(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse/wheat"), new JsonObject());
        recipes.put(ResourceLocation.fromNamespaceAndPath("minecraft", "unused"), new JsonObject());
        recipes.put(ResourceLocation.fromNamespaceAndPath("jdte", "extended_clicker"), new JsonObject());

        JDTERecipeFilter.filterDisabledRecipes(recipes, control);

        assertFalse(recipes.containsKey(ResourceLocation.fromNamespaceAndPath("jdte", "greenhouse/wheat")));
        assertFalse(recipes.containsKey(ResourceLocation.fromNamespaceAndPath("minecraft", "unused")));
        assertTrue(recipes.containsKey(ResourceLocation.fromNamespaceAndPath("jdte", "extended_clicker")));
    }
}
