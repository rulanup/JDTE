package com.jdte.common.items;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.setup.*;
import com.jdte.common.containers.*;
import com.jdte.common.containers.handlers.LargeFuelCanisterHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class LargeFuelCanisterGameTests {
 private static final BlockPos POS=new BlockPos(2,1,2);
 @GameTest(template="mineral_empty") public static void fuelFillWholeLimitsRemaindersAndNbtRoundTrip(GameTestHelper h){
  var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());var handler=new LargeFuelCanisterHandler(1,can);
  h.assertTrue(!handler.isItemValid(0,new ItemStack(Items.DIRT))&&!handler.isItemValid(0,new ItemStack(Items.LAVA_BUCKET))&&!handler.isItemValid(0,new ItemStack(Registration.Fuel_Canister.get())),"invalid/nested/container fuels rejected");
  var coal=new ItemStack(Items.COAL,64);int burn=net.minecraftforge.common.ForgeHooks.getBurnTime(coal,RecipeType.SMELTING);
  h.assertTrue(handler.insertItem(0,coal,true).isEmpty()&&FuelCanister.getFuelLevel(can)==0&&coal.getCount()==64,"simulation has no fuel mutation");
  h.assertTrue(handler.insertItem(0,coal,false).isEmpty()&&FuelCanister.getFuelLevel(can)==burn*64&&handler.getStackInSlot(0).isEmpty()&&coal.getCount()==64,"real handler consumes its owned input copy once");
  h.assertTrue(FuelCanister.getBurnSpeed(can)==10D&&LargeFuelCanisterItem.getBurnSpeedMultiplier(can)==100,"source weighted tenfold fuel value and tenfold resolved multiplier retained");
  FuelCanister.setFuelLevel(can,LargeFuelCanisterItem.getMaxFuelLevel()-burn);handler.insertItem(0,new ItemStack(Items.COAL,2),false);
  h.assertTrue(FuelCanister.getFuelLevel(can)==LargeFuelCanisterItem.getMaxFuelLevel()&&handler.extractItem(0,64,false).getCount()==1,"fullness keeps excess actual coal extractable");
  var restored=ItemStack.of(can.save(new net.minecraft.nbt.CompoundTag()));h.assertTrue(FuelCanister.getFuelLevel(restored)==LargeFuelCanisterItem.getMaxFuelLevel()&&FuelCanister.getBurnSpeed(restored)==10D&&LargeFuelCanisterItem.getFullness(restored)==4,"live fuel and weighted speed saved in native NBT");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void normalAndUpgradedBurnUseLargeFuelUnitsAndResolvedSpeed(GameTestHelper h){
  h.setBlock(POS,Registration.GeneratorT1.get());var be=(GeneratorT1BE)h.getBlockEntity(POS);var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());int unit=LargeFuelCanisterItem.getMinimumFuelConsumed();FuelCanister.setFuelLevel(can,unit*4);FuelCanister.setBurnSpeed(can,2D);be.getMachineHandler().setStackInSlot(0,can);
  be.doBurn();h.assertTrue(FuelCanister.getFuelLevel(be.getMachineHandler().getStackInSlot(0))==unit*3&&be.feRemaining==unit*be.getFePerFuelTick()&&be.getBurnSpeedMultiplier()==20*Config.GENERATOR_T1_BURN_SPEED_MULTIPLIER.get(),"native generator consumes one large unit and resolved multiplier");
  be.feRemaining=0;UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);be.doBurn();
  h.assertTrue(FuelCanister.getFuelLevel(be.getMachineHandler().getStackInSlot(0))==unit&&be.feRemaining==unit*be.getFePerFuelTick()*3,"card consumes exactly two units for source tripled credit");
  be.feRemaining=0;be.doBurn();h.assertTrue(FuelCanister.getFuelLevel(be.getMachineHandler().getStackInSlot(0))==unit&&be.feRemaining==0,"insufficient second canister unit is untouched");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void fullAndPartialGeneratorBuffersPreservePurchasedFeCredit(GameTestHelper h){
  h.setBlock(POS,Registration.GeneratorT1.get());var be=(GeneratorT1BE)h.getBlockEntity(POS);be.feRemaining=12345;be.maxBurn=100;be.burnRemaining=100;be.getEnergyStorage().setEnergy(be.getMaxEnergy());
  for(int i=0;i<5;i++)be.doGenerate();h.assertTrue(be.feRemaining==12345&&be.burnRemaining==100,"full buffer never discards pending FE or burn work");
  be.getEnergyStorage().setEnergy(be.getMaxEnergy()-1);be.doGenerate();h.assertTrue(be.feRemaining==12344&&be.getEnergyStorage().getEnergyStored()==be.getMaxEnergy(),"only actual accepted FE is debited");
  var fresh=(GeneratorT1BE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),be.saveWithFullMetadata());h.getLevel().setBlockEntity(fresh);h.assertTrue(fresh.feRemaining==12344&&fresh.burnRemaining==99,"credit and progress persist");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void boundMenuCannotMoveOwnerOrWriteReplacementAndReturnsUnusedCoal(GameTestHelper h){
  var p=h.makeMockSurvivalPlayer();var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());FuelCanister.setFuelLevel(can,LargeFuelCanisterItem.getMaxFuelLevel());p.setItemInHand(InteractionHand.MAIN_HAND,can);var menu=new LargeFuelCanisterContainer(1,p.getInventory(),can,false);
  menu.handler.insertItem(0,new ItemStack(Items.COAL,10),false);h.assertTrue(menu.handler.getStackInSlot(0).getCount()==10,"full canister retains unused input");
  var owner=menu.slots.stream().filter(s->s.getItem()==can).findFirst().orElseThrow();menu.clicked(owner.index,0,ClickType.PICKUP,p);h.assertTrue(menu.getCarried().isEmpty()&&p.getMainHandItem()==can,"owner slot locked");
  p.setItemInHand(InteractionHand.MAIN_HAND,can.copy());h.assertTrue(!menu.stillValid(p)&&menu.quickMoveStack(p,0).isEmpty(),"equal-NBT replacement cannot use stale binding");menu.removed(p);h.assertTrue(p.getInventory().countItem(Items.COAL)==10&&menu.handler.getStackInSlot(0).isEmpty(),"unused fuel returned exactly once");menu.removed(p);h.assertTrue(p.getInventory().countItem(Items.COAL)==10,"repeat removal cannot duplicate fuel");h.succeed();
 }
 @GameTest(template="mineral_empty") public static void actualRecipePreservesFuelAndCannotDuplicateNormalCanisterRemainder(GameTestHelper h){
  var input=new TransientCraftingContainer(new AbstractContainerMenu(null,0){public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}},3,3);
  var can=new ItemStack(Registration.Fuel_Canister.get());FuelCanister.setFuelLevel(can,123456);FuelCanister.setBurnSpeed(can,3.5);can.setHoverName(net.minecraft.network.chat.Component.literal("Retained"));input.setItem(0,can);
  var ingot=BuiltInRegistries.ITEM.get(new net.minecraft.resources.ResourceLocation("justdirethings","eclipsealloy_ingot"));for(int i=1;i<=4;i++)input.setItem(i,new ItemStack(ingot,64));input.setItem(5,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(),64));
  var recipe=h.getLevel().getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("jdte","large_fuel_canister")).orElseThrow();
  @SuppressWarnings("unchecked") var crafting=(net.minecraft.world.item.crafting.CraftingRecipe)recipe;
  h.assertTrue(crafting.matches(input,h.getLevel()),"actual source ingredient recipe registered");var result=crafting.assemble(input,h.getLevel().registryAccess());
  h.assertTrue(result.is(JDTEItems.LARGE_FUEL_CANISTER.get())&&FuelCanister.getFuelLevel(result)==123456&&FuelCanister.getBurnSpeed(result)==3.5&&result.getHoverName().getString().equals("Retained")&&crafting.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty),"native fuel/speed/name preserved without returning a duplicate source canister");
  for(int i=2;i<=4;i++)input.setItem(i,ItemStack.EMPTY);input.setItem(1,new ItemStack(ingot,4));h.assertTrue(!crafting.matches(input,h.getLevel()),"one stack of four cannot craft by consuming only one ingot");h.succeed();
 }
}
