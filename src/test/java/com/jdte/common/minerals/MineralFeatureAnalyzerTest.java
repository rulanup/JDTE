package com.jdte.common.minerals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralFeatureAnalyzerTest {

    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void extractsVanillaOreConfigurationFromUndergroundOres() {
        OreConfiguration config = new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.STONE), Blocks.IRON_ORE.defaultBlockState()),
                OreConfiguration.target(new BlockMatchTest(Blocks.DEEPSLATE), Blocks.DEEPSLATE_IRON_ORE.defaultBlockState())
        ), 9);

        PlacedFeature placed = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(Feature.ORE, config)),
                List.of(
                        CountPlacement.of(ConstantInt.of(5)),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64))
                )
        );

        Biome biome = createBiomeWithFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed);
        List<MineralEntry> minerals = MineralFeatureAnalyzer.analyze(Holder.direct(biome), 64);

        assertEquals(2, minerals.size());
        MineralEntry iron = findEntry(minerals, "minecraft:iron_ore");
        assertNotNull(iron);
        assertEquals(-16, iron.minY());
        assertEquals(64, iron.maxY());
        assertEquals(9, iron.veinSize());
        assertTrue(iron.weight() > 0L);

        MineralEntry deepslateIron = findEntry(minerals, "minecraft:deepslate_iron_ore");
        assertNotNull(deepslateIron);
        assertEquals(iron.weight(), deepslateIron.weight());
    }

    @Test
    void extractsModdedOreConfigurationMatchingMekanismStructure() {
        // Simulates Mekanism's ResizableOreFeatureConfig:
        // Record with targetStates() returning List<OreConfiguration.TargetBlockState> and size() returning IntSupplier
        MockMekanismOreConfig mockConfig = new MockMekanismOreConfig(
                List.of(
                        OreConfiguration.target(new BlockMatchTest(Blocks.STONE), Blocks.GOLD_ORE.defaultBlockState()),
                        OreConfiguration.target(new BlockMatchTest(Blocks.DEEPSLATE), Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState())
                ),
                () -> 8
        );

        MockCustomFeature customFeature = new MockCustomFeature(MockMekanismOreConfig.CODEC);
        PlacedFeature placed = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(customFeature, mockConfig)),
                List.of(
                        CountPlacement.of(ConstantInt.of(4)),
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(32))
                )
        );

        Biome biome = createBiomeWithFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed);
        List<MineralEntry> minerals = MineralFeatureAnalyzer.analyze(Holder.direct(biome), 64);

        assertEquals(2, minerals.size());
        MineralEntry gold = findEntry(minerals, "minecraft:gold_ore");
        assertNotNull(gold);
        assertEquals(8, gold.veinSize());
        assertEquals(-48, gold.minY()); // -64 + 16 = -48
        assertEquals(287, gold.maxY()); // 319 - 32 = 287
        assertTrue(gold.weight() > 0L);
    }

    @Test
    void extractsOresPlacedInUndergroundDecorationStep() {
        // Nether ores like Ancient Debris and Quartz are in step 7 (UNDERGROUND_DECORATION)
        OreConfiguration config = new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.NETHERRACK), Blocks.ANCIENT_DEBRIS.defaultBlockState())
        ), 3);

        PlacedFeature placed = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(Feature.ORE, config)),
                List.of(
                        CountPlacement.of(ConstantInt.of(2)),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(8), VerticalAnchor.absolute(22))
                )
        );

        Biome biome = createBiomeWithFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, placed);
        List<MineralEntry> minerals = MineralFeatureAnalyzer.analyze(Holder.direct(biome), 64);

        assertEquals(1, minerals.size());
        MineralEntry debris = findEntry(minerals, "minecraft:ancient_debris");
        assertNotNull(debris);
        assertEquals(3, debris.veinSize());
        assertEquals(8, debris.minY());
        assertEquals(22, debris.maxY());
    }

    @Test
    void skipsRetrogenAndDisabledModifiers() {
        OreConfiguration config = new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.STONE), Blocks.DIAMOND_ORE.defaultBlockState())
        ), 4);

        MockDisableablePlacement retrogenModifier = new MockDisableablePlacement(true, () -> true);
        PlacedFeature retrogenPlaced = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(Feature.ORE, config)),
                List.of(retrogenModifier, CountPlacement.of(1))
        );

        MockDisableablePlacement disabledModifier = new MockDisableablePlacement(false, () -> false);
        PlacedFeature disabledPlaced = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(Feature.ORE, config)),
                List.of(disabledModifier, CountPlacement.of(1))
        );

        Biome biome = createBiomeWithFeatures(GenerationStep.Decoration.UNDERGROUND_ORES,
                List.of(retrogenPlaced, disabledPlaced));
        List<MineralEntry> minerals = MineralFeatureAnalyzer.analyze(Holder.direct(biome), 64);

        assertTrue(minerals.isEmpty());
    }

    @Test
    void extractsModdedOreConfigurationFromJsonCodec() {
        MockJsonOnlyOreConfig jsonConfig = new MockJsonOnlyOreConfig(
                List.of(ResourceLocation.parse("minecraft:emerald_ore")), 5
        );
        MockJsonFeature jsonFeature = new MockJsonFeature(MockJsonOnlyOreConfig.CODEC);
        PlacedFeature placed = new PlacedFeature(
                Holder.direct(new ConfiguredFeature<>(jsonFeature, jsonConfig)),
                List.of(
                        CountPlacement.of(ConstantInt.of(1)),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(32))
                )
        );

        Biome biome = createBiomeWithFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed);
        List<MineralEntry> minerals = MineralFeatureAnalyzer.analyze(Holder.direct(biome), 64);

        assertEquals(1, minerals.size());
        MineralEntry emerald = findEntry(minerals, "minecraft:emerald_ore");
        assertNotNull(emerald);
        assertEquals(5, emerald.veinSize());
        assertEquals(0, emerald.minY());
        assertEquals(32, emerald.maxY());
    }

    private static Biome createBiomeWithFeature(GenerationStep.Decoration step, PlacedFeature placed) {
        return createBiomeWithFeatures(step, List.of(placed));
    }

    private static Biome createBiomeWithFeatures(GenerationStep.Decoration targetStep, List<PlacedFeature> features) {
        BiomeGenerationSettings.PlainBuilder gen = new BiomeGenerationSettings.PlainBuilder();
        for (PlacedFeature feature : features) {
            gen.addFeature(targetStep, Holder.direct(feature));
        }

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.5f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .fogColor(0xc0d8ff)
                        .skyColor(0x77adff)
                        .build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(gen.build())
                .build();
    }

    private static MineralEntry findEntry(List<MineralEntry> entries, String id) {
        ResourceLocation loc = ResourceLocation.parse(id);
        return entries.stream()
                .filter(e -> e.oreId().equals(loc))
                .findFirst()
                .orElse(null);
    }

    // Mock record matching Mekanism's ResizableOreFeatureConfig signature
    public record MockMekanismOreConfig(
            List<OreConfiguration.TargetBlockState> targetStates,
            IntSupplier size
    ) implements FeatureConfiguration {
        public static final Codec<MockMekanismOreConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                OreConfiguration.TargetBlockState.CODEC.listOf().fieldOf("targets").forGetter(MockMekanismOreConfig::targetStates)
        ).apply(instance, targets -> new MockMekanismOreConfig(targets, () -> 8)));
    }

    public static class MockCustomFeature extends Feature<MockMekanismOreConfig> {
        public MockCustomFeature(Codec<MockMekanismOreConfig> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<MockMekanismOreConfig> context) {
            return false;
        }
    }

    // Mock config testing the pure JSON codec fallback (no targetStates() or size() methods)
    public record MockJsonOnlyOreConfig(List<ResourceLocation> oreIds, int amount) implements FeatureConfiguration {
        public static final Codec<MockJsonOnlyOreConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("targets").forGetter(MockJsonOnlyOreConfig::oreIds),
                Codec.INT.fieldOf("size").forGetter(MockJsonOnlyOreConfig::amount)
        ).apply(instance, MockJsonOnlyOreConfig::new));
    }

    public static class MockJsonFeature extends Feature<MockJsonOnlyOreConfig> {
        public MockJsonFeature(Codec<MockJsonOnlyOreConfig> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<MockJsonOnlyOreConfig> context) {
            return false;
        }
    }

    // Mock placement modifier matching Mekanism's DisableableFeaturePlacement
    public static class MockDisableablePlacement extends PlacementModifier {
        private final boolean retroGen;
        private final BooleanSupplier enabledSupplier;

        public MockDisableablePlacement(boolean retroGen, BooleanSupplier enabledSupplier) {
            this.retroGen = retroGen;
            this.enabledSupplier = enabledSupplier;
        }

        public boolean retroGen() {
            return retroGen;
        }

        public BooleanSupplier enabledSupplier() {
            return enabledSupplier;
        }

        @Override
        public java.util.stream.Stream<net.minecraft.core.BlockPos> getPositions(
                net.minecraft.world.level.levelgen.placement.PlacementContext context,
                net.minecraft.util.RandomSource random,
                net.minecraft.core.BlockPos pos) {
            return java.util.stream.Stream.empty();
        }

        @Override
        public PlacementModifierType<?> type() {
            return PlacementModifierType.COUNT;
        }
    }
}
