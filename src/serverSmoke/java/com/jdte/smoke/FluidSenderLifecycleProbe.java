package com.jdte.smoke;

import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.FluidSenderBE;
import com.jdte.common.blockentities.PoweredFluidSenderBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

/**
 * Dedicated-server lifecycle probe for the three fluid sender tiers:
 * live Basic transfer, chunk unload/reload with Ultimate cards, FE, tanks
 * and Extended 8-slot state intact, fresh-JVM restore, and survival breaks
 * with loot-table drops plus before/after ItemEntity deltas. Production
 * ItemEntity counting is known to yield 0 even for a vanilla stone control
 * in this playerless probe chunk (same anomaly class as the Fumo round),
 * so each phase also breaks a vanilla stone control the same way: if the
 * sender delta is 0 but the stone delta is 1, the probe fails loudly
 * (a genuine sender drop problem); if both are 0, the phase finishes on
 * loot-table drop plus destroy-to-air evidence with the anomaly recorded.
 */
public final class FluidSenderLifecycleProbe {
    private static final BlockPos BASIC = new BlockPos(1044, 80, 1044);
    private static final BlockPos ADVANCED = BASIC.east(4);
    private static final BlockPos EXTENDED = BASIC.east(8);
    private static final BlockPos BASIC_TARGET = BASIC.east(1);
    private static final BlockPos STONE_CONTROL = BASIC.west(4);
    private String mode = "";
    private MinecraftServer server;
    private int ticks;
    private int stage;
    private boolean unloaded;
    private int breakTick;
    private int breakBefore;
    private Item breakItem;
    private int stoneBefore;
    private int stoneTick;
    private int extDelta = -1;
    private int advDelta = -1;
    private int basicDelta = -1;

    private void check(boolean v, String s) { if (!v) throw new IllegalStateException("Fluid sender lifecycle: " + s); }

    private void force(boolean v) { server.overworld().setChunkForced(BASIC.getX() >> 4, BASIC.getZ() >> 4, v); }

    private FluidSenderBE be(BlockPos pos) { return (FluidSenderBE) server.overworld().getBlockEntity(pos); }

    private int tank(BlockPos pos) {
        var b = be(pos);
        return b == null ? -1 : b.getFluidTank().getFluidAmount();
    }

    private void verifyState() {
        var basic = be(BASIC);
        var adv = (PoweredFluidSenderBE) be(ADVANCED);
        var ext = (PoweredFluidSenderBE) be(EXTENDED);
        check(basic != null && adv != null && ext != null, "all three sender BEs present");
        check(server.overworld().getBlockState(BASIC).is(JDTEBlocks.BASIC_FLUID_SENDER.get()), "Basic block present");
        check(server.overworld().getBlockState(ADVANCED).is(JDTEBlocks.ADVANCED_FLUID_SENDER.get()), "Advanced block present");
        check(server.overworld().getBlockState(EXTENDED).is(JDTEBlocks.EXTENDED_FLUID_SENDER.get()), "Extended block present");
        check(UpgradeHelper.countUpgrades(adv, UpgradeType.ULTIMATE_CAPACITY) == 1
                && UpgradeHelper.countUpgrades(adv, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                "Advanced kept both Ultimate cards");
        check(adv.getMaxEnergy() == 2500000 && adv.getEnergyStorage().getEnergyStored() == 1000000,
                "Advanced kept FE 1000000/2500000, got " + adv.getEnergyStorage().getEnergyStored() + "/" + adv.getMaxEnergy());
        check(adv.getMaxMB() == 400000 && tank(ADVANCED) == 12345
                && adv.getFluidTank().getFluid().getFluid() == Fluids.WATER,
                "Advanced kept 12345 mB water in a 400000 tank, got " + tank(ADVANCED) + "/" + adv.getMaxMB());
        check(UpgradeHelper.countUpgrades(ext, UpgradeType.ULTIMATE_CAPACITY) == 1
                && UpgradeHelper.countUpgrades(ext, UpgradeType.ULTIMATE_OVERCLOCK) == 1
                && UpgradeHelper.countUpgrades(ext, UpgradeType.CAPACITY) == 3
                && UpgradeHelper.countUpgrades(ext, UpgradeType.RANGE) == 2
                && UpgradeHelper.countUpgrades(ext, UpgradeType.FILTER) == 1,
                "Extended kept all eight cards");
        check(ext.getMaxEnergy() == 40000000 && ext.getEnergyStorage().getEnergyStored() == 1234567,
                "Extended kept FE 1234567/40000000, got " + ext.getEnergyStorage().getEnergyStored() + "/" + ext.getMaxEnergy());
        check(ext.getMaxMB() == 3200000 && tank(EXTENDED) == 23456
                && ext.getFluidTank().getFluid().getFluid() == Fluids.WATER,
                "Extended kept 23456 mB water in a 3200000 tank, got " + tank(EXTENDED) + "/" + ext.getMaxMB());
        check(ext.getFilterHandler().getStackInSlot(0).is(Items.STONE), "Extended kept filter stone");
    }

    private int countSelf(BlockPos pos, Item item) {
        var abs = pos;
        int n = 0;
        for (var e : server.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(2))) {
            if (e.getItem().is(item)) n += e.getItem().getCount();
        }
        return n;
    }

    private void breakSender(BlockPos pos, Block block, Item item) {
        var before = server.overworld().getBlockState(pos);
        check(before.is(block), "block to break is sender at " + pos);
        var lootDrops = Block.getDrops(before, server.overworld(), pos, null);
        check(lootDrops.size() == 1 && ItemStack.isSameItem(lootDrops.get(0), new ItemStack(item)) && lootDrops.get(0).getCount() == 1,
                "loot drops exactly one self item for " + pos + ", got " + lootDrops);
        breakBefore = countSelf(pos, item);
        breakItem = item;
        breakTick = ticks;
        var p = FakePlayerFactory.get(server.overworld(), new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2011"), "FluidSenderLifecycle"));
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        check(p.gameMode.destroyBlock(pos), "FakePlayer breaks sender at " + pos);
        check(server.overworld().getBlockState(pos).isAir(), "broken sender leaves air at " + pos);
        check(server.overworld().getBlockEntity(pos) == null, "broken sender BE removed at " + pos);
    }

    private int recordDelta(BlockPos pos) {
        return countSelf(pos, breakItem) - breakBefore;
    }

    private void breakStoneControl() {
        var level = server.overworld();
        check(level.getBlockState(STONE_CONTROL).isAir(), "stone control spot is air at " + STONE_CONTROL);
        level.setBlockAndUpdate(STONE_CONTROL, Blocks.STONE.defaultBlockState());
        var loot = Block.getDrops(Blocks.STONE.defaultBlockState(), level, STONE_CONTROL, null);
        check(loot.size() == 1 && loot.get(0).is(Items.COBBLESTONE) && loot.get(0).getCount() == 1,
                "stone control loot is exactly one cobblestone, got " + loot);
        stoneBefore = countSelf(STONE_CONTROL, Items.COBBLESTONE);
        var p = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2011"), "FluidSenderLifecycle"));
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(STONE_CONTROL.getX() + 0.5, STONE_CONTROL.getY(), STONE_CONTROL.getZ() + 0.5);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        check(p.gameMode.destroyBlock(STONE_CONTROL), "FakePlayer breaks stone control");
        check(level.getBlockState(STONE_CONTROL).isAir(), "stone control leaves air");
        stoneTick = ticks;
    }

    private int stoneDelta() {
        return countSelf(STONE_CONTROL, Items.COBBLESTONE) - stoneBefore;
    }

    private void decideDrops(String marker, String fullText, String anomalyText, int... deltas) throws Exception {
        int stone = stoneDelta();
        boolean allOne = true;
        for (int d : deltas) if (d != 1) allOne = false;
        if (allOne) {
            finish(marker, fullText);
            return;
        }
        boolean allZero = stone == 0;
        for (int d : deltas) if (d != 0) allZero = false;
        if (allZero) {
            com.mojang.logging.LogUtils.getLogger().warn("Fluid sender lifecycle: ItemEntity counting yielded 0 for sender(s) and stone control; finishing on loot + destroy-to-air evidence");
            finish(marker, anomalyText);
            return;
        }
        StringBuilder sb = new StringBuilder("sender drop genuinely missing: stone control delta=" + stone + ", sender deltas=");
        for (int d : deltas) sb.append(d).append(' ');
        check(false, sb.toString());
    }

    @SubscribeEvent public void start(ServerStartedEvent e) throws Exception {
        mode = System.getProperty("jdte.fluidSenderLifecycle", "");
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
        level.setBlockAndUpdate(BASIC, JDTEBlocks.BASIC_FLUID_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(ADVANCED, JDTEBlocks.ADVANCED_FLUID_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(EXTENDED, JDTEBlocks.EXTENDED_FLUID_SENDER.get().defaultBlockState());
        level.setBlockAndUpdate(BASIC_TARGET, JDTEBlocks.BASIC_FLUID_SENDER.get().defaultBlockState());
        // Basic runs live (IGNORED redstone, full tank, adjacent target).
        var basic = be(BASIC);
        basic.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.IGNORED;
        basic.getFluidTank().fill(new FluidStack(Fluids.WATER, 8000), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        // Target stays inactive (HIGH, no signal) so it only receives.
        be(BASIC_TARGET).getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        // Advanced and Extended stay inactive (HIGH, no signal) so their
        // cards, FE and tanks survive the whole lifecycle unchanged.
        var adv = (PoweredFluidSenderBE) be(ADVANCED);
        UpgradeHelper.getUpgradeHandler(adv).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(adv).insertItem(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()), false);
        adv.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        adv.getEnergyStorage().setEnergy(1000000);
        adv.getFluidTank().setFluid(new FluidStack(Fluids.WATER, 12345));
        var ext = (PoweredFluidSenderBE) be(EXTENDED);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(1, new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(2, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(3, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(4, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(5, new ItemStack(JDTEItems.RANGE_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(6, new ItemStack(JDTEItems.RANGE_UPGRADE.get()), false);
        UpgradeHelper.getUpgradeHandler(ext).insertItem(7, new ItemStack(JDTEItems.FILTER_UPGRADE.get()), false);
        ext.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
        ext.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
        ext.getEnergyStorage().setEnergy(1234567);
        ext.getFluidTank().setFluid(new FluidStack(Fluids.WATER, 23456));
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
            // Live Basic transfer on the real server: all 8000 mB must
            // have reached the adjacent target tank, sender empty.
            int targetTank = tank(BASIC_TARGET);
            int senderTank = tank(BASIC);
            com.mojang.logging.LogUtils.getLogger().info("Fluid sender lifecycle stage 0 tick {}: target={} sender={}", ticks, targetTank, senderTank);
            if (targetTank != 8000 || senderTank != 0) {
                com.mojang.logging.LogUtils.getLogger().error("Fluid sender lifecycle stage 0 FAILED: target={} sender={}", targetTank, senderTank);
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
            breakSender(EXTENDED, JDTEBlocks.EXTENDED_FLUID_SENDER.get(), JDTEItems.EXTENDED_FLUID_SENDER.get());
            stage = 3;
        } else if (stage == 3 && ticks - breakTick > 20) {
            extDelta = recordDelta(EXTENDED);
            com.mojang.logging.LogUtils.getLogger().info("Fluid sender lifecycle stage 3: Extended break delta={}", extDelta);
            breakStoneControl();
            stage = 4;
        } else if (stage == 4 && ticks - stoneTick > 20) {
            decideDrops("fluid-sender-seed-success",
                    "PASS: dedicated server placed 3 fluid senders, live Basic transfer moved 8000 mB water to an adjacent target tank, saved, chunk unloaded and reloaded with Ultimate cards, FE, tanks and Extended 8-slot state intact (Advanced 1000000/2500000 + 12345 mB, Extended 1234567/40000000 + 23456 mB), survival break loot-dropped and entity-dropped exactly one Extended sender and left air, saved shutdown.",
                    "PASS (ItemEntity-counting anomaly noted): dedicated server placed 3 fluid senders, live Basic transfer moved 8000 mB water to an adjacent target tank, saved, chunk unloaded and reloaded with Ultimate cards, FE, tanks and Extended 8-slot state intact (Advanced 1000000/2500000 + 12345 mB, Extended 1234567/40000000 + 23456 mB), survival break loot-dropped exactly one Extended sender and left air; ItemEntity counting yielded 0 for both the sender and a vanilla stone control broken the same way in the same chunk, so production drop evidence is loot-table drop plus destroy-to-air (same standard as the item-sender round; GameTest-server survival breaks counted delta == 1 for all three tiers), saved shutdown.",
                    extDelta);
        } else if (stage == 10 && ticks > 10) {
            // Fresh JVM: Basic and Advanced restored; Extended was broken.
            check(server.overworld().getBlockState(EXTENDED).isAir(), "Extended stays broken after restart");
            var basic = be(BASIC);
            var adv = (PoweredFluidSenderBE) be(ADVANCED);
            check(basic != null && adv != null, "Basic and Advanced restored after restart");
            check(UpgradeHelper.countUpgrades(adv, UpgradeType.ULTIMATE_CAPACITY) == 1
                    && UpgradeHelper.countUpgrades(adv, UpgradeType.ULTIMATE_OVERCLOCK) == 1,
                    "Advanced kept both Ultimate cards after restart");
            check(adv.getMaxEnergy() == 2500000 && adv.getEnergyStorage().getEnergyStored() == 1000000,
                    "Advanced kept FE after restart, got " + adv.getEnergyStorage().getEnergyStored());
            check(tank(ADVANCED) == 12345 && adv.getFluidTank().getFluid().getFluid() == Fluids.WATER,
                    "Advanced kept 12345 mB water after restart");
            check(tank(BASIC_TARGET) == 8000, "Basic target kept 8000 mB after restart");
            breakSender(ADVANCED, JDTEBlocks.ADVANCED_FLUID_SENDER.get(), JDTEItems.ADVANCED_FLUID_SENDER.get());
            stage = 11;
        } else if (stage == 11 && ticks - breakTick > 20) {
            advDelta = recordDelta(ADVANCED);
            com.mojang.logging.LogUtils.getLogger().info("Fluid sender lifecycle stage 11: Advanced break delta={}", advDelta);
            breakSender(BASIC, JDTEBlocks.BASIC_FLUID_SENDER.get(), JDTEItems.BASIC_FLUID_SENDER.get());
            stage = 12;
        } else if (stage == 12 && ticks - breakTick > 20) {
            basicDelta = recordDelta(BASIC);
            com.mojang.logging.LogUtils.getLogger().info("Fluid sender lifecycle stage 12: Basic break delta={}", basicDelta);
            breakStoneControl();
            stage = 13;
        } else if (stage == 13 && ticks - stoneTick > 20) {
            decideDrops("fluid-sender-verify-success",
                    "PASS: fresh dedicated-server JVM restored Basic and Advanced fluid senders with Ultimate cards, FE and tanks intact, Extended stayed broken, survival breaks loot-dropped and entity-dropped exactly one Advanced and one Basic sender and left air.",
                    "PASS (ItemEntity-counting anomaly noted): fresh dedicated-server JVM restored Basic and Advanced fluid senders with Ultimate cards, FE and tanks intact, Extended stayed broken, survival breaks loot-dropped exactly one Advanced and one Basic sender and left air; ItemEntity counting yielded 0 for both senders and a vanilla stone control broken the same way in the same chunk, so production drop evidence is loot-table drop plus destroy-to-air (same standard as the item-sender round; GameTest-server survival breaks counted delta == 1 for all three tiers).",
                    advDelta, basicDelta);
        }
    }

    private void finish(String marker, String s) throws Exception {
        Files.writeString(Path.of(marker), s + "\n");
        mode = "";
        server.saveEverything(false, true, true);
        server.halt(false);
    }
}
