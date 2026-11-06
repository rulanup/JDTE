package com.jdte.gametest;
import com.jdte.common.items.*;
import com.jdte.common.containers.CraftingPatternContainer;
import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;
@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class CraftingConversionGameTests {
 private static final BlockPos POS=new BlockPos(3,1,2);
 private static CraftingPatternData button(GameTestHelper h){var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));grid.set(0,new ItemStack(Items.OAK_PLANKS));return CraftingPatternData.capture(h.getLevel(),grid).orElseThrow();}
 private static CraftingPatternData plate(GameTestHelper h){var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));grid.set(3,new ItemStack(Items.OAK_PLANKS));grid.set(4,new ItemStack(Items.OAK_PLANKS));return CraftingPatternData.capture(h.getLevel(),grid).orElseThrow();}
 private static GreenhouseBE greenhouse(GameTestHelper h,List<CraftingPatternData> patterns){h.setBlock(POS,JDTEBlocks.GREENHOUSE.get());var be=(GreenhouseBE)h.getBlockEntity(POS);var upgrade=new ItemStack(JDTEItems.ADVANCED_ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(upgrade,patterns);UpgradeHelper.getUpgradeHandler(be).insertItem(0,upgrade,false);return be;}
 private static int count(GreenhouseBE be,Item item){int count=0;for(int i=GreenhouseBE.OUTPUT_START_SLOT;i<be.getMachineHandler().getSlots();i++)if(be.getMachineHandler().getStackInSlot(i).is(item))count+=be.getMachineHandler().getStackInSlot(i).getCount();return count;}
 @GameTest(template="mineral_empty") public static void craftingPatternGhostGridAndSaveDoNotConsumeItems(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var pattern=new ItemStack(JDTEItems.CRAFTING_PATTERN.get());p.setItemInHand(InteractionHand.MAIN_HAND,pattern);
  var menu=new CraftingPatternContainer(1,p.getInventory(),InteractionHand.MAIN_HAND);menu.setCarried(new ItemStack(Items.OAK_PLANKS,32));menu.clicked(0,0,ClickType.PICKUP,p);
  h.assertTrue(menu.getCarried().getCount()==32&&menu.slots.get(0).getItem().getCount()==1,"ghost grid consumes no planks");
  h.assertTrue(menu.hasValidRecipe()&&menu.slots.get(9).getItem().is(Items.OAK_BUTTON),"actual crafting result");menu.clicked(9,0,ClickType.PICKUP,p);h.assertTrue(menu.getCarried().getCount()==32,"preview cannot be extracted");
  h.assertTrue(menu.clickMenuButton(p,0),"server saves valid recipe");h.assertTrue(CraftingPatternItem.getPattern(pattern).resolve(h.getLevel()).isPresent(),"persisted recipe resolves");
  var saved=ItemStack.of(pattern.save(new net.minecraft.nbt.CompoundTag()));h.assertTrue(CraftingPatternItem.getPattern(saved).equals(button(h)),"pattern NBT round trip");
  h.assertTrue(menu.clickMenuButton(p,1)&&CraftingPatternItem.getPattern(pattern)==null,"clear button clears persisted ghost grid");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void patternRejectsWrongRecipeOutputAndMalformedGrid(GameTestHelper h){
  var data=button(h);var bad=new CraftingPatternData(data.recipeId(),data.ingredients(),new ItemStack(Items.DIAMOND));h.assertTrue(bad.resolve(h.getLevel()).isEmpty(),"recipe output cannot be forged");
  var tag=data.save();tag.remove("Ingredients");h.assertTrue(CraftingPatternData.load(tag).isEmpty(),"incomplete grid rejected");
  tag=data.save();tag.putInt("ResultCount",-1);h.assertTrue(CraftingPatternData.load(tag).isEmpty(),"negative result rejected");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void patternMenuLocksHeldAndOffhandItems(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var item=new ItemStack(JDTEItems.CRAFTING_PATTERN.get());p.setItemInHand(InteractionHand.OFF_HAND,item);
  var menu=new CraftingPatternContainer(1,p.getInventory(),InteractionHand.OFF_HAND);menu.clicked(10,40,ClickType.SWAP,p);h.assertTrue(p.getOffhandItem()==item,"offhand protected");
  p.setItemInHand(InteractionHand.OFF_HAND,item.copy());h.assertTrue(!menu.stillValid(p),"replacement item closes old menu");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void greenhouseRecordedCraftsReserveIngredientsAndPreserveCounts(GameTestHelper h){
  var be=greenhouse(h,List.of(button(h)));be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS,13));
  h.assertTrue(be.getMachineHandler().extractItem(GreenhouseBE.OUTPUT_START_SLOT,1,false).isEmpty(),"raw recipe inputs retained from output pipes");be.prepareOutputConversion();
  h.assertTrue(count(be,Items.OAK_BUTTON)==13&&count(be,Items.OAK_PLANKS)==0,"all recorded crafts conserve ratio");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void advancedConversionRoundRobinSurvivesMachineSave(GameTestHelper h){
  var be=greenhouse(h,List.of(button(h),plate(h)));be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS,9));be.prepareOutputConversion();
  h.assertTrue(count(be,Items.OAK_BUTTON)==3&&count(be,Items.OAK_PRESSURE_PLATE)==3,"shared inputs evenly alternate");
  var saved=be.saveWithFullMetadata();be.load(saved);be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT+2,new ItemStack(Items.OAK_PLANKS,1));be.prepareOutputConversion();
  h.assertTrue(count(be,Items.OAK_BUTTON)==4,"next fair recipe after save");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void conversionCapacityFailureKeepsInputsAndProducts(GameTestHelper h){
  var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int i=0;i<3;i++)grid.set(i,new ItemStack(Items.OAK_PLANKS));
  var slab=CraftingPatternData.capture(h.getLevel(),grid).orElseThrow();var be=greenhouse(h,List.of(slab));
  for(int i=0;i<be.getActiveOutputSlots();i++)be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT+i,new ItemStack(Items.COBBLESTONE,be.getOutputSlotLimit()));
  be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS,be.getOutputSlotLimit()));var before=be.saveWithFullMetadata().getCompound("inventory").copy();be.prepareOutputConversion();
  h.assertTrue(before.equals(be.saveWithFullMetadata().getCompound("inventory")),"blocked conversion never consumes inputs");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void anvilEnforcesBasicAndAdvancedPatternLimits(GameTestHelper h){
  var player=h.makeMockSurvivalPlayer();var base=new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get());var pattern=new ItemStack(JDTEItems.CRAFTING_PATTERN.get());CraftingPatternItem.setPattern(pattern,button(h));
  var first=new net.minecraftforge.event.AnvilUpdateEvent(base,pattern,"Named",0,player);EssenceConversionAnvilEvents.bindPattern(first);
  h.assertTrue(!first.getOutput().isEmpty()&&first.getCost()==1&&first.getMaterialCost()==1,"one level and one pattern binding");h.assertTrue(first.getOutput().getHoverName().getString().equals("Named"),"anvil rename preserved");
  var second=new net.minecraftforge.event.AnvilUpdateEvent(first.getOutput(),pattern,"",0,player);EssenceConversionAnvilEvents.bindPattern(second);h.assertTrue(second.isCanceled(),"basic limit and duplicate rejected");
  var advanced=new ItemStack(JDTEItems.ADVANCED_ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(advanced,List.of(button(h),plate(h)));
  var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int k=0;k<3;k++)grid.set(k,new ItemStack(Items.OAK_PLANKS));CraftingPatternItem.setPattern(pattern,CraftingPatternData.capture(h.getLevel(),grid).orElseThrow());
  var third=new net.minecraftforge.event.AnvilUpdateEvent(advanced,pattern,"",0,player);EssenceConversionAnvilEvents.bindPattern(third);h.assertTrue(EssenceConversionUpgradeItem.patterns(third.getOutput()).size()==3,"advanced accepts third distinct pattern");
  var fourth=new net.minecraftforge.event.AnvilUpdateEvent(third.getOutput(),pattern,"",0,player);EssenceConversionAnvilEvents.bindPattern(fourth);h.assertTrue(fourth.isCanceled(),"advanced cannot exceed three");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void adjacentOutputRetainsIncompleteRecordedInputs(GameTestHelper h){
  var be=greenhouse(h,List.of(plate(h)));be.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS));
  h.setBlock(POS.east(),net.minecraft.world.level.block.Blocks.CHEST);JDTEAttachments.autoIo(be).setMasks(0,63);
  com.jdte.common.autoioconfig.AutoIoTransferHelper.flushEventDrivenGreenhouseOutput(be,new int[]{GreenhouseBE.OUTPUT_START_SLOT});
  h.assertTrue(count(be,Items.OAK_PLANKS)==1,"incomplete two-plank recipe retained from neighbor automation");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void nativeSmithingKeepsBoundEssencePatterns(GameTestHelper h){
  var base=new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(base,List.of(button(h)));
  var input=new net.minecraft.world.SimpleContainer(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),base,new ItemStack(Items.DIAMOND));
  var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING,input,h.getLevel()).orElseThrow();
  var result=recipe.assemble(input,h.getLevel().registryAccess());h.assertTrue(result.is(JDTEItems.ADVANCED_ESSENCE_CONVERSION_UPGRADE.get())&&EssenceConversionUpgradeItem.patterns(result).equals(List.of(button(h))),"native Forge smithing preserves pattern NBT");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void largeGreenhouseConvertsStoredOutputAndPersistsPatterns(GameTestHelper h){
  h.setBlock(POS,JDTEBlocks.LARGE_GREENHOUSE.get());var be=(LargeGreenhouseBE)h.getBlockEntity(POS);var upgrade=new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(upgrade,List.of(button(h)));
  UpgradeHelper.getUpgradeHandler(be).insertItem(0,upgrade,false);be.getMachineHandler().setStackInSlot(LargeGreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS,27));be.prepareOutputConversion();
  var saved=be.saveWithFullMetadata();be.load(saved);int total=0;for(int k=LargeGreenhouseBE.OUTPUT_START_SLOT;k<be.getMachineHandler().getSlots();k++)if(be.getMachineHandler().getStackInSlot(k).is(Items.OAK_BUTTON))total+=be.getMachineHandler().getStackInSlot(k).getCount();
  h.assertTrue(total==27&&EssenceConversionUpgradeItem.patterns(UpgradeHelper.getUpgradeHandler(be).getStackInSlot(0)).equals(List.of(button(h))),"large greenhouse products and bound patterns survive save/load");h.succeed();
 }
}
