package com.jdte.gametest;
import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.network.handler.AdvancedUpgradeStorageActionPacket;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.*;
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class AdvancedUpgradeStorageGameTests {
 @GameTest(template="mineral_empty") public static void advancedBagCapacityCapabilityAndLastSlotPersist(GameTestHelper h){
  var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var handler=bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(AssertionError::new);
  h.assertTrue(handler.getSlots()==36,"36 source slots");
  for(int i=0;i<36;i++) h.assertTrue(handler.insertItem(i,new ItemStack(com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get(),64),false).isEmpty(),"compressed unstackable upgrades");
  var copy=new AdvancedUpgradeStorageHandler(ItemStack.of(bag.save(new net.minecraft.nbt.CompoundTag())));
  h.assertTrue(copy.getStackInSlot(35).getCount()==64,"last slot persisted");
  h.assertTrue(!copy.isItemValid(1,bag),"recursive bags rejected");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void advancedBagMenuUses36SlotsAndProtectsBoundItem(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());p.setItemInHand(InteractionHand.MAIN_HAND,bag);
  var menu=new AdvancedUpgradeStorageContainer(1,p.getInventory(),p,bag,InteractionHand.MAIN_HAND);
  p.getInventory().setItem(9,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64));menu.clicked(36,0,ClickType.QUICK_MOVE,p);
  h.assertTrue(menu.handler.getStackInSlot(0).getCount()==64,"shift uses correct player boundary");
  menu.clicked(63,0,ClickType.PICKUP,p);h.assertTrue(menu.getCarried().isEmpty(),"held bag protected");
  menu.clicked(0,0,ClickType.QUICK_MOVE,p);h.assertTrue(p.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get())==64,"all upgrades returned");
  p.setItemInHand(InteractionHand.MAIN_HAND,bag.copy());h.assertTrue(!menu.stillValid(p),"stale menu closed");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void advancedBagMachineInsertionRespectsLimitsAndConserves(GameTestHelper h){
  h.setBlock(new BlockPos(3,1,2),JDTEBlocks.MINERAL_EXTRACTOR.get());var machine=(MineralExtractorBE)h.getBlockEntity(new BlockPos(3,1,2));
  var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var handler=new AdvancedUpgradeStorageHandler(bag);
  handler.insertItem(35,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64),false);
  int inserted=AdvancedUpgradeStorageActionPacket.insertIntoMachine(machine,handler,35,64);
  h.assertTrue(inserted==3,"machine capacity limit");h.assertTrue(handler.getStackInSlot(35).getCount()==61,"only installed cards consumed");
  h.assertTrue(UpgradeHelper.countUpgrades(machine,com.jdte.common.upgrades.UpgradeType.CAPACITY)==3,"cards installed");
  h.assertTrue(AdvancedUpgradeStorageActionPacket.insertIntoMachine(machine,handler,35,64)==0,"full machine leaves bag untouched");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void advancedBagDiscoveryDeduplicatesHandsAndInventory(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());p.setItemInHand(InteractionHand.MAIN_HAND,bag);
  p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get()));
  h.assertTrue(AdvancedUpgradeStorageItem.findAllStorages(p).size()==2,"main hand counted once");
  var tooltip=new java.util.ArrayList<net.minecraft.network.chat.Component>();bag.getItem().appendHoverText(bag,h.getLevel(),tooltip,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
  h.assertTrue(!tooltip.isEmpty(),"dedicated server tooltip");h.succeed();
 }

 @GameTest(template="mineral_empty") public static void advancedBagCuriosDiscoveryAndCopierWithdrawal(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var curios=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(AssertionError::new);
  var slot=new top.theillusivec4.curios.common.inventory.CurioStacksHandler(curios,"jdte_test_case",1,true,false,true,top.theillusivec4.curios.api.type.capability.ICurio.DropRule.DEFAULT);
  var configured=new java.util.LinkedHashMap<>(curios.getCurios());configured.put("jdte_test_case",slot);curios.setCurios(configured);
  var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var handler=new AdvancedUpgradeStorageHandler(bag);handler.insertItem(35,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),4),false);
  slot.getStacks().setStackInSlot(0,bag);
  h.assertTrue(AdvancedUpgradeStorageItem.findAllStorages(p).stream().anyMatch(st->st==bag),"worn case discovered");
  var transfer=com.jdte.common.items.machinesettings.MachineUpgradeTransfer.prepare(p,p.getInventory(),java.util.List.of(new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),3))).orElseThrow();transfer.consume();
  h.assertTrue(handler.getStackInSlot(35).getCount()==1,"copier can withdraw from worn case");h.succeed();
 }

 @GameTest(template="mineral_empty") public static void advancedBagServerActionsValidateWindowAndConserveCursor(GameTestHelper h){
  var pos=new BlockPos(3,1,2);h.setBlock(pos,JDTEBlocks.MINERAL_EXTRACTOR.get());var machine=(MineralExtractorBE)h.getBlockEntity(pos);
  var p=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());p.getInventory().clearContent();p.setPos(h.absolutePos(pos).getX()+0.5,h.absolutePos(pos).getY(),h.absolutePos(pos).getZ()+0.5);
  var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var handler=new AdvancedUpgradeStorageHandler(bag);p.getInventory().setItem(9,bag);
  var menu=new com.jdte.common.containers.MineralExtractorContainer(123,p.getInventory(),h.absolutePos(pos));p.containerMenu=menu;
  handler.insertItem(35,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64),false);
  AdvancedUpgradeStorageActionPacket.apply(new com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload(1,35,0,122),p);
  AdvancedUpgradeStorageActionPacket.apply(new com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload(1,35,999,123),p);
  h.assertTrue(handler.getStackInSlot(35).getCount()==64,"stale window and invalid storage index rejected");
  menu.setCarried(new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),4));
  for(int action:new int[]{5,4,2,4,3})AdvancedUpgradeStorageActionPacket.apply(new com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload(action,0,0,123),p);
  h.assertTrue(menu.getCarried().isEmpty()&&handler.getStackInSlot(0).isEmpty()&&p.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get())==4,"deposit one/all, cursor pickup and inventory withdrawal conserve cards");
  for(int action:new int[]{0,1,1})AdvancedUpgradeStorageActionPacket.apply(new com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload(action,35,0,123),p);
  h.assertTrue(handler.getStackInSlot(35).getCount()==61&&UpgradeHelper.countUpgrades(machine,com.jdte.common.upgrades.UpgradeType.CAPACITY)==3,"insert one/max respects cap");
  p.containerMenu=p.inventoryMenu;p.getInventory().clearContent();h.succeed();
 }
}
