package com.jdte.common.items;

import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.*;
import com.jdte.common.entities.*;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class UltimateTimeWandGameTests {
    private static final BlockPos POS=new BlockPos(2,1,2),TARGET=new BlockPos(4,1,2);
    private static ItemStack wand(){return new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());}
    private static net.minecraftforge.energy.IEnergyStorage energy(ItemStack s){return s.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);}
    private static net.minecraftforge.fluids.capability.IFluidHandlerItem fluid(ItemStack s){return s.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);}
    private static void charge(ItemStack s){energy(s).receiveEnergy(Integer.MAX_VALUE,false);fluid(s).fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),1000),FluidAction.EXECUTE);}
    private static Player player(GameTestHelper h,ItemStack s){var p=h.makeMockSurvivalPlayer();p.setPos(Vec3.atCenterOf(h.absolutePos(POS.south(2))));p.setItemInHand(InteractionHand.MAIN_HAND,s);return p;}
    private static int rf(int multiplier){double base=Config.TIME_WAND_FE_COST.get()*JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get();return UltimateTimeWandData.saturatingEnergyCost(multiplier,(int)Math.min(Integer.MAX_VALUE,base));}
    private static double mb(int multiplier){return multiplier*Config.TIME_WAND_FLUID_COST.get()*JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get()/600d;}
    private static List<UltimateTimeWandEntity> markers(GameTestHelper h){return h.getLevel().getEntitiesOfClass(UltimateTimeWandEntity.class,new AABB(h.absolutePos(POS)),e->h.absolutePos(POS).equals(e.getTarget()));}
    private static FurnaceBlockEntity furnace(GameTestHelper h){markers(h).forEach(Entity::discard);h.setBlock(POS,Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT,true));var f=(FurnaceBlockEntity)h.getBlockEntity(POS);f.setItem(0,new ItemStack(Items.RAW_IRON,64));var tag=f.saveWithFullMetadata();tag.putShort("BurnTime",(short)1600);tag.putShort("CookTime",(short)0);tag.putShort("CookTimeTotal",(short)200);f.load(tag);return f;}
    private static InteractionResult click(GameTestHelper h,Player p,ItemStack s){return s.getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false)));}
    private static void frame(GameTestHelper h){ExtendedTimeAccelerationManager.onServerTickPost(new TickEvent.ServerTickEvent(TickEvent.Phase.END,()->true,h.getLevel().getServer()));}
    private static Cow cow(GameTestHelper h){for(var old:h.getLevel().getEntitiesOfClass(Cow.class,new AABB(h.absolutePos(TARGET)).inflate(.5)))old.discard();var c=h.spawn(EntityType.COW,TARGET);c.setNoAi(false);c.setAge(-10000);return c;}
    private static CraftingContainer grid(){return new TransientCraftingContainer(new AbstractContainerMenu(null,0){public boolean stillValid(Player p){return true;}public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}},2,2);}

    @GameTest(template="mineral_empty") public static void actualBlockActivationPricesMergeAndSaturation(GameTestHelper h){
        furnace(h);var s=wand();charge(s);var p=player(h,s);int before=energy(s).getEnergyStored();
        h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&markers(h).size()==1,"real request entity created once");
        var request=markers(h).get(0);h.assertTrue(request.getExponent()==1&&request.getRemainingTime()==JDTEConfig.COMMON.ultimateTimeWandDuration.get(),"source initial exponent and duration");
        h.assertTrue(energy(s).getEnergyStored()==before-rf(2)&&fluid(s).getFluidInTank(0).getAmount()==1000-(int)Math.floor(mb(2))&&Math.abs(UltimateTimeWandItem.pendingFluid(s)-(mb(2)-Math.floor(mb(2))))<1e-9,"exact initial FE and fractional time-fluid price");
        request.applyWandState(new UltimateTimeWandData.WandState(request.getTarget(),1,600,400));
        h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&markers(h).size()==1&&request.getExponent()==2&&request.getRemainingTime()==500,"reinforcement merges one marker and restores half elapsed");
        h.assertTrue(energy(s).getEnergyStored()==before-rf(2)-rf(4),"pay full new multiplier rather than only its delta");
        s.getOrCreateTag().putString(UltimateTimeWandItem.MODE,"max");h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&request.getExponent()==10,"source normal cap reached");
        var state=request.state();var item=s.save(new CompoundTag());h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&state.equals(request.state())&&item.equals(s.save(new CompoundTag())),"saturation spends and refreshes nothing");request.discard();h.succeed();
    }
    @GameTest(template="mineral_empty") public static void insufficientResourcesAndInvalidTargetsNeverPartPay(GameTestHelper h){
        furnace(h);var s=wand();var p=player(h,s);fluid(s).fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),100),FluidAction.EXECUTE);var before=s.save(new CompoundTag());
        h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&before.equals(s.save(new CompoundTag())),"insufficient FE cannot pre-spend fluid");
        energy(s).receiveEnergy(Integer.MAX_VALUE,false);fluid(s).drain(Integer.MAX_VALUE,FluidAction.EXECUTE);before=s.save(new CompoundTag());
        h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&before.equals(s.save(new CompoundTag())),"zero tank cannot fund positive fractional settlement");
        fluid(s).fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),100),FluidAction.EXECUTE);h.setBlock(POS,JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get());before=s.save(new CompoundTag());
        h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&before.equals(s.save(new CompoundTag())),"time accelerator machine is rejected before payment");h.setBlock(POS,Blocks.CHEST);h.assertTrue(click(h,p,s)==InteractionResult.FAIL,"non-ticking chest invalid");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void sourceAirModesBlockSneakAndCreative(GameTestHelper h){
        furnace(h);var s=wand();var p=player(h,s);p.setShiftKeyDown(true);var item=(UltimateTimeWandItem)s.getItem();
        for(var mode:List.of("x2","x4","max","normal")){h.assertTrue(item.use(h.getLevel(),p,InteractionHand.MAIN_HAND).getResult().consumesAction()&&UltimateTimeWandItem.getMode(s).serializedName().equals(mode),"air mode cycle "+mode);}
        p.getAbilities().instabuild=true;h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&markers(h).size()==1&&energy(s).getEnergyStored()==0&&fluid(s).getFluidInTank(0).isEmpty(),"sneak block accelerates, Creative bypasses resources");markers(h).forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="mineral_empty") public static void realBlockTicksAndNaturalExpiryLastSubmission(GameTestHelper h){
        var f=furnace(h);var request=new UltimateTimeWandEntity(h.getLevel(),h.absolutePos(POS),1,1);h.assertTrue(h.getLevel().addFreshEntity(request),"real one-tick request admitted");request.tick();request.tick();frame(h);
        h.assertTrue(request.isRemoved()&&request.expiredNaturally()&&f.saveWithoutMetadata().getShort("CookTime")==2,"natural final expiry submits exactly two real furnace ticks, repeated same-server-tick request is ignored");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void realRandomTickGrowthRefetchesCurrentBlockState(GameTestHelper h){
        markers(h).forEach(Entity::discard);h.setBlock(POS.below(),Blocks.SAND);h.setBlock(POS.below().east(),Blocks.WATER);h.setBlock(POS.above(),Blocks.AIR);
        h.setBlock(POS,Blocks.SUGAR_CANE.defaultBlockState().setValue(net.minecraft.world.level.block.SugarCaneBlock.AGE,14));
        var s=wand();charge(s);var p=player(h,s);int before=energy(s).getEnergyStored();h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS,"real vanilla random-tick block admits a paid request");markers(h).get(0).tick();frame(h);
        h.assertTrue(h.getBlockState(POS.above()).is(Blocks.SUGAR_CANE)&&h.getBlockState(POS).getValue(net.minecraft.world.level.block.SugarCaneBlock.AGE)==0,"two deterministic vanilla sugar-cane ticks use current age14 then15, grow once and reset, rather than reticking stale age14");
        h.assertTrue(energy(s).getEnergyStored()==before-rf(2),"random growth costs one full2x activation without per-subtick payment");markers(h).forEach(Entity::discard);
        var saved=com.direwolf20.justdirethings.setup.Config.TIME_WAND_BLOCK_BLACKLIST.get();var original=s.save(new CompoundTag());
        try{com.direwolf20.justdirethings.setup.Config.TIME_WAND_BLOCK_BLACKLIST.set(List.of("minecraft:sugar_cane"));h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&original.equals(s.save(new CompoundTag())),"Forge-configured random-block blacklist still rejects before resource payment");}finally{com.direwolf20.justdirethings.setup.Config.TIME_WAND_BLOCK_BLACKLIST.set(saved);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void canceledGenerationAndCallbackMutationAreAtomic(GameTestHelper h){
        furnace(h);var s=wand();charge(s);var p=player(h,s);var before=s.save(new CompoundTag());
        java.util.function.Consumer<EntityJoinLevelEvent> cancel=e->{if(e.getEntity() instanceof UltimateTimeWandEntity w&&h.absolutePos(POS).equals(w.getTarget()))e.setCanceled(true);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,EntityJoinLevelEvent.class,cancel);
        try{h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&before.equals(s.save(new CompoundTag())),"real Forge cancellation leaves both original resources/state unchanged");}finally{MinecraftForge.EVENT_BUS.unregister(cancel);}
        int original=energy(s).getEnergyStored();java.util.function.Consumer<EntityJoinLevelEvent> mutate=e->{if(e.getEntity() instanceof UltimateTimeWandEntity w&&h.absolutePos(POS).equals(w.getTarget()))energy(s).extractEnergy(7,false);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,EntityJoinLevelEvent.class,mutate);
        try{h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&energy(s).getEnergyStored()==original-7&&UltimateTimeWandItem.pendingFluid(s)==0,"callback's own seven FE remains; wand payment and spawned target roll back");}finally{MinecraftForge.EVENT_BUS.unregister(mutate);}
        h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&energy(s).getEnergyStored()==original-7-rf(2),"subsequent valid use charges exactly once");markers(h).forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="mineral_empty") public static void callbackHeldStackSwapAndTargetReplacementRollBack(GameTestHelper h){
        furnace(h);var s=wand();charge(s);var p=player(h,s);var before=s.save(new CompoundTag());
        java.util.function.Consumer<EntityJoinLevelEvent> swap=e->{if(e.getEntity() instanceof UltimateTimeWandEntity w&&h.absolutePos(POS).equals(w.getTarget()))p.setItemInHand(InteractionHand.MAIN_HAND,s.copy());};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,EntityJoinLevelEvent.class,swap);
        try{h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&before.equals(s.save(new CompoundTag()))&&before.equals(p.getMainHandItem().save(new CompoundTag())),"callback replacing the actual held stack cannot leave a free effect or charge an orphan stack");}finally{MinecraftForge.EVENT_BUS.unregister(swap);}
        p.setItemInHand(InteractionHand.MAIN_HAND,s);
        java.util.function.Consumer<EntityJoinLevelEvent> replace=e->{if(e.getEntity() instanceof UltimateTimeWandEntity w&&h.absolutePos(POS).equals(w.getTarget())){h.getLevel().removeBlockEntity(h.absolutePos(POS));h.getLevel().setBlockEntity(new FurnaceBlockEntity(h.absolutePos(POS),h.getBlockState(POS)));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,EntityJoinLevelEvent.class,replace);
        try{h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&markers(h).isEmpty()&&before.equals(s.save(new CompoundTag())),"callback replacing a ticker with another valid BE cancels the stale planned transaction without charge");}finally{MinecraftForge.EVENT_BUS.unregister(replace);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void recursiveActivationAndTwoPlayersDoNotDuplicateMarker(GameTestHelper h){
        furnace(h);var a=wand();var b=wand();charge(a);charge(b);var p=player(h,a);var other=player(h,b);var nested=new InteractionResult[1];
        java.util.function.Consumer<EntityJoinLevelEvent> reenter=e->{if(e.getEntity() instanceof UltimateTimeWandEntity w&&h.absolutePos(POS).equals(w.getTarget()))nested[0]=click(h,other,b);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,EntityJoinLevelEvent.class,reenter);
        try{h.assertTrue(click(h,p,a)==InteractionResult.SUCCESS&&nested[0]==InteractionResult.FAIL&&markers(h).size()==1&&energy(b).getEnergyStored()==JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get(),"nested join callback cannot activate a second unpaid request");}finally{MinecraftForge.EVENT_BUS.unregister(reenter);}
        h.assertTrue(click(h,other,b)==InteractionResult.SUCCESS&&markers(h).size()==1&&markers(h).get(0).getExponent()==2,"separate players merge serialized server state");
        h.assertTrue(energy(a).getEnergyStored()+energy(b).getEnergyStored()==2*JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get()-rf(2)-rf(4),"both legitimate activations pay their own exact price");markers(h).forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="mineral_empty") public static void entityUpgradeNaturalTicksDoNotRecurseThroughMachineExtraTicks(GameTestHelper h){
        var s=wand();charge(s);var p=player(h,s);var c=cow(h);var before=s.save(new CompoundTag());
        h.assertTrue(!UltimateTimeWandItem.applyToEntityTarget(h.getLevel(),p,s,c)&&before.equals(s.save(new CompoundTag())),"missing entity card cannot activate or charge");
        UltimateTimeWandItem.setEntityAcceleration(s,true);int energy=energy(s).getEnergyStored();h.assertTrue(UltimateTimeWandItem.applyToEntityTarget(h.getLevel(),p,s,c),"actual living effect activation");
        h.getLevel().tickNonPassenger(c);var data=EntityAccelerationManager.get(c);h.assertTrue(c.getAge()==-9998&&data.getRemainingTime()==599&&energy(s).getEnergyStored()==energy-rf(2),"natural one plus one paid extra tick; no per-tick double charge");
        EntityAccelerationManager.runAcceleratedTicks(c,3);h.assertTrue(c.getAge()==-9995&&EntityAccelerationManager.get(c).getRemainingTime()==599,"existing machine executor does not decrement/reapply wand timer");
        h.getLevel().tickNonPassenger(c);h.assertTrue(c.getAge()==-9994&&EntityAccelerationManager.get(c).getRemainingTime()==599,"same real-server-tick natural replay cannot apply wand twice");c.discard();h.succeed();
    }
    @GameTest(template="mineral_empty") public static void nonlivingEntitiesAndEntityExpiryUseSameExecutor(GameTestHelper h){
        var s=wand();charge(s);UltimateTimeWandItem.setEntityAcceleration(s,true);var p=player(h,s);var t=h.spawn(EntityType.TNT,TARGET);t.setFuse(100000);t.setNoGravity(true);t.setDeltaMovement(Vec3.ZERO);
        h.assertTrue(UltimateTimeWandItem.applyToEntityTarget(h.getLevel(),p,s,t),"source nonliving target supported");h.getLevel().tickNonPassenger(t);h.assertTrue(t.getFuse()==99998&&EntityAccelerationManager.get(t).getRemainingTime()==599,"real TNT timer receives natural plus one extra tick");
        var c=cow(h);EntityAccelerationManager.set(c,new EntityAccelerationData(10,600,1));h.getLevel().tickNonPassenger(c);h.assertTrue(c.getAge()==-9999&&EntityAccelerationManager.get(c)==null,"source final entity expiry removes effect before another boosted batch");t.discard();c.discard();h.succeed();
    }
    @GameTest(template="mineral_empty") public static void upgradeCraftAndSmithingRetainEveryWandDatum(GameTestHelper h){
        var s=wand();charge(s);s.setHoverName(net.minecraft.network.chat.Component.literal("Saved wand"));s.getOrCreateTag().putString(UltimateTimeWandItem.MODE,"x4");s.getOrCreateTag().putDouble(UltimateTimeWandItem.PENDING_FLUID,.375);s.getOrCreateTag().putString("other_mod_link","preserve");var before=s.getTag().copy();
        var input=grid();input.setItem(0,s);input.setItem(1,new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()));input.setItem(2,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()));var recipe=(CraftingRecipe)h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("ultimate_time_wand_entity_upgrade")).orElseThrow();
        h.assertTrue(recipe.matches(input,h.getLevel()),"source simultaneous crafting attachment");var result=recipe.assemble(input,h.getLevel().registryAccess());var expected=before.copy();expected.putBoolean(UltimateTimeWandItem.ENTITY_UPGRADE,true);expected.putBoolean(UltimateTimeWandItem.ULTIMATE_OVERCLOCK,true);h.assertTrue(result.getTag().equals(expected)&&s.getTag().equals(before)&&UltimateTimeWandItem.getMaxExponent(result)==15,"exact NBT plus two flags, original unmutated");input.setItem(0,result);h.assertTrue(!recipe.matches(input,h.getLevel()),"already applied flags cannot consume extra cards");
        var smith=(SmithingRecipe)h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("entity_acceleration_upgrade_apply")).orElseThrow();var smithInput=new SimpleContainer(new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()),s.copy(),new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));h.assertTrue(smith.matches(smithInput,h.getLevel()),"real source template/base/catalyst order");var smithed=smith.assemble(smithInput,h.getLevel().registryAccess());var one=before.copy();one.putBoolean(UltimateTimeWandItem.ENTITY_UPGRADE,true);h.assertTrue(smithed.getTag().equals(one),"smithing preserves every original NBT field");smithInput.getItem(2).grow(1);h.assertTrue(!smith.matches(smithInput,h.getLevel()),"source smithing demands exactly one catalyst");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void realSmithingMenuConsumesOneSetAndCannotTakeTwice(GameTestHelper h){
        h.setBlock(POS,Blocks.SMITHING_TABLE);var s=wand();charge(s);var p=player(h,s);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var menu=new SmithingMenu(3,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),h.absolutePos(POS)));
        menu.getSlot(0).set(new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()));menu.getSlot(1).set(s);menu.getSlot(2).set(new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));menu.slotsChanged(new SimpleContainer(0));
        h.assertTrue(menu.getSlot(3).getItem().is(JDTEItems.ULTIMATE_TIME_WAND.get()),"actual vanilla smithing menu has the upgraded result");menu.quickMoveStack(p,3);menu.quickMoveStack(p,3);
        var outputs=p.getInventory().items.stream().filter(i->i.is(JDTEItems.ULTIMATE_TIME_WAND.get())).toList();h.assertTrue(outputs.size()==1&&UltimateTimeWandItem.hasEntityAcceleration(outputs.get(0))&&energy(outputs.get(0)).getEnergyStored()==energy(s).getEnergyStored()&&menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty()&&menu.getSlot(2).getItem().isEmpty(),"one real output, one of each consumed, no repeated-take duplication");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void capabilitiesPersistenceInvalidFluidAndPermissionBounds(GameTestHelper h){
        furnace(h);var s=wand();charge(s);h.assertTrue(energy(s).getMaxEnergyStored()==10000000&&fluid(s).getTankCapacity(0)==800000&&fluid(s).fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),FluidAction.EXECUTE)==0,"source actual FE/Time Fluid capacities and illegal fluid rejection");var copy=ItemStack.of(s.save(new CompoundTag()));h.assertTrue(energy(copy).getEnergyStored()==energy(s).getEnergyStored()&&fluid(copy).getFluidInTank(0).isFluidStackIdentical(fluid(s).getFluidInTank(0)),"real item NBT round trip");
        var p=player(h,s);var before=s.save(new CompoundTag());p.getAbilities().mayBuild=false;h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&before.equals(s.save(new CompoundTag())),"permission denial cannot charge");p.getAbilities().mayBuild=true;p.setPos(p.position().add(100,0,0));h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&before.equals(s.save(new CompoundTag())),"out-of-reach cannot charge");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void loadedProxyCyclesAndUnloadedTargetsAreRejected(GameTestHelper h){
        var f=furnace(h);var pos=h.absolutePos(POS);class Proxy extends FurnaceBlockEntity implements TimeAccelerationTargetProxy{Proxy(){super(pos,f.getBlockState());}public TimeAccelerationTarget getTimeAccelerationTarget(){return new TimeAccelerationTarget(h.getLevel(),pos);}}var proxy=new Proxy();h.getLevel().setBlockEntity(proxy);var s=wand();charge(s);var p=player(h,s);var before=s.save(new CompoundTag());h.assertTrue(click(h,p,s)==InteractionResult.FAIL&&before.equals(s.save(new CompoundTag())),"proxy cycle rejects before payment");
        var far=new BlockPos(1000000,100,1000000);p.setPos(Vec3.atCenterOf(far.south(2)));var ctx=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(far),Direction.UP,far,false));h.assertTrue(!h.getLevel().hasChunkAt(far)&&s.getItem().useOn(ctx)==InteractionResult.FAIL&&!h.getLevel().hasChunkAt(far)&&before.equals(s.save(new CompoundTag())),"unloaded target stays unloaded and uncharged");h.succeed();
    }

    @GameTest(template="mineral_empty") public static void sourceUltimateCapAndRoundedFluidSettingAreActual(GameTestHelper h){
        furnace(h);var s=wand();charge(s);var p=player(h,s);UltimateTimeWandItem.setUltimateOverclock(s,true);s.getOrCreateTag().putString(UltimateTimeWandItem.MODE,"max");int energy=energy(s).getEnergyStored();
        h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS&&markers(h).get(0).getExponent()==15&&energy(s).getEnergyStored()==energy-rf(32768),"real Ultimate attachment reaches32768x and pays full configured FE");markers(h).forEach(Entity::discard);
        var rounded=wand();charge(rounded);var other=player(h,rounded);boolean prior=JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get();
        try{JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.set(false);h.assertTrue(click(h,other,rounded)==InteractionResult.SUCCESS&&fluid(rounded).getFluidInTank(0).getAmount()==1000-(int)Math.ceil(mb(2))&&UltimateTimeWandItem.pendingFluid(rounded)==0,"disabled fractional mode rounds activation up without saving debt");}finally{JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.set(prior);markers(h).forEach(Entity::discard);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void sharedAreaAndWandRequestsSumInOneQueue(GameTestHelper h){
        var f=furnace(h);var source=POS.west();h.setBlock(source,JDTEBlocks.BASIC_TIME_ACCELERATOR.get());var a=new BasicTimeAcceleratorBE(h.absolutePos(source),h.getBlockState(source)){@Override public AABB getAABB(BlockPos p){return new AABB(h.absolutePos(POS));}};h.getLevel().setBlockEntity(a);a.getFluidTank().setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),1000));
        var s=wand();charge(s);var p=player(h,s);h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS,"wand valid alongside area accelerator");a.tickServer();markers(h).get(0).tick();frame(h);
        h.assertTrue(f.saveWithoutMetadata().getShort("CookTime")==a.getEffectiveMultiplier()+2,"exact sum from independently paid area and wand contributors, no competing scheduler");h.assertTrue(a.saveWithoutMetadata().getDouble("pendingFluidCost")>0||a.getFluidTank().getFluidAmount()<1000,"area source pays its own fluid");markers(h).forEach(Entity::discard);h.getLevel().destroyBlock(a.getBlockPos(),false);h.succeed();
    }
    @GameTest(template="mineral_empty") public static void realCoalescedBioUltimateWorkAndMachineResources(GameTestHelper h){
        markers(h).forEach(Entity::discard);h.setBlock(POS,JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get());var b=(BioExtractorBE)h.getBlockEntity(POS);var upgrades=com.jdte.common.upgrades.UpgradeHelper.getUpgradeHandler(b);upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);upgrades.insertItem(1,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false);b.getEnergyStorage().setEnergy(b.getMaxEnergy());b.getAreaAffectingData().xRadius=2;b.getAreaAffectingData().yRadius=2;b.getAreaAffectingData().zRadius=2;b.getAreaAffectingData().xOffset=0;b.getAreaAffectingData().yOffset=0;b.getAreaAffectingData().zOffset=0;var c=cow(h);
        var s=wand();charge(s);var p=player(h,s);int machineEnergy=b.getEnergyStorage().getEnergyStored();h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS,"coalesced real production machine accepts wand");markers(h).get(0).tick();frame(h);
        h.assertTrue(b.getFluidTank().getFluidAmount()==1000&&b.getEnergyStorage().getEnergyStored()==machineEnergy-250000&&b.getScanProgress()==0,"two wand ticks times ten Ultimate machine executions produce exactly one shared-cooldown milk batch at source50x FE");markers(h).forEach(Entity::discard);c.discard();h.succeed();
    }
    @GameTest(template="mineral_empty") public static void savedEntityEffectAndRealDimensionTransferDeduplicate(GameTestHelper h){
        var c=cow(h);EntityAccelerationManager.set(c,new EntityAccelerationData(2,600,423));var tag=c.saveWithoutId(new CompoundTag());c.discard();var restored=new Cow(EntityType.COW,h.getLevel());restored.load(tag);h.getLevel().addFreshEntity(restored);
        h.assertTrue(EntityAccelerationManager.get(restored).getRemainingTime()==423&&EntityAccelerationManager.get(restored).getExponent()==2,"real Forge persistent entity NBT retains the purchased timed effect");h.getLevel().tickNonPassenger(restored);h.assertTrue(restored.getAge()==-9996&&EntityAccelerationManager.get(restored).getRemainingTime()==422,"natural replay after load gives one paid3x-extra batch");
        var destination=h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);var moved=restored.changeDimension(destination,new net.minecraftforge.common.util.ITeleporter(){@Override public net.minecraft.world.level.portal.PortalInfo getPortalInfo(Entity e,net.minecraft.server.level.ServerLevel l,java.util.function.Function<net.minecraft.server.level.ServerLevel,net.minecraft.world.level.portal.PortalInfo> fallback){return new net.minecraft.world.level.portal.PortalInfo(new Vec3(0,100,0),Vec3.ZERO,0,0);}});
        h.assertTrue(moved instanceof Cow&&EntityAccelerationManager.get(moved)!=null&&EntityAccelerationManager.get(moved).getRemainingTime()==422,"real dimension transfer preserves Forge timed NBT");destination.tickNonPassenger(moved);h.assertTrue(((Cow)moved).getAge()==-9995&&EntityAccelerationManager.get(moved).getRemainingTime()==422,"same real tick across dimensions cannot apply/decrement twice");moved.discard();h.succeed();
    }
    @GameTest(template="mineral_empty") public static void savedBlockRequestAndDimensionBindingAreValidated(GameTestHelper h){
        var f=furnace(h);var s=wand();charge(s);var p=player(h,s);h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS,"real block request");var original=markers(h).get(0);original.applyWandState(new UltimateTimeWandData.WandState(h.absolutePos(POS),5,600,321));var tag=original.saveWithoutId(new CompoundTag());original.discard();var restored=new UltimateTimeWandEntity(JDTEEntities.ULTIMATE_TIME_WAND.get(),h.getLevel());restored.load(tag);h.assertTrue(restored.getExponent()==5&&restored.getTotalTime()==600&&restored.getRemainingTime()==321&&restored.getTarget().equals(h.absolutePos(POS)),"complete actual request NBT round trip");
        tag.putString("targetDimension","minecraft:the_nether");var wrong=new UltimateTimeWandEntity(JDTEEntities.ULTIMATE_TIME_WAND.get(),h.getLevel());wrong.load(tag);h.getLevel().addFreshEntity(wrong);wrong.tick();frame(h);h.assertTrue(wrong.isRemoved()&&f.saveWithoutMetadata().getShort("CookTime")==0,"cross-dimension block marker cannot accelerate a different world's matching position");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void perFrameBudgetAndLoadedProxyRoutingAreReal(GameTestHelper h){
        var f=furnace(h);var proxyPos=POS.south();h.setBlock(proxyPos,Blocks.FURNACE);class Proxy extends FurnaceBlockEntity implements TimeAccelerationTargetProxy{Proxy(){super(h.absolutePos(proxyPos),h.getBlockState(proxyPos));}public TimeAccelerationTarget getTimeAccelerationTarget(){return new TimeAccelerationTarget(h.getLevel(),h.absolutePos(POS));}}var proxy=new Proxy();h.getLevel().setBlockEntity(proxy);
        var resolved=UltimateTimeWandTargetRuntime.resolve(h.getLevel(),proxy.getBlockPos()).orElseThrow();h.assertTrue(resolved.location().pos().equals(f.getBlockPos())&&resolved.blockEntity()==f,"loaded proxy resolves to actual furnace");
        var s=wand();charge(s);var p=player(h,s);s.getOrCreateTag().putString(UltimateTimeWandItem.MODE,"max");h.assertTrue(click(h,p,s)==InteractionResult.SUCCESS,"large requested work");int prior=JDTEConfig.COMMON.timeAcceleratorMaxExecutionsPerTick.get();boolean all=JDTEConfig.SERVER.timeAcceleratorAccelerateAllMachines.get();
        try{JDTEConfig.SERVER.timeAcceleratorAccelerateAllMachines.set(false);JDTEConfig.COMMON.timeAcceleratorMaxExecutionsPerTick.set(1);markers(h).get(0).tick();frame(h);h.assertTrue(f.saveWithoutMetadata().getShort("CookTime")==1,"real shared frame obeys one-execution configured budget");}finally{JDTEConfig.COMMON.timeAcceleratorMaxExecutionsPerTick.set(prior);JDTEConfig.SERVER.timeAcceleratorAccelerateAllMachines.set(all);markers(h).forEach(Entity::discard);}h.succeed();
    }
}
