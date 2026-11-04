package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regression guard for the two already-verified (V) machines whose Ultimate
 * behaviour the 27-family generalization touches, as identified by the
 * read-only audit. This file deliberately does NOT assert the audit's first
 * draft for the mixer: re-checking showed {@code OverclockDirectTransferHelper}
 * gates on machine type (senders/receivers only), so mixer + Ultimate does
 * NOT gain direct transfer even though {@code hasOverclock} now returns true.
 * The tests below lock the verified reality, not the draft claim.
 *
 * Mineral extractor: the ticker runs 10 executions, but executions 2-10 in
 * one game tick only accumulate transient work; settlement (outputs + FE)
 * happens on base ticks and later ticks. A single ticker tick therefore
 * settles the same as baseline; the Ultimate gain lands over time.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class VRegressionUltimateGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos POS2 = new BlockPos(5, 1, 2);

    public static final class CountingMineralBE extends MineralExtractorBE {
        public int n;
        public CountingMineralBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
    }

    public static final class CountingMixerBE extends AdvancedFluidMixerBE {
        public int n;
        public CountingMixerBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, BaseMachineBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    private static void card(BaseMachineBE be, net.minecraft.world.item.Item item, int slot) {
        if (!UpgradeHelper.getUpgradeHandler(be).insertItem(slot, new ItemStack(item), false).isEmpty())
            throw new IllegalStateException("Rejected fixture card " + item);
    }

    private static void redstoneLow(BaseMachineBE be) {
        if (be instanceof com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE redstone)
            redstone.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.LOW;
    }

    private static MineralExtractorBE mineral(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos, Blocks.AIR);
        h.setBlock(pos, JDTEBlocks.MINERAL_EXTRACTOR.get());
        var be = (MineralExtractorBE) h.getBlockEntity(pos);
        var survey = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        com.jdte.common.items.MineralSurveyItem.setSurvey(survey,
                com.jdte.common.minerals.MineralSurveyData.create(
                        com.jdte.common.minerals.MineralSurveyIndex.version(h.getLevel().getServer()),
                        ResourceLocation.withDefaultNamespace("plains"), h.getLevel().dimension().location(),
                        List.of(new com.jdte.common.minerals.MineralEntry(
                                ResourceLocation.withDefaultNamespace("iron_ore"), 1, -64, 64, 9,
                                com.jdte.common.minerals.MineralEntry.Confidence.EXACT))));
        be.getMachineHandler().setStackInSlot(0, survey);
        be.getEnergyStorage().setEnergy(2_000_000);
        be.setMultiplier(32);
        return be;
    }

    private static int mineralOutput(MineralExtractorBE be) {
        int count = 0;
        for (int i = be.outputStartSlot(); i < be.getMachineHandler().getSlots(); i++)
            count += be.getMachineHandler().getStackInSlot(i).getCount();
        return count;
    }

    private static CountingMineralBE countingMineral(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos, Blocks.AIR);
        h.setBlock(pos, JDTEBlocks.MINERAL_EXTRACTOR.get());
        var abs = h.absolutePos(pos);
        var counting = new CountingMineralBE(abs, h.getLevel().getBlockState(abs));
        h.getLevel().setBlockEntity(counting);
        var survey = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        com.jdte.common.items.MineralSurveyItem.setSurvey(survey,
                com.jdte.common.minerals.MineralSurveyData.create(
                        com.jdte.common.minerals.MineralSurveyIndex.version(h.getLevel().getServer()),
                        ResourceLocation.withDefaultNamespace("plains"), h.getLevel().dimension().location(),
                        List.of(new com.jdte.common.minerals.MineralEntry(
                                ResourceLocation.withDefaultNamespace("iron_ore"), 1, -64, 64, 9,
                                com.jdte.common.minerals.MineralEntry.Confidence.EXACT))));
        counting.getMachineHandler().setStackInSlot(0, survey);
        counting.getEnergyStorage().setEnergy(2_000_000);
        counting.setMultiplier(32);
        return counting;
    }

    @GameTest(template = "mineral_empty")
    public static void mineralUltimateTickerRunsTenExecutions(GameTestHelper h) {
        var be = countingMineral(h, POS);
        redstoneLow(be);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        tickOnce(h, be);
        h.assertTrue(be.n == 10, "mineral real ticker runs 10 executions with Ultimate Overclock, got " + be.n);
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void mineralUltimateSingleTickSettlesSameAsBaseline(GameTestHelper h) {
        // Same game tick: executions 2-10 only accumulate transient work, so a
        // single ticker tick settles exactly like baseline. Asserting 10x
        // output here would be wrong; the gain lands on later ticks.
        var base = mineral(h, POS);
        redstoneLow(base);
        tickOnce(h, base);
        int baseOut = mineralOutput(base);
        int baseFe = base.getEnergyStorage().getEnergyStored();
        var ult = mineral(h, POS);
        redstoneLow(ult);
        card(ult, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        tickOnce(h, ult);
        h.assertTrue(mineralOutput(ult) == baseOut && ult.getEnergyStorage().getEnergyStored() == baseFe,
                "single ticker tick settles same with Ultimate as baseline: output " + mineralOutput(ult) + " vs " + baseOut
                        + ", FE " + ult.getEnergyStorage().getEnergyStored() + " vs " + baseFe);
        h.assertTrue(baseOut == 3 && baseFe == 1_985_000,
                "baseline single tick settles 3 outputs for 15000 FE, got output " + baseOut + " FE " + baseFe);
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void mineralUltimateProductionOverTime(GameTestHelper h) {
        var base = mineral(h, POS);
        redstoneLow(base);
        var ult = mineral(h, POS2);
        redstoneLow(ult);
        card(ult, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        h.runAfterDelay(100, () -> {
            int outU = mineralOutput(ult);
            int outB = mineralOutput(base);
            int feU = ult.getEnergyStorage().getEnergyStored();
            int feB = base.getEnergyStorage().getEnergyStored();
            h.assertTrue(outU > outB, "Ultimate produces more over 100 ticks: " + outU + " vs " + outB);
            h.assertTrue(feU < feB, "Ultimate consumes more FE over 100 ticks: " + feU + " vs " + feB);
            h.succeed();
        });
    }

    private static FluidMixerBE mixer(GameTestHelper h) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, JDTEBlocks.ADVANCED_FLUID_MIXER.get());
        return (FluidMixerBE) h.getBlockEntity(POS);
    }

    private static CountingMixerBE countingMixer(GameTestHelper h) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, JDTEBlocks.ADVANCED_FLUID_MIXER.get());
        var abs = h.absolutePos(POS);
        var counting = new CountingMixerBE(abs, h.getLevel().getBlockState(abs));
        h.getLevel().setBlockEntity(counting);
        return counting;
    }

    private static com.jdte.common.recipes.FluidMixerRecipe mixerFixture(GameTestHelper h, String name, int ticks, int cost) {
        var recipe = new com.jdte.common.recipes.FluidMixerRecipe(
                new FluidStack(Fluids.LAVA, 100), FluidStack.EMPTY,
                Optional.of(new com.jdte.common.recipes.FluidMixerRecipe.InputItem(
                        net.minecraft.world.item.crafting.Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get()), 1)),
                new FluidStack(net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("justdirethings", "time_fluid")), 50),
                ticks, cost, com.jdte.JDTE.id("test/mixer/" + name));
        var manager = h.getLevel().getRecipeManager();
        var all = new ArrayList<Recipe<?>>(manager.getRecipes());
        all.removeIf(r -> r.getId().equals(recipe.getId()));
        all.add(recipe);
        manager.replaceRecipes(all);
        com.jdte.common.recipes.RecipeCacheSignal.invalidate();
        return recipe;
    }

    @GameTest(template = "mineral_empty")
    public static void mixerUltimateTickerStillTenExecutions(GameTestHelper h) {
        var be = countingMixer(h);
        redstoneLow(be);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        tickOnce(h, be);
        h.assertTrue(be.n == 10, "mixer real ticker runs 10 executions with Ultimate Overclock, got " + be.n);
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void mixerUltimateDirectTransferStillDisabled(GameTestHelper h) {
        // Audit correction: the direct helper gates on machine TYPE
        // (senders/receivers only). hasOverclock is now true for mixer +
        // Ultimate, but direct transfer stays off for mixers.
        var be = mixer(h);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
        h.assertTrue(UpgradeHelper.hasOverclock(be), "mixer + Ultimate reports hasOverclock");
        h.assertTrue(!OverclockDirectTransferHelper.isEnabled(be), "mixer + Ultimate still not direct-transfer eligible");
        h.assertTrue(OverclockDirectTransferHelper.transfer(be) == 0, "mixer + Ultimate direct transfer moves 0");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void mixerUltimateCostStillFiftyTimes(GameTestHelper h) {
        // Mixer was already in the old adjustEnergyCost whitelist, so x50
        // predates the 27-family round; this locks it, it does not prove a
        // change.
        var be = mixer(h);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(be.getEffectiveEnergyCost(700) == 35000,
                "mixer Ultimate cost 700 -> 35000, got " + be.getEffectiveEnergyCost(700));
        var plain = mixer(h);
        h.assertTrue(plain.getEffectiveEnergyCost(700) == 700,
                "mixer no-card cost stays 700, got " + plain.getEffectiveEnergyCost(700));
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void mixerUltimateCompletesBatchesInOneTickerTick(GameTestHelper h) {
        // Real mixer rule (FluidMixerBE.getEffectiveProcessTicks): any
        // overclock forces maxTicks to 1, so each ticker execution completes
        // batches instead of accumulating progress. One Ultimate ticker tick
        // therefore finishes work, it does not leave progress at 10.
        // Recipe inputs are distinctive (lava + time catalyst) and the
        // recipe list is restored before succeed, so no later test can
        // match this fixture.
        var manager = h.getLevel().getRecipeManager();
        var original = new ArrayList<Recipe<?>>(manager.getRecipes());
        mixerFixture(h, "ult100", 100, 700);
        var base = mixer(h);
        redstoneLow(base);
        base.inputTankA.setFluid(new FluidStack(Fluids.LAVA, 1000));
        base.getMachineHandler().setStackInSlot(0, new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(), 10));
        base.getEnergyStorage().receiveEnergy(base.getMaxEnergy(), false);
        tickOnce(h, base);
        h.assertTrue(base.progress == 1 && base.outputTank.isEmpty()
                        && base.inputTankA.getFluidAmount() == 1000
                        && base.getEnergyStorage().getEnergyStored() == 100000,
                "baseline ticker tick advances progress by 1 with no output, progress=" + base.progress);
        var ult = mixer(h);
        redstoneLow(ult);
        ult.inputTankA.setFluid(new FluidStack(Fluids.LAVA, 1000));
        ult.getMachineHandler().setStackInSlot(0, new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(), 10));
        ult.getEnergyStorage().receiveEnergy(ult.getMaxEnergy(), false);
        card(ult, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        tickOnce(h, ult);
        // Parallel batches are energy-bound: 100000 FE / 35000 per batch = 2
        // batches, then later executions find too little FE and reset.
        h.assertTrue(ult.progress == 0 && ult.outputTank.getFluidAmount() == 100
                        && ult.inputTankA.getFluidAmount() == 800
                        && ult.getEnergyStorage().getEnergyStored() == 30000
                        && ult.getMachineHandler().getStackInSlot(0).getCount() == 8,
                "Ultimate ticker tick completes 2 batches: output=" + ult.outputTank.getFluidAmount()
                        + " tankA=" + ult.inputTankA.getFluidAmount()
                        + " FE=" + ult.getEnergyStorage().getEnergyStored()
                        + " catalyst=" + ult.getMachineHandler().getStackInSlot(0).getCount());
        manager.replaceRecipes(original);
        com.jdte.common.recipes.RecipeCacheSignal.invalidate();
        h.succeed();
    }
}
