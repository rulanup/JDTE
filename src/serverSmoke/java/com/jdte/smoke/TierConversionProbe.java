package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.ExtendedUpgradeMachine;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Actual production-JAR/disk checks; opt-in and excluded from the main mod. */
public final class TierConversionProbe {
    private static final BlockPos ORIGIN = new BlockPos(3104,90,3104), GENERATOR = ORIGIN.east(24);
    private MinecraftServer server;
    private String mode = "";
    private int ticks, cancelled;
    private BlockPos deny;
    private List<Block> sources;
    private CompoundTag expected;

    private static void check(boolean condition,String reason) {
        if(!condition)throw new IllegalStateException("JDTE tier production probe: "+reason);
    }
    private static BlockPos pos(int index) { return ORIGIN.offset((index%5)*4,0,(index/5)*4); }
    private BaseMachineBE machine(int index) { return (BaseMachineBE)server.overworld().getBlockEntity(pos(index)); }
    private GeneratorFluidT1BE generator() { return (GeneratorFluidT1BE)server.overworld().getBlockEntity(GENERATOR); }
    private static net.minecraft.world.level.material.Fluid fuel() {
        return BuiltInRegistries.FLUID.stream().filter(fluid->fluid instanceof RefinedFuel refined&&refined.fePerMb()>0).findFirst().orElseThrow();
    }
    private static void paused(BaseMachineBE machine) {
        if(machine instanceof RedstoneControlledBE redstone)
            redstone.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
    }
    private InteractionResult upgrade(BlockPos pos) {
        var player=FakePlayerFactory.getMinecraft(server.overworld());
        var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());player.setItemInHand(InteractionHand.MAIN_HAND,card);
        var result=card.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
        check(result==InteractionResult.SUCCESS?card.isEmpty():card.getCount()==1,"upgrade card cost tracks outcome");return result;
    }
    @SubscribeEvent public void placement(BlockEvent.EntityPlaceEvent event) {
        if(deny!=null&&event.getPos().equals(deny)){cancelled++;event.setCanceled(true);deny=null;}
    }
    @SubscribeEvent public void started(ServerStartedEvent event)throws IOException {
        mode=System.getProperty("jdte.tierConversionLifecycle","");if(mode.isEmpty())return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"requires a marked disposable server");
        server=event.getServer();var level=server.overworld();
        sources=List.of(Registration.ClickerT2.get(),Registration.BlockBreakerT2.get(),Registration.BlockPlacerT2.get(),Registration.BlockSwapperT2.get(),Registration.DropperT2.get(),Registration.SensorT2.get(),Registration.FluidCollectorT2.get(),Registration.FluidPlacerT2.get(),JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(),JDTEBlocks.TIME_FREEZER.get(),JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(),JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(),JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(),JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),JDTEBlocks.ADVANCED_FLUID_MIXER.get());
        for(int i=0;i<sources.size();i++){var pos=pos(i);level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);}
        level.setChunkForced(GENERATOR.getX()>>4,GENERATOR.getZ()>>4,true);level.getChunkAt(GENERATOR);
        if(mode.equals("seed")) {
            for(int index=0;index<sources.size();index++)seed(index,level);
            level.setBlockAndUpdate(GENERATOR,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(GENERATOR,Registration.GeneratorFluidT1.get().defaultBlockState());
            var generator=generator();UpgradeHelper.getUpgradeHandler(generator).insertItem(0,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
            generator.getFluidTank().setFluid(new FluidStack(fuel(),1));
        }else if(mode.equals("verify")) {
            expected=NbtIo.readCompressed(Path.of("tier-conversion-expected.nbt").toFile());
            check(expected.equals(snapshot()),"all 15 production BEs, active/inactive cards, physical items, filters, capacities and resources match after real disk reload");
        }else throw new IllegalArgumentException("Unknown tier-conversion mode "+mode);
    }
    private void seed(int index,ServerLevel level) {
        var pos=pos(index);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,sources.get(index).defaultBlockState());
        var source=machine(index);paused(source);var cards=UpgradeHelper.getUpgradeHandler(source);
        for(int slot=0;slot<4;slot++) {
            var item=new ItemStack(slot==1?JDTEItems.AE_CRAFTING_READ_UPGRADE.get():JDTEItems.CAPACITY_UPGRADE.get());item.getOrCreateTag().putString("production_slot",Integer.toString(slot));
            check(cards.insertItem(slot,item,false).isEmpty(),"source card accepted index="+index);
        }
        var inventory=source.getMachineHandler();
        for(int slot=0;slot<inventory.getSlots();slot++) {var item=new ItemStack(Items.STONE,slot+3);item.getOrCreateTag().putInt("production_slot",slot);inventory.setStackInSlot(slot,item);}
        if(source instanceof FilterableBE filterable) {var item=new ItemStack(Items.DIAMOND);item.getOrCreateTag().putInt("machine",index);filterable.getFilterHandler().setStackInSlot(8,item);}
        var energy=((PoweredMachineBE)source).getEnergyStorage();int fe=((PoweredMachineBE)source).getMaxEnergy()-1;energy.setEnergy(fe);
        if(source instanceof FluidMachineBE fluid) {
            var actual=BuiltInRegistries.FLUID.stream().filter(f->f!=net.minecraft.world.level.material.Fluids.EMPTY&&fluid.getFluidTank().isFluidValid(new FluidStack(f,1))).findFirst().orElseThrow();
            fluid.getFluidTank().setFluid(new FluidStack(actual,fluid.getMaxMB()-1));
        }
        // Real protected placement must restore a native owned FE field, not just its serialized placeholder.
        if(index==4) {
            var before=source.saveWithFullMetadata();deny=pos;
            check(upgrade(pos)==InteractionResult.FAIL&&cancelled==1,"one actual Forge placement cancellation");
            source=machine(index);check(before.equals(source.saveWithFullMetadata())&&((PoweredMachineBE)source).getEnergyStorage().getEnergyStored()==fe,"cancelled production conversion restores real source NBT and FE");
            inventory=source.getMachineHandler();cards=UpgradeHelper.getUpgradeHandler(source);energy=((PoweredMachineBE)source).getEnergyStorage();
        }
        var inventoryBefore=inventory.serializeNBT();var cardBefore=cards.serializeNBT();cardBefore.putInt("Size",8);
        var filterBefore=source instanceof FilterableBE filterable?filterable.getFilterHandler().serializeNBT():null;
        var fluidBefore=source instanceof FluidMachineBE fluid?fluid.getFluidStack().copy():FluidStack.EMPTY;
        check(upgrade(pos)==InteractionResult.SUCCESS,"available conversion succeeded index="+index);
        var target=machine(index);var targetCards=UpgradeHelper.getUpgradeHandler(target);
        check(target instanceof ExtendedUpgradeMachine&&targetCards.getSlots()==8&&targetCards.serializeNBT().equals(cardBefore),"all source cards use the effective eight-slot storage index="+index);
        check(inventoryBefore.equals(target.getMachineHandler().serializeNBT())&&((PoweredMachineBE)target).getEnergyStorage().getEnergyStored()==fe,"real inventory and FE conserved index="+index);
        if(filterBefore!=null)check(filterBefore.equals(((FilterableBE)target).getFilterHandler().serializeNBT()),"actual native filter templates preserved index="+index);
        if(source instanceof FluidMachineBE)check(fluidBefore.isFluidStackIdentical(((FluidMachineBE)target).getFluidStack()),"nearly full fluid preserved index="+index);
        check(inventory.serializeNBT().getList("Items",Tag.TAG_COMPOUND).isEmpty()&&cards.serializeNBT().getList("Items",Tag.TAG_COMPOUND).isEmpty()&&energy.getEnergyStored()==0,"retained original buffers cannot duplicate resources index="+index);
        check(JDTEAttachments.upgrades(target).serializeNBT().getList("Items",Tag.TAG_COMPOUND).isEmpty(),"inactive four-slot data empty index="+index);
        check(targetCards.insertItem(7,new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()),false).isEmpty(),"actual eighth slot usable index="+index);
        target.setChanged();
    }
    private CompoundTag snapshot() {
        var result=new CompoundTag();for(int i=0;i<sources.size();i++)result.put("machine_"+i,machine(i).saveWithFullMetadata());result.put("generator",generator().saveWithFullMetadata());return result;
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws IOException {
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;
        check(++ticks<200,"timed out");if(ticks<10)return;
        var generator=generator();
        if(mode.equals("seed")) {
            check(generator.getFluidStack().getAmount()==1&&generator.getEnergyStorage().getEnergyStored()==0,"natural production ticks preserve last single mB");
            generator.getFluidTank().setFluid(new FluidStack(fuel(),3));int output=generator.getFePerFuelTick()*3;generator.doGenerate();paused(generator);
            check(generator.getFluidStack().getAmount()==1&&generator.getEnergyStorage().getEnergyStored()==output,"production Mixin buys exactly one whole two-mB batch");
            NbtIo.writeCompressed(snapshot(),Path.of("tier-conversion-expected.nbt").toFile());
            finish("tier-conversion-seed-success.txt","PASS: all 15 real production conversions preserve inventory/cards/FE/filters/fluids, expose eight slots and clear old interfaces; Forge cancellation restores native dropper; generator conserves odd fuel before saved shutdown");
        }else {
            check(expected.equals(snapshot()),"paused production state remains exact during natural ticks after restart");
            for(int i=0;i<sources.size();i++) {
                var target=machine(i);var power=(PoweredMachineBE)target;int before=power.getEnergyStorage().getEnergyStored();
                var capability=target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
                check(capability.extractEnergy(100,false)==100&&power.getEnergyStorage().getEnergyStored()==before-100,"reloaded live energy interface uses saved FE index="+i);
                var cards=UpgradeHelper.getUpgradeHandler(target);check(cards.getStackInSlot(7).is(JDTEItems.OVERCLOCK_UPGRADE.get()),"eighth slot restored index="+i);
                if(target.getMachineHandler().getSlots()>0) {
                    var actual=target.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);int beforeCount=target.getMachineHandler().getStackInSlot(0).getCount();
                    check(actual.extractItem(0,1,false).getCount()==1&&target.getMachineHandler().getStackInSlot(0).getCount()==beforeCount-1,"reloaded automation owns one physical inventory index="+i);
                }
            }
            int before=generator.getEnergyStorage().getEnergyStored();generator.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;
            for(int attempt=0;attempt<200;attempt++)generator.doGenerate();paused(generator);
            check(generator.getFluidStack().getAmount()==1&&generator.getEnergyStorage().getEnergyStored()==before,"reloaded generator cannot spend incomplete fuel");
            finish("tier-conversion-restart-success.txt","PASS: fresh production JVM restores exact state for all 15 conversions and generator; live FE/item capabilities spend only their one saved inventory; eighth slots and odd-fuel conservation remain correct");
        }
    }
    private void finish(String marker,String message)throws IOException {
        Files.writeString(Path.of(marker),message+"\n");mode="";server.halt(false);
    }
}
