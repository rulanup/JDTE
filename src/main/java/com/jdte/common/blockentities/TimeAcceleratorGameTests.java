package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Behavioural checks for the ported block-state filter cache on the time accelerators. */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class TimeAcceleratorGameTests {
    private static final BlockPos SOURCE = new BlockPos(1, 1, 2), TARGET = new BlockPos(2, 1, 3);

    private static BasicTimeAcceleratorBE source(GameTestHelper h) {
        h.setBlock(SOURCE, JDTEBlocks.BASIC_TIME_ACCELERATOR.get());
        var be = new BasicTimeAcceleratorBE(h.absolutePos(SOURCE), h.getBlockState(SOURCE)) {
            @Override public AABB getAABB(BlockPos ignored) { return new AABB(h.absolutePos(TARGET)); }
        };
        h.getLevel().setBlockEntity(be);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
        be.fluidTank.setFluid(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(), 1000));
        return be;
    }

    @GameTest(template = "mineral_empty")
    public static void filterCacheKeepsResultsAndFollowsFilterChanges(GameTestHelper h) {
        var be = source(h);
        var level = h.getLevel();
        var pos = h.absolutePos(TARGET);
        var stone = Blocks.STONE.defaultBlockState();
        var dirt = Blocks.DIRT.defaultBlockState();

        h.assertTrue(!be.hasConfiguredFilter(), "an empty filter is not configured");
        h.assertTrue(be.isBlockValidFilter(level, pos, stone) && be.isBlockValidFilter(level, pos, dirt),
                "without a filter every block passes the default branch");

        be.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STONE));
        be.getFilterData().allowlist = true;
        h.assertTrue(be.hasConfiguredFilter(), "a stocked slot configures the filter");
        h.assertTrue(be.isBlockValidFilter(level, pos, stone), "allowlisted stone passes");
        h.assertTrue(!be.isBlockValidFilter(level, pos, dirt), "unlisted dirt is rejected");

        be.clearBlockFilterCache();
        h.assertTrue(be.isBlockValidFilter(level, pos, stone) && !be.isBlockValidFilter(level, pos, dirt),
                "clearing the cache does not change any result");

        be.getFilterData().allowlist = false;
        be.setFilterSettings(be.getFilterData());
        h.assertTrue(!be.isBlockValidFilter(level, pos, stone) && be.isBlockValidFilter(level, pos, dirt),
                "switching to a denylist inverts the outcome, so the cache was invalidated");

        var saved = be.saveWithFullMetadata();
        var restored = (BasicTimeAcceleratorBE) net.minecraft.world.level.block.entity.BlockEntity
                .loadStatic(be.getBlockPos(), be.getBlockState(), saved);
        h.getLevel().setBlockEntity(restored);
        h.assertTrue(!restored.isBlockValidFilter(level, pos, stone) && restored.isBlockValidFilter(level, pos, dirt),
                "a loaded accelerator starts with a cold cache and the same answers");
        h.succeed();
    }
}
