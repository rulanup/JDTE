package com.jdte.common.blockentities;

import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.jdte.common.autoioconfig.OverclockDirectTransferHelper;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Ultimate-card acceptance for the three item sender tiers, strict original
 * 0.6.5.2 behaviour: Ultimate Overclock is 10 ticker executions, the normal
 * path keeps the base 64-per-execution cap (getItemsToSend is unchanged), the
 * normal path only gates on FE (no per-operation deduction), and the direct
 * path deducts one standard cost per execution that actually moved items.
 *
 * Throughput notes (do not "fix" these into impossible assertions):
 * - Internal stock is 9 slots x 64 = 576, so a single ticker tick can move at
 *   most 576 from internal stock alone, even at 640/tick or 20000/tick.
 *   Execution count is therefore proven with counting block entities, and
 *   per-execution caps are proven with single tickServer calls.
 * - The 100000/tick direct figure is theoretical: it needs source stock,
 *   target capacity, the 256-operation-per-execution budget and FE to allow
 *   it. Direct tests use measured available stock instead of that figure.
 */
@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class ItemSenderUltimateGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos TARGET = new BlockPos(3, 1, 2);

    // Counting subclasses: the only way to observe how many times the real
    // block ticker executed sendItems inside one ticker call.
    public static final class CountingBasicItemSenderBE extends BasicItemSenderBE {
        public int sendCalls;
        public CountingBasicItemSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override protected void sendItems() { sendCalls++; super.sendItems(); }
    }
    public static final class CountingAdvancedItemSenderBE extends AdvancedItemSenderBE {
        public int sendCalls;
        public CountingAdvancedItemSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override protected void sendItems() { sendCalls++; super.sendItems(); }
    }
    public static final class CountingExtendedItemSenderBE extends ExtendedItemSenderBE {
        public int sendCalls;
        public CountingExtendedItemSenderBE(BlockPos pos, BlockState state) { super(pos, state); }
        @Override protected void sendItems() { sendCalls++; super.sendItems(); }
    }

    private static ItemSenderBE place(GameTestHelper h, int tier) {
        // AIR first: re-placing the same block state does not replace the
        // block entity, so without this a second place() in one test would
        // silently reuse the previous sender (and its cards) and chest.
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_ITEM_SENDER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_ITEM_SENDER.get() : JDTEBlocks.EXTENDED_ITEM_SENDER.get());
        var be = (ItemSenderBE) h.getBlockEntity(POS);
        h.setBlock(TARGET, Blocks.AIR);
        h.setBlock(TARGET, Blocks.CHEST);
        return be;
    }

    private static ItemSenderBE placeCounting(GameTestHelper h, int tier) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_ITEM_SENDER.get()
                : tier == 1 ? JDTEBlocks.ADVANCED_ITEM_SENDER.get() : JDTEBlocks.EXTENDED_ITEM_SENDER.get());
        var abs = h.absolutePos(POS);
        var state = h.getLevel().getBlockState(abs);
        ItemSenderBE be = tier == 0 ? new CountingBasicItemSenderBE(abs, state)
                : tier == 1 ? new CountingAdvancedItemSenderBE(abs, state)
                : new CountingExtendedItemSenderBE(abs, state);
        h.getLevel().setBlockEntity(be);
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

    private static IItemHandler targetHandler(GameTestHelper h) {
        return h.getLevel().getBlockEntity(h.absolutePos(TARGET))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
    }

    private static int targetCount(GameTestHelper h) {
        var handler = targetHandler(h);
        int n = 0;
        for (int i = 0; i < handler.getSlots(); i++) n += handler.getStackInSlot(i).getCount();
        return n;
    }

    @SuppressWarnings("unchecked")
    private static void tickOnce(GameTestHelper h, ItemSenderBE be) {
        var ticker = (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)
                be.getBlockState().getTicker(h.getLevel(), be.getType());
        ticker.tick(h.getLevel(), be.getBlockPos(), be.getBlockState(), be);
    }

    private static int sendCalls(ItemSenderBE be) {
        if (be instanceof CountingBasicItemSenderBE c) return c.sendCalls;
        if (be instanceof CountingAdvancedItemSenderBE c) return c.sendCalls;
        if (be instanceof CountingExtendedItemSenderBE c) return c.sendCalls;
        throw new IllegalStateException("Not a counting sender");
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCardsInstallAndSpeedCardsAreExclusive(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            var upgrades = UpgradeHelper.getUpgradeHandler(be);
            h.assertTrue(UpgradeHelper.countUpgrades(be, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                    && UpgradeHelper.countUpgrades(be, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                    "both Ultimate cards install on tier " + tier);
            h.assertTrue(!upgrades.isItemValid(2, new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()))
                    && !upgrades.isItemValid(2, new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get()))
                    && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()))
                    && !upgrades.isItemValid(2, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                    "speed cards exclusive and Ultimate cards capped at one on tier " + tier);
            // Other restricted families must stay restricted: the collector
            // rejects both Ultimate cards in original and port alike, so it
            // stays the negative control now that receivers take Ultimate
            // cards (27-family round). A receiver is no longer a valid
            // negative control here.
            h.setBlock(new BlockPos(5, 1, 2), JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get());
            var collector = h.getBlockEntity(new BlockPos(5, 1, 2));
            h.assertTrue(collector instanceof AdvancedItemCollectorBE
                    && !UpgradeHelper.getUpgradeHandler((com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE) collector)
                            .isItemValid(0, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())),
                    "Ultimate stays restricted for non-27 families");
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCapacityValuesAndBasicHasNoEnergyEffect(GameTestHelper h) {
        // Basic: Ultimate Capacity installs but changes nothing measurable.
        var basic = place(h, 0);
        card(basic, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        h.assertTrue(basic.getItemHandler().getSlots() == 9 && senderCount(basic) == 0,
                "Basic Ultimate Capacity does not add item slots");
        // Advanced: 50000 -> 2500000, plus 3 Capacity -> 20000000.
        var adv = (PoweredItemSenderBE) place(h, 1);
        card(adv, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        h.assertTrue(adv.getMaxEnergy() == 2500000, "Advanced Ultimate Capacity 2500000, got " + adv.getMaxEnergy());
        adv.getEnergyStorage().setEnergy(adv.getMaxEnergy());
        h.assertTrue(adv.getEnergyStorage().getEnergyStored() == 2500000, "Advanced can actually hold 2500000 FE");
        card(adv, JDTEItems.CAPACITY_UPGRADE.get(), 1);
        card(adv, JDTEItems.CAPACITY_UPGRADE.get(), 2);
        card(adv, JDTEItems.CAPACITY_UPGRADE.get(), 3);
        h.assertTrue(adv.getMaxEnergy() == 20000000, "Advanced Ultimate + 3 Capacity 20000000, got " + adv.getMaxEnergy());
        // Extended: 100000 -> 5000000, plus 3 Capacity -> 40000000.
        var ext = (PoweredItemSenderBE) place(h, 2);
        card(ext, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        h.assertTrue(ext.getMaxEnergy() == 5000000, "Extended Ultimate Capacity 5000000, got " + ext.getMaxEnergy());
        card(ext, JDTEItems.CAPACITY_UPGRADE.get(), 1);
        card(ext, JDTEItems.CAPACITY_UPGRADE.get(), 2);
        card(ext, JDTEItems.CAPACITY_UPGRADE.get(), 3);
        h.assertTrue(ext.getMaxEnergy() == 40000000, "Extended Ultimate + 3 Capacity 40000000, got " + ext.getMaxEnergy());
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateCapacityRemovalTruncatesStoredEnergy(GameTestHelper h) {
        var adv = (PoweredItemSenderBE) place(h, 1);
        card(adv, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
        adv.getEnergyStorage().setEnergy(adv.getMaxEnergy());
        h.assertTrue(adv.getEnergyStorage().getEnergyStored() == 2500000, "filled to Ultimate capacity");
        var removed = UpgradeHelper.getUpgradeHandler(adv).extractItem(0, 1, false);
        h.assertTrue(removed.is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), "Ultimate Capacity card returns on removal");
        UpgradeHelper.syncCapacities(adv);
        h.assertTrue(adv.getMaxEnergy() == 50000 && adv.getEnergyStorage().getEnergyStored() == 50000,
                "stored FE truncates to base capacity on removal, got " + adv.getEnergyStorage().getEnergyStored());
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateOverclockTickerRunsTenExecutionsStockLimited(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = placeCounting(h, tier);
            if (be instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            fillSender(be, 64);
            tickOnce(h, be);
            h.assertTrue(sendCalls(be) == 10, "ticker runs 10 sendItems executions on tier " + tier + ", got " + sendCalls(be));
            h.assertTrue(targetCount(h) == 576 && senderCount(be) == 0,
                    "one ticker tick moves all 576 internal stock on tier " + tier + ", target=" + targetCount(h));
            // No-card control on a fresh sender: one execution, 64 moved.
            // Powered tiers need FE first, otherwise the energy gate (not
            // the execution count) would be what stops the transfer.
            var plain = placeCounting(h, tier);
            if (plain instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            fillSender(plain, 64);
            tickOnce(h, plain);
            h.assertTrue(sendCalls(plain) == 1 && targetCount(h) == 64,
                    "no-card ticker runs 1 execution moving 64 on tier " + tier);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateOverclockNormalPathPerExecutionCapIsBaseRate(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            if (be instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            fillSender(be, 64);
            // Auxiliary single tickServer call = one execution: must move 64,
            // not the 10000 overclock rate. This is the original behaviour.
            be.tickServer();
            h.assertTrue(targetCount(h) == 64 && senderCount(be) == 512,
                    "Ultimate normal per-execution cap is 64 on tier " + tier + ", target=" + targetCount(h));
            // Standard Overclock control: one execution moves the whole 576
            // stock (cap 10000, stock-limited), proving the two caps differ.
            var oc = place(h, tier);
            if (oc instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            card(oc, JDTEItems.OVERCLOCK_UPGRADE.get(), 0);
            fillSender(oc, 64);
            oc.tickServer();
            h.assertTrue(targetCount(h) == 576 && senderCount(oc) == 0,
                    "standard Overclock single execution is stock-limited at 576 on tier " + tier);
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateOverclockStandardCostIsFiftyTimes(GameTestHelper h) {
        for (int tier : List.of(1, 2)) {
            var be = (PoweredItemSenderBE) place(h, tier);
            h.assertTrue(be.getStandardEnergyCost() == 500, "base cost 500 on tier " + tier);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            h.assertTrue(be.getStandardEnergyCost() == 25000, "Ultimate cost 25000 on tier " + tier + ", got " + be.getStandardEnergyCost());
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 1);
            h.assertTrue(be.getStandardEnergyCost() == 25000, "Ultimate cost stays 25000 with Capacity on tier " + tier);
        }
        // Standard Overclock control still costs 3x, not 50x.
        var oc = (PoweredItemSenderBE) place(h, 1);
        card(oc, JDTEItems.OVERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(oc.getStandardEnergyCost() == 1500, "standard Overclock cost 1500, got " + oc.getStandardEnergyCost());
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void normalPathEnergyGateWithoutPerOperationDeduction(GameTestHelper h) {
        for (int tier : List.of(1, 2)) {
            // Separate senders: a failed attempt starts the transfer backoff
            // (10 ticks), so the passing case must not reuse the failed one
            // on the immediately following tickServer call.
            var blocked = (PoweredItemSenderBE) place(h, tier);
            card(blocked, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            blocked.getEnergyStorage().setEnergy(24999);
            fillSender(blocked, 64);
            blocked.tickServer();
            h.assertTrue(targetCount(h) == 0 && blocked.getEnergyStorage().getEnergyStored() == 24999,
                    "FE below 25000 gate blocks normal path on tier " + tier);
            var be = (PoweredItemSenderBE) place(h, tier);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            be.getEnergyStorage().setEnergy(25000);
            fillSender(be, 64);
            be.tickServer();
            h.assertTrue(targetCount(h) == 64 && be.getEnergyStorage().getEnergyStored() == 25000,
                    "normal path gates but does not deduct on tier " + tier + ", energy=" + be.getEnergyStorage().getEnergyStored());
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferEnabledAndDeductsPerMovingExecution(GameTestHelper h) {
        // Advanced tier, AutoIO input on all sides. Direct transfer moves
        // from internal stock and adjacent sources to AREA targets, but
        // adjacent endpoint positions are excluded as targets, so the
        // targets must sit outside the adjacent ring: widen the area to
        // radius 3 and put targets at dx = 2 and dx = 3. One direct
        // tickServer execution that moves items must deduct exactly one
        // 25000 standard cost.
        var be = (PoweredItemSenderBE) place(h, 1);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        h.assertTrue(OverclockDirectTransferHelper.isEnabled(be) == false,
                "direct path off before AutoIO is configured");
        AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
        h.assertTrue(OverclockDirectTransferHelper.isEnabled(be),
                "Ultimate alone enables direct path once AutoIO input is configured");
        be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
        var farTarget1 = new BlockPos(4, 1, 2);
        var farTarget2 = new BlockPos(5, 1, 2);
        h.setBlock(farTarget1, Blocks.CHEST);
        h.setBlock(farTarget2, Blocks.CHEST);
        // Source chest on the west side, full of stone.
        var sourcePos = new BlockPos(1, 1, 2);
        h.setBlock(sourcePos, Blocks.CHEST);
        var source = h.getLevel().getBlockEntity(h.absolutePos(sourcePos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < source.getSlots(); i++) source.insertItem(i, new ItemStack(Items.STONE, 64), false);
        int sourceTotal = 0;
        for (int i = 0; i < source.getSlots(); i++) sourceTotal += source.getStackInSlot(i).getCount();
        h.assertTrue(sourceTotal > 0, "source chest actually filled, total=" + sourceTotal);
        fillSender(be, 64);
        be.getEnergyStorage().setEnergy(be.getMaxEnergy());
        int energyBefore = be.getEnergyStorage().getEnergyStored();
        be.tickServer();
        int moved = 0;
        for (var targetPos : List.of(farTarget1, farTarget2)) {
            var handler = h.getLevel().getBlockEntity(h.absolutePos(targetPos))
                    .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
            for (int i = 0; i < handler.getSlots(); i++) moved += handler.getStackInSlot(i).getCount();
        }
        h.assertTrue(moved > 0 && moved <= 10000,
                "one direct execution moves between 1 and 10000, moved=" + moved);
        h.assertTrue(be.getEnergyStorage().getEnergyStored() == energyBefore - 25000,
                "direct execution that moved deducts exactly 25000, energy=" + be.getEnergyStorage().getEnergyStored());
        // No-card control: direct path stays off even with AutoIO configured.
        var plain = (PoweredItemSenderBE) place(h, 1);
        AutoIoConfigHelper.setMasks(plain, AutoIoConfigData.ALL_SIDES_MASK, 0);
        h.assertTrue(!OverclockDirectTransferHelper.isEnabled(plain), "no-card sender has no direct path");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void directTransferTickerTickMovesAllAvailableStock(GameTestHelper h) {
        // Full ticker tick on the direct path (Advanced tier): internal 576
        // plus one adjacent source chest (1728) = 2304 available, all within
        // the first direct execution's 10000 cap, so executions 2-10 move
        // nothing and deduct nothing. Total deduction is therefore exactly
        // one 25000 standard cost, not 10 x 25000.
        var be = (PoweredItemSenderBE) place(h, 1);
        card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
        AutoIoConfigHelper.setMasks(be, AutoIoConfigData.ALL_SIDES_MASK, 0);
        be.setAreaSettings(3, 1, 3, 0, 0, 0, false);
        var farTarget1 = new BlockPos(4, 1, 2);
        var farTarget2 = new BlockPos(5, 1, 2);
        h.setBlock(farTarget1, Blocks.CHEST);
        h.setBlock(farTarget2, Blocks.CHEST);
        var sourcePos = new BlockPos(1, 1, 2);
        h.setBlock(sourcePos, Blocks.CHEST);
        var source = h.getLevel().getBlockEntity(h.absolutePos(sourcePos))
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < source.getSlots(); i++) source.insertItem(i, new ItemStack(Items.STONE, 64), false);
        fillSender(be, 64);
        be.getEnergyStorage().setEnergy(be.getMaxEnergy());
        tickOnce(h, be);
        int moved = 0;
        for (var targetPos : List.of(farTarget1, farTarget2)) {
            var handler = h.getLevel().getBlockEntity(h.absolutePos(targetPos))
                    .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
            for (int i = 0; i < handler.getSlots(); i++) moved += handler.getStackInSlot(i).getCount();
        }
        h.assertTrue(moved == 2304, "ticker direct tick moves all 2304 available, moved=" + moved);
        h.assertTrue(be.getEnergyStorage().getEnergyStored() == 25000,
                "only the moving execution deducts, energy=" + be.getEnergyStorage().getEnergyStored());
        h.assertTrue(senderCount(be) == 0, "internal stock fully drained");
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void ultimateBothCardsAndSaveReload(GameTestHelper h) {
        for (int tier = 0; tier < 3; tier++) {
            var be = place(h, tier);
            card(be, JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(), 0);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 1);
            fillSender(be, 64);
            if (be instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            var saved = be.saveWithFullMetadata();
            var fresh = tier == 0 ? new BasicItemSenderBE(be.getBlockPos(), be.getBlockState())
                    : tier == 1 ? new AdvancedItemSenderBE(be.getBlockPos(), be.getBlockState())
                    : new ExtendedItemSenderBE(be.getBlockPos(), be.getBlockState());
            h.getLevel().setBlockEntity(fresh);
            fresh.load(saved);
            var upgrades = UpgradeHelper.getUpgradeHandler(fresh);
            h.assertTrue(upgrades.getStackInSlot(0).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())
                    && upgrades.getStackInSlot(1).is(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get())
                    && senderCount(fresh) == 576,
                    "cards and inventory survive save/reload on tier " + tier);
            if (fresh instanceof PoweredItemSenderBE powered) {
                h.assertTrue(powered.getMaxEnergy() == (tier == 1 ? 2500000 : 5000000)
                        && powered.getEnergyStorage().getEnergyStored() == powered.getMaxEnergy(),
                        "FE capacity and stored energy survive save/reload on tier " + tier);
            }
        }
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void noTargetBackoffDoesNotLoseItems(GameTestHelper h) {
        // No chest target at all: sender must keep its stock and its cards,
        // and must not throw or spin into a moved state.
        for (int tier = 0; tier < 3; tier++) {
            h.setBlock(POS, Blocks.AIR);
            h.setBlock(TARGET, Blocks.AIR);
            h.setBlock(POS, tier == 0 ? JDTEBlocks.BASIC_ITEM_SENDER.get()
                    : tier == 1 ? JDTEBlocks.ADVANCED_ITEM_SENDER.get() : JDTEBlocks.EXTENDED_ITEM_SENDER.get());
            var be = (ItemSenderBE) h.getBlockEntity(POS);
            card(be, JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(), 0);
            if (be instanceof PoweredItemSenderBE powered) powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            fillSender(be, 64);
            tickOnce(h, be);
            h.assertTrue(senderCount(be) == 576, "no-target ticker keeps all stock on tier " + tier);
            // Restoring a target resumes transfer. The area target cache
            // refreshes on a 40-tick interval in a real server, and manual
            // ticker calls do not advance game time, so the test invalidates
            // the cache to simulate that passage of time; the backoff retry
            // ticks are drained by the first executions of the next tick.
            h.setBlock(TARGET, Blocks.AIR);
            h.setBlock(TARGET, Blocks.CHEST);
            OverclockDirectTransferHelper.invalidate(be);
            tickOnce(h, be);
            h.assertTrue(targetCount(h) > 0, "transfer resumes after target returns on tier " + tier);
        }
        h.succeed();
    }
}
