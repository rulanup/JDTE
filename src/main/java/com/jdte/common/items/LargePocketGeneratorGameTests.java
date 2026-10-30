package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.*;
import com.direwolf20.justdirethings.setup.*;
import com.jdte.common.containers.*;
import com.jdte.common.containers.handlers.LargePocketGeneratorHandler;
import com.jdte.common.recipes.LargePortableContainerRecipe;
import com.jdte.setup.JDTEItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.gametest.GameTestHolder;

@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
@GameTestHolder("jdte")
public final class LargePocketGeneratorGameTests {
    private static ItemStack fresh() { return new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get()); }
    private static LargePocketGeneratorItem item() { return JDTEItems.LARGE_POCKET_GENERATOR.get(); }
    private static int ticks(ItemStack s) { return s.hasTag()?s.getTag().getInt(PocketGenerator.COUNTER):0; }
    @GameTest(template="mineral_empty") public static void realCapabilityIsNoReceiveDynamicAndSimulationReadOnly(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);var before=s.save(new CompoundTag());
        h.assertTrue(e.getMaxEnergyStored()==PocketGeneratorMath.multiply(Config.POCKET_GENERATOR_MAX_FE.get(),4)&&!e.canReceive()&&e.receiveEnergy(100,false)==0,"source fourfold native configured capacity and output-only FE");
        h.assertTrue(e.forceReceiveEnergy(100,true)==100&&s.save(new CompoundTag()).equals(before),"generation simulation does not write NBT");
        e.forceReceiveEnergy(100,false);s.getOrCreateTag().putInt("Energy",50);h.assertTrue(e.getEnergyStored()==50&&e.extractEnergy(-1,false)==0&&e.forceReceiveEnergy(-1,false)==0,"existing native-compatible capability reads current owner NBT; no negative FE");
        var copy=ItemStack.of(s.save(new CompoundTag()));h.assertTrue(LargePocketGeneratorItem.energy(copy).getEnergyStored()==50&&e.extractEnergy(20,true)==20&&e.getEnergyStored()==50,"item save/copy and extraction simulation preserve resource");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void coalProducesExactSourceWholeTickBudget(GameTestHelper h) {
        var s=fresh();var f=LargePocketGeneratorItem.fuel(s);var coal=new ItemStack(Items.COAL,1);var original=s.save(new CompoundTag());
        h.assertTrue(f.insertItem(0,coal,true).isEmpty()&&s.save(new CompoundTag()).equals(original),"simulated insertion is side-effect free");
        f.insertItem(0,coal,false);int count=1600/item().getBurnSpeedMultiplier(s),rate=item().productionRate(s);
        for(int i=0;i<count;i++)item().tryBurn(LargePocketGeneratorItem.energy(s),s);
        h.assertTrue(LargePocketGeneratorItem.energy(s).getEnergyStored()==count*rate&&ticks(s)==0&&f.getStackInSlot(0).isEmpty()&&coal.getCount()==1,"one owned coal consumed for exact source FE, caller untouched and burn exhausted");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void fullBufferNeverSpendsFuelAndPartialLastSpaceMatchesSource(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);var f=LargePocketGeneratorItem.fuel(s);f.insertItem(0,new ItemStack(Items.COAL,2),false);e.forceReceiveEnergy(Integer.MAX_VALUE,false);var before=s.save(new CompoundTag());
        item().tryBurn(e,s);h.assertTrue(s.save(new CompoundTag()).equals(before),"full storage never initializes or consumes fuel");
        e.extractEnergy(1,false);item().tryBurn(e,s);int remaining=ticks(s);h.assertTrue(e.getEnergyStored()==e.getMaxEnergyStored()&&remaining>0&&f.getStackInSlot(0).getCount()==1,"source clips to last one FE of space and spends one whole burn tick");
        item().tryBurn(e,s);h.assertTrue(ticks(s)==remaining,"full storage pauses existing burn counter");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void sourceLastTickPrefetchPreservesFutureFuelCredit(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);LargePocketGeneratorItem.fuel(s).insertItem(0,new ItemStack(Items.COAL,2),false);s.getOrCreateTag().putInt(PocketGenerator.COUNTER,1);s.getOrCreateTag().putInt(PocketGenerator.MAXBURN,1);item().tryBurn(e,s);
        h.assertTrue(e.getEnergyStored()==item().productionRate(s)&&ticks(s)==1600/item().getBurnSpeedMultiplier(s)&&LargePocketGeneratorItem.fuel(s).getStackInSlot(0).getCount()==1,"source preloads next fuel after last tick, with full future credit and no second FE production");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void tooShortFuelRejectsWithoutChangingMultiplierOrInventory(GameTestHelper h) {
        int old=Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER.get();try {Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER.set(1000);var s=fresh();LargePocketGeneratorItem.fuel(s).insertItem(0,new ItemStack(Items.STICK),false);var before=s.save(new CompoundTag());item().tryBurn(LargePocketGeneratorItem.energy(s),s);h.assertTrue(s.save(new CompoundTag()).equals(before),"zero whole burn ticks cannot consume fuel or alter a multiplier");}finally{Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER.set(old);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void fuelRemainderAndIllegalInputsAreConserved(GameTestHelper h) {
        var s=fresh();var f=LargePocketGeneratorItem.fuel(s);h.assertTrue(f.insertItem(0,new ItemStack(Items.STONE),false).is(Items.STONE),"nonfuel insertion rejected");
        var offered=new ItemStack(Items.LAVA_BUCKET,2);var rest=f.insertItem(0,offered,false);h.assertTrue(rest.getCount()==1&&offered.getCount()==2,"container fuels reserve one item, never delete stacked containers");item().tryBurn(LargePocketGeneratorItem.energy(s),s);
        h.assertTrue(f.getStackInSlot(0).is(Items.BUCKET)&&f.extractItem(0,1,false).is(Items.BUCKET),"real crafting remainder persists and is extractable");
        var custom=new ItemStack(Registration.CoalBlock_T1_ITEM.get());h.assertTrue(PortableFuelBurnSpeedHelper.resolveBurnTime(custom)>0&&f.insertItem(0,custom,false).isEmpty(),"pinned coal block fallback is usable in the actual handler");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void bothFuelCanistersPreserveRemaindersAndResolvedSpeed(GameTestHelper h) {
        for(boolean large:new boolean[]{false,true}){var s=fresh();var can=new ItemStack(large?JDTEItems.LARGE_FUEL_CANISTER.get():Registration.Fuel_Canister.get());FuelCanister.setFuelLevel(can,large?LargeFuelCanisterItem.getMinimumFuelConsumed()*2:Config.FUEL_CANISTER_MINIMUM_TICKS_CONSUMED.get()*2);FuelCanister.setBurnSpeed(can,2D);int before=FuelCanister.getFuelLevel(can),unit=PortableFuelBurnSpeedHelper.resolveBurnTime(can),speed=PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(can);LargePocketGeneratorItem.fuel(s).insertItem(0,can,false);item().tryBurn(LargePocketGeneratorItem.energy(s),s);var after=LargePocketGeneratorItem.fuel(s).getStackInSlot(0);h.assertTrue(FuelCanister.getFuelLevel(after)==before-unit&&item().getFuelMultiplier(s)==speed&&LargePocketGeneratorItem.energy(s).getEnergyStored()==item().productionRate(s),"one exact canister fuel unit, source resolved multiplier and native remainder");}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void nativePocketAcceptsLargeFuelMultiplierWithoutChangingNativeCapacity(GameTestHelper h) {
        var s=new ItemStack(Registration.Pocket_Generator.get());var nativeItem=(PocketGenerator)s.getItem();var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());FuelCanister.setBurnSpeed(can,2D);FuelCanister.setFuelLevel(can,LargeFuelCanisterItem.getMinimumFuelConsumed()*2);var inv=new net.minecraftforge.items.ItemStackHandler(1);inv.setStackInSlot(0,can);s.getOrCreateTag().put("FuelInventory",inv.serializeNBT());var e=s.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);nativeItem.tryBurn(e,s);h.assertTrue(nativeItem.getFuelMultiplier(s)==20&&e.getMaxEnergyStored()==Config.POCKET_GENERATOR_MAX_FE.get(),"actual native initBurn redirect resolves large speed; native capacity unchanged: multiplier="+nativeItem.getFuelMultiplier(s)+", capacity="+e.getMaxEnergyStored()+", configured="+Config.POCKET_GENERATOR_MAX_FE.get()+", tag="+s.getTag());h.succeed();
    }
    @GameTest(template="mineral_empty") public static void sharedFuelViewsCannotRestoreBurnedFuelAndOldMenuIsInert(GameTestHelper h) {
        var p=h.makeMockSurvivalPlayer();var s=fresh();p.setItemInHand(InteractionHand.MAIN_HAND,s);var menu=new LargePocketGeneratorContainer(1,p.getInventory(),s,false);p.containerMenu=menu;menu.setCarried(new ItemStack(Items.COAL,2));menu.clicked(0,0,ClickType.PICKUP,p);var other=new LargePocketGeneratorHandler(s,true);item().tryBurn(LargePocketGeneratorItem.energy(s),s);
        h.assertTrue(menu.handler.getStackInSlot(0).getCount()==1&&other.extractItem(0,64,false).getCount()==1&&menu.handler.getStackInSlot(0).isEmpty(),"menu, automation and real production share canonical fuel, not snapshots");
        var replacement=s.copy();p.setItemInHand(InteractionHand.MAIN_HAND,replacement);menu.setCarried(new ItemStack(Items.COAL));menu.clicked(0,0,ClickType.PICKUP,p);h.assertTrue(!menu.stillValid(p)&&LargePocketGeneratorItem.fuel(s).getStackInSlot(0).isEmpty()&&menu.getCarried().is(Items.COAL),"replaced item's old menu cannot consume carried fuel");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void realInventoryChargingDisabledAndDuplicateTickAreSafe(GameTestHelper h) {
        var p=h.makeMockSurvivalPlayer();var s=fresh();var target=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());p.getInventory().setItem(0,s);p.getInventory().setItem(1,target);var e=LargePocketGeneratorItem.energy(s);e.forceReceiveEnergy(6000,false);item().setEnabled(s,false);var before=s.save(new CompoundTag());item().inventoryTick(s,h.getLevel(),p,0,true);h.assertTrue(s.save(new CompoundTag()).equals(before),"disabled generator never generates or charges");item().setEnabled(s,true);item().inventoryTick(s,h.getLevel(),p,0,true);item().inventoryTick(s,h.getLevel(),p,0,true);
        h.assertTrue(target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()==5000&&e.getEnergyStored()==1000,"actual inventory charge transfers5000 only once per real server tick");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void smallerActualAcceptanceAndRejectedTargetsDoNotLoseFe(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);e.forceReceiveEnergy(1000,false);var target=new EnergyStorage(10000){@Override public int receiveEnergy(int n,boolean simulate){return super.receiveEnergy(simulate?n:Math.min(123,n),simulate);}};
        h.assertTrue(e.transferTo(target,5000)==123&&e.getEnergyStored()==877&&target.getEnergyStored()==123,"actual acceptance smaller than simulation is refunded exactly");var full=new EnergyStorage(0);h.assertTrue(e.transferTo(full,5000)==0&&e.transferTo(e,5000)==0&&e.getEnergyStored()==877,"full/self targets do not debit");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void malformedNbtAndOffThreadMutationsCannotProduceFe(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);s.getOrCreateTag().putInt("Energy",-1);h.assertTrue(e.getEnergyStored()==0,"negative saved FE reads zero");s.getOrCreateTag().putInt("Energy",Integer.MAX_VALUE);h.assertTrue(e.getEnergyStored()==e.getMaxEnergyStored(),"overcapacity NBT is bounded");var before=s.save(new CompoundTag());java.util.concurrent.CompletableFuture.runAsync(()->{e.extractEnergy(100,false);e.forceReceiveEnergy(100,false);LargePocketGeneratorItem.fuel(s).insertItem(0,new ItemStack(Items.COAL),false);}).join();h.assertTrue(s.save(new CompoundTag()).equals(before),"wrong-thread capability calls cannot mutate FE or fuel");h.assertTrue(!s.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent(),"no invented fluid interface");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void nativeTransmitterActuallyBurnsLargePocketWithoutInvalidCast(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(pos,Registration.EnergyTransmitter.get().defaultBlockState());var be=(com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE)h.getLevel().getBlockEntity(pos);var s=fresh();LargePocketGeneratorItem.fuel(s).insertItem(0,new ItemStack(Items.COAL),false);be.getMachineHandler().setStackInSlot(0,s);be.getEnergyStorage().setEnergy(0);be.drainFromSlot();h.assertTrue(be.getEnergyStorage().getRealEnergyStored()+LargePocketGeneratorItem.energy(s).getEnergyStored()==item().productionRate(s)&&ticks(s)==1600/item().getBurnSpeedMultiplier(s)-1,"actual native transmitter accepts newly generated FE with exact one-tick ledger, no unrelated storage cast");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void preservingRecipeAndWireRetainAllNativeState(GameTestHelper h) {
        var nativeStack=new ItemStack(Registration.Pocket_Generator.get());nativeStack.setHoverName(net.minecraft.network.chat.Component.literal("Keep portable name"));var tag=nativeStack.getOrCreateTag();tag.putInt("Energy",12345);tag.putInt(PocketGenerator.COUNTER,29);tag.putInt(PocketGenerator.MAXBURN,40);tag.putInt(PocketGenerator.FUELMULTIPLIER,2);tag.putBoolean("enabled",false);var inv=new net.minecraftforge.items.ItemStackHandler(1);inv.setStackInSlot(0,new ItemStack(Items.COAL,17));tag.put("FuelInventory",inv.serializeNBT());
        var player=h.makeMockSurvivalPlayer();var grid=new TransientCraftingContainer(new AbstractContainerMenu(null,1){public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}},3,3);grid.setItem(0,nativeStack);for(int i=1;i<=4;i++)grid.setItem(i,new ItemStack(Registration.EclipseAlloyIngot.get()));grid.setItem(5,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));var recipe=new LargePortableContainerRecipe(new ResourceLocation("jdte","large_pocket_generator"),Registration.Pocket_Generator.get(),JDTEItems.LARGE_POCKET_GENERATOR.get());var result=recipe.assemble(grid,h.getLevel().registryAccess());h.assertTrue(result.is(JDTEItems.LARGE_POCKET_GENERATOR.get())&&result.getTag().equals(nativeStack.getTag())&&LargePocketGeneratorItem.energy(result).getEnergyStored()==12345&&LargePocketGeneratorItem.fuel(result).getStackInSlot(0).getCount()==17,"recipe carries FE/native counter/multiplier/enabled/name/fuel exactly");var buf=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{((LargePortableContainerRecipe.Serializer)recipe.getSerializer()).toNetwork(buf,recipe);var decoded=((LargePortableContainerRecipe.Serializer)recipe.getSerializer()).fromNetwork(recipe.getId(),buf);h.assertTrue(decoded.getResultItem(h.getLevel().registryAccess()).is(JDTEItems.LARGE_POCKET_GENERATOR.get()),"portable recipe wire preserves both item IDs");}finally{buf.release();}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void sourceAlsoChargesActualCuriosRecipients(GameTestHelper h) {
        var p=h.makeMockSurvivalPlayer();var s=fresh();p.getInventory().setItem(0,s);var e=LargePocketGeneratorItem.energy(s);e.forceReceiveEnergy(6000,false);
        var inv=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(AssertionError::new);
        var slot=new top.theillusivec4.curios.common.inventory.CurioStacksHandler(inv,"jdte_pocket_test",1,true,false,true,top.theillusivec4.curios.api.type.capability.ICurio.DropRule.DEFAULT);
        var slots=new java.util.LinkedHashMap<>(inv.getCurios());slots.put("jdte_pocket_test",slot);inv.setCurios(slots);
        var target=new ItemStack(Registration.EclipseAlloyPickaxe.get());slot.getStacks().setStackInSlot(0,target);item().inventoryTick(s,h.getLevel(),p,0,true);
        h.assertTrue(target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new).getEnergyStored()==5000&&e.getEnergyStored()==1000,"real Curios recipient is charged after vanilla inventory under pinned Neo parent behavior");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void maximumConfiguredRateAndCapacityDoNotOverflow(GameTestHelper h) {
        int cap=Config.POCKET_GENERATOR_MAX_FE.get(), rate=Config.POCKET_GENERATOR_FE_PER_FUEL_TICK.get();
        try{Config.POCKET_GENERATOR_MAX_FE.set(Integer.MAX_VALUE);Config.POCKET_GENERATOR_FE_PER_FUEL_TICK.set(Integer.MAX_VALUE);var s=fresh();LargePocketGeneratorItem.fuel(s).insertItem(0,new ItemStack(Items.COAL),false);var e=LargePocketGeneratorItem.energy(s);item().tryBurn(e,s);h.assertTrue(e.getMaxEnergyStored()==Integer.MAX_VALUE&&e.getEnergyStored()==Integer.MAX_VALUE&&ticks(s)==399,"actual native-compatible capability saturates capacity/rate and retains exact burn tick");var before=s.save(new CompoundTag());item().tryBurn(e,s);h.assertTrue(s.save(new CompoundTag()).equals(before),"overflow boundary full buffer cannot spend another fuel tick");}finally{Config.POCKET_GENERATOR_MAX_FE.set(cap);Config.POCKET_GENERATOR_FE_PER_FUEL_TICK.set(rate);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void throwingReceiverRefundsReservedFeAndPropagatesFailure(GameTestHelper h) {
        var s=fresh();var e=LargePocketGeneratorItem.energy(s);e.forceReceiveEnergy(1000,false);var target=new EnergyStorage(10000){@Override public int receiveEnergy(int n,boolean simulate){if(!simulate)throw new IllegalStateException("intentional no-accept failure");return super.receiveEnergy(n,true);}};
        boolean thrown=false;try{e.transferTo(target,5000);}catch(IllegalStateException failure){thrown=true;}
        h.assertTrue(thrown&&e.getEnergyStored()==1000&&target.getEnergyStored()==0,"failure before receiver mutation propagates and refunds exact reserved FE");h.succeed();
    }

}
