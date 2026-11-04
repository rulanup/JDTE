package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * B1 formal validation for the three fluid sender tiers: Ultimate Capacity
 * and Ultimate Overclock installation, limits, real capacities, execution
 * counts, tick speed, per-transfer FE deduction, both-cards-together,
 * Extended 8-slot logic, unlimited on/off, normal vs AutoIO direct transfer,
 * redstone HIGH/LOW/PULSE with real signals, no-target backoff, area and
 * Range, filter parity, save/reload, survival break, and sustained refill
 * throughput. Theoretical per-tick figures are never asserted from a single
 * 8000 mB tank: execution count, per-execution cap, and actual moved volume
 * are asserted separately, and sustained rates use refill loops.
 *
 * Original 0.6.5.2 rules locked here (do not "fix" them): Ultimate Overclock
 * does NOT raise the per-execution cap, so the normal path is 10 x 20000
 * mB/tick, below standard Overclock at 2 x 1000000 mB/tick; the 1000000 cap
 * itself can never saturate from a 400000 mB Ultimate-capacity tank in one
 * execution. FE is deducted once per ticker execution that actually moved
 * fluid, never for a failed or empty execution.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class FluidSenderValidationGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos TARGET = new BlockPos(3, 1, 2);
    private static final BlockPos FAR = new BlockPos(4, 1, 2);
    private static final BlockPos WEST = new BlockPos(1, 1, 2);

    public static final class CountingBasicFluidSenderBE extends BasicFluidSenderBE {
        public int n;
        public CountingBasicFluidSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
    }

    public static final class CountingAdvancedFluidSenderBE extends AdvancedFluidSenderBE {
        public int n;
        public CountingAdvancedFluidSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
    }

    public static final class CountingExtendedFluidSenderBE extends ExtendedFluidSenderBE {
        public int n;
        public CountingExtendedFluidSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public void tickServer() { n++; super.tickServer(); }
    }

    private static FluidSenderBE place(GameTestHelper h, int tier) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_FLUID_SENDER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_FLUID_SENDER.get() : JDTEBlocks.EXTENDED_FLUID_SENDER.get());
        return (FluidSenderBE) h.getBlockEntity(POS);
    }

    /** Big passive target: Extended sender with Ultimate Capacity (400000 mB
     * tank) held inactive by HIGH mode without a signal. It is ticked once
     * after installing the card so its tank capacity syncs to 400000. */
    private static FluidSenderBE bigTarget(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos, Blocks.AIR);
        h.setBlock(pos, JDTEBlocks.EXTENDED_FLUID_SENDER.get());
        var target = (FluidSenderBE) h.getBlockEntity(pos);
        card(target, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        target.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        tickOnce(h, target);
        return target;
    }

    private static void card(FluidSenderBE be, Item item, int slot) {
        if (!UpgradeHelper.getUpgradeHandler(be).insertItem(slot, new ItemStack(item), false).isEmpty())
            throw new IllegalStateException("Rejected fixture card " + item);
    }

    private static void fill(FluidSenderBE be, Fluid fluid, int amount) {
        int filled = be.getFluidTank().fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
        if (filled != amount) throw new IllegalStateException("Fixture fill moved " + filled + " of " + amount);
    }

    private static void drain(FluidSenderBE be) {
        be.getFluidTank().setFluid(FluidStack.EMPTY);
    }

    private static int tank(FluidSenderBE be) {
        return be.getFluidTank().getFluidAmount();
    }

    private static void charge(FluidSenderBE be) {
        if (be instanceof PoweredFluidSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
    }

    private static int fe(FluidSenderBE be) {
        return be instanceof PoweredFluidSenderBE powered ? powered.getEnergyStorage().getEnergyStored() : 0;
    }

    private static void setUnlimited(boolean value) {
        JDTEConfig.COMMON.fluidSenderUnlimitedTransfer.set(value);
    }

    private static void redstoneLow(FluidSenderBE be) {
        be.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, BaseMachineBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateInstallLimitsAndCapacities(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            var handler = UpgradeHelper.getUpgradeHandler(be);
            h.assertTrue(!handler.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())),
                    "second Ultimate Capacity rejected on tier " + tier);
            h.assertTrue(!handler.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                    "second Ultimate Overclock rejected on tier " + tier);
            h.assertTrue(!handler.isItemValid(2, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get())),
                    "standard Overclock rejected with Ultimate Overclock on tier " + tier);
            h.assertTrue(!handler.isItemValid(2, new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get())),
                    "Underclock rejected with Ultimate Overclock on tier " + tier);
            h.assertTrue(UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_CAPACITY) == 1
                            && UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                    "both Ultimate cards installed together on tier " + tier);
        }
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 1);
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 2);
            card(be, JDTEItems.CAPACITY_UPGRADE.get(), 3);
            tickOnce(h, be);
            h.assertTrue(be.getMaxMB() == 3_200_000,
                    "Ultimate Capacity + 3 Capacity gives 3200000 mB tank on tier " + tier + ", got " + be.getMaxMB());
            if (be instanceof PoweredFluidSenderBE powered) {
                int expectedFe = tier == 1 ? 20_000_000 : 40_000_000;
                h.assertTrue(powered.getMaxEnergy() == expectedFe,
                        "Ultimate Capacity + 3 Capacity gives " + expectedFe + " FE on tier " + tier
                                + ", got " + powered.getMaxEnergy());
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void unlimitedTrueMovesWholeTank(GameTestHelper h) {
        setUnlimited(true);
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            var target = bigTarget(h, TARGET);
            if (tier == 2) {
                card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
                card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
                tickOnce(h, be);
                charge(be);
                fill(be, Fluids.WATER, 400_000);
                tickOnce(h, be);
                h.assertTrue(tank(target) == 400_000 && tank(be) == 0,
                        "Extended unlimited moves whole 400000 mB tank in one execution, target=" + tank(target));
                h.assertTrue(fe(be) == 4_975_000,
                        "Extended unlimited deducts exactly one Ultimate cost 25000, FE=" + fe(be));
            } else {
                charge(be);
                fill(be, Fluids.WATER, 8000);
                tickOnce(h, be);
                h.assertTrue(tank(target) == 8000 && tank(be) == 0,
                        "unlimited moves whole 8000 mB tank on tier " + tier + ", target=" + tank(target));
                if (tier == 1) h.assertTrue(fe(be) == 49_500,
                        "Advanced unlimited deducts exactly one standard cost 500, FE=" + fe(be));
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void unlimitedFalseBaseRate(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            var target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            tickOnce(h, be);
            charge(be);
            fill(be, Fluids.WATER, 100_000);
            tickOnce(h, be);
            h.assertTrue(tank(target) == 20_000 && tank(be) == 80_000,
                    "base rate moves exactly 20000 mB per execution, target=" + tank(target));
            h.assertTrue(fe(be) == 2_499_500,
                    "base rate deducts exactly one standard cost 500, FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void unlimitedFalseStandardOverclock(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            var target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.OVERCLOCK_UPGRADE.get(), 1);
            tickOnce(h, be);
            charge(be);
            fill(be, Fluids.WATER, 400_000);
            tickOnce(h, be);
            h.assertTrue(tank(target) == 400_000 && tank(be) == 0,
                    "standard Overclock drains the 400000 tank (2 x 1000000 cap, tank-limited), target=" + tank(target));
            h.assertTrue(fe(be) == 2_498_500,
                    "standard Overclock deducts only for the execution that moved (1500), FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void unlimitedFalseUltimateOverclock(GameTestHelper h) {
        setUnlimited(false);
        try {
            for (int tier = 0; tier < 3; tier++) {
                var be = place(h, tier);
                var target = bigTarget(h, TARGET);
                card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
                card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
                tickOnce(h, be);
                charge(be);
                fill(be, Fluids.WATER, 400_000);
                tickOnce(h, be);
                h.assertTrue(tank(target) == 200_000 && tank(be) == 200_000,
                        "Ultimate normal path moves 10 x 20000 = 200000 mB per ticker tick on tier " + tier
                                + ", target=" + tank(target));
                if (tier > 0) {
                    int expectedFe = (tier == 1 ? 2_500_000 : 5_000_000) - 250_000;
                    h.assertTrue(fe(be) == expectedFe,
                            "Ultimate deducts 10 x 25000 = 250000 FE on tier " + tier + ", FE=" + fe(be));
                }
            }
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void realTickerRunsTenExecutions(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            h.setBlock(POS, Blocks.AIR);
            h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_FLUID_SENDER.get()
                    : tier == 1 ? JDTEBlocks.ADVANCED_FLUID_SENDER.get() : JDTEBlocks.EXTENDED_FLUID_SENDER.get());
            var abs = h.absolutePos(POS);
            var counting = tier == 0 ? new CountingBasicFluidSenderBE(abs, h.getLevel().getBlockState(abs))
                    : tier == 1 ? new CountingAdvancedFluidSenderBE(abs, h.getLevel().getBlockState(abs))
                    : new CountingExtendedFluidSenderBE(abs, h.getLevel().getBlockState(abs));
            h.getLevel().setBlockEntity(counting);
            redstoneLow(counting);
            card(counting, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            tickOnce(h, counting);
            int n = tier == 0 ? ((CountingBasicFluidSenderBE) counting).n
                    : tier == 1 ? ((CountingAdvancedFluidSenderBE) counting).n
                    : ((CountingExtendedFluidSenderBE) counting).n;
            h.assertTrue(n == 10, "real ticker runs 10 executions with Ultimate Overclock on tier " + tier + ", got " + n);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void tickSpeedUltimateOneUnderclockForty(GameTestHelper h) {
        setUnlimited(true);
        var ult = place(h, 0);
        card(ult, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(ult, 20) == 1, "Ultimate tick speed 1");
        var under = place(h, 0);
        card(under, JDTEItems.UNDERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(under, 20) == 40, "Underclock tick speed 40");
        var target = bigTarget(h, TARGET);
        fill(under, Fluids.WATER, 8000);
        for (int i = 0; i < 39; i++) tickOnce(h, under);
        h.assertTrue(tank(target) == 0, "Underclock moves nothing in the first 39 ticker ticks");
        tickOnce(h, under);
        h.assertTrue(tank(target) == 8000, "Underclock moves the whole tank on the 40th ticker tick");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferUltimate(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            // Direct targets must NOT be adjacent: adjacent fluid handlers
            // on the input mask are source endpoints and are excluded from
            // direct targets by design (same as the item sender direct
            // tests, which use far targets).
            var target = bigTarget(h, FAR);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
            be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
            tickOnce(h, be);
            charge(be);
            fill(be, Fluids.WATER, 400_000);
            h.assertTrue(OverclockDirectTransferHelper.isEnabled(be), "direct transfer enabled with Ultimate + AutoIO mask");
            tickOnce(h, be);
            h.assertTrue(tank(target) == 400_000 && tank(be) == 0,
                    "direct path moves the whole 400000 tank (fluidLimit 1000000, tank-limited), target=" + tank(target));
            h.assertTrue(fe(be) == 2_475_000,
                    "direct path deducts exactly one Ultimate cost 25000, FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferWithoutMaskFallsBackToNormal(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            var target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            tickOnce(h, be);
            charge(be);
            fill(be, Fluids.WATER, 400_000);
            h.assertTrue(!OverclockDirectTransferHelper.isEnabled(be), "no AutoIO mask means direct transfer stays off");
            tickOnce(h, be);
            h.assertTrue(tank(target) == 200_000,
                    "without a mask the normal Ultimate path moves 200000, target=" + tank(target));
            h.assertTrue(fe(be) == 2_250_000,
                    "normal Ultimate path deducts 10 x 25000, FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferPullsAdjacentSource(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            var target = bigTarget(h, FAR);
            // Adjacent source tank: Basic sender held inactive by HIGH mode
            // without a signal, at the west side of the sender. It stays
            // empty through the capacity-sync tick so the measuring tick is
            // the one that pulls.
            h.setBlock(WEST, Blocks.AIR);
            h.setBlock(WEST, JDTEBlocks.BASIC_FLUID_SENDER.get());
            var sourceTank = (FluidSenderBE) h.getBlockEntity(WEST);
            sourceTank.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
            be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
            tickOnce(h, be);
            charge(be);
            fill(sourceTank, Fluids.WATER, 8000);
            h.assertTrue(OverclockDirectTransferHelper.isEnabled(be), "direct transfer enabled");
            tickOnce(h, be);
            h.assertTrue(tank(target) == 8000 && tank(sourceTank) == 0,
                    "direct path pulls 8000 from the adjacent source tank, target=" + tank(target));
            h.assertTrue(fe(be) == 2_475_000,
                    "direct pull deducts exactly one Ultimate cost 25000, FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferNoSuccessNoDeduction(GameTestHelper h) {
        setUnlimited(false);
        try {
            var be = (PoweredFluidSenderBE) place(h, 1);
            bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
            tickOnce(h, be);
            charge(be);
            int before = fe(be);
            tickOnce(h, be);
            h.assertTrue(fe(be) == before, "empty direct transfer deducts nothing, FE=" + fe(be));
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void poweredZeroFeBlocksTransfer(GameTestHelper h) {
        setUnlimited(true);
        var be = (PoweredFluidSenderBE) place(h, 1);
        var target = bigTarget(h, TARGET);
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 0 && tank(be) == 8000,
                "zero FE blocks the powered transfer (energy gate, not deduction)");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void redstoneModesWithRealSignal(GameTestHelper h) {
        setUnlimited(true);
        // HIGH: silent without a signal, active with one.
        var high = place(h, 0);
        var target = bigTarget(h, TARGET);
        high.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        fill(high, Fluids.WATER, 8000);
        tickOnce(h, high);
        h.assertTrue(tank(target) == 0, "HIGH without signal blocks transfer");
        h.setBlock(POS.above(), Blocks.REDSTONE_BLOCK);
        tickOnce(h, high);
        h.assertTrue(tank(target) == 8000, "HIGH with a real signal transfers");
        h.setBlock(POS.above(), Blocks.AIR);
        // LOW: active without a signal, blocked with one.
        var low = place(h, 0);
        drain(target);
        low.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
        fill(low, Fluids.WATER, 8000);
        tickOnce(h, low);
        h.assertTrue(tank(target) == 8000, "LOW without signal transfers");
        drain(target);
        fill(low, Fluids.WATER, 8000);
        h.setBlock(POS.above(), Blocks.REDSTONE_BLOCK);
        tickOnce(h, low);
        h.assertTrue(tank(target) == 0, "LOW with a real signal blocks transfer");
        h.setBlock(POS.above(), Blocks.AIR);
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void pulseModeRisingEdgeOnly(GameTestHelper h) {
        // Original JDT PULSE semantics, verified from bytecode: a rising
        // edge sets a one-shot pulsed flag that tickServer consumes, so a
        // held signal transfers exactly once and a falling edge never does.
        setUnlimited(true);
        var be = place(h, 0);
        var target = bigTarget(h, TARGET);
        be.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.PULSE;
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 0, "PULSE without signal transfers nothing");
        h.setBlock(POS.above(), Blocks.REDSTONE_BLOCK);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 8000, "PULSE rising edge transfers");
        drain(target);
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 0, "PULSE held signal transfers nothing more (one-shot consumed)");
        h.setBlock(POS.above(), Blocks.AIR);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 0, "PULSE falling edge transfers nothing");
        drain(target);
        // No refill here: the held-signal tick moved nothing, so the tank
        // still holds its 8000.
        h.setBlock(POS.above(), Blocks.REDSTONE_BLOCK);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 8000, "PULSE second rising edge transfers again");
        h.setBlock(POS.above(), Blocks.AIR);
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void noTargetBackoffDoubles(GameTestHelper h) {
        setUnlimited(true);
        var be = place(h, 0);
        var target = bigTarget(h, TARGET);
        fill(target, Fluids.WATER, 400_000);
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(be) == 8000, "full target fails the first transfer (backoff 10 starts)");
        for (int i = 0; i < 10; i++) tickOnce(h, be);
        tickOnce(h, be);
        h.assertTrue(tank(be) == 8000, "retry after 10 ticks fails again against the full target (backoff doubles to 20)");
        for (int i = 0; i < 20; i++) tickOnce(h, be);
        drain(target);
        tickOnce(h, be);
        h.assertTrue(tank(be) == 0 && tank(target) == 8000,
                "retry after the doubled backoff succeeds with no fluid lost");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void areaRadiusAndRange(GameTestHelper h) {
        setUnlimited(true);
        var be = place(h, 0);
        var far = bigTarget(h, FAR);
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(far) == 0, "default radius 1 does not reach dx=2");
        be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
        tickOnce(h, be);
        h.assertTrue(tank(far) == 8000, "radius 3 reaches dx=2");
        double base = UpgradeHelper.getMaxAreaRadius(be);
        card(be, JDTEItems.RANGE_UPGRADE.get(), 0);
        h.assertTrue(UpgradeHelper.getMaxAreaRadius(be) == base * 2,
                "one Range card doubles max area radius: " + base + " -> " + UpgradeHelper.getMaxAreaRadius(be));
        card(be, JDTEItems.RANGE_UPGRADE.get(), 1);
        h.assertTrue(UpgradeHelper.getMaxAreaRadius(be) == base * 4,
                "two Range cards quadruple max area radius: " + base + " -> " + UpgradeHelper.getMaxAreaRadius(be));
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void filterInstallsButDoesNotChangeTransfer(GameTestHelper h) {
        // Parity observation, not a bug to fix: the filter is GUI-present
        // but inert in both original 0.6.5.2 and the port.
        setUnlimited(true);
        var be = place(h, 0);
        var target = bigTarget(h, TARGET);
        card(be, JDTEItems.FILTER_UPGRADE.get(), 0);
        be.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
        fill(be, Fluids.WATER, 8000);
        tickOnce(h, be);
        h.assertTrue(tank(target) == 8000, "filter installed does not change fluid transfer");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void lavaTypePreserved(GameTestHelper h) {
        setUnlimited(true);
        var be = place(h, 0);
        var target = bigTarget(h, TARGET);
        fill(be, Fluids.LAVA, 8000);
        tickOnce(h, be);
        h.assertTrue(target.getFluidTank().getFluid().getFluid() == Fluids.LAVA && tank(target) == 8000,
                "transferred fluid keeps its type (lava)");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void saveReloadPreservesFluidFeCardsSettings(GameTestHelper h) {
        for (int tier = 1; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            if (tier == 2) {
                card(be, JDTEItems.RANGE_UPGRADE.get(), 2);
                card(be, JDTEItems.FILTER_UPGRADE.get(), 3);
            }
            be.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
            be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
            be.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
            tickOnce(h, be);
            fill(be, Fluids.WATER, 12_345);
            if (be instanceof PoweredFluidSenderBE powered)
                powered.getEnergyStorage().setEnergy(tier == 1 ? 1_000_000 : 1_234_567);
            var saved = be.saveWithFullMetadata();
            var fresh = tier == 1 ? new AdvancedFluidSenderBE(be.getBlockPos(), be.getBlockState())
                    : new ExtendedFluidSenderBE(be.getBlockPos(), be.getBlockState());
            h.getLevel().setBlockEntity(fresh);
            fresh.load(saved);
            h.assertTrue(fresh.getFluidTank().getFluid().getFluid() == Fluids.WATER
                            && fresh.getFluidTank().getFluidAmount() == 12_345,
                    "tank fluid type and amount survive save/reload on tier " + tier);
            h.assertTrue(fe(fresh) == (tier == 1 ? 1_000_000 : 1_234_567),
                    "FE survives save/reload on tier " + tier + ", FE=" + fe(fresh));
            h.assertTrue(UpgradeHelper.countUpgrades(fresh, UpgradeType.ULTIMATE_CAPACITY) == 1
                            && UpgradeHelper.countUpgrades(fresh, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                    "both Ultimate cards survive save/reload on tier " + tier);
            h.assertTrue(fresh.getAreaAffectingData().xRadius == 3 && fresh.getAreaAffectingData().zRadius == 3,
                    "area settings survive save/reload on tier " + tier);
            h.assertTrue(fresh.getRedstoneControlData().redstoneMode == MiscHelpers.RedstoneMode.LOW,
                    "redstone mode survives save/reload on tier " + tier);
            h.assertTrue(fresh.getFilterHandler().getStackInSlot(0).is(Items.STONE),
                    "filter contents survive save/reload on tier " + tier);
            if (tier == 2) h.assertTrue(UpgradeHelper.countUpgrades(fresh, UpgradeType.RANGE) == 1
                            && UpgradeHelper.countUpgrades(fresh, UpgradeType.FILTER) == 1,
                    "Range and Filter cards survive save/reload on Extended");
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void extendedEightSlots(GameTestHelper h) {
        var be = place(h, 2);
        card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
        card(be, JDTEItems.CAPACITY_UPGRADE.get(), 2);
        card(be, JDTEItems.CAPACITY_UPGRADE.get(), 3);
        card(be, JDTEItems.CAPACITY_UPGRADE.get(), 4);
        card(be, JDTEItems.RANGE_UPGRADE.get(), 5);
        card(be, JDTEItems.RANGE_UPGRADE.get(), 6);
        card(be, JDTEItems.FILTER_UPGRADE.get(), 7);
        h.assertTrue(UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_CAPACITY) == 1
                        && UpgradeHelper.countUpgrades(be, UpgradeType.ULTIMATE_OVERCLOCK) == 1
                        && UpgradeHelper.countUpgrades(be, UpgradeType.CAPACITY) == 3
                        && UpgradeHelper.countUpgrades(be, UpgradeType.RANGE) == 2
                        && UpgradeHelper.countUpgrades(be, UpgradeType.FILTER) == 1,
                "all eight Extended slots hold their cards");
        tickOnce(h, be);
        h.assertTrue(be.getMaxMB() == 3_200_000, "eight-slot tank max 3200000, got " + be.getMaxMB());
        var saved = be.saveWithFullMetadata();
        var fresh = new ExtendedFluidSenderBE(be.getBlockPos(), be.getBlockState());
        h.getLevel().setBlockEntity(fresh);
        fresh.load(saved);
        h.assertTrue(UpgradeHelper.countUpgrades(fresh, UpgradeType.ULTIMATE_CAPACITY) == 1
                        && UpgradeHelper.countUpgrades(fresh, UpgradeType.ULTIMATE_OVERCLOCK) == 1
                        && UpgradeHelper.countUpgrades(fresh, UpgradeType.CAPACITY) == 3
                        && UpgradeHelper.countUpgrades(fresh, UpgradeType.RANGE) == 2
                        && UpgradeHelper.countUpgrades(fresh, UpgradeType.FILTER) == 1,
                "all eight Extended cards survive save/reload");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakWithUltimateCards(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            be.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
            if (be instanceof PoweredFluidSenderBE powered) powered.getEnergyStorage().setEnergy(1000);
            fill(be, Fluids.WATER, 1000);
            var level = h.getLevel();
            var abs = h.absolutePos(POS);
            var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
            player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            var expected = tier == 0 ? JDTEItems.BASIC_FLUID_SENDER.get()
                    : tier == 1 ? JDTEItems.ADVANCED_FLUID_SENDER.get() : JDTEItems.EXTENDED_FLUID_SENDER.get();
            int before = 0;
            for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(abs).inflate(2))) {
                if (e.getItem().is(expected)) before += e.getItem().getCount();
            }
            h.assertTrue(player.gameMode.destroyBlock(abs), "FakePlayer breaks tier " + tier);
            h.assertTrue(level.getBlockState(abs).isAir() && level.getBlockEntity(abs) == null,
                    "broken sender leaves air and no BE on tier " + tier);
            int drops = 0;
            for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(abs).inflate(2))) {
                if (e.getItem().is(expected)) drops += e.getItem().getCount();
            }
            h.assertTrue(drops - before == 1,
                    "survival break drops exactly one self item on tier " + tier + ", drops=" + (drops - before));
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void refillLoopSustainedThroughput(GameTestHelper h) {
        setUnlimited(false);
        try {
            // Ultimate: 5 refill iterations x 200000 per ticker tick.
            var be = (PoweredFluidSenderBE) place(h, 1);
            var target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            tickOnce(h, be);
            charge(be);
            int total = 0;
            for (int i = 0; i < 5; i++) {
                drain(be);
                fill(be, Fluids.WATER, 400_000);
                tickOnce(h, be);
                total += tank(target);
                drain(target);
            }
            h.assertTrue(total == 1_000_000,
                    "Ultimate sustained throughput 5 x 200000 = 1000000 mB, got " + total);
            // Baseline (Ultimate Capacity only): 5 x 20000 = 100000.
            be = (PoweredFluidSenderBE) place(h, 1);
            target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            tickOnce(h, be);
            charge(be);
            total = 0;
            for (int i = 0; i < 5; i++) {
                drain(be);
                fill(be, Fluids.WATER, 400_000);
                tickOnce(h, be);
                total += tank(target);
                drain(target);
            }
            h.assertTrue(total == 100_000, "baseline sustained throughput 5 x 20000 = 100000 mB, got " + total);
            // Standard Overclock: 5 x 400000 = 2000000 (tank-limited).
            be = (PoweredFluidSenderBE) place(h, 1);
            target = bigTarget(h, TARGET);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.OVERCLOCK_UPGRADE.get(), 1);
            tickOnce(h, be);
            charge(be);
            total = 0;
            for (int i = 0; i < 5; i++) {
                drain(be);
                fill(be, Fluids.WATER, 400_000);
                tickOnce(h, be);
                total += tank(target);
                drain(target);
            }
            h.assertTrue(total == 2_000_000,
                    "standard Overclock sustained throughput 5 x 400000 = 2000000 mB, got " + total);
        } finally {
            setUnlimited(true);
        }
        h.succeed();
    }
}
