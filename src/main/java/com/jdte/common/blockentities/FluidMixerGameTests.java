package com.jdte.common.blockentities;

import com.jdte.JDTE;
import com.jdte.common.recipes.*;
import com.jdte.common.upgrades.*;
import com.jdte.common.containers.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class FluidMixerGameTests {
    private static final BlockPos POS=new BlockPos(3,1,2);
    private static net.minecraft.world.level.material.Fluid time(){return BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid"));}
    private static FluidMixerBE machine(GameTestHelper h,boolean extended){h.setBlock(POS,extended?JDTEBlocks.EXTENDED_FLUID_MIXER.get():JDTEBlocks.ADVANCED_FLUID_MIXER.get());return (FluidMixerBE)h.getBlockEntity(POS);}
    private static void nativeInputs(FluidMixerBE be,int batches){be.inputTankA.setFluid(new FluidStack(Fluids.WATER,1000*batches));be.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(),batches));be.getEnergyStorage().receiveEnergy(be.getMaxEnergy(),false);}
    private static FluidMixerRecipe fixture(GameTestHelper h,String name,int ticks,int cost,boolean dual){
        var recipe=new FluidMixerRecipe(new FluidStack(name.equals("parallel")?Fluids.LAVA:Fluids.WATER,100),dual?new FluidStack(Fluids.LAVA,200):FluidStack.EMPTY,name.equals("codec")?Optional.of(new FluidMixerRecipe.InputItem(Ingredient.of(Items.REDSTONE),1)):Optional.empty(),new FluidStack(time(),50),ticks,cost,JDTE.id("test/mixer/"+name));
        var manager=h.getLevel().getRecipeManager();var all=new ArrayList<Recipe<?>>(manager.getRecipes());all.removeIf(r->r.getId().equals(recipe.getId()));all.add(recipe);manager.replaceRecipes(all);RecipeCacheSignal.invalidate();return recipe;
    }
    @GameTest(template="mineral_empty") public static void nativeSingleFluidRecipeResumesAcrossFreshBlockEntity(GameTestHelper h){
        var be=machine(h,false);nativeInputs(be,2);h.assertTrue(h.getLevel().getRecipeManager().byKey(JDTE.id("fluid_mixer/time_fluid_mixing")).isPresent(),"actual source recipe loaded");
        for(int i=0;i<73;i++)be.tickServer();h.assertTrue(be.progress==73&&be.outputTank.isEmpty(),"source 100-tick work preserved");
        var nbt=be.saveWithFullMetadata();var restored=new AdvancedFluidMixerBE(be.getBlockPos(),be.getBlockState());restored.load(nbt);h.getLevel().setBlockEntity(restored);
        for(int i=0;i<27;i++)restored.tickServer();
        h.assertTrue(restored.outputTank.getFluid().getFluid()==time()&&restored.outputTank.getFluidAmount()==250,"real output fluid, not empty/air");
        h.assertTrue(restored.inputTankA.getFluidAmount()==1000&&restored.getMachineHandler().getStackInSlot(0).getCount()==1&&restored.getEnergyStorage().getEnergyStored()==95000,"one batch charges exact fluid, catalyst and FE");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void swappedDualFluidsAndBlockedOutputPreserveResources(GameTestHelper h){
        fixture(h,"swapped",1,700,true);var be=machine(h,true);be.inputTankA.setFluid(new FluidStack(Fluids.LAVA,1000));be.inputTankB.setFluid(new FluidStack(Fluids.WATER,1000));be.getEnergyStorage().receiveEnergy(10000,false);be.tickServer();
        h.assertTrue(be.inputTankA.getFluidAmount()==800&&be.inputTankB.getFluidAmount()==900&&be.outputTank.getFluidAmount()==50&&be.getEnergyStorage().getEnergyStored()==9300,"unordered dual-input recipe charges matching quantities");
        be.outputTank.setFluid(new FluidStack(Fluids.WATER,1));be.tickServer();h.assertTrue(be.inputTankA.getFluidAmount()==800&&be.inputTankB.getFluidAmount()==900&&be.getEnergyStorage().getEnergyStored()==9300,"wrong output type cannot consume resources");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void mixingTiersLimitBatchesAndExcludeEachOther(GameTestHelper h){
        fixture(h,"parallel",1,1,false);var be=machine(h,true);be.inputTankA.setFluid(new FluidStack(Fluids.LAVA,8000));be.getEnergyStorage().receiveEnergy(10000,false);
        var upgrades=UpgradeHelper.getUpgradeHandler(be);upgrades.insertItem(0,new ItemStack(JDTEItems.MIXING_UPGRADE.get()),false);be.tickServer();
        h.assertTrue(be.outputTank.getFluidAmount()==400&&be.inputTankA.getFluidAmount()==7200&&be.getEnergyStorage().getEnergyStored()==9992,"ordinary mixing caps at eight batches");
        h.assertTrue(!upgrades.isItemValid(1,new ItemStack(JDTEItems.ULTIMATE_MIXING_PARALLEL_UPGRADE.get())),"parallel tiers mutually exclusive");
        upgrades.extractItem(0,1,false);upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_MIXING_PARALLEL_UPGRADE.get()),false);be.tickServer();
        h.assertTrue(be.outputTank.getFluidAmount()==4000&&be.inputTankA.isEmpty()&&be.getEnergyStorage().getEnergyStored()==9920,"ultimate batches bounded by actual input/output/FE");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void combinedCapabilitiesRouteThreeTanksOnAllSides(GameTestHelper h){
        var be=machine(h,false);
        for(var side:Direction.values()){
            var cap=be.getCapability(ForgeCapabilities.FLUID_HANDLER,side).orElseThrow(()->new IllegalStateException("fluid capability"));h.assertTrue(cap.getTanks()==3,"all sided views expose both inputs and output");
            be.inputTankA.setFluid(FluidStack.EMPTY);be.inputTankB.setFluid(FluidStack.EMPTY);be.outputTank.setFluid(FluidStack.EMPTY);
            h.assertTrue(cap.fill(new FluidStack(Fluids.WATER,500),IFluidHandler.FluidAction.SIMULATE)==500&&be.inputTankA.isEmpty(),"simulation does not fill");
            cap.fill(new FluidStack(Fluids.WATER,500),IFluidHandler.FluidAction.EXECUTE);cap.fill(new FluidStack(Fluids.LAVA,300),IFluidHandler.FluidAction.EXECUTE);
            h.assertTrue(be.inputTankA.getFluidAmount()==500&&be.inputTankB.getFluidAmount()==300&&cap.drain(1000,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"automation cannot drain inputs");
            be.outputTank.setFluid(new FluidStack(time(),200));h.assertTrue(cap.drain(1000,IFluidHandler.FluidAction.EXECUTE).getAmount()==200&&!cap.isFluidValid(2,new FluidStack(Fluids.WATER,1)),"only product tank drains");
        }h.succeed();
    }
    @GameTest(template="mineral_empty") public static void capacityRemovalClampsAllTanksAndMenuPreservesHighBits(GameTestHelper h){
        var be=machine(h,true);var upgrades=UpgradeHelper.getUpgradeHandler(be);upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
        be.inputTankA.setFluid(new FluidStack(Fluids.WATER,120000));be.inputTankB.setFluid(new FluidStack(Fluids.LAVA,80000));be.outputTank.setFluid(new FluidStack(time(),100000));
        var menu=new ExtendedFluidMixerContainer(1,h.makeMockSurvivalPlayer().getInventory(),be.getBlockPos());var data=be.getFluidMixerData();var copy=new SimpleContainerData(data.getCount());for(int k=0;k<data.getCount();k++)copy.set(k,(short)data.get(k));
        try{var field=FluidMixerContainer.class.getDeclaredField("fluidMixerData");field.setAccessible(true);field.set(menu,copy);}catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
        h.assertTrue(menu.getInputAFluidStack().getAmount()==120000&&menu.getInputBFluidAmount()==80000&&menu.getOutputFluidAmount()==100000&&menu.getFluidCapacity()==400000,"vanilla signed-short packets preserve full counts/capacity");
        upgrades.extractItem(0,1,false);h.assertTrue(be.inputTankA.getFluidAmount()==8000&&be.inputTankB.getFluidAmount()==8000&&be.outputTank.getFluidAmount()==8000,"every tank follows ordinary capacity-removal rules");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void recipeSerializerRoundTripsAndRejectsBadFluid(GameTestHelper h){
        var recipe=fixture(h,"codec",40000,123,true);var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());var serializer=new FluidMixerRecipe.Serializer();serializer.toNetwork(buffer,recipe);var copy=serializer.fromNetwork(recipe.getId(),buffer);buffer.release();
        h.assertTrue(copy.processTicks()==40000&&copy.energy()==123&&copy.matchesFluids(new FluidStack(Fluids.LAVA,200),new FluidStack(Fluids.WATER,100),new ItemStack(Items.REDSTONE))&&copy.output().getFluid()==time(),"network recipe retains swapped inputs, output and large processing duration");
        var json=com.google.gson.JsonParser.parseString("{\"fluid_input_a\":{\"id\":\"missing:no_fluid\",\"amount\":1},\"output\":{\"id\":\"minecraft:water\",\"amount\":1},\"process_ticks\":1,\"energy\":1}").getAsJsonObject();
        boolean rejected=false;try{serializer.fromJson(JDTE.id("bad"),json);}catch(com.google.gson.JsonSyntaxException expected){rejected=true;}h.assertTrue(rejected,"unknown fluid cannot silently become air");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void accelerationAndReadGateDoNotBypassCosts(GameTestHelper h){
        var be=machine(h,false);nativeInputs(be,2);be.accumulateAcceleratedTicks(99);be.flushAcceleratedTicks();h.assertTrue(be.progress==99&&be.outputTank.isEmpty(),"accelerated ticks retain fractional recipe progress");
        UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get()),false);be.accumulateAcceleratedTicks(1);be.flushAcceleratedTicks();be.tickServer();
        h.assertTrue(be.progress==99&&be.inputTankA.getFluidAmount()==2000&&be.getEnergyStorage().getEnergyStored()==100000,"unbound read gate preserves queued work/resources");
        UpgradeHelper.getUpgradeHandler(be).extractItem(0,1,false);be.flushAcceleratedTicks();h.assertTrue(be.progress==0&&be.outputTank.getFluidAmount()==250&&be.getEnergyStorage().getEnergyStored()==95000,"resumed acceleration charges exactly one completed batch");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void ultimateOverclockUsesTenRealTicksAndFiftyfoldCost(GameTestHelper h){
        var be=machine(h,true);var upgrades=UpgradeHelper.getUpgradeHandler(be);upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);upgrades.insertItem(1,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false);nativeInputs(be,20);
        @SuppressWarnings("unchecked") var ticker=(BlockEntityTicker<FluidMixerBE>)be.getBlockState().getTicker(h.getLevel(),be.getType());ticker.tick(h.getLevel(),be.getBlockPos(),be.getBlockState(),be);
        h.assertTrue(be.outputTank.getFluidAmount()==2500&&be.inputTankA.getFluidAmount()==10000&&be.getEnergyStorage().getEnergyStored()==7500000,"ten native batches cost 2.5M FE, preserving source ultimate overclock values");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void ordinaryMixerDropsKeepNonItemStateWithoutDuplicatingCards(GameTestHelper h){
        var be=machine(h,false);be.inputTankB.setFluid(new FluidStack(Fluids.LAVA,1234));be.outputTank.setFluid(new FluidStack(time(),456));be.getEnergyStorage().receiveEnergy(12345,false);be.progress=73;
        be.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(),3));UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.MIXING_UPGRADE.get()),false);
        var prior=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(2)).stream().map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
        h.getLevel().destroyBlock(be.getBlockPos(),true);var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(2)).stream().filter(e->!prior.contains(e.getUUID())).toList();
        var block=drops.stream().filter(e->e.getItem().is(JDTEItems.ADVANCED_FLUID_MIXER.get())).findFirst().orElseThrow().getItem();var data=block.getTag().getCompound("JustDiresBEData");
        h.assertTrue(data.getInt("energy")==12345&&data.getInt("progress")==73&&data.getCompound("inputTankB").getInt("Amount")==1234&&data.getCompound("outputTank").getInt("Amount")==456,"ordinary block loot keeps FE, B/output fluids and work");
        h.assertTrue(!data.contains("inventory")&&drops.stream().filter(e->e.getItem().is(JDTEItems.MIXING_UPGRADE.get())).mapToInt(e->e.getItem().getCount()).sum()==1&&drops.stream().filter(e->e.getItem().is(JDTEItems.TIME_FLUID_CATALYST.get())).mapToInt(e->e.getItem().getCount()).sum()==3,"catalyst and mixing card drop exactly once");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void retainedMixerFluidEndpointRejectsAReplacement(GameTestHelper h){
        var be=machine(h,false);be.outputTank.setFluid(new FluidStack(time(),500));var retained=be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(()->new IllegalStateException("fluid capability"));
        var replacement=new AdvancedFluidMixerBE(be.getBlockPos(),be.getBlockState());replacement.load(be.saveWithFullMetadata());h.getLevel().setBlockEntity(replacement);be.clearRemoved();
        h.assertTrue(retained.drain(500,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"stale combined endpoint cannot duplicate the saved product");
        h.assertTrue(replacement.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(()->new IllegalStateException("fluid capability")).drain(500,IFluidHandler.FluidAction.EXECUTE).getAmount()==500,"one live product inventory remains usable");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void mixingCardsUseStorageInsertionAndTypedCopierRules(GameTestHelper h){
        var source=machine(h,false);UpgradeHelper.getUpgradeHandler(source).insertItem(0,new ItemStack(JDTEItems.MIXING_UPGRADE.get()),false);
        var targetPos=new BlockPos(1,1,2);h.setBlock(targetPos,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var target=(FluidMixerBE)h.getBlockEntity(targetPos);target.getEnergyStorage().receiveEnergy(4321,false);
        var bag=new ItemStack(JDTEItems.UPGRADE_STORAGE.get());var handler=bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(()->new IllegalStateException("bag"));h.assertTrue(handler.insertItem(0,new ItemStack(JDTEItems.MIXING_UPGRADE.get()),false).isEmpty(),"plain mixing card accepted by storage");
        var copier=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get().saveSettings(source,copier);
        var player=h.makeMockSurvivalPlayer();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,copier);player.getInventory().setItem(9,bag);
        var result=JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get().useOn(new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(target.getBlockPos()),Direction.UP,target.getBlockPos(),false)));
        h.assertTrue(result==net.minecraft.world.InteractionResult.SUCCESS&&UpgradeHelper.hasMixingUpgrade(target)&&handler.getStackInSlot(0).isEmpty()&&target.getEnergyStorage().getEnergyStored()==4321,"typed copier costs one stored mixing card without cloning FE");h.succeed();
    }
}
