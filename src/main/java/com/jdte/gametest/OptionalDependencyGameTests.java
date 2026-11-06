package com.jdte.gametest;
import com.jdte.common.upgrades.*;
import com.jdte.setup.JDTEItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class OptionalDependencyGameTests {
 @GameTest(template="mineral_empty") public static void transientMissingClientMachineHasNoUpgrades(GameTestHelper h){
  h.assertTrue(UpgradeHelper.getUpgradeHandler(null)==null,"missing client block entity has no handler");
  h.assertTrue(UpgradeHelper.countUpgrades(null,UpgradeType.CAPACITY)==0,"safe query while client chunk is arriving");h.succeed();
 }
}
