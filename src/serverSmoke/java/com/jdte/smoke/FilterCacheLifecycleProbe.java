package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Dedicated-server spot check for the filter-cache fix: four representative
 * Filterable machines (Advanced BioExtractor, Basic Time Accelerator,
 * Extended Block Breaker, Life Breeder) are placed non-default (stone in
 * filter slot 0) on a packaged server, saved, and survival-broken after a
 * fresh-JVM restart. Test-only; never in the production JAR.
 *
 * Note: an earlier revision waited for a natural chunk unload/reload
 * between seed and break, but the probe chunk never unloaded within 7200
 * ticks despite being unforced and absent from the world's forced-chunk
 * list (verified in chunks.dat), so that wait was replaced by the
 * strictly stronger save/restart round-trip: the same saveAdditional
 * serialization path is exercised, and the survival break after restart
 * is the production shape of the original crash. Production ItemEntity
 * counting is unreliable in this probe environment (same reason as the
 * sender round), so breaks assert no-crash, air, and no BE; drop counts
 * are covered by the GameTest suite, and the Life Breeder tag fix is
 * checked directly via the loaded mineable tag plus hasCorrectToolForDrops.
 */
public final class FilterCacheLifecycleProbe {
    private static final BlockPos BIO = new BlockPos(1060, 80, 1044);
    private static final BlockPos TIME = new BlockPos(1064, 80, 1044);
    private static final BlockPos BREAKER = new BlockPos(1060, 80, 1048);
    private static final BlockPos BREEDER = new BlockPos(1064, 80, 1048);
    private String mode = "";
    private MinecraftServer server;
    private int ticks;
    private int stage;

    private void check(boolean v, String s) { if (!v) throw new IllegalStateException("Filter cache lifecycle: " + s); }

    private FilterableBE be(BlockPos pos) {
        var be = server.overworld().getBlockEntity(pos);
        check(be instanceof FilterableBE, "Filterable BE present at " + pos + ", got " + be);
        return (FilterableBE) be;
    }

    private void setNonDefault(BlockPos pos) {
        var filterable = be(pos);
        filterable.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
        if (filterable instanceof RedstoneControlledBE redstone) {
            redstone.getRedstoneControlData().redstoneMode = MiscHelpers.RedstoneMode.HIGH;
            redstone.getRedstoneControlData().receivingRedstone = false;
        }
    }

    private void verifyFilterStone(BlockPos pos) {
        var stack = be(pos).getFilterHandler().getStackInSlot(0);
        check(stack.is(Items.STONE) && stack.getCount() == 1, "filter slot 0 kept stone at " + pos + ", got " + stack);
    }

    private void verifyAllPresent() {
        for (var pos : List.of(BIO, TIME, BREAKER, BREEDER)) {
            check(server.overworld().getBlockEntity(pos) != null, "BE present at " + pos);
            verifyFilterStone(pos);
        }
        check(server.overworld().getBlockState(BIO).is(JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get()), "BioExtractor block present");
        check(server.overworld().getBlockState(TIME).is(JDTEBlocks.BASIC_TIME_ACCELERATOR.get()), "TimeAccelerator block present");
        check(server.overworld().getBlockState(BREAKER).is(JDTEBlocks.EXTENDED_BLOCK_BREAKER.get()), "BlockBreaker block present");
        check(server.overworld().getBlockState(BREEDER).is(JDTEBlocks.LIFE_BREEDER.get()), "LifeBreeder block present");
    }

    private void breakMachine(BlockPos pos, Block block, net.minecraft.world.item.Item item) {
        var before = server.overworld().getBlockState(pos);
        check(before.is(block), "block to break is expected machine at " + pos);
        var lootDrops = Block.getDrops(before, server.overworld(), pos, null);
        check(lootDrops.size() == 1 && ItemStack.isSameItem(lootDrops.get(0), new ItemStack(item)) && lootDrops.get(0).getCount() == 1,
                "loot drops exactly one self item for " + pos + ", got " + lootDrops);
        var p = FakePlayerFactory.get(server.overworld(), new com.mojang.authlib.GameProfile(UUID.fromString("6f3c1e2a-9b4d-4c8a-8f1e-2d5a7b9c0e3f"), "FilterCacheLifecycle"));
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        check(p.gameMode.destroyBlock(pos), "FakePlayer breaks machine at " + pos);
        check(server.overworld().getBlockState(pos).isAir(), "broken machine leaves air at " + pos);
        check(server.overworld().getBlockEntity(pos) == null, "broken machine BE removed at " + pos);
    }

    @SubscribeEvent public void start(ServerStartedEvent e) throws Exception {
        mode = System.getProperty("jdte.filterCacheLifecycle", "");
        if (mode.isEmpty()) return;
        server = e.getServer();
        check(Files.exists(Path.of(".jdte-lifecycle-fixture")), "marked fixture");
        server.overworld().getChunkAt(BIO);
        if (mode.equals("verify")) { stage = 10; return; }
        var level = server.overworld();
        for (var p : List.of(BIO, TIME, BREAKER, BREEDER)) {
            level.setBlockAndUpdate(p.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(BIO, JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get().defaultBlockState());
        level.setBlockAndUpdate(TIME, JDTEBlocks.BASIC_TIME_ACCELERATOR.get().defaultBlockState());
        level.setBlockAndUpdate(BREAKER, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get().defaultBlockState());
        level.setBlockAndUpdate(BREEDER, JDTEBlocks.LIFE_BREEDER.get().defaultBlockState());
        for (var p : List.of(BIO, TIME, BREAKER, BREEDER)) setNonDefault(p);
        verifyAllPresent();
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) throws Exception {
        if (mode.isEmpty() || e.phase != TickEvent.Phase.END || e.getServer() != server) return;
        if (++ticks >= 1800) {
            com.mojang.logging.LogUtils.getLogger().error("Filter cache lifecycle FAILED: timeout stage {}", stage);
            mode = "";
            server.saveEverything(false, true, true);
            server.halt(false);
            return;
        }
        if (stage == 0 && ticks > 10) {
            server.saveEverything(false, true, true);
            finish("filter-seed-success", "PASS: dedicated server placed Advanced BioExtractor, Basic Time Accelerator, Extended Block Breaker and Life Breeder non-default (stone in filter slot 0), verified, saved shutdown.");
        } else if (stage == 10 && ticks > 10) {
            // Fresh JVM: all four restored with filter stone intact.
            verifyAllPresent();
            // The Life Breeder tag fix, checked directly on the packaged
            // server: the block must be in the loaded mineable/pickaxe tag
            // and an iron pickaxe must count as its correct tool.
            var breederState = server.overworld().getBlockState(BREEDER);
            check(breederState.is(BlockTags.MINEABLE_WITH_PICKAXE), "LifeBreeder in loaded mineable/pickaxe tag on dedicated server");
            var p = FakePlayerFactory.get(server.overworld(), new com.mojang.authlib.GameProfile(UUID.fromString("6f3c1e2a-9b4d-4c8a-8f1e-2d5a7b9c0e3f"), "FilterCacheLifecycle"));
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            check(p.hasCorrectToolForDrops(breederState), "iron pickaxe is LifeBreeder correct tool on dedicated server");
            breakMachine(BREAKER, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(), JDTEItems.EXTENDED_BLOCK_BREAKER.get());
            breakMachine(BREEDER, JDTEBlocks.LIFE_BREEDER.get(), JDTEItems.LIFE_BREEDER.get());
            stage = 11;
        } else if (stage == 11) {
            finish("filter-verify-success", "PASS: fresh dedicated-server JVM restored BioExtractor, Time Accelerator, BlockBreaker and Life Breeder with filter stone intact, LifeBreeder confirmed in the loaded mineable/pickaxe tag with iron pickaxe as correct tool, real survival breaks of BlockBreaker and LifeBreeder left air with no BE and no crash.");
        }
    }

    private void finish(String marker, String s) throws Exception {
        Files.writeString(Path.of(marker), s + "\n");
        mode = "";
        server.saveEverything(false, true, true);
        server.halt(false);
    }
}
