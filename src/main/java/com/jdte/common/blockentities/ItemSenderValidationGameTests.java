package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Remaining sender-function validation beyond the Ultimate pilot:
 * redstone modes, Creative card, Underclock timing, area persistence and
 * reach, Range scaling, Filter parity, and reliable throughput that
 * distinguishes internal stock (9 x 64 = 576) from theoretical per-tick
 * rates (640 Ultimate normal, 20000 standard Overclock, 100000 direct).
 * No original transfer logic is changed to reach a theoretical figure.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class ItemSenderValidationGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos TARGET = new BlockPos(3, 1, 2);

    private static ItemSenderBE place(GameTestHelper h, int tier) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_ITEM_SENDER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_ITEM_SENDER.get() : JDTEBlocks.EXTENDED_ITEM_SENDER.get());
        var be = (ItemSenderBE) h.getBlockEntity(POS);
        h.setBlock(TARGET, Blocks.AIR);
        h.setBlock(TARGET, Blocks.CHEST);
        return be;
    }

    private static void card(ItemSenderBE be, Item item, int slot) {
        if (!UpgradeHelper.getUpgradeHandler(be).insertItem(slot, new ItemStack(item), false).isEmpty())
            throw new IllegalStateException("Rejected fixture card " + item);
    }

    private static void fillSender(ItemSenderBE be, int perSlot) {
        for (int i = 0; i < 9; i++) be.getItemHandler().setStackInSlot(i, new ItemStack(Items.STONE, perSlot));
    }

    private static int senderCount(ItemSenderBE be) {
        int n = 0;
        for (int i = 0; i < 9; i++) n += be.getItemHandler().getStackInSlot(i).getCount();
        return n;
    }

    private static int targetCount(GameTestHelper h, BlockPos pos) {
        var handler = h.getLevel().getBlockEntity(h.absolutePos(pos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
        int n = 0;
        for (int i = 0; i < handler.getSlots(); i++) n += handler.getStackInSlot(i).getCount();
        return n;
    }

    private static void clearTarget(GameTestHelper h, BlockPos pos) {
        var handler = h.getLevel().getBlockEntity(h.absolutePos(pos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < handler.getSlots(); i++) handler.extractItem(i, Integer.MAX_VALUE, false);
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, ItemSenderBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    private static void charge(ItemSenderBE be) {
        if (be instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
    }

    @GameTest(template = "mineral_empty")
    public static void redstoneHighBlocksAndLowAllowsWithoutSignal(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var blocked = place(h, tier);
            charge(blocked);
            blocked.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
            blocked.getRedstoneControlData().receivingRedstone = false;
            fillSender(blocked, 64);
            tickOnce(h, blocked);
            h.assertTrue(targetCount(h, TARGET) == 0 && senderCount(blocked) == 576,
                    "HIGH mode without signal blocks transfer on tier " + tier);
            var allowed = place(h, tier);
            charge(allowed);
            allowed.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.LOW;
            allowed.getRedstoneControlData().receivingRedstone = false;
            fillSender(allowed, 64);
            tickOnce(h, allowed);
            // One ticker tick without a speed card is one execution, so it
            // moves 64, not the full 576 stock.
            h.assertTrue(targetCount(h, TARGET) == 64 && senderCount(allowed) == 512,
                    "LOW mode without signal allows transfer on tier " + tier + ", moved=" + targetCount(h, TARGET));
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void creativeCardMovesWithoutEnergyAndCostsNothing(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.CREATIVE_UPGRADE.get(), 0);
            // No FE at all for powered tiers: Creative must still move.
            fillSender(be, 64);
            tickOnce(h, be);
            h.assertTrue(targetCount(h, TARGET) == 576 && senderCount(be) == 0,
                    "Creative moves full stock without FE on tier " + tier);
            if (be instanceof PoweredItemSenderBE powered) {
                h.assertTrue(powered.getStandardEnergyCost() == 0, "Creative cost is 0 on tier " + tier);
                h.assertTrue(powered.getEnergyStorage().getEnergyStored() == 0, "Creative spends no FE on tier " + tier);
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void underclockSlowsSenderToOneMovePerFortyTicks(GameTestHelper h) {
        // Measured timing, not assumed: a sender with Underclock must not
        // move on the first 39 ticker ticks, then moves on the 40th cycle.
        // If the timing differs, this test fails loudly instead of silently
        // recording a wrong interval.
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            charge(be);
            card(be, JDTEItems.UNDERCLOCK_UPGRADE.get(), 0);
            fillSender(be, 64);
            int movedBy39 = 0;
            for (int i = 0; i < 39; i++) {
                tickOnce(h, be);
                movedBy39 = targetCount(h, TARGET);
                if (movedBy39 > 0) break;
            }
            h.assertTrue(movedBy39 == 0, "Underclock moves nothing in the first 39 ticker ticks on tier " + tier + ", moved=" + movedBy39);
            tickOnce(h, be);
            h.assertTrue(targetCount(h, TARGET) == 64,
                    "Underclock moves one 64-item batch on the 40th ticker tick on tier " + tier + ", moved=" + targetCount(h, TARGET));
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void areaSettingsPersistAndReachFartherTargets(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            charge(be);
            be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
            var far = new BlockPos(4, 1, 2);
            h.setBlock(far, Blocks.AIR);
            h.setBlock(far, Blocks.CHEST);
            // Remove the adjacent target chest: a single tickServer call
            // moves only 64 items total, and they would go to the first
            // target in cache order, not necessarily the far one.
            h.setBlock(TARGET, Blocks.AIR);
            fillSender(be, 64);
            be.tickServer();
            h.assertTrue(targetCount(h, far) == 64, "widened area reaches dx=2 target on tier " + tier + ", far=" + targetCount(h, far));
            var saved = be.saveWithFullMetadata();
            var fresh = tier == 0 ? new BasicItemSenderBE(be.getBlockPos(), be.getBlockState())
                    : tier == 1 ? new AdvancedItemSenderBE(be.getBlockPos(), be.getBlockState())
                    : new ExtendedItemSenderBE(be.getBlockPos(), be.getBlockState());
            h.getLevel().setBlockEntity(fresh);
            fresh.load(saved);
            h.assertTrue(fresh.getAreaAffectingData().xRadius == 3 && fresh.getAreaAffectingData().zRadius == 3,
                    "area settings survive save/reload on tier " + tier + ", radius=" + fresh.getAreaAffectingData().xRadius);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void rangeUpgradesScaleMaxAreaRadius(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            double base = UpgradeHelper.getMaxAreaRadius(be);
            card(be, JDTEItems.RANGE_UPGRADE.get(), 0);
            double one = UpgradeHelper.getMaxAreaRadius(be);
            card(be, JDTEItems.RANGE_UPGRADE.get(), 1);
            double two = UpgradeHelper.getMaxAreaRadius(be);
            h.assertTrue(one == base * 2 && two == base * 4,
                    "Range scales max radius x2 per card on tier " + tier + ", base=" + base + " one=" + one + " two=" + two);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void filterCardInstallsAndDoesNotChangeSending(GameTestHelper h) {
        // Parity observation, recorded honestly: the original 0.6.5.2 sender
        // code never applies its filter to sendItems, and neither does the
        // Forge port. Installing a Filter card and setting filter slots must
        // therefore not break sending, and must not be claimed as filtering.
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            charge(be);
            card(be, JDTEItems.FILTER_UPGRADE.get(), 0);
            be.getFilterHandler().setStackInSlot(0, new ItemStack(Items.DIRT));
            fillSender(be, 64);
            be.tickServer();
            h.assertTrue(targetCount(h, TARGET) == 64,
                    "sending still works with Filter card installed on tier " + tier);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void normalPathRefillLoopDistinguishesStockFromTheory(GameTestHelper h) {
        // Ultimate normal: theoretical 640/tick, stock-limited to 576/tick.
        // Standard Overclock: theoretical 20000/tick, also stock-limited.
        // Refilling between ticker ticks proves the bottleneck is stock,
        // not the per-execution cap, without asserting an unreachable total.
        for (var spec : List.of(
                new Object[]{JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 576},
                new Object[]{JDTEItems.OVERCLOCK_UPGRADE.get(), 576})) {
            for (int tier = 0; tier < 3; tier++) {
                var be = place(h, tier);
                charge(be);
                card(be, (Item) spec[0], 0);
                int total = 0;
                for (int tick = 0; tick < 5; tick++) {
                    fillSender(be, 64);
                    clearTarget(h, TARGET);
                    int before = targetCount(h, TARGET);
                    tickOnce(h, be);
                    int moved = targetCount(h, TARGET) - before;
                    h.assertTrue(moved == 576, "refilled ticker tick moves 576 on tier " + tier + " with " + ((Item) spec[0]) + ", moved=" + moved);
                    total += moved;
                }
                h.assertTrue(total == 2880, "five refilled ticks move 2880 on tier " + tier + ", total=" + total);
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void survivalBreakOfNonDefaultSenderDoesNotCrash(GameTestHelper h) {
        // Regression for a production-server crash: vanilla survival
        // destroyBlock removes the block entity (invalidating its
        // capabilities) BEFORE dropResources, and JDT getDrops then calls
        // saveAdditional, which reads getFilterHandler(). A sender with
        // Ultimate cards installed is non-default, so it exercises that
        // path. The cached filter handler must let the break complete and
        // drop exactly one self item per tier.
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            if (be instanceof PoweredItemSenderBE powered) {
                card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
                card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
                powered.getEnergyStorage().setEnergy(1000);
            }
            fillSender(be, 1);
            var abs = h.absolutePos(POS);
            var level = h.getLevel();
            var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
            player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
            // This call crashed before the survival-break fixes with
            // "Missing JDTE filter capability" (JDT saveAdditional) and
            // "Missing JDTE upgrade capability" (provider energy init
            // during capability serialization) from saveAdditional.
            // Before/after delta, not an absolute count: the GameTest world
            // persists between runs, so a previous run's drop would otherwise
            // be counted again (same flake class as the Fumo drop test).
            var expectedBefore = tier == 0 ? JDTEItems.BASIC_ITEM_SENDER.get()
                    : tier == 1 ? JDTEItems.ADVANCED_ITEM_SENDER.get() : JDTEItems.EXTENDED_ITEM_SENDER.get();
            int before = 0;
            for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(abs).inflate(2))) {
                if (e.getItem().is(expectedBefore)) before += e.getItem().getCount();
            }
            h.assertTrue(player.gameMode.destroyBlock(abs), "FakePlayer breaks tier " + tier);
            h.assertTrue(level.getBlockState(abs).isAir() && level.getBlockEntity(abs) == null,
                    "broken sender leaves air and no BE on tier " + tier);
            var expected = tier == 0 ? JDTEItems.BASIC_ITEM_SENDER.get()
                    : tier == 1 ? JDTEItems.ADVANCED_ITEM_SENDER.get() : JDTEItems.EXTENDED_ITEM_SENDER.get();
            int drops = 0;
            for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(abs).inflate(2))) {
                if (e.getItem().is(expected)) drops += e.getItem().getCount();
            }
            h.assertTrue(drops - before == 1, "survival break drops exactly one self item on tier " + tier + ", drops=" + (drops - before));
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directPathRefillLoopAndSingleExecutionCap(GameTestHelper h) {
        // Direct path, Advanced tier, AutoIO input on all sides, area radius
        // 3, far targets at dx=2 and dx=3. One adjacent source chest (1728)
        // plus internal 576 = 2304 per ticker tick; refilling between ticks
        // sustains 2304/tick, far below the 100000/tick theoretical ceiling.
        var be = (PoweredItemSenderBE) place(h, 1);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
        be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
        var far1 = new BlockPos(4, 1, 2);
        var far2 = new BlockPos(5, 1, 2);
        h.setBlock(far1, Blocks.CHEST);
        h.setBlock(far2, Blocks.CHEST);
        var sourcePos = new BlockPos(1, 1, 2);
        h.setBlock(sourcePos, Blocks.CHEST);
        var source = h.getLevel().getBlockEntity(h.absolutePos(sourcePos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST).orElseThrow(IllegalStateException::new);
        int total = 0;
        for (int tick = 0; tick < 5; tick++) {
            for (int i = 0; i < source.getSlots(); i++) source.insertItem(i, new ItemStack(Items.STONE, 64), false);
            fillSender(be, 64);
            be.getEnergyStorage().setEnergy(be.getMaxEnergy());
            // Far targets accumulate across ticks and would fill up (3456
            // total), so clear them between ticks to measure each tick.
            clearTarget(h, far1);
            clearTarget(h, far2);
            int before = targetCount(h, far1) + targetCount(h, far2);
            tickOnce(h, be);
            int moved = targetCount(h, far1) + targetCount(h, far2) - before;
            h.assertTrue(moved == 2304, "refilled direct ticker tick moves 2304, moved=" + moved);
            total += moved;
        }
        h.assertTrue(total == 11520, "five refilled direct ticks move 11520, total=" + total);
        // Single direct execution with all available stock: one adjacent
        // source (west, 1728) plus internal 576 = 2304, below both the
        // 10000 cap and the two far chests' combined capacity (3456), so
        // it must move exactly all available stock.
        var be2 = (PoweredItemSenderBE) place(h, 1);
        card(be2, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        AutoIoConfigHelper.setMasks(be2, AutoIoConfigData.ALL_SIDES_MASK, 0);
        be2.setAreaSettings(3, 1, 3, 0, 0, 0, false);
        h.setBlock(far1, Blocks.CHEST);
        h.setBlock(far2, Blocks.CHEST);
        clearTarget(h, far1);
        clearTarget(h, far2);
        int available = 0;
        var westPos = new BlockPos(1, 1, 2);
        h.setBlock(westPos, Blocks.AIR);
        h.setBlock(westPos, Blocks.CHEST);
        var westHandler = h.getLevel().getBlockEntity(h.absolutePos(westPos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < westHandler.getSlots(); i++) westHandler.insertItem(i, new ItemStack(Items.STONE, 64), false);
        for (int i = 0; i < westHandler.getSlots(); i++) available += westHandler.getStackInSlot(i).getCount();
        fillSender(be2, 64);
        available += 576;
        be2.getEnergyStorage().setEnergy(be2.getMaxEnergy());
        int before = targetCount(h, far1) + targetCount(h, far2);
        be2.tickServer();
        int moved = targetCount(h, far1) + targetCount(h, far2) - before;
        h.assertTrue(available == 2304 && moved == available,
                "single direct execution moves all available below cap, available=" + available + " moved=" + moved);
        h.succeed();
    }
}
