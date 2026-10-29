package com.jdte.common.blockentities;

import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class BioExtractorGameTests {
 private static final BlockPos POS=new BlockPos(2,1,2),TARGET=new BlockPos(4,1,2);
 private static BioExtractorBE place(GameTestHelper h,boolean extended){
  h.setBlock(POS,Blocks.AIR);var block=extended?JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get():JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get();h.setBlock(POS,block);var abs=h.absolutePos(POS);var state=h.getLevel().getBlockState(abs);
  BioExtractorBE be=extended?new ExtendedBioExtractorBE(abs,state){@Override public AABB getAABB(BlockPos p){return new AABB(h.absolutePos(TARGET)).inflate(.5);}}:new AdvancedBioExtractorBE(abs,state){@Override public AABB getAABB(BlockPos p){return new AABB(h.absolutePos(TARGET)).inflate(.5);}};
  h.getLevel().setBlockEntity(be);be.getEnergyStorage().setEnergy(be.getMaxEnergy());return be;
 }
 private static <T extends Mob> T animal(GameTestHelper h,EntityType<T> type){T mob=h.spawn(type,TARGET);mob.setNoAi(true);mob.setPersistenceRequired();return mob;}
 private static void card(BioExtractorBE be,net.minecraft.world.item.Item item,int slot){if(!UpgradeHelper.getUpgradeHandler(be).insertItem(slot,new ItemStack(item),false).isEmpty())throw new IllegalStateException("Rejected fixture card "+item);}
 private static int wool(BioExtractorBE be,Item type){int n=0;for(int i=0;i<be.getOutputSlotCount();i++)if(be.getMachineHandler().getStackInSlot(i).is(type))n+=be.getMachineHandler().getStackInSlot(i).getCount();return n;}
 private static FluidStack milk(int n){return new FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(),n);}
 @GameTest(template="mineral_empty") public static void nineSourceRecipesAndNetworkRoundTripPreserveValues(GameTestHelper h){
  var all=h.getLevel().getRecipeManager().getAllRecipesFor(JDTERecipes.BIO_EXTRACTOR_RECIPE_TYPE.get());h.assertTrue(all.size()==9,"all nine actual processing JSONs loaded");var serializer=JDTERecipes.BIO_EXTRACTOR_RECIPE_SERIALIZER.get();
  for(var r:all){var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{serializer.toNetwork(buf,r);var copy=serializer.fromNetwork(r.id(),buf);h.assertTrue(copy.entityType().equals(r.entityType())&&copy.energy()==r.energy()&&copy.cooldown()==r.cooldown()&&copy.outputItems().size()==r.outputItems().size(),"wire preserves source recipe values");if(r.outputFluid().isPresent())h.assertTrue(copy.outputFluid().orElseThrow().isFluidStackIdentical(milk(1000)),"source Minecraft milk maps to real Forge milk");}finally{buf.release();}}
  h.succeed();
 }
 @GameTest(template="mineral_empty") public static void cowHarvestIsNonlethalAndCooldownIsShared(GameTestHelper h){
  var be=place(h,false);var cow=animal(h,EntityType.COW);float health=cow.getHealth();int before=be.getEnergyStorage().getEnergyStored();int done=be.executeOneExtractCycle(h.getLevel());
  h.assertTrue(done==1&&be.getFluidTank().getFluid().isFluidStackIdentical(milk(1000))&&be.getEnergyStorage().getEnergyStored()==before-5000&&cow.getHealth()==health,"real cow milk costs source 5000 FE without harming cow");long end=cow.getPersistentData().getLong(BioExtractorBE.COOLDOWN_KEY);h.assertTrue(end==h.getLevel().getGameTime()+600,"source 600 tick entity cooldown");
  var other=place(h,true);h.assertTrue(other.executeOneExtractCycle(h.getLevel())==0&&other.getFluidTank().isEmpty()&&other.getEnergyStorage().getEnergyStored()==other.getMaxEnergy(),"second tier cannot bypass shared cooldown");cow.discard();h.succeed();
 }
 @GameTest(template="mineral_empty") public static void coloredSheepOutputBypassesOnlyExternalInsertionGuard(GameTestHelper h){
  var be=place(h,false);var sheep=animal(h,EntityType.SHEEP);sheep.setColor(DyeColor.BLUE);int before=be.getEnergyStorage().getEnergyStored();
  h.assertTrue(be.executeOneExtractCycle(h.getLevel())==1&&wool(be,Items.BLUE_WOOL)==2&&be.getEnergyStorage().getEnergyStored()==before-3000&&sheep.isSheared(),"actual output-only handler receives two colored wool and exact FE debit");
  sheep.getPersistentData().remove(BioExtractorBE.COOLDOWN_KEY);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&wool(be,Items.BLUE_WOOL)==2,"sheared sheep cannot produce again without regrowing");
  var port=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);h.assertTrue(!port.isItemValid(0,new ItemStack(Items.BLUE_WOOL))&&port.insertItem(0,new ItemStack(Items.BLUE_WOOL),false).getCount()==1&&port.extractItem(0,2,false).getCount()==2,"external insertion rejected and extraction works");sheep.discard();h.succeed();
 }
 @GameTest(template="mineral_empty") public static void fullOutputsAndWrongFluidDoNotChargeOrSetCooldown(GameTestHelper h){
  var be=place(h,false);var sheep=animal(h,EntityType.SHEEP);for(int i=0;i<9;i++)be.getMachineHandler().setStackInSlot(i,new ItemStack(Items.STONE,64));int before=be.getEnergyStorage().getEnergyStored();
  h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&!sheep.isSheared()&&!sheep.getPersistentData().contains(BioExtractorBE.COOLDOWN_KEY)&&be.getEnergyStorage().getEnergyStored()==before,"full inventory preserves FE/sheep/cooldown");sheep.discard();var cow=animal(h,EntityType.COW);
  for(var fluid:List.of(milk(be.getMaxMB()),new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000))){be.getFluidTank().setFluid(fluid);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&be.getFluidTank().getFluid().isFluidStackIdentical(fluid)&&be.getEnergyStorage().getEnergyStored()==before&&!cow.getPersistentData().contains(BioExtractorBE.COOLDOWN_KEY),"full or incompatible tank does not spend FE or cooldown");}
  cow.discard();h.succeed();
 }
 @GameTest(template="mineral_empty") public static void insufficientEnergyAndShortDebitRollbackWithoutProducts(GameTestHelper h){
  var be=place(h,false);var cow=animal(h,EntityType.COW);be.getEnergyStorage().setEnergy(4999);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&be.getEnergyStorage().getEnergyStored()==4999&&be.getFluidTank().isEmpty(),"insufficient FE does not buy partial milk");
  be.getEnergyStorage().setEnergy(9000);((com.jdte.mixin.EnergyStorageAccessor)be.getEnergyStorage()).jdte$setMaxExtract(1);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&be.getEnergyStorage().getEnergyStored()==9000&&be.getFluidTank().isEmpty()&&!cow.getPersistentData().contains(BioExtractorBE.COOLDOWN_KEY),"unexpected short commit rolls FE back before fluid/entity effects");cow.discard();h.succeed();
 }
 @GameTest(template="mineral_empty") public static void sourceTierAndOverclockTargetLimitsAreEnforced(GameTestHelper h){
  for(boolean extended:List.of(false,true))for(boolean overclock:List.of(false,true)){var be=place(h,extended);if(overclock)card(be,JDTEItems.OVERCLOCK_UPGRADE.get(),0);var sheep=new ArrayList<Sheep>();for(int i=0;i<9;i++)sheep.add(animal(h,EntityType.SHEEP));int count=extended?(overclock?8:4):(overclock?2:1);int cost=UpgradeHelper.adjustEnergyCost(be,3000);
   h.assertTrue(be.executeOneExtractCycle(h.getLevel())==count&&sheep.stream().filter(Sheep::isSheared).count()==count&&wool(be,Items.WHITE_WOOL)==count*2&&be.getEnergyStorage().getEnergyStored()==be.getMaxEnergy()-cost*count,"actual source target cap and output/FE "+extended+"/"+overclock);sheep.forEach(Entity::discard);
  }h.succeed();
 }
 @GameTest(template="mineral_empty") public static void sourceIntervalsUltimateTickerAndAcceleratedProgressWork(GameTestHelper h){
  var be=place(h,false);h.assertTrue(be.getExtractInterval()==20,"normal interval");card(be,JDTEItems.UNDERCLOCK_UPGRADE.get(),0);h.assertTrue(be.getExtractInterval()==40&&be.getEffectiveEnergyCost(3000)==600,"underclock interval and cost");UpgradeHelper.getUpgradeHandler(be).extractItem(0,1,false);card(be,JDTEItems.OVERCLOCK_UPGRADE.get(),0);h.assertTrue(be.getExtractInterval()==5,"normal overclock scan interval");UpgradeHelper.getUpgradeHandler(be).extractItem(0,1,false);
  card(be,JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(),0);card(be,JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),1);h.assertTrue(be.getExtractInterval()==20&&be.getEffectiveEnergyCost(3000)==150000&&be.getMaxEnergy()==5000000&&be.getFluidTank().getCapacity()==800000,"source Ultimate rules and actual capacities");be.tickCounter=0;
  @SuppressWarnings("unchecked") var ticker=(net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)be.getBlockState().getTicker(h.getLevel(),be.getType());ticker.tick(h.getLevel(),be.getBlockPos(),be.getBlockState(),be);h.assertTrue(be.tickCounter==10,"actual block ticker runs ten source counter executions");be.accumulateAcceleratedTicks(7);be.flushAcceleratedTicks();h.assertTrue(be.tickCounter==17,"Time Accelerator coalesced ticks reach real scan progress");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void spawnEggFiltersExcludeWrongTargetsAndPlayers(GameTestHelper h){
  var be=place(h,false);card(be,JDTEItems.FILTER_UPGRADE.get(),0);be.getFilterData().allowlist=true;be.getFilterHandler().setStackInSlot(0,new ItemStack(Items.COW_SPAWN_EGG));var sheep=animal(h,EntityType.SHEEP);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==0&&!sheep.isSheared(),"allowlist refuses wrong species");var cow=animal(h,EntityType.COW);h.assertTrue(be.executeOneExtractCycle(h.getLevel())==1&&be.getFluidTank().getFluidAmount()==1000,"real Forge spawn-egg filter accepts cow");h.assertTrue(!be.isValidTarget(h.makeMockSurvivalPlayer(),true),"players are always excluded");cow.discard();sheep.discard();h.succeed();
 }
 @GameTest(template="mineral_empty") public static void realCapabilitySidesAndRetainedEndpointsRespectLifecycle(GameTestHelper h){
  var be=place(h,true);be.getMachineHandler().setStackInSlot(17,new ItemStack(Items.EGG,31));be.getFluidTank().setFluid(milk(10000));
  for(Direction side:Direction.values())h.assertTrue(be.getCapability(ForgeCapabilities.ITEM_HANDLER,side).isPresent()&&be.getCapability(ForgeCapabilities.FLUID_HANDLER,side).isPresent()&&be.getCapability(ForgeCapabilities.ENERGY,side).isPresent(),"all source capability directions exposed");
  var items=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);var energy=be.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);var fluid=be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);be.onChunkUnloaded();h.assertTrue(items.extractItem(17,31,false).isEmpty()&&energy.extractEnergy(50,false)==0&&fluid.drain(10,FluidAction.EXECUTE).isEmpty(),"unloaded retained endpoints inert");be.reviveCaps();be.onLoad();be.tickCounter=7;be.accumulateAcceleratedTicks(11);var data=be.saveWithFullMetadata();var restored=(BioExtractorBE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),data);h.getLevel().setBlockEntity(restored);be.clearRemoved();
  h.assertTrue(items.extractItem(17,31,false).isEmpty()&&energy.extractEnergy(50,false)==0&&fluid.drain(10,FluidAction.EXECUTE).isEmpty(),"detached owner cannot access new ownership");h.assertTrue(restored.getMachineHandler().getStackInSlot(17).getCount()==31&&restored.tickCounter==7&&restored.accumulatedAcceleratedTicks==11&&restored.getFluidTank().getFluidAmount()==10000,"fresh registered BE retains output18, fluid and both progress counters");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void capacityCardRemovalCannotTruncateBioBuffers(GameTestHelper h){
  var be=place(h,false);card(be,JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),0);be.getEnergyStorage().setEnergy(be.getMaxEnergy());be.getFluidTank().setFluid(milk(20000));var u=UpgradeHelper.getUpgradeHandler(be);h.assertTrue(u.extractItem(0,1,false).isEmpty(),"stored FE blocks unsafe downgrade");be.getEnergyStorage().setEnergy(0);h.assertTrue(u.extractItem(0,1,false).isEmpty()&&be.getFluidTank().getFluidAmount()==20000,"fluid independently blocks unsafe downgrade");be.getFluidTank().setFluid(milk(1000));h.assertTrue(u.extractItem(0,1,false).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())&&be.getFluidTank().getFluidAmount()==1000,"safe removal retains fluid");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void automaticOutputReachesRealAdjacentInventory(GameTestHelper h){
  var be=place(h,false);be.getMachineHandler().setStackInSlot(0,new ItemStack(Items.BLUE_WOOL,12));var neighbor=be.getBlockPos().east();h.getLevel().setBlockAndUpdate(neighbor,Blocks.CHEST.defaultBlockState());JDTEAttachments.autoIo(be).setMasks(0,1<<3);com.jdte.common.autoioconfig.AutoIoTransferHelper.tick(be);
  var chest=h.getLevel().getBlockEntity(neighbor).getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.WEST).orElseThrow(IllegalStateException::new);int total=0;for(int i=0;i<chest.getSlots();i++)if(chest.getStackInSlot(i).is(Items.BLUE_WOOL))total+=chest.getStackInSlot(i).getCount();h.assertTrue(total==12&&wool(be,Items.BLUE_WOOL)==0,"configured actual east output moves twelve items without insertion into output-only machine");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void actualMenuShiftExtractionUsesForgeVisibleInvoker(GameTestHelper h){
  var be=place(h,false);be.getMachineHandler().setStackInSlot(8,new ItemStack(Items.EGG,31));var player=h.makeMockSurvivalPlayer();player.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+1,be.getBlockPos().getZ()+.5);player.getInventory().setItem(9,new ItemStack(Items.STONE,64));var menu=new com.jdte.common.containers.AdvancedBioExtractorContainer(1,player.getInventory(),be.getBlockPos());
  var slot=menu.slots.stream().filter(v->v.getItem().is(Items.EGG)).findFirst().orElseThrow();menu.quickMoveStack(player,slot.index);h.assertTrue(player.getInventory().countItem(Items.EGG)==31&&be.getMachineHandler().getStackInSlot(8).isEmpty(),"real menu Shift takes entire output without private native-method access");var input=menu.slots.stream().filter(v->v.getItem().is(Items.STONE)).findFirst().orElseThrow();menu.quickMoveStack(player,input.index);h.assertTrue(player.getInventory().countItem(Items.STONE)==64,"Shift cannot insert ordinary items into output inventory");h.succeed();
 }
}
