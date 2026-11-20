package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Behavioural coverage for the time accelerator families: the machine must actually advance a real
 * block entity inside its configured area, not merely pass its own filter. The Creative card is
 * installed so the reference path runs without draining the small base tank, which keeps the
 * assertion about the target instead of about fluid accounting.
 */
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class TimeAcceleratorAreaGameTests {
    private static final BlockPos BASIC_POS = new BlockPos(1, 1, 2);
    private static final BlockPos EXTENDED_POS = new BlockPos(1, 1, 4);
    private static final BlockPos FURNACE = new BlockPos(3, 1, 2);
    private static final BlockPos FURNACE_2 = new BlockPos(3, 1, 4);

    private static TimeAcceleratorBE accelerator(GameTestHelper h, BlockPos pos, BlockPos target, boolean extended) {
        h.setBlock(pos, extended ? JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get() : JDTEBlocks.BASIC_TIME_ACCELERATOR.get());
        AABB area = new AABB(h.absolutePos(target));
        TimeAcceleratorBE be = extended
                ? new ExtendedTimeAcceleratorBE(h.absolutePos(pos), h.getBlockState(pos)) {
                    @Override public AABB getAABB(BlockPos ignored) { return area; }
                }
                : new BasicTimeAcceleratorBE(h.absolutePos(pos), h.getBlockState(pos)) {
                    @Override public AABB getAABB(BlockPos ignored) { return area; }
                };
        h.getLevel().setBlockEntity(be);
        be.getRedstoneControlData().redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()), false);
        return be;
    }

    private static FurnaceBlockEntity furnace(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos, Blocks.FURNACE);
        FurnaceBlockEntity furnace = (FurnaceBlockEntity) h.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Items.RAW_IRON, 3));
        furnace.setItem(1, new ItemStack(Items.COAL, 8));
        return furnace;
    }

    @GameTest(template = "mineral_empty")
    public static void basicAcceleratorAdvancesARealFurnaceInItsArea(GameTestHelper h) {
        FurnaceBlockEntity furnace = furnace(h, FURNACE);
        TimeAcceleratorBE be = accelerator(h, BASIC_POS, FURNACE, false);

        // A vanilla smelt needs 200 ticks; each reference pass costs one effective multiplier, so a
        // single ordinary server tick could never finish an item here.
        for (int pass = 0; pass < 10; pass++) {
            be.accelerateArea();
        }

        ItemStack output = furnace.getItem(2);
        h.assertTrue(output.is(Items.IRON_INGOT) && output.getCount() >= 1,
                "the accelerator smelted a real iron ingot in its area, output=" + output + " input=" + furnace.getItem(0));
        h.succeed();
    }

    @GameTest(template = "mineral_empty")
    public static void extendedTierFinishesRealWorkInASinglePass(GameTestHelper h) {
        FurnaceBlockEntity furnace = furnace(h, FURNACE_2);
        // The Creative card makes the tier report its overclock multiplier (1024 for Extended).
        TimeAcceleratorBE extended = accelerator(h, EXTENDED_POS, FURNACE_2, true);

        h.assertTrue(extended.getEffectiveMultiplier() > 200,
                "one extended pass alone exceeds a full vanilla smelt, multiplier=" + extended.getEffectiveMultiplier());

        extended.accelerateArea();

        ItemStack output = furnace.getItem(2);
        h.assertTrue(output.is(Items.IRON_INGOT) && output.getCount() >= 2,
                "one extended pass smelted several items, output=" + output + " input=" + furnace.getItem(0));
        h.succeed();
    }
}
