package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.blockentities.BasicTimeAcceleratorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.ExtendedTimeFreezerBE;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseMachineTickerGateTest {
    @Test
    void aeDenyPausesTheRealTickerWithoutAdvancingExistingWork() {
        AtomicInteger progress = new AtomicInteger(12);

        UpgradeHelper.runServerTicker(true, () -> false, false, progress::incrementAndGet);

        assertEquals(12, progress.get());
    }

    @Test
    void activeCraftingTaskResumesTheRealTicker() {
        AtomicInteger progress = new AtomicInteger(12);

        UpgradeHelper.runServerTicker(true, () -> true, false, progress::incrementAndGet);

        assertEquals(13, progress.get());
    }

    @Test
    void machineWithoutUpgradeKeepsOriginalTickerAndOverclockBehavior() {
        AtomicInteger executions = new AtomicInteger();
        AtomicInteger permissionReads = new AtomicInteger();

        UpgradeHelper.runServerTicker(true, () -> {
            permissionReads.incrementAndGet();
            return true;
        }, true, executions::incrementAndGet);

        assertEquals(2, executions.get());
        assertEquals(1, permissionReads.get());
    }

    @Test
    void redstoneOffStillRunsOriginalTickerResetWithoutReadingAePermission() {
        AtomicInteger resets = new AtomicInteger();
        AtomicInteger permissionReads = new AtomicInteger();

        UpgradeHelper.runServerTicker(false, () -> {
            permissionReads.incrementAndGet();
            return false;
        }, true, resets::incrementAndGet);

        assertEquals(1, resets.get());
        assertEquals(0, permissionReads.get());
    }

    @Test
    void taskFourStateMachinesAndMaintenanceTickersBypassTheCommonGate() {
        List<BaseMachineBE> excluded = List.of(
                new GreenhouseBE(BlockPos.ZERO, JDTEBlocks.GREENHOUSE.get().defaultBlockState()),
                new LargeGreenhouseBE(BlockPos.ZERO, JDTEBlocks.LARGE_GREENHOUSE.get().defaultBlockState()),
                new LifeSynthesisVatBE(BlockPos.ZERO, JDTEBlocks.LIFE_SYNTHESIS_VAT.get().defaultBlockState()),
                new MineralExtractorBE(BlockPos.ZERO, JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState()),
                new LargeMineralExtractorBE(BlockPos.ZERO, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState()),
                new LifeBreederBE(BlockPos.ZERO, JDTEBlocks.LIFE_BREEDER.get().defaultBlockState()),
                new AdvancedItemCollectorBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get().defaultBlockState()),
                new TimeFreezerBE(BlockPos.ZERO, JDTEBlocks.TIME_FREEZER.get().defaultBlockState()),
                new ExtendedTimeFreezerBE(BlockPos.ZERO, JDTEBlocks.EXTENDED_TIME_FREEZER.get().defaultBlockState()),
                new BasicTimeAcceleratorBE(BlockPos.ZERO, JDTEBlocks.BASIC_TIME_ACCELERATOR.get().defaultBlockState()),
                new EntitySuppressorBE(BlockPos.ZERO, JDTEBlocks.ENTITY_SUPPRESSOR.get().defaultBlockState()),
                new RangeBlockerBE(BlockPos.ZERO, JDTEBlocks.RANGE_BLOCKER.get().defaultBlockState()),
                new AdvancedEnergyTransmitterBE(BlockPos.ZERO, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState()),
                new FactoryPackerBE(BlockPos.ZERO, JDTEBlocks.FACTORY_PACKER.get().defaultBlockState()));

        excluded.forEach(machine -> assertFalse(UpgradeHelper.usesCommonAeTickerGate(machine),
                machine.getClass().getSimpleName()));
    }

    @Test
    void ordinaryJdtMachineStillUsesCommonGateAndDenySkipsItsTicker() {
        BaseMachineBE ordinary = new BaseMachineBE(BlockEntityType.SIGN, BlockPos.ZERO,
                Blocks.OAK_SIGN.defaultBlockState());
        AtomicInteger executions = new AtomicInteger();

        assertTrue(UpgradeHelper.usesCommonAeTickerGate(ordinary));
        UpgradeHelper.runServerTicker(true, true, () -> false, false, executions::incrementAndGet);

        assertEquals(0, executions.get());
    }

    @Test
    void excludedStateMachineKeepsOriginalOverclockWithoutReadingPermission() {
        AtomicInteger executions = new AtomicInteger();
        AtomicInteger permissionReads = new AtomicInteger();

        UpgradeHelper.runServerTicker(false, true, () -> {
            permissionReads.incrementAndGet();
            return false;
        }, true, executions::incrementAndGet);

        assertEquals(2, executions.get());
        assertEquals(0, permissionReads.get());
    }
}
