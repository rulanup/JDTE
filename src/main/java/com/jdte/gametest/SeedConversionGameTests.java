package com.jdte.gametest;

import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.List;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class SeedConversionGameTests {
    @GameTest(template="mineral_empty") public static void seedUpgradeHasOriginalLimitAndCompatibility(GameTestHelper h){
        var pos=new BlockPos(3,1,2);h.setBlock(pos,JDTEBlocks.GREENHOUSE.get());var greenhouse=(GreenhouseBE)h.getBlockEntity(pos);var card=new ItemStack(JDTEItems.SEED_CONVERSION_UPGRADE.get());var upgrades=UpgradeHelper.getUpgradeHandler(greenhouse);
        h.assertTrue(upgrades.insertItem(0,card,false).isEmpty()&&!upgrades.isItemValid(1,card),"one seed conversion card per greenhouse");
        var saved=greenhouse.saveWithFullMetadata();greenhouse.load(saved);h.assertTrue(UpgradeHelper.hasSeedConversionUpgrade(greenhouse),"card persists in machine NBT");
        h.setBlock(pos,JDTEBlocks.MINERAL_EXTRACTOR.get());h.assertTrue(!UpgradeHelper.getUpgradeHandler((MineralExtractorBE)h.getBlockEntity(pos)).isItemValid(0,card),"mineral machines reject greenhouse-only card");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void ordinaryPlantsKeepTheirSeedsAndOtherDrops(GameTestHelper h){
        var drops=List.of(new ItemStack(Items.WHEAT_SEEDS,3),new ItemStack(Items.WHEAT,2));
        var output=GreenhouseEssenceConversionHelper.replaceSeeds(drops,new ItemStack(Items.WHEAT_SEEDS),new ItemStack(Items.WHEAT));
        h.assertTrue(output.size()==2&&output.get(0).is(Items.WHEAT_SEEDS)&&output.get(0).getCount()==3&&output.get(1).getCount()==2,"non-essence primary outputs do not enable seed replacement");h.succeed();
    }
}
