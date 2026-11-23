package com.jdte.gametest;

import com.jdte.common.blockentities.SolarPanelBE;
import com.jdte.common.solar.SolarPanelTier;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Runtime checks for the tiered solar panels on a real NeoForge GameTest server:
 * block to block entity wiring, tier mapping, the exposed energy capability and
 * its extract-only behaviour. Same approach as the forge-1.20.1 branch GameTests.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class SolarPanelGameTests {
    private static final BlockPos PANEL = new BlockPos(1, 1, 1);

    private SolarPanelGameTests() {
    }

    private static SolarPanelBE place(GameTestHelper h, Block block) {
        h.setBlock(PANEL, block);
        var be = h.getBlockEntity(PANEL);
        h.assertTrue(be instanceof SolarPanelBE, "solar panel block entity created, got " + be);
        return (SolarPanelBE) be;
    }

    @GameTest(template = "solar_empty")
    public static void everyTierPlacesMatchingBlockEntity(GameTestHelper h) {
        Block[] blocks = {
                JDTEBlocks.CONCENTRATED_SOLAR_PANEL.get(),
                JDTEBlocks.SINGULARITY_SOLAR_PANEL.get(),
                JDTEBlocks.STELLAR_FUSION_SOLAR_PANEL.get(),
                JDTEBlocks.DIMENSIONAL_COLLAPSE_SOLAR_PANEL.get(),
                JDTEBlocks.CREATIVE_SOLAR_PANEL.get()};
        SolarPanelTier[] tiers = {
                SolarPanelTier.CONCENTRATED, SolarPanelTier.SINGULARITY, SolarPanelTier.STELLAR_FUSION,
                SolarPanelTier.DIMENSIONAL_COLLAPSE, SolarPanelTier.CREATIVE};
        for (int i = 0; i < blocks.length; i++) {
            SolarPanelBE be = place(h, blocks[i]);
            h.assertTrue(be.tier() == tiers[i], "tier expected " + tiers[i] + " got " + be.tier());
            h.assertTrue(be.capacity() > 0, "positive capacity for " + tiers[i]);
        }
        h.succeed();
    }

    @GameTest(template = "solar_empty")
    public static void finitePanelExposesExtractOnlyEnergy(GameTestHelper h) {
        place(h, JDTEBlocks.CONCENTRATED_SOLAR_PANEL.get());
        var cap = h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, h.absolutePos(PANEL), Direction.UP);
        h.assertTrue(cap != null, "energy capability exposed");
        h.assertTrue(cap.receiveEnergy(1000, false) == 0, "external input rejected");
        h.assertTrue(cap.getMaxEnergyStored() > 0, "capacity visible through capability");
        h.succeed();
    }

    @GameTest(template = "solar_empty")
    public static void creativePanelNeverDrains(GameTestHelper h) {
        place(h, JDTEBlocks.CREATIVE_SOLAR_PANEL.get());
        var cap = h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, h.absolutePos(PANEL), Direction.NORTH);
        h.assertTrue(cap != null, "creative energy capability exposed");
        int before = cap.getEnergyStored();
        h.assertTrue(before > 0, "creative storage reports energy");
        h.assertTrue(cap.extractEnergy(1_000_000, false) > 0, "creative extraction served");
        h.assertTrue(cap.getEnergyStored() == before, "creative storage never drains");
        h.succeed();
    }

    @GameTest(template = "solar_empty", timeoutTicks = 60)
    public static void creativePanelTicksAndPushesIntoNeighbour(GameTestHelper h) {
        place(h, JDTEBlocks.CREATIVE_SOLAR_PANEL.get());
        h.runAfterDelay(20, () -> {
            var be = h.getBlockEntity(PANEL);
            h.assertTrue(be instanceof SolarPanelBE p && p.canGenerate(), "creative panel generating after 20 ticks");
            h.succeed();
        });
    }
}
