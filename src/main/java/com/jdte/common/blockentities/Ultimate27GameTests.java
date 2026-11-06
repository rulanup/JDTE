package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Ultimate-card acceptance for the remaining 27 restricted families (41 tiers),
 * strict original 0.6.5.2 behaviour (see ultimate-card-27-machines-analysis.md):
 * 25 families take both Ultimate cards (explicit lists or the original default
 * branch) and the suppressor/blocker pair takes Ultimate Capacity only.
 *
 * Per family, one representative tier is exercised (roots cover their tiers:
 * TimeAcceleratorBE covers all accelerator tiers and the crystal, the Powered*
 * roots cover their tiers, the eight Extended*BE classes are singletons).
 * The gate uses root classes, so one tier proves the family; tier shells only
 * pass their own BlockEntityType, energy constants and the extended marker.
 *
 * What these tests do NOT prove (validation-round work, as with the sender
 * pilot): real-client GUI/JEI/creative visibility and packaged dedicated-server
 * lifecycle per family. Passing here keeps families at P, it does not move
 * them to V.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class Ultimate27GameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private interface Make {
        BaseMachineBE make(BlockPos pos, BlockState state);
    }

    private record Fam(String name, net.minecraft.world.level.block.Block block, Make make, Make counting,
                       boolean capacityOnly, boolean lockedDelay) {
    }

    // Representative tier per family. lockedDelay follows the port's
    // usesLockedDelay list after the crystal restoration: time accelerators,
    // greenhouses, bio factory, breeder, vat and crystal are NOT locked-delay.
    private static final Fam[] FAMS = {
            new Fam("time_accelerator", JDTEBlocks.BASIC_TIME_ACCELERATOR.get(), BasicTimeAcceleratorBE::new, CTimeAccelerator::new, false, false),
            new Fam("extended_clicker", JDTEBlocks.EXTENDED_CLICKER.get(), ExtendedClickerBE::new, CClicker::new, false, true),
            new Fam("extended_block_breaker", JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(), ExtendedBlockBreakerBE::new, CBlockBreaker::new, false, true),
            new Fam("extended_block_placer", JDTEBlocks.EXTENDED_BLOCK_PLACER.get(), ExtendedBlockPlacerBE::new, CBlockPlacer::new, false, true),
            new Fam("extended_block_swapper", JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(), ExtendedBlockSwapperBE::new, CBlockSwapper::new, false, true),
            new Fam("extended_dropper", JDTEBlocks.EXTENDED_DROPPER.get(), ExtendedDropperBE::new, CDropper::new, false, true),
            new Fam("extended_sensor", JDTEBlocks.EXTENDED_SENSOR.get(), ExtendedSensorBE::new, CSensor::new, false, true),
            new Fam("extended_fluid_collector", JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(), ExtendedFluidCollectorBE::new, CFluidCollector::new, false, true),
            new Fam("extended_fluid_placer", JDTEBlocks.EXTENDED_FLUID_PLACER.get(), ExtendedFluidPlacerBE::new, CFluidPlacer::new, false, true),
            new Fam("entity_suppressor", JDTEBlocks.ENTITY_SUPPRESSOR.get(), EntitySuppressorBE::new, null, true, true),
            new Fam("range_blocker", JDTEBlocks.RANGE_BLOCKER.get(), RangeBlockerBE::new, null, true, true),
            new Fam("crystal_incubator", JDTEBlocks.CRYSTAL_INCUBATOR.get(), CrystalIncubatorBE::new, CCrystal::new, false, false),
            new Fam("greenhouse", JDTEBlocks.GREENHOUSE.get(), GreenhouseBE::new, CGreenhouse::new, false, false),
            new Fam("large_greenhouse", JDTEBlocks.LARGE_GREENHOUSE.get(), LargeGreenhouseBE::new, CLargeGreenhouse::new, false, false),
            new Fam("life_synthesis_vat", JDTEBlocks.LIFE_SYNTHESIS_VAT.get(), LifeSynthesisVatBE::new, CVat::new, false, false),
            new Fam("bio_factory", JDTEBlocks.BIO_FACTORY.get(), BioFactoryBE::new, CBioFactory::new, false, false),
            new Fam("life_breeder", JDTEBlocks.LIFE_BREEDER.get(), LifeBreederBE::new, CBreeder::new, false, false),
            new Fam("factory_packer", JDTEBlocks.FACTORY_PACKER.get(), FactoryPackerBE::new, CPacker::new, false, true),
            new Fam("gel_generator", JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), AdvancedGelGeneratorBE::new, CGel::new, false, true),
            new Fam("fluid_stabilizer", JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), AdvancedFluidStabilizerBE::new, CStabilizer::new, false, true),
            new Fam("fluid_sender", JDTEBlocks.ADVANCED_FLUID_SENDER.get(), AdvancedFluidSenderBE::new, CFluidSender::new, false, true),
            new Fam("item_receiver", JDTEBlocks.ADVANCED_ITEM_RECEIVER.get(), AdvancedItemReceiverBE::new, CItemReceiver::new, false, true),
            new Fam("fluid_receiver", JDTEBlocks.ADVANCED_FLUID_RECEIVER.get(), AdvancedFluidReceiverBE::new, CFluidReceiver::new, false, true),
            new Fam("life_extractor", JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), AdvancedLifeExtractorBE::new, CLifeExtractor::new, false, true),
            new Fam("infusion_machine", JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), AdvancedInfusionMachineBE::new, CInfusion::new, false, true),
            new Fam("potion_brewer", JDTEBlocks.ADVANCED_POTION_BREWER.get(), AdvancedPotionBrewerBE::new, CPotion::new, false, true),
            new Fam("bio_crusher", JDTEBlocks.ADVANCED_BIO_CRUSHER.get(), AdvancedBioCrusherBE::new, CBioCrusher::new, false, true),
    };

    private static BaseMachineBE place(GameTestHelper h, Fam fam) {
        // AIR first: re-placing the same block state does not replace the
        // block entity, so without this a second place() in one test would
        // silently reuse the previous machine (and its cards).
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, fam.block());
        return (BaseMachineBE) h.getBlockEntity(POS);
    }

    private static BaseMachineBE placeCounting(GameTestHelper h, Fam fam) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, fam.block());
        var abs = h.absolutePos(POS);
        var state = h.getLevel().getBlockState(abs);
        var counting = fam.counting().make(abs, state);
        h.getLevel().setBlockEntity(counting);
        // Redstone-active fixture: the mixin only runs 10 executions when the
        // machine is redstone-active, and at least one family (the swapper)
        // defaults to a mode that is inactive with no signal in the test
        // world, which measures 1 execution instead of 10. LOW means "run
        // without a signal", the same fixture the sender tests use.
        if (counting instanceof com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE redstone)
            redstone.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.LOW;
        return counting;
    }

    private static void card(BaseMachineBE be, net.minecraft.world.item.Item item, int slot) {
        if (!UpgradeHelper.getUpgradeHandler(be).insertItem(slot, new ItemStack(item), false).isEmpty())
            throw new IllegalStateException("Rejected fixture card " + item);
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, BaseMachineBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateGateAllows27Families(GameTestHelper h) {
        for (var fam : FAMS) {
            var be = place(h, fam);
            h.assertTrue(UpgradeHelper.isUpgradeCompatible(be, UpgradeType.ULTIMATE_CAPACITY),
                    fam.name() + " allows Ultimate Capacity");
            if (fam.capacityOnly()) {
                h.assertTrue(!UpgradeHelper.isUpgradeCompatible(be, UpgradeType.ULTIMATE_OVERCLOCK),
                        fam.name() + " still rejects Ultimate Overclock (original: capacity only)");
            } else {
                h.assertTrue(UpgradeHelper.isUpgradeCompatible(be, UpgradeType.ULTIMATE_OVERCLOCK),
                        fam.name() + " allows Ultimate Overclock");
            }
        }
        // Negative controls: families outside the 27 keep their gates.
        // The collector rejects both cards in original and port alike.
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get());
        var collector = (BaseMachineBE) h.getBlockEntity(POS);
        h.assertTrue(!UpgradeHelper.isUpgradeCompatible(collector, UpgradeType.ULTIMATE_CAPACITY)
                        && !UpgradeHelper.isUpgradeCompatible(collector, UpgradeType.ULTIMATE_OVERCLOCK),
                "collector still rejects both Ultimate cards");
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get());
        var glue = (BaseMachineBE) h.getBlockEntity(POS);
        h.assertTrue(!UpgradeHelper.isUpgradeCompatible(glue, UpgradeType.ULTIMATE_CAPACITY)
                        && !UpgradeHelper.isUpgradeCompatible(glue, UpgradeType.ULTIMATE_OVERCLOCK),
                "glue activator (outside the 27) still rejects both Ultimate cards");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCardsInstallAndSpeedCardsExclusive(GameTestHelper h) {
        for (var fam : FAMS) {
            var be = place(h, fam);
            var upgrades = UpgradeHelper.getUpgradeHandler(be);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            h.assertTrue(UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_CAPACITY) == 1,
                    fam.name() + " installs Ultimate Capacity");
            if (fam.capacityOnly()) {
                h.assertTrue(!upgrades.isItemValid(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                        fam.name() + " rejects Ultimate Overclock in a free slot");
            } else {
                card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
                h.assertTrue(UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                        fam.name() + " installs Ultimate Overclock");
                h.assertTrue(!upgrades.isItemValid(2, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()))
                                && !upgrades.isItemValid(2, new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get()))
                                && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()))
                                && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                        fam.name() + " speed cards exclusive and Ultimate cards capped at one");
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCapacityScalesFiftyTimes(GameTestHelper h) {
        for (var fam : FAMS) {
            var be = place(h, fam);
            // Real storage, where the family exposes an energy capability:
            // base max first, then Ultimate Capacity must give exactly x50.
            // The basic time accelerator and the dropper expose no energy
            // capability, so they assert the helper math only (below).
            var energy = be.getCapability(ForgeCapabilities.ENERGY, null);
            Integer baseMax = energy.map(e -> e.getMaxEnergyStored()).orElse(null);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            h.assertTrue(UpgradeHelper.adjustEnergyCapacity(be, 1000) == 50000,
                    fam.name() + " helper capacity 1000 -> 50000 with Ultimate Capacity");
            if (baseMax != null) {
                var after = be.getCapability(ForgeCapabilities.ENERGY, null).orElseThrow(IllegalStateException::new).getMaxEnergyStored();
                h.assertTrue(after == baseMax * 50,
                        fam.name() + " real max energy " + baseMax + " -> " + after + " (expected x50)");
            }
            // Stacking with 3 normal Capacity cards: x50 x8 = x400.
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 1);
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 2);
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 3);
            h.assertTrue(UpgradeHelper.adjustEnergyCapacity(be, 1000) == 400000,
                    fam.name() + " helper capacity 1000 -> 400000 with Ultimate + 3 Capacity");
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateOverclockCostAndTickSpeed(GameTestHelper h) {
        for (var fam : FAMS) {
            var be = place(h, fam);
            if (fam.capacityOnly()) {
                card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
                h.assertTrue(UpgradeHelper.adjustEnergyCost(be, 500) == 500,
                        fam.name() + " cost unchanged without Overclock");
                h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(be, 20) == 20,
                        fam.name() + " tick speed unchanged without Overclock");
                continue;
            }
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            h.assertTrue(UpgradeHelper.adjustEnergyCost(be, 500) == 25000,
                    fam.name() + " cost 500 -> 25000 with Ultimate Overclock");
            if (fam.lockedDelay()) {
                h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(be, 20) == 1,
                        fam.name() + " tick speed 20 -> 1 with Ultimate Overclock");
            } else {
                // Non-locked-delay families keep their own speed in original:
                // time accelerators, greenhouses, bio factory, breeder, vat
                // and, after the crystal restoration, the crystal incubator.
                h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(be, 20) == 20,
                        fam.name() + " tick speed unchanged with Ultimate Overclock (not locked-delay)");
            }
        }
        // Crystal restoration lock: standard Overclock also leaves crystal
        // tick speed unchanged in original (it is not a locked-delay machine).
        var crystal = place(h, FAMS[11]);
        card(crystal, JDTEItems.OVERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(crystal, 20) == 20,
                "crystal tick speed unchanged with standard Overclock (restored original)");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCardsAndSaveReload(GameTestHelper h) {
        for (var fam : FAMS) {
            var be = place(h, fam);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            if (!fam.capacityOnly()) card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            var saved = be.saveWithFullMetadata();
            var fresh = fam.make().make(be.getBlockPos(), be.getBlockState());
            h.getLevel().setBlockEntity(fresh);
            fresh.load(saved);
            var upgrades = UpgradeHelper.getUpgradeHandler(fresh);
            h.assertTrue(upgrades.getStackInSlot(0).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),
                    fam.name() + " Ultimate Capacity survives save/reload");
            if (!fam.capacityOnly()) {
                h.assertTrue(upgrades.getStackInSlot(1).is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),
                        fam.name() + " Ultimate Overclock survives save/reload");
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateOverclockTickerRunsTenExecutions(GameTestHelper h) {
        for (var fam : FAMS) {
            if (fam.capacityOnly()) continue;
            var be = placeCounting(h, fam);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            var counting = (TickCounter) be;
            h.assertTrue(counting.tickCount() == 0, fam.name() + " counting starts at 0");
            tickOnce(h, be);
            h.assertTrue(counting.tickCount() == 10,
                    fam.name() + " real ticker runs 10 executions with Ultimate Overclock, got " + counting.tickCount());
        }
        // Baseline: the same path runs once with no speed card.
        var breaker = placeCounting(h, FAMS[2]);
        tickOnce(h, breaker);
        h.assertTrue(((TickCounter) breaker).tickCount() == 1, "baseline ticker runs 1 execution without cards");
        h.succeed();
    }

    private interface TickCounter {
        int tickCount();
    }

    // Counting subclasses: tickServer is the method the block ticker invokes,
    // so counting at its entry counts real ticker executions (the mixin loops
    // the original tick 10 times with Ultimate Overclock).
    public static final class CTimeAccelerator extends BasicTimeAcceleratorBE implements TickCounter {
        public int n;
        public CTimeAccelerator(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CClicker extends ExtendedClickerBE implements TickCounter {
        public int n;
        public CClicker(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBlockBreaker extends ExtendedBlockBreakerBE implements TickCounter {
        public int n;
        public CBlockBreaker(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBlockPlacer extends ExtendedBlockPlacerBE implements TickCounter {
        public int n;
        public CBlockPlacer(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBlockSwapper extends ExtendedBlockSwapperBE implements TickCounter {
        public int n;
        public CBlockSwapper(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CDropper extends ExtendedDropperBE implements TickCounter {
        public int n;
        public CDropper(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CSensor extends ExtendedSensorBE implements TickCounter {
        public int n;
        public CSensor(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CFluidCollector extends ExtendedFluidCollectorBE implements TickCounter {
        public int n;
        public CFluidCollector(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CFluidPlacer extends ExtendedFluidPlacerBE implements TickCounter {
        public int n;
        public CFluidPlacer(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CCrystal extends CrystalIncubatorBE implements TickCounter {
        public int n;
        public CCrystal(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CGreenhouse extends GreenhouseBE implements TickCounter {
        public int n;
        public CGreenhouse(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CLargeGreenhouse extends LargeGreenhouseBE implements TickCounter {
        public int n;
        public CLargeGreenhouse(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CVat extends LifeSynthesisVatBE implements TickCounter {
        public int n;
        public CVat(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBioFactory extends BioFactoryBE implements TickCounter {
        public int n;
        public CBioFactory(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBreeder extends LifeBreederBE implements TickCounter {
        public int n;
        public CBreeder(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CPacker extends FactoryPackerBE implements TickCounter {
        public int n;
        public CPacker(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CGel extends AdvancedGelGeneratorBE implements TickCounter {
        public int n;
        public CGel(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CStabilizer extends AdvancedFluidStabilizerBE implements TickCounter {
        public int n;
        public CStabilizer(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CFluidSender extends AdvancedFluidSenderBE implements TickCounter {
        public int n;
        public CFluidSender(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CItemReceiver extends AdvancedItemReceiverBE implements TickCounter {
        public int n;
        public CItemReceiver(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CFluidReceiver extends AdvancedFluidReceiverBE implements TickCounter {
        public int n;
        public CFluidReceiver(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CLifeExtractor extends AdvancedLifeExtractorBE implements TickCounter {
        public int n;
        public CLifeExtractor(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CInfusion extends AdvancedInfusionMachineBE implements TickCounter {
        public int n;
        public CInfusion(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CPotion extends AdvancedPotionBrewerBE implements TickCounter {
        public int n;
        public CPotion(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }

    public static final class CBioCrusher extends AdvancedBioCrusherBE implements TickCounter {
        public int n;
        public CBioCrusher(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
        @Override public int tickCount() { return n; }
    }
}
