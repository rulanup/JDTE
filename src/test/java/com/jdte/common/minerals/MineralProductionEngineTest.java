package com.jdte.common.minerals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralProductionEngineTest {
    private static final MineralEntry COPPER = entry("copper_ore", 3L);
    private static final MineralEntry IRON = entry("iron_ore", 2L);
    private static final MineralEntry GOLD = entry("gold_ore", 1L);

    @Test
    void distributesWholeBatchWithoutLosingCycles() {
        MineralProductionEngine.Batch batch = MineralProductionEngine.distribute(
                List.of(COPPER, IRON, GOLD), 1_024L, 1_024L, 0, RandomSource.create(7L));

        assertEquals(1_024L, batch.consumedCycles());
        assertEquals(1_024L, batch.producedItems());
    }

    @Test
    void preservesConservationWhenWeightSumExceedsLongRange() {
        List<MineralEntry> entries = List.of(
                entry("ore_a", Long.MAX_VALUE),
                entry("ore_b", Long.MAX_VALUE),
                entry("ore_c", Long.MAX_VALUE));

        MineralProductionEngine.Batch batch = MineralProductionEngine.distribute(
                entries, 10_000L, 10_000L, 0, RandomSource.create(11L));

        assertEquals(10_000L, batch.producedItems());
    }

    @Test
    void capsCyclesBeforeApplyingFortuneYield() {
        MineralProductionEngine.Batch batch = MineralProductionEngine.distribute(
                List.of(COPPER), 2_048L, 1_024L, 50, RandomSource.create(13L));

        assertEquals(1_024L, batch.consumedCycles());
        assertEquals(1_536L, batch.producedItems());
    }

    @Test
    void filtersCandidatesBeforeWeightsAreRenormalized() {
        List<MineralEntry> selected = MineralProductionEngine.select(
                List.of(COPPER, IRON, GOLD), entry -> !entry.oreId().getPath().contains("iron"));
        MineralProductionEngine.Batch batch = MineralProductionEngine.distribute(
                selected, 400L, 400L, 0, RandomSource.create(17L));

        assertEquals(400L, batch.producedItems());
        assertEquals(300L, batch.amounts().get(COPPER.oreId()));
        assertEquals(100L, batch.amounts().get(GOLD.oreId()));
    }

    @Test
    void accumulatesVirtualWorkWithOverflowAndQueueCaps() {
        assertEquals(1_024L, MineralProductionEngine.accumulateWork(0L, 1L, 1_024L, 8_192L));
        assertEquals(8_192L, MineralProductionEngine.accumulateWork(8_000L, 10L, 1_024L, 8_192L));
        assertEquals(Long.MAX_VALUE, MineralProductionEngine.accumulateWork(
                Long.MAX_VALUE - 1L, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    void producesNothingAfterAllCandidatesAreFiltered() {
        List<MineralEntry> selected = MineralProductionEngine.select(List.of(COPPER, IRON), entry -> false);
        MineralProductionEngine.Batch batch = MineralProductionEngine.distribute(
                selected, 1_024L, 1_024L, 100, RandomSource.create(19L));

        assertEquals(0L, batch.consumedCycles());
        assertEquals(0L, batch.producedItems());
        assertTrue(batch.amounts().isEmpty());
    }

    @Test
    void clampsNegativeWorkAndCycleInputs() {
        assertEquals(0L, MineralProductionEngine.accumulateWork(-10L, -5L, 1_024L, 8_192L));
        assertEquals(0L, MineralProductionEngine.distribute(
                List.of(COPPER), -1L, 1_024L, 100, RandomSource.create(23L)).producedItems());
        assertEquals(0L, MineralProductionEngine.distribute(
                List.of(COPPER), 1_024L, -1L, 100, RandomSource.create(29L)).producedItems());
    }

    @Test
    void decodesLegacySurveyDefaultsAndNormalizesEntryBounds() {
        String legacy = """
                {
                  "biome": "minecraft:plains",
                  "dimension": "minecraft:overworld",
                  "entries": [{
                    "ore": "minecraft:iron_ore",
                    "weight": 0,
                    "min_y": 64,
                    "max_y": -32,
                    "vein_size": 0
                  }]
                }
                """;

        MineralSurveyData survey = MineralSurveyData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(legacy))
                .getOrThrow();
        MineralEntry decoded = survey.entries().getFirst();

        assertEquals(MineralSurveyData.CURRENT_SCHEMA, survey.schemaVersion());
        assertEquals(0L, survey.indexVersion());
        assertEquals(1L, decoded.weight());
        assertEquals(-32, decoded.minY());
        assertEquals(64, decoded.maxY());
        assertEquals(1, decoded.veinSize());
        assertEquals(MineralEntry.Confidence.ESTIMATED, decoded.confidence());
    }

    @Test
    void surveyTotalWeightSaturatesInsteadOfOverflowing() {
        MineralSurveyData survey = MineralSurveyData.create(7L,
                ResourceLocation.withDefaultNamespace("plains"),
                ResourceLocation.withDefaultNamespace("overworld"),
                List.of(entry("ore_a", Long.MAX_VALUE), entry("ore_b", Long.MAX_VALUE)));

        assertEquals(Long.MAX_VALUE, survey.totalWeight());
    }

    private static MineralEntry entry(String path, long weight) {
        return new MineralEntry(ResourceLocation.fromNamespaceAndPath("test", path), weight,
                -64, 320, 8, MineralEntry.Confidence.ESTIMATED);
    }
}