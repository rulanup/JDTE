package com.jdte.gametest;

import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.entities.BulkItemEntity;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class MineralLifecycleGameTests {
    private static final BlockPos POS = new BlockPos(3, 1, 2);
    @GameTest(template="mineral_empty")
    public static void mineralDestructionBoundsEntitiesAndConservesAllItems(GameTestHelper h) {
        h.setBlock(POS, JDTEBlocks.MINERAL_EXTRACTOR.get());
        var be = (MineralExtractorBE) h.getBlockEntity(POS);
        UpgradeHelper.getUpgradeHandler(be).insertItem(0, new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()), false);
        be.getEnergyStorage().setEnergy(50_000_000);
        for (int i=0;i<64;i++) be.getMachineHandler().setStackInSlot(be.outputStartSlot()+i,new ItemStack(Items.IRON_ORE,6_400_000));
        var prior=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(2)).stream().map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
        h.getLevel().destroyBlock(h.absolutePos(POS),true);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(2)).stream().filter(e->!prior.contains(e.getUUID())).toList();
        long total=drops.stream().filter(e->e.getItem().is(Items.IRON_ORE)).mapToLong(e->e.getItem().getCount()).sum();
        h.assertTrue(total==409_600_000L,"all 409.6 million items dropped exactly once: "+total);
        var block= drops.stream().filter(e->e.getItem().is(JDTEItems.MINERAL_EXTRACTOR.get())).findFirst().orElseThrow().getItem();
        var data=block.getTag().getCompound("JustDiresBEData");
        h.assertTrue(!data.contains("inventory")&&!data.contains("machineHandler"),"block item cannot duplicate separately dropped outputs");
        h.assertTrue(!data.getCompound("ForgeCaps").getCompound("jdte:machine_data").contains("extended_upgrade_handler"),"block item cannot duplicate separately dropped upgrade cards");
        h.assertTrue(data.getInt("energy")==50_000_000,"ordinary loot retains stored energy");
        h.assertTrue(drops.size()<=70,"bounded entity count: "+drops.size());
        h.assertTrue(drops.stream().filter(e->e.getItem().is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).mapToInt(e->e.getItem().getCount()).sum()==1,"exactly one upgrade dropped");
        h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void bulkDropRoundTripAndLegalPartialPickup(GameTestHelper h) {
        var e=new BulkItemEntity(h.getLevel(),0,0,0,new ItemStack(Items.IRON_ORE,6_400_000));
        var tag=new CompoundTag();e.addAdditionalSaveData(tag);
        var restored=new BulkItemEntity(JDTEEntities.BULK_ITEM.get(),h.getLevel());restored.readAdditionalSaveData(tag);
        h.assertTrue(restored.getItem().getCount()==6_400_000,"integer count survives entity NBT");
        var player=h.makeMockSurvivalPlayer();
        for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        restored.setNoPickUpDelay();restored.playerTouch(player);
        h.assertTrue(restored.getItem().getCount()==6_400_000,"full inventory cannot void drop");
        player.getInventory().setItem(9,new ItemStack(Items.IRON_ORE,63));
        restored.playerTouch(player);
        h.assertTrue(restored.getItem().getCount()==6_399_999,"partial pickup conserves count");
        h.assertTrue(player.getInventory().getItem(9).getCount()==64,"player inventory limit respected");
        var next=new CompoundTag();restored.addAdditionalSaveData(next);
        h.assertTrue(next.getInt("JDTECount")==6_399_999,"partial pickup updates persistent count");
        h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void mineralInventoryRejectsCorruptSizeAndCount(GameTestHelper h) {
        h.setBlock(POS,JDTEBlocks.MINERAL_EXTRACTOR.get());
        var be=(MineralExtractorBE)h.getBlockEntity(POS);var saved=be.saveWithFullMetadata();
        var inventory=saved.getCompound("inventory");inventory.putInt("Size",Integer.MAX_VALUE);
        var items=new net.minecraft.nbt.ListTag();var item=new ItemStack(Items.IRON_ORE,1).save(new CompoundTag());
        item.putInt("Slot",1);item.putInt("jdteRealCount",-1);items.add(item);inventory.put("Items",items);
        be.load(saved);
        h.assertTrue(be.getMachineHandler().getSlots()==65,"saved NBT cannot resize handler");
        h.assertTrue(be.getMachineHandler().getStackInSlot(1).isEmpty(),"invalid negative count discarded");h.succeed();
    }

    @GameTest(template="mineral_lifecycle_empty", timeoutTicks=600)
    public static void factoryPackerMovesPopulatedLargeMineralMachine(GameTestHelper h) { packerRoundTrip(h,0); }
    @GameTest(template="mineral_lifecycle_empty", timeoutTicks=600)
    public static void factoryPackerRotatesPopulatedLargeMineralMachine(GameTestHelper h) { packerRoundTrip(h,1); }
    private static void packerRoundTrip(GameTestHelper h,int rotation) {
        var controller=h.absolutePos(POS);
        h.setBlock(POS,JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());
        h.assertTrue(com.jdte.common.blocks.LargeMineralExtractorStructure.placeParts(h.getLevel(),controller,net.minecraft.core.Direction.NORTH),"structure");
        var machine=(MineralExtractorBE)h.getBlockEntity(POS);
        UpgradeHelper.getUpgradeHandler(machine).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
        machine.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
        for(int i=0;i<4;i++){
            var survey=new ItemStack(JDTEItems.MINERAL_SURVEY.get());
            com.jdte.common.items.MineralSurveyItem.setSurvey(survey,com.jdte.common.minerals.MineralSurveyData.create(com.jdte.common.minerals.MineralSurveyIndex.version(h.getLevel().getServer()),new net.minecraft.resources.ResourceLocation("minecraft:plains"),h.getLevel().dimension().location(),java.util.List.of(new com.jdte.common.minerals.MineralEntry(new net.minecraft.resources.ResourceLocation("minecraft:iron_ore"),1,-64,64,9,com.jdte.common.minerals.MineralEntry.Confidence.EXACT))));
            machine.getMachineHandler().setStackInSlot(i,survey);
        }
        for(int i=4;i<68;i++) machine.getMachineHandler().setStackInSlot(i,new ItemStack(Items.IRON_ORE,6_400_000));
        machine.getTimeFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),80000));
        machine.getExperienceFluidTank().setFluid(new net.minecraftforge.fluids.FluidStack(com.direwolf20.justdirethings.setup.Registration.XP_FLUID_SOURCE.get(),70000));
        machine.getEnergyStorage().setEnergy(50_000_000);machine.setMultiplier(17);
        var saved=machine.saveWithFullMetadata();saved.putLong("pendingBaseWork",13);saved.putLong("pendingAcceleratedWork",7);machine.load(saved);
        var packerPos=new BlockPos(0,1,0);h.setBlock(packerPos,JDTEBlocks.FACTORY_PACKER.get());
        var packer=(com.jdte.common.blockentities.FactoryPackerBE)h.getBlockEntity(packerPos);
        packer.getAreaAffectingData().area=new AABB(controller.offset(-1,0,0),controller.offset(2,2,3));
        packer.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.FACTORY_PACKAGE.get()));
        packer.getEnergyStorage().setEnergy(packer.getEnergyStorage().getMaxEnergyStored());
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        // Forge login integrations need a real Netty channel. Register only this test owner's
        // lookup entry; the capture still executes the real permission and move pipeline.
        java.util.Map<java.util.UUID,net.minecraft.server.level.ServerPlayer> owners=net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(net.minecraft.server.players.PlayerList.class,h.getLevel().getServer().getPlayerList(),"f_11197_");
        owners.put(player.getUUID(),player);
        var start=packer.startOperation(player); h.assertTrue(packer.isBusy(),"capture starts: "+start);
        int[] stage={0};var origin=controller.offset(-1,0,6);var target=rotation==0?origin.offset(1,0,0):origin.offset(2,0,1);
        h.succeedWhen(()->{
            h.assertTrue(!packer.isBusy(),"packer still processing");
            h.assertTrue(packer.saveWithFullMetadata().getInt("factoryPackerError")==0,"packer operation error: "+packer.saveWithFullMetadata().getInt("factoryPackerError"));
            if(stage[0]==0){
                h.assertTrue(h.getLevel().isEmptyBlock(controller),"source controller removed");
                var parcel=packer.getMachineHandler().getStackInSlot(0);
                h.assertTrue(com.jdte.common.items.FactoryPackageItem.getPackageId(parcel).isPresent(),"filled package persists");
                com.jdte.common.items.FactoryPackageItem.rotate(parcel,rotation);
                com.jdte.common.items.FactoryPackageItem.setPlacementTarget(parcel,origin,h.getLevel().dimension().location());
                var placeStart=packer.startOperation(player);h.assertTrue(packer.isBusy(),"placement starts: "+placeStart);stage[0]=1;
                h.assertTrue(false,"wait for placement");
            }
            var restored=(MineralExtractorBE)h.getLevel().getBlockEntity(target);
            h.assertTrue(restored!=null,"restored controller");
            h.assertTrue(restored.getMachineHandler().getStackInSlot(67).getCount()==6_400_000,"all oversized items preserved");
            h.assertTrue(restored.getEnergyStorage().getEnergyStored()==50_000_000,"FE preserved, actual="+restored.getEnergyStorage().getEnergyStored()+", upgrades="+UpgradeHelper.getUpgradeHandler(restored).serializeNBT());
            h.assertTrue(UpgradeHelper.hasUltimateCapacity(restored),"upgrade preserved");
            h.assertTrue(restored.getMultiplier()==17,"configuration preserved");
            h.assertTrue(restored.getTimeFluidTank().getFluidAmount()==80000&&restored.getExperienceFluidTank().getFluidAmount()==70000,"both fluids preserved");
            for(int i=0;i<4;i++) h.assertTrue(com.jdte.common.items.MineralSurveyItem.getSurvey(restored.getMachineHandler().getStackInSlot(i))!=null,"survey preserved");
            for(int i=4;i<68;i++)h.assertTrue(restored.getMachineHandler().getStackInSlot(i).getCount()==6_400_000,"all output pages preserved");
            h.assertTrue(restored.saveWithFullMetadata().getLong("pendingBaseWork")==13,"pending work preserved");
            var facing=restored.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
            h.assertTrue(facing==(rotation==0?net.minecraft.core.Direction.NORTH:net.minecraft.core.Direction.EAST),"rotated facing");
            for(int y=0;y<2;y++)for(int z=0;z<3;z++)for(int x=-1;x<=1;x++){
                var member=com.jdte.common.blocks.LargeMineralExtractorStructure.position(target,facing,x,z,y);
                h.assertTrue(h.getLevel().getBlockEntity(member)!=null,"every rotated structure member restored");
            }
            owners.remove(player.getUUID(),player);
        });
    }

    @GameTest(template="mineral_lifecycle_empty")
    public static void factoryPackerRejectsPartialMineralStructureWithoutRemovingItems(GameTestHelper h) {
        h.setBlock(POS,JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());var controller=h.absolutePos(POS);
        com.jdte.common.blocks.LargeMineralExtractorStructure.placeParts(h.getLevel(),controller,net.minecraft.core.Direction.NORTH);
        var machine=(MineralExtractorBE)h.getBlockEntity(POS);machine.getMachineHandler().setStackInSlot(67,new ItemStack(Items.IRON_ORE,6_400_000));
        h.setBlock(new BlockPos(0,1,0),JDTEBlocks.FACTORY_PACKER.get());
        var packer=(com.jdte.common.blockentities.FactoryPackerBE)h.getBlockEntity(new BlockPos(0,1,0));
        packer.getAreaAffectingData().area=new AABB(controller);
        packer.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.FACTORY_PACKAGE.get()));
        packer.startOperation(net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel()));
        h.runAfterDelay(3,()->{
            h.assertTrue(!packer.isBusy()&&packer.saveWithFullMetadata().getInt("factoryPackerError")==21,"partial selection rejected with useful error");
            h.assertTrue(h.getLevel().getBlockEntity(controller)==machine,"controller remains in world");
            h.assertTrue(machine.getMachineHandler().getStackInSlot(67).getCount()==6_400_000,"all items remain");
            h.assertTrue(com.jdte.common.items.FactoryPackageItem.getPackageId(packer.getMachineHandler().getStackInSlot(0)).isEmpty(),"no partial package created");h.succeed();
        });
    }

    @GameTest(template="mineral_empty")
    public static void detachedPartCannotWriteEvenIfItsRemovedFlagIsCleared(GameTestHelper h) {
        h.setBlock(POS,JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());var controller=h.absolutePos(POS);
        com.jdte.common.blocks.LargeMineralExtractorStructure.placeParts(h.getLevel(),controller,net.minecraft.core.Direction.NORTH);
        var machine=(MineralExtractorBE)h.getBlockEntity(POS);machine.getEnergyStorage().setEnergy(1000);
        var pos=controller.east();var old=h.getLevel().getBlockEntity(pos);
        var retained=old.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new);
        h.getLevel().removeBlockEntity(pos);var current=h.getLevel().getBlockEntity(pos);h.assertTrue(current!=old,"world recreated part");
        old.clearRemoved();
        h.assertTrue(retained.receiveEnergy(1000,false)==0&&retained.getEnergyStored()==0,"detached endpoint cannot route through replacement");
        h.assertTrue(current.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new).receiveEnergy(1000,false)==1000,"current endpoint still works");
        h.assertTrue(machine.getEnergyStorage().getEnergyStored()==2000,"exact resource conservation");old.setRemoved();h.succeed();
    }
}
