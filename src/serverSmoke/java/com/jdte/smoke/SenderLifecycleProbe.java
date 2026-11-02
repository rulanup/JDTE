package com.jdte.smoke;

import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.blockentities.PoweredItemSenderBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Dedicated-server lifecycle probe for the three item sender tiers. Test-only; never in the production JAR. */
public final class SenderLifecycleProbe {
    private static final BlockPos BASIC = new BlockPos(1044, 80, 1044);
    private static final BlockPos ADVANCED = BASIC.east(4);
    private static final BlockPos EXTENDED = BASIC.east(8);
    private static final BlockPos BASIC_TARGET = BASIC.east(1);
    private String mode = "";
    private MinecraftServer server;
    private int ticks;
    private int stage;
    private boolean unloaded;

    private void check(boolean v, String s) { if (!v) throw new IllegalStateException("Sender lifecycle: " + s); }

    private void force(boolean v) { server.overworld().setChunkForced(BASIC.getX() >> 4, BASIC.getZ() >> 4, v); }

    private ItemSenderBE be(BlockPos pos) { return (ItemSenderBE) server.overworld().getBlockEntity(pos); }

    private int chestCount(BlockPos pos) {
        var handler = server.overworld().getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
        int n = 0;
        for (int i = 0; i < handler.getSlots(); i++) n += handler.getStackInSlot(i).getCount();
        return n;
    }

    private void verifyState() {
        var basic = be(BASIC);
        var adv = (PoweredItemSenderBE) be(ADVANCED);
        var ext = (PoweredItemSenderBE) be(EXTENDED);
        check(basic != null && adv != null && ext != null, "all three sender BEs present");
        // Diagnostic: does the filter capability survive chunk reload?
        // Extended is the tier that crashed at break time, so log all three.
        for (var sender : List.of(basic, adv, ext)) {
            var filter = sender.getFilterHandler();
            com.mojang.logging.LogUtils.getLogger().info("Sender lifecycle verifyState: {} filterSlots={} be={}",
                    sender.getBlockPos(), filter.getSlots(), sender);
        }
        check(server.overworld().getBlockState(BASIC).is(JDTEBlocks.BASIC_ITEM_SENDER.get()), "Basic block present");
        check(server.overworld().getBlockState(ADVANCED).is(JDTEBlocks.ADVANCED_ITEM_SENDER.get()), "Advanced block present");
        check(server.overworld().getBlockState(EXTENDED).is(JDTEBlocks.EXTENDED_ITEM_SENDER.get()), "Extended block present");
        check(UpgradeHelper.countUpgrades(adv, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                && UpgradeHelper.countUpgrades(adv, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                "Advanced kept both Ultimate cards");
        check(UpgradeHelper.countUpgrades(ext, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                && UpgradeHelper.countUpgrades(ext, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                "Extended kept both Ultimate cards");
        check(adv.getMaxEnergy() == 2500000 && adv.getEnergyStorage().getEnergyStored() == 1000000,
                "Advanced kept FE 1000000/2500000, got " + adv.getEnergyStorage().getEnergyStored() + "/" + adv.getMaxEnergy());
        check(ext.getMaxEnergy() == 5000000 && ext.getEnergyStorage().getEnergyStored() == 1234567,
                "Extended kept FE 1234567/5000000, got " + ext.getEnergyStorage().getEnergyStored() + "/" + ext.getMaxEnergy());
    }

    private void breakSender(BlockPos pos, Block block, net.minecraft.world.item.Item item) {
        var before = server.overworld().getBlockState(pos);
        check(before.is(block), "block to break is sender at " + pos);
        // Diagnostic: probe the filter capability immediately before destroyBlock,
        // so a crash here distinguishes "caps already dead" from "destroy invalidates".
        var probeBe = be(pos);
        com.mojang.logging.LogUtils.getLogger().info("Sender lifecycle breakSender: pos={} be={} filterSlots={}",
                pos, probeBe, probeBe == null ? -1 : probeBe.getFilterHandler().getSlots());
        var lootDrops = Block.getDrops(before, server.overworld(), pos, null);
        check(lootDrops.size() == 1 && ItemStack.isSameItem(lootDrops.get(0), new ItemStack(item)) && lootDrops.get(0).getCount() == 1,
                "loot drops exactly one self item for " + pos + ", got " + lootDrops);
        var p = FakePlayerFactory.get(server.overworld(), new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2010"), "SenderLifecycle"));
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        check(p.gameMode.destroyBlock(pos), "FakePlayer breaks sender at " + pos);
        check(server.overworld().getBlockState(pos).isAir(), "broken sender leaves air at " + pos);
        check(server.overworld().getBlockEntity(pos) == null, "broken sender BE removed at " + pos);
    }

    @SubscribeEvent public void start(ServerStartedEvent e) throws Exception {
        mode = System.getProperty("jdte.senderLifecycle", "");
        if (mode.isEmpty()) return;
        server = e.getServer();
        check(Files.exists(Path.of(".jdte-lifecycle-fixture")), "marked fixture");
        force(true);
        server.overworld().getChunkAt(BASIC);
        if (mode.equals("verify")) { stage = 10; return; }
        var level = server.overworld();
        for (var p : List.of(BASIC, ADVANCED, EXTENDED, BASIC_TARGET)) {
            level.setBlockAndUpdate(p.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(BASIC, JDTEBlocks.BASIC_ITEM_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(ADVANCED, JDTEBlocks.ADVANCED_ITEM_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(EXTENDED, JDTEBlocks.EXTENDED_ITEM_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(BASIC_TARGET, Blocks.CHEST.defaultBlockState());
        // Basic runs live (IGNORED redstone, full stock, adjacent target).
        var basic = be(BASIC);
        for (int i = 0; i < 9; i++) basic.getItemHandler().setStackInSlot(i, new ItemStack(Items.STONE, 64));
        // Advanced and Extended stay inactive (HIGH, no signal) so their
        // cards and FE survive the whole lifecycle unchanged.
        for (var pos : List.of(ADVANCED, EXTENDED)) {
            var powered = (PoweredItemSenderBE) be(pos);
            UpgradeHelper.getUpgradeHandler(powered).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
            UpgradeHelper.getUpgradeHandler(powered).insertItem(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()), false);
            powered.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
            powered.getRedstoneControlData().receivingRedstone = false;
            powered.getItemHandler().setStackInSlot(0, new ItemStack(Items.DIRT, 7));
        }
        ((PoweredItemSenderBE) be(ADVANCED)).getEnergyStorage().setEnergy(1000000);
        ((PoweredItemSenderBE) be(EXTENDED)).getEnergyStorage().setEnergy(1234567);
        verifyState();
    }

    @SubscribeEvent public void unload(ChunkEvent.Unload e) {
        if (server != null && stage == 2 && e.getLevel() == server.overworld() && e.getChunk().getPos().equals(new net.minecraft.world.level.ChunkPos(BASIC))) unloaded = true;
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) throws Exception {
        if (mode.isEmpty() || e.phase != TickEvent.Phase.END || e.getServer() != server) return;
        check(++ticks < 1800, "timeout stage " + stage);
        var level = server.overworld();
        if (stage == 0 && ticks > 200) {
            // Live Basic transfer on the real server: all 576 stone must
            // have reached the adjacent chest, and the sender is empty.
            // Waited to tick 200 on purpose: a stale initial area cache
            // plus the sender's doubling failure backoff (10 -> 20 -> 40)
            // can push the first successful retry well past tick 41, and
            // a real server does not need the transfer by tick 41.
            int chest = chestCount(BASIC_TARGET);
            int senderStock = 0;
            var basicBe = be(BASIC);
            if (basicBe != null) for (int i = 0; i < 9; i++) senderStock += basicBe.getItemHandler().getStackInSlot(i).getCount();
            var cached = basicBe == null ? List.<BlockPos>of() : com.jdte.common.autoioconfig.OverclockDirectTransferHelper.getCachedItemPositions(server.overworld(), basicBe, basicBe);
            com.mojang.logging.LogUtils.getLogger().info("Sender lifecycle stage 0 tick {}: chest={} senderStock={} cachedTargets={}",
                    ticks, chest, senderStock, cached);
            if (chest != 576 || !basicBe.getItemHandler().getStackInSlot(0).isEmpty()) {
                com.mojang.logging.LogUtils.getLogger().error("Sender lifecycle stage 0 FAILED: chest={} senderStock={} cachedTargets={}", chest, senderStock, cached);
                mode = "";
                server.saveEverything(false, true, true);
                server.halt(false);
                return;
            }
            server.saveEverything(false, true, true);
            stage = 1;
        } else if (stage == 1) {
            force(false);
            stage = 2;
        } else if (stage == 2 && unloaded && !level.getChunkSource().hasChunk(BASIC.getX() >> 4, BASIC.getZ() >> 4)) {
            level.getChunkSource().save(true);
            force(true);
            level.getChunkAt(BASIC);
            verifyState();
            breakSender(EXTENDED, JDTEBlocks.EXTENDED_ITEM_SENDER.get(), JDTEItems.EXTENDED_ITEM_SENDER.get());
            stage = 3;
        } else if (stage == 3) {
            server.saveEverything(false, true, true);
            finish("sender-seed-success", "PASS: dedicated server placed 3 senders, live Basic transfer moved 576 stone to an adjacent chest, saved, chunk unloaded and reloaded with Ultimate cards and FE intact (Advanced 1000000/2500000, Extended 1234567/5000000), real break loot-dropped exactly one Extended sender and left air, saved shutdown.");
        } else if (stage == 10 && ticks > 10) {
            // Fresh JVM: Basic and Advanced restored; Extended was broken.
            check(server.overworld().getBlockState(EXTENDED).isAir(), "Extended stays broken after restart");
            var basic = be(BASIC);
            var adv = (PoweredItemSenderBE) be(ADVANCED);
            check(basic != null && adv != null, "Basic and Advanced restored after restart");
            check(UpgradeHelper.countUpgrades(adv, com.jdte.common.upgrades.UpgradeType.ULTIMATE_CAPACITY) == 1
                    && UpgradeHelper.countUpgrades(adv, com.jdte.common.upgrades.UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                    "Advanced kept both Ultimate cards after restart");
            check(adv.getMaxEnergy() == 2500000 && adv.getEnergyStorage().getEnergyStored() == 1000000,
                    "Advanced kept FE after restart, got " + adv.getEnergyStorage().getEnergyStored());
            check(adv.getItemHandler().getStackInSlot(0).is(Items.DIRT) && adv.getItemHandler().getStackInSlot(0).getCount() == 7,
                    "Advanced kept inventory after restart");
            breakSender(ADVANCED, JDTEBlocks.ADVANCED_ITEM_SENDER.get(), JDTEItems.ADVANCED_ITEM_SENDER.get());
            stage = 11;
        } else if (stage == 11) {
            finish("sender-verify-success", "PASS: fresh dedicated-server JVM restored Basic and Advanced senders with Ultimate cards, FE and inventory intact, Extended stayed broken, real break loot-dropped exactly one Advanced sender and left air.");
        }
    }

    private void finish(String marker, String s) throws Exception {
        Files.writeString(Path.of(marker), s + "\n");
        mode = "";
        server.saveEverything(false, true, true);
        server.halt(false);
    }
}
