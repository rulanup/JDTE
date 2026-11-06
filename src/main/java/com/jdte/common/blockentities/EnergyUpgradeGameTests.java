package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class EnergyUpgradeGameTests {
    private static final BlockPos POS=new BlockPos(2,1,2),OTHER=new BlockPos(4,1,2);
    private static AdvancedPotionBrewerBE brewer(GameTestHelper h){h.setBlock(POS,net.minecraft.world.level.block.Blocks.AIR);h.setBlock(POS,JDTEBlocks.ADVANCED_POTION_BREWER.get());return (AdvancedPotionBrewerBE)h.getBlockEntity(POS);}
    private static void card(AdvancedPotionBrewerBE b){UpgradeHelper.getUpgradeHandler(b).insertItem(0,new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()),false);}
    private static void inputs(AdvancedPotionBrewerBE b){var inv=b.getMachineHandler();inv.setStackInSlot(AdvancedPotionBrewerBE.BOTTLE_SLOT_0,PotionUtils.setPotion(new ItemStack(Items.POTION),Potions.WATER));inv.setStackInSlot(AdvancedPotionBrewerBE.INGREDIENT_SLOT,new ItemStack(Items.NETHER_WART));}
    private static int fuel(AdvancedPotionBrewerBE b){return b.saveWithFullMetadata().getInt("fuel");}
    private static EnergyTransmitterBE transmitter(GameTestHelper h){h.setBlock(POS,Registration.EnergyTransmitter.get());return (EnergyTransmitterBE)h.getBlockEntity(POS);}
    @GameTest(template="mineral_empty") public static void specialCardsRegisterRecipesLimitsAndMachineCompatibility(GameTestHelper h){
        var b=brewer(h);var handler=UpgradeHelper.getUpgradeHandler(b);var brew=new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get());var overload=new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get());
        h.assertTrue(brew.getMaxStackSize()==1&&overload.getMaxStackSize()==1&&UpgradeCardInsertionHelper.isUpgradeCard(brew)&&UpgradeHelper.isUpgrade(overload),"plain source items use generic upgrade integration");
        h.assertTrue(handler.insertItem(0,brew.copy(),false).isEmpty()&&!handler.isItemValid(1,brew)&&!handler.isItemValid(1,overload),"brewer accepts exactly one correct card");
        var t=transmitter(h);var u=UpgradeHelper.getUpgradeHandler(t);h.assertTrue(u.insertItem(0,overload.copy(),false).isEmpty()&&!u.isItemValid(1,overload)&&!u.isItemValid(1,brew),"native transmitter accepts one overload only");
        h.setBlock(OTHER,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var wrong=UpgradeHelper.getUpgradeHandler((FluidMixerBE)h.getBlockEntity(OTHER));h.assertTrue(!wrong.isItemValid(0,brew)&&!wrong.isItemValid(0,overload),"unrelated machine rejects both");
        for(String name:new String[]{"energy_brewing_upgrade","energy_overload_upgrade"})h.assertTrue(h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id(name)).isPresent(),"actual Forge recipe loaded: "+name);h.succeed();
    }
    @GameTest(template="mineral_empty") public static void twentyActualBrewsUseOneChargeAndPreserveStoredPowder(GameTestHelper h){
        var b=brewer(h);b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT,new ItemStack(Items.BLAZE_POWDER,5));card(b);b.getEnergyStorage().setEnergy(50000);b.getTimeFluidTank().setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),8000));
        int made=0;
        for(int cycle=0;cycle<20;cycle++){
            inputs(b);b.brew();b.brew();var result=b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START);
            h.assertTrue(result.is(Items.POTION)&&PotionUtils.getPotion(result)==Potions.AWKWARD,"real water/wart recipe finished: cycle="+cycle+" state="+b.saveWithFullMetadata());made+=result.getCount();b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START,ItemStack.EMPTY);
        }
        h.assertTrue(made==20&&fuel(b)==0&&b.getEnergyStorage().getEnergyStored()==39000,"one 5000-FE charge plus twenty normal 300-FE completions");
        h.assertTrue(b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT).getCount()==5,"previous powder retained without fallback consumption");
        inputs(b);b.brew();h.assertTrue(fuel(b)==19&&b.getEnergyStorage().getEnergyStored()==33700,"twenty-first start buys the next charge exactly once");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void absentInputsBlockedOutputAndShortEnergyDoNotBuyFuel(GameTestHelper h){
        var b=brewer(h);card(b);b.getEnergyStorage().setEnergy(50000);b.brew();h.assertTrue(fuel(b)==0&&b.getEnergyStorage().getEnergyStored()==50000,"no recipe cannot buy fuel");
        inputs(b);for(int i=0;i<AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT;i++)b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START+i,new ItemStack(Items.COBBLESTONE));b.brew();h.assertTrue(fuel(b)==0&&b.getEnergyStorage().getEnergyStored()==50000,"blocked output cannot buy fuel");
        for(int i=0;i<AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT;i++)b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START+i,ItemStack.EMPTY);
        b.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT,new ItemStack(Items.BLAZE_POWDER,5));b.getEnergyStorage().setEnergy(4999);b.brew();h.assertTrue(fuel(b)==0&&b.getEnergyStorage().getEnergyStored()==4999&&b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT).getCount()==5,"short FE does not partially charge or consume powder");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void powderManualShiftAndSidedAutomationRespectInstalledCard(GameTestHelper h){
        var b=brewer(h);b.setFuelInputEnabled(true);var powder=new ItemStack(Items.BLAZE_POWDER);h.assertTrue(b.getAutomationItemHandler(Direction.UP).insertItem(AdvancedPotionBrewerBE.FUEL_SLOT,powder.copy(),true).isEmpty(),"normal powder automation available");card(b);
        for(var side:Direction.values())h.assertTrue(!b.getAutomationItemHandler(side).isItemValid(AdvancedPotionBrewerBE.FUEL_SLOT,powder)&&!b.getAutomationItemHandler(side).insertItem(AdvancedPotionBrewerBE.FUEL_SLOT,powder.copy(),false).isEmpty(),"all sides reject powder with card");
        var player=h.makeMockSurvivalPlayer();var menu=new AdvancedPotionBrewerContainer(1,player.getInventory(),b.getBlockPos());h.assertTrue(!menu.slots.get(AdvancedPotionBrewerBE.FUEL_SLOT).mayPlace(powder),"manual fuel slot rejects");
        int idx=menu.slots.size()-36;menu.slots.get(idx).set(powder.copy());menu.quickMoveStack(player,idx);h.assertTrue(b.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT).isEmpty()&&java.util.stream.IntStream.range(0,menu.slots.size()).map(i->menu.slots.get(i).getItem().is(Items.BLAZE_POWDER)?menu.slots.get(i).getItem().getCount():0).sum()==1,"Shift cannot bypass fuel validation");
        UpgradeHelper.getUpgradeHandler(b).extractItem(0,1,false);h.assertTrue(menu.slots.get(AdvancedPotionBrewerBE.FUEL_SLOT).mayPlace(powder)&&b.getAutomationItemHandler(Direction.UP).isItemValid(AdvancedPotionBrewerBE.FUEL_SLOT,powder),"removal restores powder input");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void savedChargeAndMidBrewResumeWithoutRepurchase(GameTestHelper h){
        var b=brewer(h);card(b);inputs(b);b.getEnergyStorage().setEnergy(50000);for(int i=0;i<175;i++)b.brew();h.assertTrue(fuel(b)==19&&b.getEnergyStorage().getEnergyStored()==45000,"purchase once before 400-tick recipe");
        var saved=b.saveWithFullMetadata();var restored=(AdvancedPotionBrewerBE)BlockEntity.loadStatic(b.getBlockPos(),b.getBlockState(),saved);h.getLevel().setBlockEntity(restored);
        h.assertTrue(restored.hasEnergyBrewingUpgrade()&&fuel(restored)==19,"canonical upgrade and credits saved");for(int i=0;i<225;i++)restored.brew();restored.brew();
        h.assertTrue(PotionUtils.getPotion(restored.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.OUTPUT_SLOT_START))==Potions.AWKWARD&&fuel(restored)==19&&restored.getEnergyStorage().getEnergyStored()==44700,"resume completes at exact cost without buying again");var menu=new AdvancedPotionBrewerContainer(2,h.makeMockSurvivalPlayer().getInventory(),restored.getBlockPos());h.assertTrue(menu.getFuel()==19,"persisted fuel credits exposed through actual menu data");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void zeroConfiguredAndCreativeFuelAndLegacyPowderRemainSourceCompatible(GameTestHelper h){
        int cost=JDTEConfig.COMMON.potionBrewerEnergyPerBlazePowder.get();try{
            JDTEConfig.COMMON.potionBrewerEnergyPerBlazePowder.set(0);var b=brewer(h);card(b);inputs(b);b.getEnergyStorage().setEnergy(1000);b.brew();h.assertTrue(fuel(b)==19&&b.getEnergyStorage().getEnergyStored()==1000,"zero-priced charge still leaves normal completion cost pending");
            JDTEConfig.COMMON.potionBrewerEnergyPerBlazePowder.set(5000);UpgradeHelper.getUpgradeHandler(b).insertItem(1,new ItemStack(JDTEItems.CREATIVE_UPGRADE.get()),false);b.getEnergyStorage().setEnergy(0);var data=b.saveWithFullMetadata();data.putInt("fuel",0);data.putInt("brewProgress",0);b.load(data);b.brew();h.assertTrue(fuel(b)==19&&b.getEnergyStorage().getEnergyStored()==0,"Creative fuel bypasses FE");
            var legacy=brewer(h);inputs(legacy);legacy.getEnergyStorage().setEnergy(1000);legacy.getMachineHandler().setStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT,new ItemStack(Items.BLAZE_POWDER,2));legacy.brew();h.assertTrue(fuel(legacy)==19&&legacy.getMachineHandler().getStackInSlot(AdvancedPotionBrewerBE.FUEL_SLOT).getCount()==1,"unupgraded legacy powder behavior retained");
        }finally{JDTEConfig.COMMON.potionBrewerEnergyPerBlazePowder.set(cost);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void energyCardsUseCanonicalBagsInsertionAndTypedCopier(GameTestHelper h){
        var source=brewer(h);var player=h.makeMockSurvivalPlayer();player.setShiftKeyDown(true);var held=new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get());h.assertTrue(UpgradeCardInsertionHelper.insertAll(h.getLevel(),source.getBlockPos(),player,held)==1&&held.isEmpty(),"plain energy card Shift insertion uses handler");
        h.setBlock(OTHER,JDTEBlocks.ADVANCED_POTION_BREWER.get());var target=(AdvancedPotionBrewerBE)h.getBlockEntity(OTHER);target.getEnergyStorage().setEnergy(12345);var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());var bagHandler=new AdvancedUpgradeStorageHandler(bag);bagHandler.setStackInSlot(0,new ItemStack(JDTEItems.ENERGY_BREWING_UPGRADE.get()));
        var basic=new ItemStack(JDTEItems.UPGRADE_STORAGE.get());h.assertTrue(basic.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new).insertItem(0,new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()),false).isEmpty(),"basic bag accepts overload");
        var copier=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get().saveSettings(source,copier);player.setShiftKeyDown(false);player.setItemInHand(InteractionHand.MAIN_HAND,copier);player.getInventory().setItem(9,bag);
        var paste=copier.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(target.getBlockPos()),Direction.UP,target.getBlockPos(),false)));
        h.assertTrue(target.hasEnergyBrewingUpgrade(),"typed paste accepts plain brewing upgrade: result="+paste+" copier="+copier+" tag="+copier.getTag()+" source="+source.saveWithFullMetadata()+" target="+target.saveWithFullMetadata());h.assertTrue(bagHandler.getStackInSlot(0).isEmpty(),"exactly one brewing upgrade removed from advanced case: "+bag.getTag());h.assertTrue(target.getEnergyStorage().getEnergyStored()==12345,"configuration does not clone FE");h.succeed();
    }

    @GameTest(template="mineral_empty")
    public static void brewingCopierTemplatesHandleBothForgeEmptyFormsWithoutAcceptingInvalidItems(GameTestHelper h) {
        var b = brewer(h);
        var codec = com.jdte.common.items.machinesettings.MachineSettingsCodecRegistry
                .find(com.jdte.JDTE.id("advanced_potion_brewer"), b).orElseThrow();
        var registries = h.getLevel().registryAccess();
        var encoded = codec.encode(b, registries);
        h.assertTrue(codec.decode(encoded, registries).isPresent(), "unlocked empty templates round-trip");
        encoded.getCompound("recipeLockTemplates").put("slot_0", ItemStack.EMPTY.save(new net.minecraft.nbt.CompoundTag()));
        h.assertTrue(codec.decode(encoded, registries).isPresent(), "older Forge air/Count:0 empty template remains readable");
        var templates = net.minecraft.core.NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
        templates.set(AdvancedPotionBrewerBE.INGREDIENT_SLOT, new ItemStack(Items.NETHER_WART));
        b.applyCopiedRecipeLock(true, templates);
        encoded = codec.encode(b, registries);
        h.assertTrue(codec.decode(encoded, registries).isPresent(), "locked valid ingredients with empty bottle/output templates round-trip");
        var invalid = new net.minecraft.nbt.CompoundTag(); invalid.putString("id", "jdte:missing_template_item"); invalid.putByte("Count", (byte)1);
        encoded.getCompound("recipeLockTemplates").put("slot_3", invalid);
        h.assertTrue(codec.decode(encoded, registries).isEmpty(), "unknown registry item is rejected rather than silently erased");
        invalid.putString("id", "minecraft:air");
        h.assertTrue(codec.decode(encoded, registries).isEmpty(), "noncanonical air with positive count is rejected");
        h.succeed();
    }
    @GameTest(template="mineral_empty") public static void actualNativeTransmitterOverloadTransfersStoredFeWithoutCreatingIt(GameTestHelper h){
        var tx=transmitter(h);h.setBlock(OTHER,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var target=(FluidMixerBE)h.getBlockEntity(OTHER);
        var controlled=new EnergyTransmitterBE(tx.getBlockPos(),tx.getBlockState()){@Override public AABB getAABB(BlockPos ignored){return new AABB(target.getBlockPos());}};h.getLevel().setBlockEntity(controlled);
        int rate=com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.get();double loss=com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.get();
        try{
            com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(1000);com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(0D);
            controlled.getEnergyStorage().setEnergy(9000);controlled.getBlocksToCharge();controlled.providePower();h.assertTrue(target.getEnergyStorage().getEnergyStored()==1000&&controlled.getEnergyStorage().getEnergyStored()==8000,"ordinary native rate enforced");
            UpgradeHelper.getUpgradeHandler(controlled).insertItem(0,new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()),false);h.assertTrue(controlled.fePerTick()==Integer.MAX_VALUE,"Mixin lifts native cap");controlled.providePower();h.assertTrue(target.getEnergyStorage().getEnergyStored()==9000&&controlled.getEnergyStorage().getEnergyStored()==0,"actual world capability transfer conserves all 9000 FE");
            var saved=controlled.saveWithFullMetadata();var restored=(EnergyTransmitterBE)BlockEntity.loadStatic(controlled.getBlockPos(),controlled.getBlockState(),saved);h.assertTrue(restored.fePerTick()==Integer.MAX_VALUE,"overload survives native BE persistence");UpgradeHelper.getUpgradeHandler(restored).extractItem(0,1,false);h.assertTrue(restored.fePerTick()==1000,"removal restores native configured cap");
        }finally{com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(rate);com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(loss);}h.succeed();
    }
}
