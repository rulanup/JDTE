package com.jdte.gametest;
import com.jdte.common.blockentities.*;
import com.jdte.common.items.*;
import com.jdte.common.containers.handlers.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class MachineSettingsCopierGameTests {
 private static final BlockPos A=new BlockPos(1,1,1), B=new BlockPos(3,1,2);
 private static MineralExtractorBE machine(GameTestHelper h,BlockPos pos){h.setBlock(pos,JDTEBlocks.MINERAL_EXTRACTOR.get());return (MineralExtractorBE)h.getBlockEntity(pos);}
 private static AdvancedMachineSettingsCopierItem copier(){return JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get();}
 private static net.minecraft.world.entity.player.Player player(GameTestHelper h,ItemStack stack){var p=h.makeMockSurvivalPlayer();p.setItemInHand(InteractionHand.MAIN_HAND,stack);return p;}
 private static InteractionResult paste(GameTestHelper h,net.minecraft.world.entity.player.Player p,BlockPos pos){return copier().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(pos)),Direction.UP,h.absolutePos(pos),false)));}
 @GameTest(template="mineral_empty") public static void emptyUpgradePresetRefundsExistingCards(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);target.setMultiplier(3);source.setMultiplier(17);
  UpgradeHelper.getUpgradeHandler(target).insertItem(0,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);
  h.assertTrue(paste(h,p,B)==InteractionResult.SUCCESS,"paste accepted");
  h.assertTrue(UpgradeHelper.countUpgrades(target,UpgradeType.CAPACITY)==0,"extra upgrade removed");
  h.assertTrue(p.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get())==1,"removed card refunded");
  h.assertTrue(target.getMultiplier()==17,"mineral configuration copied");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void copierConsumesBasicAndAdvancedBagCardsAtomically(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);
  for(int i=0;i<3;i++)UpgradeHelper.getUpgradeHandler(source).insertItem(i,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);
  var basic=new ItemStack(JDTEItems.UPGRADE_STORAGE.get());var advanced=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
  var bh=new UpgradeStorageHandler(basic);var ah=new AdvancedUpgradeStorageHandler(advanced);bh.insertItem(19,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);ah.insertItem(35,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64),false);
  p.getInventory().setItem(9,basic);p.setItemInHand(InteractionHand.OFF_HAND,advanced);
  h.assertTrue(paste(h,p,B)==InteractionResult.SUCCESS,"cards consumed from both bags");
  h.assertTrue(bh.getStackInSlot(19).isEmpty() && ah.getStackInSlot(35).getCount()==62,"exact withdrawal");
  h.assertTrue(UpgradeHelper.countUpgrades(target,UpgradeType.CAPACITY)==3,"all copied cards installed");
  h.assertTrue(paste(h,p,B)==InteractionResult.SUCCESS && ah.getStackInSlot(35).getCount()==62,"identical paste costs no extra cards");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void missingCardsLeaveMachineAndBagsUntouched(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);source.setMultiplier(17);
  for(int i=0;i<3;i++)UpgradeHelper.getUpgradeHandler(source).insertItem(i,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);
  var bag=new ItemStack(JDTEItems.UPGRADE_STORAGE.get());var handler=new UpgradeStorageHandler(bag);handler.insertItem(0,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),2),false);p.getInventory().setItem(9,bag);
  var before=target.saveWithFullMetadata();h.assertTrue(paste(h,p,B)==InteractionResult.PASS,"insufficient cards rejected");
  h.assertTrue(before.equals(target.saveWithFullMetadata()),"machine unchanged");h.assertTrue(handler.getStackInSlot(0).getCount()==2,"bag unchanged");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void copierDoesNotCopyInventoryEnergyFluidOrPendingWork(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);source.setMultiplier(17);
  source.getMachineHandler().setStackInSlot(1,new ItemStack(Items.DIAMOND,20));target.getMachineHandler().setStackInSlot(1,new ItemStack(Items.IRON_ORE,31));
  target.getEnergyStorage().setEnergy(123456);var saved=target.saveWithFullMetadata();saved.putLong("pendingBaseWork",13);target.load(saved);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);
  h.assertTrue(paste(h,p,B)==InteractionResult.SUCCESS,"configuration paste");
  h.assertTrue(target.getMachineHandler().getStackInSlot(1).is(Items.IRON_ORE)&&target.getMachineHandler().getStackInSlot(1).getCount()==31,"real inventory untouched");
  h.assertTrue(target.getEnergyStorage().getEnergyStored()==123456,"energy untouched");h.assertTrue(target.saveWithFullMetadata().getLong("pendingBaseWork")==13,"progress untouched");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void copierRejectsWrongTypeAndMalformedPreset(GameTestHelper h){
  var source=machine(h,A);h.setBlock(B,JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());var target=(MineralExtractorBE)h.getBlockEntity(B);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);
  h.assertTrue(paste(h,p,B)==InteractionResult.PASS,"normal and large types distinct");
  stack.getTag().getCompound("CopiedData").getCompound("jdteMachineSettings").putInt("schemaVersion",Integer.MAX_VALUE);
  h.assertTrue(paste(h,p,A)==InteractionResult.PASS,"unknown schema rejected");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void nativeBasicCopierStillCopiesFiltersAndRedstone(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);source.getFilterHandler().setStackInSlot(0,new ItemStack(Items.IRON_ORE));
  source.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
  var stack=new ItemStack(com.direwolf20.justdirethings.setup.Registration.MachineSettingsCopier.get());
  com.direwolf20.justdirethings.common.items.MachineSettingsCopier.setSettings(stack,true,true,true,true);
  var item=(com.direwolf20.justdirethings.common.items.MachineSettingsCopier)stack.getItem();item.saveSettings(source,stack);item.loadSettings(target,stack);
  h.assertTrue(target.getFilterHandler().getStackInSlot(0).is(Items.IRON_ORE),"native basic filter copied");
  h.assertTrue(target.getRedstoneControlData().redstoneMode==source.getRedstoneControlData().redstoneMode,"native basic redstone copied");h.succeed();
 }

 @GameTest(template="mineral_empty") public static void compressedNativeUpgradeWithdrawalAndRollbackConserve(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var handler=new AdvancedUpgradeStorageHandler(bag);
  var item=com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get();handler.insertItem(35,new ItemStack(item,64),false);p.getInventory().setItem(9,bag);
  var transfer=com.jdte.common.items.machinesettings.MachineUpgradeTransfer.prepare(p,p.getInventory(),java.util.List.of(new ItemStack(item,2))).orElseThrow();transfer.consume();
  h.assertTrue(handler.getStackInSlot(35).getCount()==62,"bulk withdrawal of normally unstackable cards");transfer.rollback();h.assertTrue(handler.getStackInSlot(35).getCount()==64,"rollback restores exact compressed stack");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void fullPlayerInventoryDropsRefundedUpgradeWithoutVoiding(GameTestHelper h){
  var source=machine(h,A);var target=machine(h,B);UpgradeHelper.getUpgradeHandler(target).insertItem(0,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
  var stack=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());copier().saveSettings(source,stack);var p=player(h,stack);p.setPos(h.absolutePos(B).getX()+0.5,h.absolutePos(B).getY(),h.absolutePos(B).getZ()+0.5);
  for(int i=1;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
  var bounds=new AABB(p.position(),p.position()).inflate(2);
  var prior=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).stream().map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
  h.assertTrue(paste(h,p,B)==InteractionResult.SUCCESS,"empty preset accepted");
  long returned=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds,e->!prior.contains(e.getUUID())&&e.getItem().is(JDTEItems.CAPACITY_UPGRADE.get())).stream().mapToLong(e->e.getItem().getCount()).sum();
  h.assertTrue(returned==1&&UpgradeHelper.countUpgrades(target,UpgradeType.CAPACITY)==0,"exactly one card refunded into world");h.succeed();
 }
}
