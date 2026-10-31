package com.jdte.common.blocks;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("jdte")
@PrefixGameTestTemplate(false)
public final class DecorativeFumoGameTests {
    @GameTest(template = "mineral_empty")
    public static void cainiaoyoShapesAndSelfDrop(GameTestHelper helper) {
        var block = JDTEBlocks.CAINIAOYO_FUMO.get();
        var pos = new BlockPos(2, 2, 2);
        for (var facing : Direction.Plane.HORIZONTAL) {
            var state = block.defaultBlockState().setValue(DecorativeFumoBlock.FACING, facing);
            helper.setBlock(pos, state);
            helper.assertTrue(helper.getBlockState(pos).getValue(DecorativeFumoBlock.FACING) == facing, "facing retained");
            helper.assertTrue(state.getShape(helper.getLevel(), helper.absolutePos(pos)).equals(DecorativeFumoBlock.shapeFor(facing)), "source shape for " + facing);
        }
        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null);
        helper.assertTrue(drops.size() == 1 && ItemStack.isSameItem(drops.get(0), new ItemStack(JDTEItems.CAINIAOYO_FUMO.get())) && drops.get(0).getCount() == 1, "actual self loot");
        helper.succeed();
    }
}
