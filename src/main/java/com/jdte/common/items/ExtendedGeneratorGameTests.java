package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.*;
import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.*;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ExtendedGeneratorGameTests {
    private static final BlockPos POS=new BlockPos(2,1,2);
    private static Fluid fuel() { return BuiltInRegistries.FLUID.stream().filter(f->f instanceof RefinedFuel r&&r.fePerMb()>0).findFirst().orElseThrow(IllegalStateException::new); }
    private static BaseMachineBE place(GameTestHelper h,boolean fluid) {
        h.setBlock(POS,Blocks.AIR);h.setBlock(POS,fluid?JDTEBlocks.EXTENDED_FLUID_GENERATOR.get():JDTEBlocks.EXTENDED_GENERATOR.get());
        return (BaseMachineBE)h.getBlockEntity(POS);
    }
    private static UseOnContext context(GameTestHelper h,ItemStack card) {
        var p=h.makeMockSurvivalPlayer();p.setItemInHand(InteractionHand.MAIN_HAND,card);
        return new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false));
    }
    @GameTest(template="mineral_empty")
    public static void solidFuelAutomationAndEightPhysicalUpgradeSlots(GameTestHelper h) {
        var be=(ExtendedGeneratorBE)place(h,false);
        h.assertTrue(!be.getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING),"native generator has no directional state");
        var items=be.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(IllegalStateException::new);
        var offered=new ItemStack(Items.COAL,64);
        h.assertTrue(items.insertItem(0,new ItemStack(Items.DIRT),false).is(Items.DIRT)&&be.getMachineHandler().getStackInSlot(0).isEmpty(),"invalid solid fuel rejected by automation");
        h.assertTrue(items.insertItem(0,offered,true).isEmpty()&&be.getMachineHandler().getStackInSlot(0).isEmpty(),"simulation is inert");
        h.assertTrue(items.insertItem(0,offered,false).isEmpty()&&offered.getCount()==64&&items.insertItem(0,new ItemStack(Items.COAL),false).getCount()==1,"whole stacks, caller ownership and full slot");
        h.assertTrue(items.extractItem(0,7,false).getCount()==7&&be.getMachineHandler().getStackInSlot(0).getCount()==57,"automation extraction uses actual inventory");
        var u=UpgradeHelper.getUpgradeHandler(be);
        var cards=List.of(JDTEItems.CAPACITY_UPGRADE.get(),JDTEItems.CAPACITY_UPGRADE.get(),JDTEItems.CAPACITY_UPGRADE.get(),JDTEItems.GENERATOR_UPGRADE.get(),JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(),JDTEItems.AE_OUTPUT_UPGRADE.get(),JDTEItems.AE_CRAFTING_READ_UPGRADE.get());
        h.assertTrue(u.getSlots()==8,"eight real upgrade slots");
        for(int i=0;i<8;i++)h.assertTrue(u.insertItem(i,new ItemStack(cards.get(i)),false).isEmpty(),"compatible card enters slot "+i);
        h.assertTrue(!u.isItemValid(6,new ItemStack(JDTEItems.UNDERCLOCK_UPGRADE.get()))&&!u.isItemValid(6,new ItemStack(JDTEItems.FILTER_UPGRADE.get())),"speed conflict and unsupported filters rejected");
        h.assertTrue(be.getMaxEnergy()==(long)com.direwolf20.justdirethings.setup.Config.GENERATOR_T1_MAX_FE.get()*400&&be.getEnergyStorage().getMaxEnergyStored()==be.getMaxEnergy(),"capacity card physical FE buffer equals advertised capacity");
        var energy=be.getCapability(ForgeCapabilities.ENERGY,Direction.DOWN).orElseThrow(IllegalStateException::new);h.assertTrue(!energy.canReceive()&&energy.receiveEnergy(100,false)==0&&energy.receiveEnergy(-100,false)==0,"output-only FE port, no negative input");
        h.assertTrue(be.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()==false,"solid generator exposes no phantom fluid tank");
        var coalBefore=be.getMachineHandler().getStackInSlot(0).getCount();be.doBurn();
        int ticks=net.minecraftforge.common.ForgeHooks.getBurnTime(new ItemStack(Items.COAL),net.minecraft.world.item.crafting.RecipeType.SMELTING);
        h.assertTrue(be.getMachineHandler().getStackInSlot(0).getCount()==coalBefore-2&&be.feRemaining==ticks*be.getFePerFuelTick()*3,"real solid upgraded batch");
        h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void fluidValidationAutomationWholeBatchesAndFullBuffer(GameTestHelper h) {
        var be=(ExtendedFluidGeneratorBE)place(h,true);var port=be.getCapability(ForgeCapabilities.FLUID_HANDLER,Direction.NORTH).orElseThrow(IllegalStateException::new);
        h.assertTrue(!port.isFluidValid(0,new FluidStack(Fluids.WATER,1))&&port.fill(new FluidStack(Fluids.WATER,1000),FluidAction.EXECUTE)==0,"only native refined fuel is accepted");
        h.assertTrue(port.fill(new FluidStack(fuel(),1234),FluidAction.SIMULATE)==1234&&be.getFluidTank().isEmpty(),"fluid simulation inert");
        int capacity=be.getMaxMB();h.assertTrue(port.fill(new FluidStack(fuel(),capacity+1),FluidAction.EXECUTE)==capacity&&port.fill(new FluidStack(fuel(),1),FluidAction.EXECUTE)==0,"fill bounded by capacity");
        h.assertTrue(port.drain(capacity-3,FluidAction.EXECUTE).getAmount()==capacity-3&&be.getFluidTank().getFluidAmount()==3,"automation drain conservation");
        int fe=be.getFePerFuelTick();be.doGenerate();h.assertTrue(be.getFluidTank().getFluidAmount()==2&&be.getEnergyStorage().getEnergyStored()==fe,"normal one-mB generation");
        UpgradeHelper.getUpgradeHandler(be).insertItem(7,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
        be.getEnergyStorage().setEnergy(be.getMaxEnergy()-fe*3+1);be.doGenerate();h.assertTrue(be.getFluidTank().getFluidAmount()==2&&be.getEnergyStorage().getEnergyStored()==be.getMaxEnergy()-fe*3+1,"partial headroom cannot spend upgraded fluid");
        be.getEnergyStorage().setEnergy(0);be.doGenerate();h.assertTrue(be.getFluidTank().isEmpty()&&be.getEnergyStorage().getEnergyStored()==fe*3,"two mB buy exactly triple FE");
        port.fill(new FluidStack(fuel(),1),FluidAction.EXECUTE);for(int i=0;i<100;i++)be.doGenerate();h.assertTrue(be.getFluidTank().getFluidAmount()==1&&be.getEnergyStorage().getEnergyStored()==fe*3,"odd residual fuel survives insufficient attempts");
        var items=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);h.assertTrue(!items.isItemValid(0,new ItemStack(Items.WATER_BUCKET))&&!items.isItemValid(0,new ItemStack(Items.COAL)),"fluid item validator matches actual native container rules");
        h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void bothNativeConversionsPreserveFuelProgressActualBuffersAndAllCards(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            h.setBlock(POS,Blocks.AIR);h.setBlock(POS,fluid?Registration.GeneratorFluidT1.get():Registration.GeneratorT1.get());var old=(BaseMachineBE)h.getBlockEntity(POS);
            var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());com.direwolf20.justdirethings.common.items.FuelCanister.setFuelLevel(can,123456);
            old.getMachineHandler().setStackInSlot(0,fluid?new ItemStack(Items.BUCKET):can);
            var handler=UpgradeHelper.getUpgradeHandler(old);for(int i=0;i<3;i++)handler.insertItem(i,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);handler.insertItem(3,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
            ((PoweredMachineBE)old).getEnergyStorage().setEnergy(1234567);
            if(fluid)((GeneratorFluidT1BE)old).getFluidTank().setFluid(new FluidStack(fuel(),12345));
            else {var b=(GeneratorT1BE)old;b.feRemaining=654321;b.maxBurn=500000;b.burnRemaining=400001;}
            var oldItems=old.getMachineHandler();var oldEnergy=((PoweredMachineBE)old).getEnergyStorage();var inventory=oldItems.serializeNBT();var beforeCards=handler.serializeNBT().getList("Items",10).copy();
            var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(context(h,card))==InteractionResult.SUCCESS&&card.isEmpty(),"native conversion succeeds "+fluid);
            var be=(BaseMachineBE)h.getBlockEntity(POS);h.assertTrue(be instanceof ExtendedUpgradeMachine&&be.getMachineHandler().serializeNBT().equals(inventory)&&((PoweredMachineBE)be).getEnergyStorage().getEnergyStored()==1234567,"inventory and actual FE moved "+fluid);
            h.assertTrue(UpgradeHelper.getUpgradeHandler(be).getSlots()==8&&UpgradeHelper.getUpgradeHandler(be).serializeNBT().getList("Items",10).equals(beforeCards),"four original cards reach eight-slot active handler");
            h.assertTrue(oldItems.getStackInSlot(0).isEmpty()&&oldEnergy.getEnergyStored()==0,"old actual interfaces are empty");
            var save=be.saveWithFullMetadata();var fresh=(BaseMachineBE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),save);h.getLevel().setBlockEntity(fresh);
            h.assertTrue(((PoweredMachineBE)fresh).getEnergyStorage().getEnergyStored()==1234567&&fresh.getMachineHandler().serializeNBT().equals(inventory)&&UpgradeHelper.getUpgradeHandler(fresh).getStackInSlot(3).is(JDTEItems.GENERATOR_UPGRADE.get()),"fresh BE reload retains migrated ownership");
            if(fluid)h.assertTrue(((GeneratorFluidT1BE)fresh).getFluidTank().getFluidAmount()==12345&&((GeneratorFluidT1BE)old).getFluidTank().isEmpty(),"actual fluid migrated exactly once");
            else {var b=(GeneratorT1BE)fresh;h.assertTrue(b.feRemaining==654321&&b.maxBurn==500000&&b.burnRemaining==400001,"all native generation counters preserved above 16 bits");}
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void bothProtectionDeniedConversionsRollbackExactState(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            h.setBlock(POS,Blocks.AIR);var block=fluid?Registration.GeneratorFluidT1.get():Registration.GeneratorT1.get();h.setBlock(POS,block);var be=(BaseMachineBE)h.getBlockEntity(POS);
            be.getMachineHandler().setStackInSlot(0,new ItemStack(Items.COAL,37));UpgradeHelper.getUpgradeHandler(be).insertItem(3,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);((PoweredMachineBE)be).getEnergyStorage().setEnergy(321123);
            if(fluid)((GeneratorFluidT1BE)be).getFluidTank().setFluid(new FluidStack(fuel(),321));else {((GeneratorT1BE)be).feRemaining=123456;((GeneratorT1BE)be).burnRemaining=12345;}
            var before=be.saveWithFullMetadata();var count=new int[1];
            java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> deny=event->{if(event.getLevel()==h.getLevel()&&event.getPos().equals(be.getBlockPos())){count[0]++;event.setCanceled(true);}};
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent.class,deny);
            try {var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(context(h,card))==InteractionResult.FAIL&&card.getCount()==1&&count[0]==1,"protection event denies exactly once");var fresh=(BaseMachineBE)h.getBlockEntity(POS);h.assertTrue(fresh.saveWithFullMetadata().equals(before)&&be.getMachineHandler().getStackInSlot(0).isEmpty()&&((PoweredMachineBE)be).getEnergyStorage().getEnergyStored()==0,"failure restores exact unique inventory/FE/fluid/progress owner "+fluid);}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(deny);}
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void unloadAndRemovalInvalidateRetainedPublicPorts(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            var be=place(h,fluid);((PoweredMachineBE)be).getEnergyStorage().setEnergy(10000);be.getMachineHandler().setStackInSlot(0,new ItemStack(Items.COAL,9));if(fluid)((GeneratorFluidT1BE)be).getFluidTank().setFluid(new FluidStack(fuel(),123));
            var energy=be.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);var items=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);var tank=be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            be.onChunkUnloaded();h.assertTrue(energy.extractEnergy(100,false)==0&&items.extractItem(0,9,false).isEmpty()&&(tank==null||tank.drain(100,FluidAction.EXECUTE).isEmpty()),"retained unloaded interfaces are inert");
            be.reviveCaps();be.onLoad();h.assertTrue(energy.extractEnergy(1,true)==1,"same live owner can resume after reload");var save=be.saveWithFullMetadata();var fresh=(BaseMachineBE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),save);h.getLevel().setBlockEntity(fresh);be.clearRemoved();
            h.assertTrue(energy.extractEnergy(100,false)==0&&items.extractItem(0,9,false).isEmpty()&&(tank==null||tank.fill(new FluidStack(fuel(),100),FluidAction.EXECUTE)==0),"world identity rejects resurrected detached owner");
            h.assertTrue(((PoweredMachineBE)fresh).getEnergyStorage().getEnergyStored()==10000&&fresh.getMachineHandler().getStackInSlot(0).getCount()==9,"new unique owner intact");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void wrenchPickupAndActualBlockItemPlacementPreserveState(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            var be=place(h,fluid);var player=h.makeMockSurvivalPlayer();var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());com.direwolf20.justdirethings.common.items.FuelCanister.setFuelLevel(can,98765);be.getMachineHandler().setStackInSlot(0,fluid?new ItemStack(Items.BUCKET):can);
            UpgradeHelper.getUpgradeHandler(be).insertItem(7,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);((PoweredMachineBE)be).getEnergyStorage().setEnergy(123456);if(fluid)((GeneratorFluidT1BE)be).getFluidTank().setFluid(new FluidStack(fuel(),123));else {((GeneratorT1BE)be).feRemaining=765432;((GeneratorT1BE)be).burnRemaining=12345;}
            var inv=be.getMachineHandler().serializeNBT();var retained=be.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
            h.assertTrue(EclipseAlloyWrenchItem.pickupMachine(h.getLevel(),be.getBlockPos(),player),"actual wrench packs generator");var item=player.getInventory().items.stream().filter(s->s.is(fluid?JDTEItems.EXTENDED_FLUID_GENERATOR.get():JDTEItems.EXTENDED_GENERATOR.get())).findFirst().orElseThrow(IllegalStateException::new);
            player.setItemInHand(InteractionHand.MAIN_HAND,item);var use=new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false));
            h.assertTrue(item.useOn(use).consumesAction(),"actual BlockItem placement handles saved NBT");var fresh=(BaseMachineBE)h.getBlockEntity(POS);
            h.assertTrue(((PoweredMachineBE)fresh).getEnergyStorage().getEnergyStored()==123456&&fresh.getMachineHandler().serializeNBT().equals(inv)&&UpgradeHelper.getUpgradeHandler(fresh).getStackInSlot(7).is(JDTEItems.GENERATOR_UPGRADE.get()),"placed packed machine retains actual FE, inventory and eighth card");
            h.assertTrue(retained.extractEnergy(100,false)==0,"old endpoint cannot duplicate packed FE");if(fluid)h.assertTrue(((GeneratorFluidT1BE)fresh).getFluidTank().getFluidAmount()==123,"placed fluid preserved");else h.assertTrue(((GeneratorT1BE)fresh).feRemaining==765432&&((GeneratorT1BE)fresh).burnRemaining==12345,"placed pending FE/progress preserved");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void sourceOutputFaceRatesAndShortActualAcceptanceConserveFe(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            var be=place(h,fluid);var powered=(PoweredMachineBE)be;powered.getEnergyStorage().setEnergy(20000);
            var accepted=new int[1];var sideSeen=new Direction[1];var targetPos=h.absolutePos(POS).east();var block=Registration.BlockPlacerT2.get();h.getLevel().setBlockAndUpdate(targetPos,block.defaultBlockState());
            var target=new BlockPlacerT2BE(targetPos,block.defaultBlockState()) {
                @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side) {
                    if(cap!=ForgeCapabilities.ENERGY)return super.getCapability(cap,side);sideSeen[0]=side;
                    return LazyOptional.of(()->new EnergyStorage(100000) {
                        @Override public int receiveEnergy(int amount,boolean simulate) { int n=simulate?amount:Math.min(amount,7);if(!simulate)accepted[0]+=n;return n; }
                    }).cast();
                }
            };h.getLevel().setBlockEntity(target);
            if(fluid)((ExtendedFluidGeneratorBE)be).providePowerAdjacent();else ((ExtendedGeneratorBE)be).providePowerAdjacent();
            h.assertTrue(accepted[0]==7&&powered.getEnergyStorage().getEnergyStored()==19993&&sideSeen[0]==Direction.WEST,"actual short acceptance and opposite face preserve FE");
            h.getLevel().removeBlockEntity(targetPos);h.getLevel().setBlockAndUpdate(targetPos,Blocks.STONE.defaultBlockState());
            if(fluid)((ExtendedFluidGeneratorBE)be).providePowerAdjacent();else ((ExtendedGeneratorBE)be).providePowerAdjacent();
            h.assertTrue(powered.getEnergyStorage().getEnergyStored()==19993&&accepted[0]==7,"no transfer to cached removed target");
            h.getLevel().setBlockAndUpdate(targetPos,block.defaultBlockState());var real=(PoweredMachineBE)h.getLevel().getBlockEntity(targetPos);
            int limit=fluid?FuelCanisterMath.rate(((ExtendedFluidGeneratorBE)be).getFEOutputPerTick(),10):((ExtendedGeneratorBE)be).getFEPerTick();int offered=Math.min(19993,Math.min(limit,real.getMaxEnergy()));
            if(fluid)((ExtendedFluidGeneratorBE)be).providePowerAdjacent();else ((ExtendedGeneratorBE)be).providePowerAdjacent();
            h.assertTrue(real.getEnergyStorage().getEnergyStored()==offered&&powered.getEnergyStorage().getEnergyStored()==19993-offered,"native per-face export limit retained, including fluid x10");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void ultimateTickerAndCapacityDriveActualGeneration(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            var be=place(h,fluid);var u=UpgradeHelper.getUpgradeHandler(be);
            u.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);u.insertItem(1,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false);
            h.assertTrue(UpgradeHelper.getEffectiveTickSpeed(be,20)==1&&UpgradeHelper.hasOverclock(be),"ultimate speed participates in real ticker");
            var energy=((PoweredMachineBE)be).getEnergyStorage();
            int fe;
            if(fluid){var g=(ExtendedFluidGeneratorBE)be;g.getFluidTank().setFluid(new FluidStack(fuel(),100));fe=g.getFePerFuelTick();h.assertTrue(g.getFluidTank().getCapacity()==g.getMaxMB(),"physical ultimate fluid capacity synchronized");}
            else {var g=(ExtendedGeneratorBE)be;g.feRemaining=100000;g.burnRemaining=500;fe=g.fePerTick();}
            @SuppressWarnings("unchecked") var ticker=(net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>)be.getBlockState().getTicker(h.getLevel(),be.getType());ticker.tick(h.getLevel(),be.getBlockPos(),be.getBlockState(),be);
            h.assertTrue(energy.getEnergyStored()==fe*10,"actual block ticker performs ten generation executions "+fluid+" actual="+energy.getEnergyStored()+" unit="+fe);
            if(fluid)h.assertTrue(((ExtendedFluidGeneratorBE)be).getFluidTank().getFluidAmount()==90,"ten executions spend ten actual mB");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void packedCraftingRecipePreservesCountersCardsFluidAndName(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            h.setBlock(POS,Blocks.AIR);var block=fluid?Registration.GeneratorFluidT1.get():Registration.GeneratorT1.get();h.setBlock(POS,block);var be=(BaseMachineBE)h.getBlockEntity(POS);
            UpgradeHelper.getUpgradeHandler(be).insertItem(3,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);((PoweredMachineBE)be).getEnergyStorage().setEnergy(765432);be.getMachineHandler().setStackInSlot(0,new ItemStack(Items.BUCKET));
            if(fluid)((GeneratorFluidT1BE)be).getFluidTank().setFluid(new FluidStack(fuel(),321));else {((GeneratorT1BE)be).feRemaining=456789;((GeneratorT1BE)be).burnRemaining=123456;}
            var item=new ItemStack(block.asItem());item.getOrCreateTag().put("JustDiresBEData",be.saveWithoutMetadata());item.setHoverName(net.minecraft.network.chat.Component.literal("Retained machine"));var before=item.copy();
            var player=h.makeMockSurvivalPlayer();var input=new TransientCraftingContainer(new AbstractContainerMenu(null,0){public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}},3,3);input.setItem(1,new ItemStack(JDTEItems.EXTENDED_UPGRADE.get()));input.setItem(4,item);
            var id=new net.minecraft.resources.ResourceLocation("jdte",fluid?"extended_fluid_generator":"extended_generator");var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)h.getLevel().getRecipeManager().byKey(id).orElseThrow(IllegalStateException::new);h.assertTrue(recipe.matches(input,h.getLevel()),"actual preserving shaped recipe matches");
            var result=recipe.assemble(input,h.getLevel().registryAccess());h.assertTrue(ItemStack.matches(item,before)&&result.getHoverName().getString().equals("Retained machine"),"craft preview is pure and keeps custom name");
            var target=place(h,fluid);target.load(result.getTag().getCompound("JustDiresBEData"));UpgradeHelper.syncCapacities(target);
            h.assertTrue(((PoweredMachineBE)target).getEnergyStorage().getEnergyStored()==765432&&target.getMachineHandler().getStackInSlot(0).is(Items.BUCKET)&&UpgradeHelper.getUpgradeHandler(target).getStackInSlot(3).is(JDTEItems.GENERATOR_UPGRADE.get()),"craft carries unique real FE inventory and migrated upgrade slots");
            if(fluid)h.assertTrue(((GeneratorFluidT1BE)target).getFluidTank().getFluidAmount()==321,"craft fluid preserved");else h.assertTrue(((GeneratorT1BE)target).feRemaining==456789&&((GeneratorT1BE)target).burnRemaining==123456,"craft prepaid FE and progress preserved");
            h.assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty),"no duplicate old generator remainder");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void unsafeCapacityRemovalAndMenuShiftCannotDiscardBuffers(GameTestHelper h) {
        for(boolean fluid:List.of(false,true)) {
            var be=place(h,fluid);var u=UpgradeHelper.getUpgradeHandler(be);u.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
            var energy=((PoweredMachineBE)be).getEnergyStorage();energy.setEnergy(((PoweredMachineBE)be).getMaxEnergy());
            if(fluid)((ExtendedFluidGeneratorBE)be).getFluidTank().setFluid(new FluidStack(fuel(),100000));
            int stored=energy.getEnergyStored();
            h.assertTrue(u.extractItem(0,1,true).isEmpty()&&u.extractItem(0,1,false).isEmpty()&&energy.getEnergyStored()==stored,"unsafe simulated/real capacity extraction is refused");
            u.setStackInSlot(0,ItemStack.EMPTY);h.assertTrue(u.getStackInSlot(0).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())&&energy.getEnergyStored()==stored,"direct card replacement cannot clip FE");
            var p=h.makeMockSurvivalPlayer();p.setPos(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+1,be.getBlockPos().getZ()+.5);
            var m=fluid?new ExtendedFluidGeneratorContainer(1,p.getInventory(),be.getBlockPos()):new ExtendedGeneratorContainer(1,p.getInventory(),be.getBlockPos());
            var slot=m.slots.stream().filter(v->v instanceof com.jdte.common.upgrades.UpgradeSlot&&v.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).findFirst().orElseThrow();
            m.quickMoveStack(p,slot.index);h.assertTrue(p.getInventory().countItem(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())==0&&energy.getEnergyStored()==stored&&!u.getStackInSlot(0).isEmpty(),"shift-click respects buffer-preserving extraction guard");
            energy.setEnergy(0);
            if(fluid) {h.assertTrue(u.extractItem(0,1,false).isEmpty()&&((ExtendedFluidGeneratorBE)be).getFluidTank().getFluidAmount()==100000,"fluid independently prevents unsafe downgrade");((ExtendedFluidGeneratorBE)be).getFluidTank().setFluid(new FluidStack(fuel(),1000));}
            h.assertTrue(u.extractItem(0,1,false).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())&&energy.getEnergyStored()==0,"safe drained-buffer card removal succeeds");
            if(fluid)h.assertTrue(((ExtendedFluidGeneratorBE)be).getFluidTank().getFluidAmount()==1000,"safe removal conserves remaining fluid");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void overflowingFuelBudgetRejectsBeforeConsumptionAndSpeedSaturates(GameTestHelper h) {
        h.setBlock(POS,Registration.GeneratorT1.get());var pos=h.absolutePos(POS);var state=h.getLevel().getBlockState(pos);
        var huge=new GeneratorT1BE(pos,state) { @Override public int getFePerFuelTick() {return Integer.MAX_VALUE;} };h.getLevel().setBlockEntity(huge);huge.getMachineHandler().setStackInSlot(0,new ItemStack(Items.COAL,2));
        huge.doBurn();h.assertTrue(huge.feRemaining==0&&huge.getMachineHandler().getStackInSlot(0).getCount()==2,"normal overflowing credit does not consume fuel");
        UpgradeHelper.getUpgradeHandler(huge).insertItem(0,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);huge.doBurn();h.assertTrue(huge.feRemaining==0&&huge.getMachineHandler().getStackInSlot(0).getCount()==2,"upgraded overflowing credit does not consume fuel");
        var data=huge.saveWithFullMetadata();data.putInt("fuelBurnMultiplier",Integer.MAX_VALUE);huge.load(data);
        h.assertTrue(huge.getBurnSpeedMultiplier()==Integer.MAX_VALUE&&huge.fePerTick()==Integer.MAX_VALUE,"real native rate multiplication saturates without negative FE");h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void realContainerFuelsKeepTheirCorrectRemainderAndWholeFluidTransfer(GameTestHelper h) {
        var solid=(ExtendedGeneratorBE)place(h,false);var items=solid.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        UpgradeHelper.getUpgradeHandler(solid).insertItem(7,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
        h.assertTrue(items.insertItem(0,new ItemStack(Items.LAVA_BUCKET),false).isEmpty(),"real furnace container fuel accepted");
        int burn=net.minecraftforge.common.ForgeHooks.getBurnTime(new ItemStack(Items.LAVA_BUCKET),net.minecraft.world.item.crafting.RecipeType.SMELTING);solid.doBurn();
        h.assertTrue(solid.feRemaining==burn*solid.getFePerFuelTick()*3&&items.extractItem(0,1,false).is(Items.BUCKET),"source container-fuel exception uses one container, tripled credit, one empty bucket");
        var liquid=(ExtendedFluidGeneratorBE)place(h,true);var fuelBucket=new ItemStack(fuel().getBucket());var automation=liquid.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        h.assertTrue(automation.isItemValid(0,fuelBucket)&&automation.insertItem(0,fuelBucket,false).isEmpty(),"real native Refined Fuel bucket accepted by automation");
        liquid.getFluidTank().setFluid(new FluidStack(fuel(),liquid.getMaxMB()-1));liquid.handleItemStack();
        h.assertTrue(liquid.getFluidTank().getFluidAmount()==liquid.getMaxMB()-1&&liquid.getMachineHandler().getStackInSlot(0).is(fuelBucket.getItem()),"bucket cannot be partially debited when only one mB fits");
        liquid.getFluidTank().setFluid(FluidStack.EMPTY);liquid.handleItemStack();
        h.assertTrue(liquid.getFluidTank().getFluidAmount()==1000&&automation.extractItem(0,1,false).is(Items.BUCKET),"exactly one real bucket becomes 1000 mB and one empty bucket");h.succeed();
    }
}
