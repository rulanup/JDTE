package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.containers.*;
import com.jdte.common.containers.handlers.LargePotionCanisterHandler;
import com.jdte.mixin.BaseBowInvoker;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class LargePotionCanisterGameTests {
 public static ItemStack potion(Potion kind,int count){var s=PotionUtils.setPotion(new ItemStack(Items.POTION,count),kind);return s;}
 public static ItemStack custom(int count){var s=potion(Potions.SWIFTNESS,count);PotionUtils.setCustomEffects(s,List.of(new MobEffectInstance(MobEffects.REGENERATION,321,2,true,false,false)));s.getOrCreateTag().putInt("CustomPotionColor",0x12ab34);return s;}
 public static ItemStack filled(){var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());for(int i=0;i<4;i++)if(!LargePotionCanisterItem.tryFillBatch(s,custom(4)))throw new IllegalStateException("batch rejected");return s;}
 public static LargePotionCanisterHandler handler(ItemStack s){return (LargePotionCanisterHandler)s.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);}
 public static ItemStack bow(){var s=new ItemStack(Registration.EclipseAlloyBow.get());ToggleableTool.setSetting(s,"enabled",true);for(var a:new Ability[]{Ability.POTIONARROW,Ability.SPLASH,Ability.LINGERING}){s.getOrCreateTag().putBoolean("upgrade_"+a.getName(),true);ToggleableTool.setSetting(s,a.getName(),true);}s.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(Integer.MAX_VALUE,false);return s;}
 @GameTest(template="mineral_empty") public static void realFourPotionBatchReturnsFourBottlesAndNoSimulationMutation(GameTestHelper h){
  var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());var cap=handler(s);var offered=custom(7);var before=s.save(new CompoundTag());
  h.assertTrue(cap.insertItem(0,offered,true).getCount()==3&&s.save(new CompoundTag()).equals(before)&&offered.getCount()==7,"simulation/caller input remain unchanged");
  var remainder=cap.insertItem(0,offered,false);h.assertTrue(remainder.getCount()==3&&offered.getCount()==7&&PotionCanister.getPotionAmount(s)==1000,"one four-potion batch accepted from seven without consuming caller reference");
  h.assertTrue(cap.getStackInSlot(0).is(Items.GLASS_BOTTLE)&&cap.getStackInSlot(0).getCount()==4&&cap.extractItem(0,64,true).getCount()==4&&cap.getStackInSlot(0).getCount()==4,"four returned bottles, extraction simulation pure");
  h.assertTrue(cap.extractItem(0,64,false).getCount()==4&&cap.getStackInSlot(0).isEmpty(),"real bottle extraction exactly once");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void fullMismatchedAndInvalidInputsKeepExactResources(GameTestHelper h){
  var s=filled();var cap=handler(s);var offered=custom(4);var before=LargePotionCanisterData.contents(s);
  h.assertTrue(cap.insertItem(0,offered,false).isEmpty()&&PotionCanister.getPotionAmount(s)==4000&&cap.getStackInSlot(0).getCount()==4&&cap.getStackInSlot(0).is(Items.POTION),"full tank keeps four unprocessed potions in persistent slot");
  int returned=0;for(int i=0;i<4;i++)returned+=cap.extractItem(0,4,false).getCount();h.assertTrue(returned==4&&LargePotionCanisterData.contents(s).equals(before),"full-state recovery returns exactly four untouched potions");
  PotionCanister.reducePotionAmount(s,1000);cap.insertItem(0,potion(Potions.HEALING,4),false);h.assertTrue(PotionCanister.getPotionAmount(s)==3000&&LargePotionCanisterData.contents(s).equals(before),"different potion never merges or debits stored fluid");
  for(int i=0;i<4;i++)cap.extractItem(0,4,false);
  h.assertTrue(cap.insertItem(0,new ItemStack(Items.DIRT),false).is(Items.DIRT)&&cap.getStackInSlot(0).isEmpty(),"invalid item rejected");
  var invalid=custom(4);invalid.getOrCreateTag().putString("Potion","not valid");var virgin=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());h.assertTrue(!LargePotionCanisterItem.tryFillBatch(virgin,invalid)&&invalid.getCount()==4&&PotionCanister.getPotionAmount(virgin)==0,"invalid potion identifier rejected without resource changes");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void allPotionContentsIncludingEffectsAndColorMustMatch(GameTestHelper h){
  var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());h.assertTrue(LargePotionCanisterItem.tryFillBatch(s,custom(4)),"full content batch");var before=s.save(new CompoundTag());
  var mismatch=custom(4);mismatch.getOrCreateTag().putInt("CustomPotionColor",0x999999);h.assertTrue(!LargePotionCanisterItem.tryFillBatch(s,mismatch)&&mismatch.getCount()==4&&s.save(new CompoundTag()).equals(before),"same base but different color cannot mix");
  mismatch=custom(4);PotionUtils.setCustomEffects(mismatch,List.of(new MobEffectInstance(MobEffects.REGENERATION,322,2,true,false,false)));h.assertTrue(!LargePotionCanisterItem.tryFillBatch(s,mismatch),"custom effect duration/flags part of identity");
  var reloaded=ItemStack.of(s.save(new CompoundTag()));var effects=LargePotionCanisterData.effects(reloaded);h.assertTrue(effects.stream().anyMatch(e->e.getEffect()==MobEffects.REGENERATION&&e.getDuration()==321&&e.getAmplifier()==2&&e.isAmbient()&&!e.isVisible()&&!e.showIcon())&&LargePotionCanisterData.color(reloaded)==0x12ab34&&PotionCanister.getPotionColor(reloaded)==0x12ab34&&PotionCanister.getStoredPotion(reloaded)==Potions.SWIFTNESS,"full effect flags and native color/base getters survive actual item NBT codec");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void partialSlotsAndParallelViewsShareCanonicalSavedContents(GameTestHelper h){
  var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());var a=handler(s);var b=new LargePotionCanisterHandler(s,true);
  a.insertItem(0,custom(2),false);h.assertTrue(PotionCanister.getPotionAmount(s)==0&&b.getStackInSlot(0).getCount()==2,"partial batch saved across two independent views");b.insertItem(0,custom(2),false);
  h.assertTrue(a.getStackInSlot(0).is(Items.GLASS_BOTTLE)&&a.getStackInSlot(0).getCount()==4&&PotionCanister.getPotionAmount(s)==1000,"fourth potion commits once, first view refreshes");
  h.assertTrue(a.extractItem(0,4,false).getCount()==4&&b.extractItem(0,4,false).isEmpty(),"two views cannot duplicate returned bottles");
  a.insertItem(0,custom(3),false);var restored=ItemStack.of(s.save(new CompoundTag()));h.assertTrue(handler(restored).getStackInSlot(0).getCount()==3&&PotionCanister.getPotionAmount(restored)==1000,"pending three potions survive save and reload separately from processed fluid");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void nativeAmountConsumersRespectFourThousandAndLeaveNativeItemsAlone(GameTestHelper h){
  var s=filled();PotionCanister.reducePotionAmount(s,25);h.assertTrue(PotionCanister.getPotionAmount(s)==3975,"native static consumer cannot clamp remainder to1000");
  PotionCanister.reducePotionAmount(s,-100);h.assertTrue(PotionCanister.getPotionAmount(s)==3975,"negative drains cannot create contents");PotionCanister.addPotionAmount(s,Integer.MAX_VALUE);h.assertTrue(PotionCanister.getPotionAmount(s)==4000,"wide addition saturates without overflow");
  PotionCanister.setPotionAmount(s,-1);h.assertTrue(PotionCanister.getPotionAmount(s)==0&&LargePotionCanisterData.contents(s).isEmpty(),"emptying clears type/effects/color");
  var normal=new ItemStack(Registration.PotionCanister.get());PotionCanister.setStoredPotion(normal,Potions.SWIFTNESS);PotionCanister.setPotionAmount(normal,4000);h.assertTrue(PotionCanister.getPotionAmount(normal)==1000,"unrelated native capacity behavior remains1000");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void actualNativeBowAppliesCustomEffectsAndExactTwoSlotCosts(GameTestHelper h){
  var player=h.makeMockSurvivalPlayer();var bow=bow();var nativeBow=(BaseBow)bow.getItem();h.assertTrue(nativeBow.canUseAbilityAndDurability(bow,Ability.POTIONARROW)&&nativeBow.canUseAbilityAndDurability(bow,Ability.SPLASH)&&nativeBow.canUseAbilityAndDurability(bow,Ability.LINGERING),"real three native abilities provisioned");
  BaseBow.setPotionCanister(bow,filled(),0);BaseBow.setPotionCanister(bow,filled(),1);var arrow=new JustDireArrow(h.getLevel(),player);((BaseBowInvoker)nativeBow).jdte$setupArrowAbilities(bow,arrow,player);
  h.assertTrue(PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==3900&&PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,1))==3900,"25+25+50mB paid once per occupied native slot");
  h.assertTrue(arrow.getEffects().stream().anyMatch(e->e.getEffect()==MobEffects.REGENERATION&&e.getDuration()==321&&e.getAmplifier()==2&&!e.isVisible()),"actual native arrow receives custom effect metadata");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void disabledInsufficientAndWaterBowBehaviorMatchesSource(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var bow=bow();var item=(BaseBow)bow.getItem();var c=filled();PotionCanister.setPotionAmount(c,99);BaseBow.setPotionCanister(bow,c,0);((BaseBowInvoker)item).jdte$setupArrowAbilities(bow,new JustDireArrow(h.getLevel(),p),p);
  h.assertTrue(PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==99,"less than full source ability cost never debited");
  BaseBow.setPotionCanister(bow,filled(),0);ToggleableTool.setSetting(bow,"enabled",false);((BaseBowInvoker)item).jdte$setupArrowAbilities(bow,new JustDireArrow(h.getLevel(),p),p);h.assertTrue(PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==4000,"disabled bow does not consume");
  ToggleableTool.setSetting(bow,"enabled",true);var water=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());LargePotionCanisterItem.tryFillBatch(water,potion(Potions.WATER,4));BaseBow.setPotionCanister(bow,water,0);var arrow=new JustDireArrow(h.getLevel(),p);((BaseBowInvoker)item).jdte$setupArrowAbilities(bow,arrow,p);h.assertTrue(PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==900&&arrow.getEffects().isEmpty(),"source water charged but creates no potion effects");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void menuIdentityGuardAndStaleViewsCannotMutateMovedItem(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var canister=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());p.setItemInHand(InteractionHand.MAIN_HAND,canister);var menu=new LargePotionCanisterContainer(1,p.getInventory(),canister,false);p.containerMenu=menu;
  h.assertTrue(menu.stillValid(p)&&menu.slots.size()==37,"actual bound handheld menu");menu.setCarried(custom(1));menu.clicked(0,0,ClickType.PICKUP,p);h.assertTrue(handler(canister).getStackInSlot(0).getCount()==1,"one real mouse click deposits one potion");
  var replacement=canister.copy();p.setItemInHand(InteractionHand.MAIN_HAND,replacement);menu.setCarried(custom(1));menu.clicked(0,0,ClickType.PICKUP,p);
  h.assertTrue(!menu.stillValid(p)&&handler(canister).getStackInSlot(0).getCount()==1&&menu.getCarried().getCount()==1,"same-NBT replacement invalidates old identity, no lost carried item");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void menuQuickMoveAccumulatesFourSinglesAndFullInventoryHasNoDebit(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var canister=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());p.setItemInHand(InteractionHand.MAIN_HAND,canister);for(int i=9;i<13;i++)p.getInventory().setItem(i,custom(1));var menu=new LargePotionCanisterContainer(1,p.getInventory(),canister,false);p.containerMenu=menu;
  for(int i=1;i<=4;i++)h.assertTrue(!menu.quickMoveStack(p,i).isEmpty(),"unstackable potion enters source four-wide slot");h.assertTrue(PotionCanister.getPotionAmount(canister)==1000&&handler(canister).getStackInSlot(0).getCount()==4&&handler(canister).getStackInSlot(0).is(Items.GLASS_BOTTLE),"four singles produce one batch/four bottles");
  for(int i=0;i<p.getInventory().getContainerSize();i++)if(i!=p.getInventory().selected)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));var before=canister.save(new CompoundTag());h.assertTrue(menu.quickMoveStack(p,0).isEmpty()&&canister.save(new CompoundTag()).equals(before),"blocked bottle output does not consume or duplicate pending containers");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void preservingPortableRecipeAndWireCodecKeepAllNativeData(GameTestHelper h){
  var nativeItem=new ItemStack(Registration.PotionCanister.get());PotionCanister.setStoredPotion(nativeItem,Potions.STRONG_HEALING);PotionCanister.setPotionAmount(nativeItem,750);nativeItem.setHoverName(net.minecraft.network.chat.Component.literal("Keep name"));nativeItem.getOrCreateTag().putString("unknown_user_tag","retain");
  var grid=new TransientCraftingContainer(new AbstractContainerMenu(null,1){public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}public boolean stillValid(Player p){return true;}},3,3);grid.setItem(0,nativeItem);for(int i=1;i<=4;i++)grid.setItem(i,new ItemStack(Registration.EclipseAlloyIngot.get()));grid.setItem(5,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));
  var recipe=(com.jdte.common.recipes.LargePortableContainerRecipe)h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("large_potion_canister")).orElseThrow();h.assertTrue(recipe.matches(grid,h.getLevel()),"source portable upgrade ingredients");var out=recipe.assemble(grid,h.getLevel().registryAccess());h.assertTrue(out.is(JDTEItems.LARGE_POTION_CANISTER.get())&&out.getTag().equals(nativeItem.getTag())&&PotionCanister.getPotionAmount(out)==750,"transmutation keeps base potion, amount, name and arbitrary NBT");
  var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{var codec=new com.jdte.common.recipes.LargePortableContainerRecipe.Serializer();codec.toNetwork(buf,recipe);var decoded=codec.fromNetwork(recipe.getId(),buf);h.assertTrue(decoded.matches(grid,h.getLevel())&&decoded.getResultItem(h.getLevel().registryAccess()).is(JDTEItems.LARGE_POTION_CANISTER.get()),"client recipe wire includes exact source/result pair");}finally{buf.release();}h.succeed();
 }
 @GameTest(template="mineral_empty") public static void actualNativeReleaseShootsArrowAndPaysPotionAndAmmoOnce(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new BlockPos(2,2,2))));var bow=bow();p.setItemInHand(InteractionHand.MAIN_HAND,bow);p.getInventory().setItem(9,new ItemStack(Items.ARROW,5));BaseBow.setPotionCanister(bow,filled(),0);
  ((BaseBow)bow.getItem()).releaseUsing(bow,h.getLevel(),p,bow.getUseDuration()-20);
  var arrows=h.getLevel().getEntitiesOfClass(JustDireArrow.class,new net.minecraft.world.phys.AABB(p.blockPosition()).inflate(4));
  h.assertTrue(arrows.size()==1&&arrows.get(0).getEffects().stream().anyMatch(e->e.getEffect()==MobEffects.REGENERATION&&e.getAmplifier()==2)&&PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==3900&&p.getInventory().countItem(Items.ARROW)==4,"real native release admits one actual custom-effect arrow and debits100mB/one ammo");arrows.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
 }
 @GameTest(template="mineral_empty") public static void physicalItemTransferInvalidatesOldMenuAndPreservesPendingBatch(GameTestHelper h){
  var a=h.makeMockSurvivalPlayer();var b=h.makeMockSurvivalPlayer();var item=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());a.setItemInHand(InteractionHand.MAIN_HAND,item);handler(item).insertItem(0,custom(3),false);var old=new LargePotionCanisterContainer(1,a.getInventory(),item,false);a.containerMenu=old;
  a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);b.setItemInHand(InteractionHand.MAIN_HAND,item);var next=new LargePotionCanisterContainer(2,b.getInventory(),item,false);b.containerMenu=next;old.setCarried(custom(1));old.clicked(0,0,ClickType.PICKUP,a);
  h.assertTrue(!old.stillValid(a)&&old.getCarried().getCount()==1&&handler(item).getStackInSlot(0).getCount()==3,"old player cannot complete or extract moved item");next.setCarried(custom(1));next.clicked(0,0,ClickType.PICKUP,b);
  h.assertTrue(next.getCarried().isEmpty()&&PotionCanister.getPotionAmount(item)==1000&&handler(item).getStackInSlot(0).is(Items.GLASS_BOTTLE)&&handler(item).getStackInSlot(0).getCount()==4,"new owner commits fourth potion exactly once; old carried potion retained");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void malformedNbtAndWrongThreadNeverCreatePotionContents(GameTestHelper h){
  var s=filled();s.getOrCreateTag().putString("PotionAmt","corrupt");var before=s.save(new CompoundTag());var input=custom(4);h.assertTrue(!LargePotionCanisterItem.tryFillBatch(s,input)&&input.getCount()==4&&s.save(new CompoundTag()).equals(before),"wrong amount NBT type rejected without rewriting input or state");
  var malformed=custom(4);var list=new ListTag();list.add(IntTag.valueOf(3));malformed.getOrCreateTag().put("CustomPotionEffects",list);var fresh=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());h.assertTrue(!LargePotionCanisterItem.tryFillBatch(fresh,malformed),"invalid effect-list element type rejected");
  var legacy=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());legacy.getOrCreateTag().putString("StoredPotionId","not a valid id");legacy.getOrCreateTag().putInt("PotionAmt",1000);var damaged=legacy.save(new CompoundTag());h.assertTrue(PotionCanister.getStoredPotion(legacy)==Potions.EMPTY&&PotionCanister.getPotionColor(legacy)==-1&&legacy.save(new CompoundTag()).equals(damaged),"native read APIs reject malformed legacy potion IDs without throwing or mutating data");
  var cap=handler(fresh);before=fresh.save(new CompoundTag());var remainder=java.util.concurrent.CompletableFuture.supplyAsync(()->cap.insertItem(0,custom(4),false)).join();h.assertTrue(remainder.getCount()==4&&fresh.save(new CompoundTag()).equals(before),"off-thread automation cannot mutate inventory or potion data");
  h.assertTrue(!fresh.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()&&!fresh.getCapability(ForgeCapabilities.ENERGY).isPresent(),"source potion units expose no invented fluid or FE interface");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void equivalentComponentDefaultsAndCustomOnlyPotionAreSupported(GameTestHelper h){
  var s=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());LargePotionCanisterItem.tryFillBatch(s,potion(Potions.SWIFTNESS,4));var emptyEffects=potion(Potions.SWIFTNESS,4);emptyEffects.getOrCreateTag().put("CustomPotionEffects",new ListTag());
  h.assertTrue(LargePotionCanisterItem.tryFillBatch(s,emptyEffects)&&PotionCanister.getPotionAmount(s)==2000,"absent and empty custom-effect defaults describe the same composition");
  var customOnly=new ItemStack(Items.POTION,4);PotionUtils.setCustomEffects(customOnly,List.of(new MobEffectInstance(MobEffects.REGENERATION,101,1)));var canister=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());h.assertTrue(LargePotionCanisterItem.tryFillBatch(canister,customOnly),"custom-only PotionContents do not require a registered base potion");
  var bow=bow();BaseBow.setPotionCanister(bow,canister,0);var arrow=new JustDireArrow(h.getLevel(),h.makeMockSurvivalPlayer());((BaseBowInvoker)bow.getItem()).jdte$setupArrowAbilities(bow,arrow,h.makeMockSurvivalPlayer());h.assertTrue(arrow.getEffects().stream().anyMatch(e->e.getDuration()==101&&e.getAmplifier()==1)&&PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==900,"actual native bow consumes custom-only composition and transfers effects");h.succeed();
 }

}
