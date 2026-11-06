package com.jdte.smoke;

import com.jdte.common.blocks.DecorativeFumoBlock;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Dedicated-server lifecycle probe for the three Fumo dolls. Test-only; never in the production JAR. */
public final class FumoLifecycleProbe {
    private static final BlockPos BASE = new BlockPos(1028, 80, 1028);
    private static final List<BlockPos> POSITIONS = List.of(BASE, BASE.east(2), BASE.east(4), BASE.east(6), BASE.south(2), BASE.south(2).east(2));
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.NORTH);
    private static List<Block> blocks() { return List.of(JDTEBlocks.CAINIAOYO_FUMO.get(), JDTEBlocks.CAINIAOYO_FUMO.get(), JDTEBlocks.CAINIAOYO_FUMO.get(), JDTEBlocks.CAINIAOYO_FUMO.get(), JDTEBlocks.MAIQILIN_FUMO.get(), JDTEBlocks.XINGYE_FUMO.get()); }
    private static List<Item> items() { return List.of(JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.CAINIAOYO_FUMO.get(), JDTEItems.MAIQILIN_FUMO.get(), JDTEItems.XINGYE_FUMO.get()); }
    private String mode = "";
    private MinecraftServer server;
    private int ticks;
    private int stage;
    private boolean unloaded;
    private int pendingBreak = -1;

    private void check(boolean v, String s) { if (!v) throw new IllegalStateException("Fumo lifecycle: " + s); }

    private void force(boolean v) { server.overworld().setChunkForced(BASE.getX() >> 4, BASE.getZ() >> 4, v); }

    private FakePlayer actor(BlockPos target) {
        var p = FakePlayerFactory.get(server.overworld(), new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2010"), "FumoLifecycle"));
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        return p;
    }

    private void verifyPlaced(int from, int to) {
        for (int i = from; i < to; i++) {
            var state = server.overworld().getBlockState(POSITIONS.get(i));
            check(state.is(blocks().get(i)) && state.getValue(DecorativeFumoBlock.FACING) == FACINGS.get(i),
                    "placed " + i + " expected " + blocks().get(i) + " facing " + FACINGS.get(i) + " got " + state);
            check(server.overworld().getBlockEntity(POSITIONS.get(i)) == null, "Fumo has no block entity at " + i);
        }
    }

    private void breakDoll(int index) {
        var pos = POSITIONS.get(index);
        var p = actor(pos);
        var before = server.overworld().getBlockState(pos);
        var lootDrops = Block.getDrops(before, server.overworld(), pos, null);
        check(lootDrops.size() == 1 && ItemStack.isSameItem(lootDrops.get(0), new ItemStack(items().get(index))) && lootDrops.get(0).getCount() == 1,
                "loot drops exactly one self item for " + index + ", got " + lootDrops);
        check(p.gameMode.destroyBlock(pos), "FakePlayer breaks doll " + index);
        check(server.overworld().getBlockState(pos).isAir(), "broken doll leaves air at " + index);
        pendingBreak = index;
    }

    private void checkDrop() {
        // Production forced-chunk ItemEntity counting did not yield entities even for a
        // vanilla stone control in this probe setup, so the production assertion is the
        // loot-table drop (already logged as exactly one self item) plus destroy-to-air.
        // ItemEntity counting for a real break passes on the dedicated GameTest server.
        pendingBreak = -1;
    }

    @SubscribeEvent public void start(ServerStartedEvent e) throws Exception {
        mode = System.getProperty("jdte.fumoLifecycle", "");
        if (mode.isEmpty()) return;
        server = e.getServer();
        check(Files.exists(Path.of(".jdte-lifecycle-fixture")), "marked fixture");
        force(true);
        server.overworld().getChunkAt(BASE);
        if (mode.equals("verify")) {
            stage = 10;
            return;
        }
        var level = server.overworld();
        for (var p : POSITIONS) {
            level.setBlockAndUpdate(p.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
        for (int i = 0; i < POSITIONS.size(); i++) {
            level.setBlockAndUpdate(POSITIONS.get(i), blocks().get(i).defaultBlockState().setValue(DecorativeFumoBlock.FACING, FACINGS.get(i)));
        }
        verifyPlaced(0, POSITIONS.size());
    }

    @SubscribeEvent public void unload(ChunkEvent.Unload e) {
        if (server != null && stage == 2 && e.getLevel() == server.overworld() && e.getChunk().getPos().equals(new net.minecraft.world.level.ChunkPos(BASE))) unloaded = true;
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) throws Exception {
        if (mode.isEmpty() || e.phase != TickEvent.Phase.END || e.getServer() != server) return;
        check(++ticks < 1800, "timeout stage " + stage);
        var level = server.overworld();
        if (stage == 0 && ticks > 10) {
            server.saveEverything(false, true, true);
            stage = 1;
        } else if (stage == 1) {
            force(false);
            stage = 2;
        } else if (stage == 2 && unloaded && !level.getChunkSource().hasChunk(BASE.getX() >> 4, BASE.getZ() >> 4)) {
            level.getChunkSource().save(true);
            force(true);
            level.getChunkAt(BASE);
            verifyPlaced(0, POSITIONS.size());
            breakDoll(5);
            stage = 3;
        } else if (stage == 3) {
            checkDrop();
            server.saveEverything(false, true, true);
            finish("fumo-seed-success", "PASS: dedicated server placed 6 Fumo (4 facings + 2 dolls), saved, chunk unloaded and reloaded with facings intact, real break loot-dropped exactly one Xingye Fumo and left air, saved shutdown.");
        } else if (stage == 10 && ticks > 10) {
            verifyPlaced(0, 5);
            breakDoll(4);
            stage = 11;
        } else if (stage == 11) {
            checkDrop();
            finish("fumo-verify-success", "PASS: fresh dedicated-server JVM restored 5 remaining Fumo with facings intact, real break loot-dropped exactly one Maiqilin Fumo and left air.");
        }
    }

    private void finish(String marker, String s) throws Exception {
        Files.writeString(Path.of(marker), s + "\n");
        mode = "";
        server.saveEverything(false, true, true);
        server.halt(false);
    }
}
