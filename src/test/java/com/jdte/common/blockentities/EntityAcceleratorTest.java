package com.jdte.common.blockentities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.setup.config.EntityAcceleratorConfig;
import com.jdte.common.upgrades.UpgradeType;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityAcceleratorTest {

    private static Path source(String path) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(path);
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        return Path.of(path);
    }

    private static JsonObject read(String relative) throws IOException {
        try (Reader reader = Files.newBufferedReader(source(relative), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Test
    void resourcesAndContractsHonored() throws IOException {
        // 1. Recipes
        JsonObject advancedRecipe = read("src/main/resources/data/jdte/recipe/advanced_entity_accelerator.json");
        assertEquals("minecraft:crafting_shaped", advancedRecipe.get("type").getAsString());
        assertEquals("jdte:advanced_entity_accelerator", advancedRecipe.getAsJsonObject("result").get("id").getAsString());

        JsonObject extendedRecipe = read("src/main/resources/data/jdte/recipe/extended_entity_accelerator.json");
        assertEquals("minecraft:crafting_shaped", extendedRecipe.get("type").getAsString());
        assertEquals("jdte:extended_entity_accelerator", extendedRecipe.getAsJsonObject("result").get("id").getAsString());

        // 2. Loot Tables
        JsonObject advancedLoot = read("src/main/resources/data/jdte/loot_table/blocks/advanced_entity_accelerator.json");
        assertEquals("minecraft:block", advancedLoot.get("type").getAsString());
        JsonObject extendedLoot = read("src/main/resources/data/jdte/loot_table/blocks/extended_entity_accelerator.json");
        assertEquals("minecraft:block", extendedLoot.get("type").getAsString());

        // 3. Blockstates & Models
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/blockstates/advanced_entity_accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/blockstates/extended_entity_accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/models/block/advanced_entity_accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/models/block/extended_entity_accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/models/item/advanced_entity_accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/models/item/extended_entity_accelerator.json")));

        // 4. Textures (16x16)
        for (String texture : List.of(
                "src/main/resources/assets/jdte/textures/block/advanced_entity_accelerator_top.png",
                "src/main/resources/assets/jdte/textures/block/advanced_entity_accelerator_side.png",
                "src/main/resources/assets/jdte/textures/block/extended_entity_accelerator_top.png",
                "src/main/resources/assets/jdte/textures/block/extended_entity_accelerator_side.png"
        )) {
            Path texturePath = source(texture);
            assertTrue(Files.isRegularFile(texturePath), "Missing texture: " + texture);
            assertEquals(16, ImageIO.read(texturePath.toFile()).getWidth());
            assertEquals(16, ImageIO.read(texturePath.toFile()).getHeight());
        }

        // 5. Mining Tag
        JsonObject pickaxeTag = read("src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json");
        String tagValues = pickaxeTag.getAsJsonArray("values").toString();
        assertTrue(tagValues.contains("jdte:advanced_entity_accelerator"));
        assertTrue(tagValues.contains("jdte:extended_entity_accelerator"));

        // 6. Translations
        for (String locale : List.of("en_us", "zh_cn")) {
            JsonObject lang = read("src/main/resources/assets/jdte/lang/" + locale + ".json");
            assertTrue(lang.has("block.jdte.advanced_entity_accelerator"), "Missing advanced translation in " + locale);
            assertTrue(lang.has("block.jdte.extended_entity_accelerator"), "Missing extended translation in " + locale);
        }

        // 7. Guide & Patchouli Entries
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/guides/jdte/guide/entity-accelerator.md")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/guides/jdte/guide/_en_us/entity-accelerator.md")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/patchouli_books/jdte_guide/en_us/entries/entity-accelerator.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/patchouli_books/jdte_guide/zh_cn/entries/entity-accelerator.json")));
    }

    @Test
    void selectiveUpgradeCompatibilityHonored() {
        Set<UpgradeType> allowed = AdvancedEntityAcceleratorBE.ALLOWED_UPGRADES;

        // Supported upgrades
        assertTrue(allowed.contains(UpgradeType.CAPACITY));
        assertTrue(allowed.contains(UpgradeType.FLUID));
        assertTrue(allowed.contains(UpgradeType.RANGE));
        assertTrue(allowed.contains(UpgradeType.FILTER));
        assertTrue(allowed.contains(UpgradeType.OVERCLOCK));
        assertTrue(allowed.contains(UpgradeType.UNDERCLOCK));
        assertTrue(allowed.contains(UpgradeType.CREATIVE));
        assertTrue(allowed.contains(UpgradeType.AE_CRAFTING_READ));

        // Unsupported upgrades
        assertFalse(allowed.contains(UpgradeType.GENERATOR));
        assertFalse(allowed.contains(UpgradeType.FLUID_STORAGE));
        assertFalse(allowed.contains(UpgradeType.FORTUNE));
        assertFalse(allowed.contains(UpgradeType.PRECISION));
        assertFalse(allowed.contains(UpgradeType.AE_ACCELERATION));
        assertFalse(allowed.contains(UpgradeType.AE_OUTPUT));
        assertFalse(allowed.contains(UpgradeType.ESSENCE_CONVERSION));
        assertFalse(allowed.contains(UpgradeType.SEED_CONVERSION));
    }

    @Test
    void configDefaultsAndMultipliers() {
        // Advanced defaults
        assertEquals(200_000, EntityAcceleratorConfig.ADVANCED_BASE_ENERGY_CAPACITY);
        assertEquals(10_000, EntityAcceleratorConfig.BASE_FLUID_CAPACITY);
        assertEquals(64, EntityAcceleratorConfig.ADVANCED_MAX_MULTIPLIER);
        assertEquals(128, EntityAcceleratorConfig.ADVANCED_OVERCLOCK_MULTIPLIER);
        assertEquals(2.0D, EntityAcceleratorConfig.ADVANCED_TIER_FLUID_COST_MULTIPLIER, 1.0E-6);
        assertEquals(32, EntityAcceleratorConfig.ADVANCED_MAX_ENTITIES_PER_TICK);

        // Extended defaults
        assertEquals(500_000, EntityAcceleratorConfig.EXTENDED_BASE_ENERGY_CAPACITY);
        assertEquals(512, EntityAcceleratorConfig.EXTENDED_MAX_MULTIPLIER);
        assertEquals(1024, EntityAcceleratorConfig.EXTENDED_OVERCLOCK_MULTIPLIER);
        assertEquals(5.0D, EntityAcceleratorConfig.EXTENDED_TIER_FLUID_COST_MULTIPLIER, 1.0E-6);
        assertEquals(64, EntityAcceleratorConfig.EXTENDED_MAX_ENTITIES_PER_TICK);
    }

    @Test
    void costCalculationAndTiming() {
        // Multiplier 1 contributes 0 additional cycles
        assertEquals(0, TimeAcceleratorTiming.additionalCycles(1));
        // Multiplier 64 contributes 63 additional cycles
        assertEquals(63, TimeAcceleratorTiming.additionalCycles(64));
        // Multiplier 128 contributes 127 additional cycles
        assertEquals(127, TimeAcceleratorTiming.additionalCycles(128));
        // Multiplier 1024 contributes 1023 additional cycles
        assertEquals(1023, TimeAcceleratorTiming.additionalCycles(1024));

        // Fractional fluid settlement
        TimeAcceleratorCostMath.Settlement settlement = TimeAcceleratorCostMath.settleFluid(0.5D, 0.6D);
        assertEquals(1, settlement.drainMb());
        assertEquals(0.1D, settlement.remainingCost(), 1.0E-9D);
    }
}
