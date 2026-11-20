package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * The extended T2 tier exists to make the configured area reach the real execution path; the source
 * T1 machines only ever look at the single block they face. These tests give each machine a 3x3 area
 * with several candidates and assert its own discovery method returns more than that one position.
 */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ExtendedTierAreaGameTests {
    private static final BlockPos POS = new BlockPos(4, 1, 4);

    private static FakePlayer fakePlayer(GameTestHelper h) {
        return new FakePlayer(h.getLevel(), new GameProfile(UUID.randomUUID(), "jdte-area-test"));
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> T place(
            GameTestHelper h, net.minecraft.world.level.block.Block block, Class<T> type) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, block);
        return type.cast(h.getBlockEntity(POS));
    }

    /** Three stones inside the radius: the single-block T1 path could only ever see one of them. */
    @GameTest(template = "mineral_empty")
    public static void extendedBreakerDiscoversEveryBlockInItsArea(GameTestHelper h) {
        ExtendedBlockBreakerBE breaker = place(h, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(), ExtendedBlockBreakerBE.class);
        breaker.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        BlockPos[] targets = {POS.north(), POS.north().east(), POS.north().west()};
        for (BlockPos target : targets) h.setBlock(target, Blocks.STONE);

        List<BlockPos> found = breaker.findBlocksToMine(fakePlayer(h));
        h.assertTrue(found.size() >= 2,
                "the breaker sees more than the single faced block, found=" + found.size() + " " + found);
        h.succeed();
    }

    /** Two air pockets above the radius: a placer holding one block must reach both. */
    @GameTest(template = "mineral_empty")
    public static void extendedPlacerDiscoversEveryAirSpotInItsArea(GameTestHelper h) {
        ExtendedBlockPlacerBE placer = place(h, JDTEBlocks.EXTENDED_BLOCK_PLACER.get(), ExtendedBlockPlacerBE.class);
        placer.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        placer.getMachineHandler().setStackInSlot(0, new ItemStack(Items.STONE, 8));
        for (BlockPos target : new BlockPos[]{POS.north(), POS.north().east(), POS.north().west()}) {
            h.setBlock(target, Blocks.AIR);
        }

        List<BlockPos> found = placer.findSpotsToPlace(fakePlayer(h));
        h.assertTrue(found.size() >= 2,
                "the placer sees more than the single faced spot, found=" + found.size() + " " + found);
        h.succeed();
    }

    /** Two water sources in the radius: the collector must offer both, not only the faced one. */
    @GameTest(template = "mineral_empty")
    public static void extendedCollectorDiscoversEveryFluidInItsArea(GameTestHelper h) {
        ExtendedFluidCollectorBE collector = place(h, JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(), ExtendedFluidCollectorBE.class);
        collector.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        for (BlockPos target : new BlockPos[]{POS.north(), POS.north().east(), POS.north().west()}) {
            h.setBlock(target, Blocks.WATER);
        }

        List<BlockPos> found = collector.findSpotsToCollect(fakePlayer(h));
        h.assertTrue(found.size() >= 2,
                "the collector sees more than the single faced source, found=" + found.size() + " " + found);
        h.assertTrue(h.getLevel().getFluidState(h.absolutePos(POS.north())).is(Fluids.WATER),
                "the fixture water is still in place");
        h.succeed();
    }

    /** A replacement block in the machine plus two candidates: the swapper must offer both. */
    @GameTest(template = "mineral_empty")
    public static void extendedSwapperDiscoversEveryCandidateInItsArea(GameTestHelper h) {
        ExtendedBlockSwapperBE swapper = place(h, JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(), ExtendedBlockSwapperBE.class);
        swapper.setAreaSettings(1, 1, 1, 0, 0, 0, false);
        // Discovery only needs the area and the filter; the swapper has no inventory slots to seed.
        for (BlockPos target : new BlockPos[]{POS.north(), POS.north().east(), POS.north().west()}) {
            h.setBlock(target, Blocks.STONE);
        }

        List<BlockPos> found = swapper.findSpotsToSwap();
        h.assertTrue(found.size() >= 2,
                "the swapper sees more than the single faced block, found=" + found.size() + " " + found);
        h.succeed();
    }
}
