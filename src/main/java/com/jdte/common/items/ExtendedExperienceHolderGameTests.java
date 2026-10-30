package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.ExperienceHolderBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload;
import com.jdte.common.network.handler.ExtendedExperienceHolderSettingsPacket;
import com.jdte.common.upgrades.*;
import com.jdte.mixin.ExperienceOrbAccessor;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ExtendedExperienceHolderGameTests {
 private static final BlockPos P=new BlockPos(2,1,2);
 private static ExtendedExperienceHolderBE place(GameTestHelper h){h.setBlock(P,JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());return (ExtendedExperienceHolderBE)h.getBlockEntity(P);}
 private static Player player(GameTestHelper h,ExtendedExperienceHolderBE b){var p=h.makeMockSurvivalPlayer();p.setPos(Vec3.atCenterOf(b.getBlockPos().south()));b.setPlacedBy(p.getUUID());return p;}
 private static UseOnContext context(Player p,BlockPos pos,ItemStack stack){p.setItemInHand(InteractionHand.MAIN_HAND,stack);return new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
 @GameTest(template="mineral_empty") public static void actualFluidViewWholeXpInvalidInputsAndMaximum(GameTestHelper h){
  var b=place(h);var cap=b.getXpFluidHandler();var xp=new FluidStack(Registration.XP_FLUID_SOURCE.get(),39);
  h.assertTrue(cap.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),FluidAction.EXECUTE)==0,"non-XP fluid rejected");
  h.assertTrue(cap.fill(xp,FluidAction.SIMULATE)==20&&b.exp==0&&cap.fill(xp,FluidAction.EXECUTE)==20&&b.exp==1&&xp.getAmount()==39,"only complete XP accepted and caller input/simulation unchanged");
  h.assertTrue(cap.drain(19,FluidAction.EXECUTE).isEmpty()&&cap.drain(39,FluidAction.SIMULATE).getAmount()==20&&b.exp==1,"sub-XP drains rejected, simulation pure");
  h.assertTrue(cap.drain(39,FluidAction.EXECUTE).getAmount()==20&&b.exp==0,"exact actual XP drain");
  b.exp=Integer.MAX_VALUE;h.assertTrue(cap.getFluidInTank(0).getAmount()==Integer.MAX_VALUE&&cap.fill(xp,FluidAction.EXECUTE)==0,"source XP range beyond int mB safely exposed");
  h.assertTrue(cap.drain(Integer.MAX_VALUE,FluidAction.EXECUTE).getAmount()==2147483640&&b.exp==2040109465,"int maximum drain cannot overflow or create negative XP");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void storeExtractLevelButtonsConserveExactPlayerXp(GameTestHelper h){
  var b=place(h);var p=player(h,b);p.giveExperiencePoints(400);int initial=ExperienceUtils.getPlayerTotalExperience(p);
  b.storeExp(p,1);h.assertTrue(p.experienceProgress==0&&b.exp+ExperienceUtils.getPlayerTotalExperience(p)==initial,"store partial current level without dropping rounding XP");
  b.storeExp(p,10);h.assertTrue(b.exp+ExperienceUtils.getPlayerTotalExperience(p)==initial,"ten levels conserve total");b.storeExp(p,-1);
  h.assertTrue(ExperienceUtils.getPlayerTotalExperience(p)==0&&b.exp==initial,"all store empties only accepted player XP");
  b.extractExp(p,1);h.assertTrue(p.experienceLevel==1&&b.exp==initial-7,"one whole level costs seven XP at zero");b.extractExp(p,10);
  h.assertTrue(p.experienceLevel==11&&b.exp+ExperienceUtils.getPlayerTotalExperience(p)==initial,"ten-level extract uses vanilla curve");b.extractExp(p,-1);
  h.assertTrue(b.exp==0&&ExperienceUtils.getPlayerTotalExperience(p)==initial,"all extract exact restoration");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void fullStorageReturnsRemainderAndOwnerSettingsAreAuthoritative(GameTestHelper h){
  var b=place(h);var owner=player(h,b);var visitor=h.makeMockSurvivalPlayer();visitor.setPos(owner.position());owner.giveExperiencePoints(100);b.exp=Integer.MAX_VALUE-3;b.storeExp(owner,-1);
  h.assertTrue(b.exp==Integer.MAX_VALUE&&ExperienceUtils.getPlayerTotalExperience(owner)==97,"only three XP removed with almost full holder");
  b.ownerOnly=true;visitor.giveExperiencePoints(50);int before=b.exp;b.storeExp(visitor,-1);b.extractExp(visitor,-1);b.changeSettings(visitor,5,false,true,false);
  h.assertTrue(b.exp==before&&visitor.totalExperience==50&&b.ownerOnly&&!b.collectExp&&b.targetExp==0,"non-owner cannot mutate XP or protected settings");
  b.changeSettings(owner,1001,false,true,false);h.assertTrue(b.ownerOnly,"invalid target rejected before any settings mutation");
  h.assertTrue(b.addExp(-2)==0&&b.subExp(-2)==0&&b.exp==before,"negative internal XP cannot mutate state");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void actualMergedExperienceOrbsAndOverflowRemainConserved(GameTestHelper h){
  var b=place(h);b.collectExp=true;b.showParticles=false;b.setTickSpeed(1);var data=b.getAreaAffectingData();data.yOffset=0;data.zOffset=0;data.xRadius=2;data.yRadius=2;data.zRadius=2;
  var orb=new ExperienceOrb(h.getLevel(),b.getBlockPos().getX()+0.5,b.getBlockPos().getY()+0.5,b.getBlockPos().getZ()+0.5,7);((ExperienceOrbAccessor)orb).jdte$setCount(3);h.getLevel().addFreshEntity(orb);
  b.exp=Integer.MAX_VALUE-10;b.tickServer();h.assertTrue(b.exp==Integer.MAX_VALUE-3&&orb.isAlive()&&((ExperienceOrbAccessor)orb).jdte$getCount()==2,"consume one merged multiplicity, leave fourteen XP in world");
  b.exp=0;b.tickServer();h.assertTrue(b.exp==14&&orb.isRemoved(),"remaining actual merged orb contributes both copies once");b.tickServer();h.assertTrue(b.exp==14,"removed orb cannot be processed twice");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void staleFluidInterfacesReloadAndOffThreadAreInert(GameTestHelper h){
  var b=place(h);b.exp=500;var old=b.getXpFluidHandler();b.invalidateCaps();b.reviveCaps();h.assertTrue(old.drain(1000,FluidAction.EXECUTE).isEmpty(),"retained invalid capability cannot spend XP after revival");
  var current=b.getXpFluidHandler();var off=java.util.concurrent.CompletableFuture.supplyAsync(()->current.drain(1000,FluidAction.EXECUTE)).join();h.assertTrue(off.isEmpty()&&b.exp==500,"off-thread access rejected");
  var saved=b.saveWithFullMetadata();var restored=(ExtendedExperienceHolderBE)BlockEntity.loadStatic(b.getBlockPos(),b.getBlockState(),saved);h.getLevel().setBlockEntity(restored);
  h.assertTrue(current.drain(1000,FluidAction.EXECUTE).isEmpty()&&restored.getXpFluidHandler().drain(1000,FluidAction.EXECUTE).getAmount()==1000&&restored.exp==450,"fresh save restoration has exclusive fluid ownership");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void menuPacketsRejectWrongPositionDistanceMalformedAndStaleTarget(GameTestHelper h){
  var b=place(h);var p=player(h,b);var menu=new ExtendedExperienceHolderContainer(3,p.getInventory(),b.getBlockPos());p.containerMenu=menu;p.giveExperiencePoints(100);
  h.assertTrue(menu.slots.size()==44&&menu.clickMenuButton(p,2)&&b.exp==100,"eight plus 36 slots and real deposit action");
  h.assertTrue(!ExtendedExperienceHolderSettingsPacket.apply(p,new ExtendedExperienceHolderSettingsPayload(b.getBlockPos().east(),5)),"wrong-position packet rejected");
  h.assertTrue(!ExtendedExperienceHolderSettingsPacket.apply(p,new ExtendedExperienceHolderSettingsPayload(b.getBlockPos(),Integer.MAX_VALUE)),"unbounded level packet rejected");
  h.assertTrue(ExtendedExperienceHolderSettingsPacket.apply(p,new ExtendedExperienceHolderSettingsPayload(b.getBlockPos(),12))&&b.targetExp==12,"valid current-menu packet accepted");
  p.setPos(p.position().add(20,0,0));h.assertTrue(!menu.clickMenuButton(p,5)&&b.exp==100,"distant stale view cannot withdraw");p.setPos(Vec3.atCenterOf(b.getBlockPos()));h.getLevel().removeBlockEntity(b.getBlockPos());
  h.assertTrue(!menu.clickMenuButton(p,5)&&!ExtendedExperienceHolderSettingsPacket.apply(p,new ExtendedExperienceHolderSettingsPayload(b.getBlockPos(),1)),"removed target rejects button/settings");h.setBlock(P,Blocks.AIR);h.succeed();
 }
 @GameTest(template="mineral_empty") public static void nativeConversionMovesXpSettingsAndFourCardsToEightSlots(GameTestHelper h){
  h.setBlock(P,Registration.ExperienceHolder.get());var source=(ExperienceHolderBE)h.getBlockEntity(P);source.exp=1234567890;source.targetExp=37;source.collectExp=true;source.ownerOnly=true;source.showParticles=false;
  var p=h.makeMockSurvivalPlayer();source.setPlacedBy(p.getUUID());p.setPos(Vec3.atCenterOf(source.getBlockPos()));var old=source.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);var cards=UpgradeHelper.getUpgradeHandler(source);
  cards.insertItem(0,new ItemStack(JDTEItems.RANGE_UPGRADE.get()),false);cards.insertItem(1,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);cards.insertItem(2,new ItemStack(JDTEItems.FLUID_UPGRADE.get()),false);cards.insertItem(3,new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get()),false);
  var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(context(p,source.getBlockPos(),card))==InteractionResult.SUCCESS&&card.isEmpty(),"final pinned source upgrade mapping is real");
  var target=(ExtendedExperienceHolderBE)h.getBlockEntity(P);h.assertTrue(target.exp==1234567890&&target.targetExp==37&&target.collectExp&&target.ownerOnly&&!target.showParticles&&p.getUUID().equals(target.placedByUUID),"complete XP/settings/owner retained");
  h.assertTrue(UpgradeHelper.getUpgradeHandler(target).getSlots()==8&&UpgradeHelper.getUpgradeHandler(target).getStackInSlot(3).is(JDTEItems.AE_CRAFTING_READ_UPGRADE.get())&&cards.getStackInSlot(0).isEmpty()&&source.exp==0&&old.drain(1000,FluidAction.EXECUTE).isEmpty(),"cards/XP transferred, retained native endpoint cannot duplicate XP");
  h.assertTrue(UpgradeHelper.getUpgradeHandler(target).insertItem(7,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false).isEmpty()&&target.getTickSpeed()==1,"new eighth slot uses actual Ultimate delay");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void conversionFailureRollsBackXpAndCardAndRevokesOldOwner(GameTestHelper h){
  h.setBlock(P,Registration.ExperienceHolder.get());var source=(ExperienceHolderBE)h.getBlockEntity(P);source.exp=76543;source.targetExp=24;var p=h.makeMockSurvivalPlayer();var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());var before=source.saveWithFullMetadata();
  var result=MachineTierConversion.convert(context(p,source.getBlockPos(),card),JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get(),(level,pos,state)->{level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);return true;});
  h.assertTrue(result==InteractionResult.FAIL&&card.getCount()==1&&((ExperienceHolderBE)h.getBlockEntity(P)).saveWithFullMetadata().equals(before)&&source.exp==0,"failed replacement restores exact XP/config and clears detached ownership");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void packedRecipeAndRealSurvivalWrenchPreserveXpAndUpgradeLayout(GameTestHelper h){
  h.setBlock(P,Registration.ExperienceHolder.get());var source=(ExperienceHolderBE)h.getBlockEntity(P);source.exp=9999999;source.targetExp=64;source.showParticles=false;UpgradeHelper.getUpgradeHandler(source).insertItem(0,new ItemStack(JDTEItems.RANGE_UPGRADE.get()),false);
  var packed=new ItemStack(Registration.ExperienceHolder.get());packed.getOrCreateTag().put("JustDiresBEData",source.saveWithFullMetadata());var grid=new TransientCraftingContainer(new AbstractContainerMenu(null,1){public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}public boolean stillValid(Player p){return true;}},3,3);grid.setItem(0,new ItemStack(JDTEItems.EXTENDED_UPGRADE.get()));grid.setItem(3,packed);
  var recipe=h.getLevel().getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("jdte","extended_experience_holder")).orElseThrow();var shaped=(net.minecraft.world.item.crafting.CraftingRecipe)recipe;
  h.assertTrue(shaped.matches(grid,h.getLevel()),"exact source shaped recipe accepts packed native holder");var result=shaped.assemble(grid,h.getLevel().registryAccess());h.assertTrue(result.getTag().getCompound("JustDiresBEData").getInt("exp")==9999999,"recipe keeps XP rather than deleting state");
  h.setBlock(P,Blocks.AIR);h.setBlock(P.below(),Blocks.STONE);var p=h.makeMockSurvivalPlayer();p.setPos(Vec3.atCenterOf(h.absolutePos(P.south())));
  h.assertTrue(result.useOn(context(p,h.absolutePos(P.below()),result)).consumesAction(),"actual survival placement consumes crafted item");var b=(ExtendedExperienceHolderBE)h.getBlockEntity(P);
  h.assertTrue(b.exp==9999999&&b.targetExp==64&&!b.showParticles&&UpgradeHelper.getUpgradeHandler(b).getStackInSlot(0).is(JDTEItems.RANGE_UPGRADE.get()),"late Forge placement NBT restores XP/config/cards");
  h.assertTrue(EclipseAlloyWrenchItem.pickupMachine(h.getLevel(),b.getBlockPos(),p),"actual wrench picks up machine");var found=java.util.stream.IntStream.range(0,p.getInventory().getContainerSize()).mapToObj(p.getInventory()::getItem).filter(i->i.is(JDTEItems.EXTENDED_EXPERIENCE_HOLDER.get())).findFirst().orElseThrow();
  h.assertTrue(found.getTag().getCompound("JustDiresBEData").getInt("exp")==9999999&&b.getXpFluidHandler().drain(1000,FluidAction.EXECUTE).isEmpty(),"one transported XP owner, stale packed owner inert");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void automaticPlayerTargetClearsMovedAndRemovedReference(GameTestHelper h){
  var b=place(h);b.targetExp=5;b.exp=1000;b.showParticles=false;b.redstoneControlData.redstoneMode=MiscHelpers.RedstoneMode.IGNORED;b.setTickSpeed(1);var a=b.getAreaAffectingData();a.xOffset=a.yOffset=a.zOffset=0;a.xRadius=a.yRadius=a.zRadius=2;
  var p=player(h,b);p.setPos(Vec3.atCenterOf(b.getBlockPos()));h.getLevel().addFreshEntity(p);b.tickServer();h.assertTrue(p.experienceLevel==1&&b.exp==993,"actual area player gains exactly one source level");
  p.setPos(p.position().add(100,0,0));int before=b.exp;b.tickServer();h.assertTrue(b.exp==before&&p.experienceLevel==1,"cached player outside area not remotely modified");p.discard();b.tickServer();h.assertTrue(b.exp==before,"removed player reference never used");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void twoMenusSerializeSharedXpAndSettingsWithoutDuplicateWithdrawal(GameTestHelper h){
  var b=place(h);var p=player(h,b);var q=h.makeMockSurvivalPlayer();q.setPos(p.position());var m=new ExtendedExperienceHolderContainer(1,p.getInventory(),b.getBlockPos());var n=new ExtendedExperienceHolderContainer(2,q.getInventory(),b.getBlockPos());p.containerMenu=m;q.containerMenu=n;b.exp=150;
  for(int i=0;i<5;i++){m.clickMenuButton(p,5);n.clickMenuButton(q,5);}
  h.assertTrue(b.exp==0&&ExperienceUtils.getPlayerTotalExperience(p)+ExperienceUtils.getPlayerTotalExperience(q)==150,"interleaved repeated shared-menu withdrawals conserve source XP once");
  n.clickMenuButton(q,7);m.clickMenuButton(p,8);h.assertTrue(b.collectExp&&!b.showParticles,"distinct toggles merge authoritative state without stale absolute snapshots");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void stackedBucketsFillAndEmptyWithoutLosingContainers(GameTestHelper h){
  var b=place(h);b.exp=100;var p=player(h,b);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BUCKET,3));
  var block=b.getBlockState().getBlock();var hit=new BlockHitResult(Vec3.atCenterOf(b.getBlockPos()),Direction.UP,b.getBlockPos(),false);
  h.assertTrue(block.use(b.getBlockState(),h.getLevel(),b.getBlockPos(),p,InteractionHand.MAIN_HAND,hit).consumesAction(),"actual block fluid interaction succeeds");
  h.assertTrue(b.exp==50&&p.getMainHandItem().is(Items.BUCKET)&&p.getMainHandItem().getCount()==2,"one1000mB bucket uses50XP and retains two empty buckets");
  var full=java.util.stream.IntStream.range(0,p.getInventory().getContainerSize()).mapToObj(p.getInventory()::getItem).filter(i->i.is(Registration.XP_FLUID_BUCKET.get())).findFirst().orElseThrow();
  p.getInventory().removeItemNoUpdate(p.getInventory().findSlotMatchingItem(full));p.setItemInHand(InteractionHand.MAIN_HAND,full);
  h.assertTrue(block.use(b.getBlockState(),h.getLevel(),b.getBlockPos(),p,InteractionHand.MAIN_HAND,hit).consumesAction()&&b.exp==100&&p.getMainHandItem().is(Items.BUCKET),"one filled bucket returns exactly50XP and one empty container");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void normalBreakKeepsXpOnceAndDropsInstalledCardsSeparately(GameTestHelper h){
  var b=place(h);b.exp=3456789;b.collectExp=true;b.targetExp=13;UpgradeHelper.getUpgradeHandler(b).insertItem(7,new ItemStack(JDTEItems.RANGE_UPGRADE.get()),false);var old=b.getXpFluidHandler();
  h.getLevel().destroyBlock(b.getBlockPos(),true);
  var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(b.getBlockPos()).inflate(2));
  var machines=drops.stream().map(net.minecraft.world.entity.item.ItemEntity::getItem).filter(i->i.is(JDTEItems.EXTENDED_EXPERIENCE_HOLDER.get())).toList();
  h.assertTrue(machines.size()==1&&machines.get(0).getTag().getCompound("JustDiresBEData").getInt("exp")==3456789,"normal break carries saved XP in exactly one machine item");
  h.assertTrue(drops.stream().mapToInt(e->e.getItem().is(JDTEItems.RANGE_UPGRADE.get())?e.getItem().getCount():0).sum()==1&&!machines.get(0).getTag().getCompound("JustDiresBEData").getCompound("ForgeCaps").getCompound("jdte:machine_data").contains("extended_upgrade_handler"),"upgrades separately dropped once and removed from package");
  h.assertTrue(old.drain(1000,FluidAction.EXECUTE).isEmpty(),"retained broken endpoint cannot drain saved XP");drops.forEach(Entity::discard);h.succeed();
 }
 @GameTest(template="mineral_empty") public static void copierCopiesOnlySettingsAndUpgradeBagKeepsEighthCard(GameTestHelper h){
  var b=place(h);b.exp=12345;b.targetExp=43;b.ownerOnly=true;b.collectExp=true;b.showParticles=false;var codec=com.jdte.common.items.machinesettings.MachineSettingsCodecRegistry.find(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(b.getType()),b).orElseThrow();var snapshot=codec.encode(b,h.getLevel().registryAccess());
  var other=P.east(2);h.setBlock(other,JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());var target=(ExtendedExperienceHolderBE)h.getBlockEntity(other);target.exp=54321;var owner=java.util.UUID.randomUUID();target.setPlacedBy(owner);codec.decode(snapshot,h.getLevel().registryAccess()).orElseThrow().apply(target);
  h.assertTrue(target.exp==54321&&target.targetExp==43&&target.ownerOnly&&target.collectExp&&!target.showParticles&&owner.equals(target.placedByUUID),"typed copier changes source controls, never resources or owner identity");
  var bag=new ItemStack(JDTEItems.UPGRADE_STORAGE.get());var storage=bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);storage.insertItem(0,new ItemStack(JDTEItems.RANGE_UPGRADE.get()),false);var card=storage.extractItem(0,1,false);h.assertTrue(UpgradeHelper.getUpgradeHandler(target).insertItem(7,card,false).isEmpty(),"actual bag item handler feeds actual eighth slot");
  var saved=target.saveWithFullMetadata();var restored=(ExtendedExperienceHolderBE)BlockEntity.loadStatic(target.getBlockPos(),target.getBlockState(),saved);h.getLevel().setBlockEntity(restored);var removed=UpgradeHelper.getUpgradeHandler(restored).extractItem(7,1,false);h.assertTrue(storage.insertItem(0,removed,false).isEmpty()&&storage.getStackInSlot(0).is(JDTEItems.RANGE_UPGRADE.get())&&restored.exp==54321,"save restores eighth slot; bag gets exact card back without XP change");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void maximumPlayerXpRoundTripAndUnloadedTargetAreSafe(GameTestHelper h){
  var b=place(h);var p=player(h,b);b.exp=Integer.MAX_VALUE;b.extractExp(p,-1);h.assertTrue(ExperienceUtils.getPlayerTotalExperience(p)==Integer.MAX_VALUE&&b.exp==0,"maximum player extract has no overflow");b.storeExp(p,-1);h.assertTrue(b.exp==Integer.MAX_VALUE&&ExperienceUtils.getPlayerTotalExperience(p)==0,"maximum XP can be stored back without loss");
  var far=new BlockPos(1000000,100,1000000);var detached=new ExtendedExperienceHolderBE(far,b.getBlockState());detached.setLevel(h.getLevel());detached.exp=500;detached.tickServer();h.assertTrue(detached.getXpFluidHandler().drain(1000,FluidAction.EXECUTE).isEmpty()&&!h.getLevel().hasChunkAt(far)&&detached.exp==500,"unloaded detached target cannot force chunks or spend XP");h.succeed();
 }

 @GameTest(template="mineral_empty") public static void actualUltimateTickerMovesTenLevelsAndNeverCreatesExperience(GameTestHelper h){
  var b=place(h);b.exp=10000;b.targetExp=100;b.showParticles=false;b.redstoneControlData.redstoneMode=MiscHelpers.RedstoneMode.IGNORED;
  var area=b.getAreaAffectingData();area.xOffset=area.yOffset=area.zOffset=0;area.xRadius=area.yRadius=area.zRadius=2;
  var p=player(h,b);p.setPos(Vec3.atCenterOf(b.getBlockPos()));h.getLevel().addFreshEntity(p);
  h.assertTrue(UpgradeHelper.getUpgradeHandler(b).insertItem(7,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false).isEmpty(),"source Ultimate card accepted in real eighth slot");
  var ticker=((com.jdte.common.blocks.ExtendedExperienceHolderBlock)b.getBlockState().getBlock()).getTicker(h.getLevel(),b.getBlockState(),JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get());ticker.tick(h.getLevel(),b.getBlockPos(),b.getBlockState(),b);
  h.assertTrue(p.experienceLevel==10&&b.exp==9840&&ExperienceUtils.getPlayerTotalExperience(p)+b.exp==10000,"actual wrapped Forge ticker executes ten source level settlements for exactly160XP");p.discard();h.succeed();
 }

 @GameTest(template="mineral_empty") public static void experienceTransfersDoNotInvokeUnpaidGainCallbacks(GameTestHelper h){
  var b=place(h);var mock=player(h,b);var realServer=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"XpTransaction"));realServer.setPos(mock.position());
  var events=new java.util.concurrent.atomic.AtomicInteger();java.util.function.Consumer<net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange> callback=e->{if(e.getEntity()==mock||e.getEntity()==realServer){events.incrementAndGet();e.setAmount(100);}};
  net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(callback);
  try{for(Player p:java.util.List.of(mock,realServer)){
   p.experienceLevel=0;p.experienceProgress=0;p.totalExperience=0;b.exp=100;b.extractExp(p,1);
   h.assertTrue(ExperienceUtils.getPlayerTotalExperience(p)==7&&b.exp==93,"source one-level transaction cannot be multiplied by an XP-gain event");b.storeExp(p,-1);
   h.assertTrue(ExperienceUtils.getPlayerTotalExperience(p)==0&&b.exp==100,"exact return transfer conserves all XP");
  }h.assertTrue(events.get()==0,"no synthetic zero-XP gain events during storage transactions");}
  finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(callback);}h.succeed();
 }

}
