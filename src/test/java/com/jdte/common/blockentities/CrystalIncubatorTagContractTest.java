package com.jdte.common.blockentities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalIncubatorTagContractTest {
    private static final String TAG_PATH =
            "data/jdte/tags/block/crystal_incubator_harvestable_crystals.json";

    @Test
    void neoEcoImmatureBudsAreExcludedFromHarvestableCrystals() {
        JsonObject tag = json(TAG_PATH);
        JsonArray removals = tag.getAsJsonArray("remove");

        assertNotNull(removals, "The broad c:clusters import must exclude Neo ECO's immature buds");
        Set<String> removedIds = removals.asList().stream()
                .map(CrystalIncubatorTagContractTest::entryId)
                .collect(Collectors.toSet());

        assertTrue(removedIds.containsAll(Set.of(
                "neoecoae:small_energized_crystal_bud",
                "neoecoae:medium_energized_crystal_bud",
                "neoecoae:large_energized_crystal_bud"
        )));
        assertFalse(removedIds.contains("neoecoae:energized_crystal_cluster"));
    }

    private static String entryId(JsonElement entry) {
        return entry.isJsonPrimitive()
                ? entry.getAsString()
                : entry.getAsJsonObject().get("id").getAsString();
    }

    private static JsonObject json(String path) {
        InputStream resource = CrystalIncubatorTagContractTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(resource, path);
        return JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
