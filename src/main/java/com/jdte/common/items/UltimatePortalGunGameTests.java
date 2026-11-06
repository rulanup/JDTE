package com.jdte.common.items;

import com.direwolf20.justdirethings.common.entities.PortalProjectile;
import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class UltimatePortalGunGameTests {
    private static ItemStack gun(){return new ItemStack(JDTEItems.ULTIMATE_PORTAL_GUN.get());}
    private static IFluidHandlerItem fluid(ItemStack gun){return gun.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);}
    private static IEnergyStorage energy(ItemStack gun){return gun.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);}
    private static void charge(ItemStack gun,int mb,int fe){fluid(gun).fill(new FluidStack(Registration.PORTAL_FLUID_SOURCE.get(),mb),IFluidHandler.FluidAction.EXECUTE);energy(gun).receiveEnergy(fe,false);}
    private static Player player(GameTestHelper h,ItemStack gun){var p=h.makeMockSurvivalPlayer();p.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(2,1,2))));p.setItemInHand(InteractionHand.MAIN_HAND,gun);return p;}
    private static NBTHelpers.PortalDestination destination(Player p,boolean cross,double distance){return new NBTHelpers.PortalDestination(cross?Level.NETHER:p.level().dimension(),p.position().add(distance,0,0),Direction.NORTH,"cost-test");}
    private static void manual(ItemStack gun,NBTHelpers.PortalDestination dest){UltimatePortalGunItem.getDestinations(gun);UltimatePortalGunItem.setDestination(gun,0,dest);UltimatePortalGunItem.setFavoritePosition(gun,0);}
    private static List<PortalProjectile> projectiles(GameTestHelper h,Player p){return h.getLevel().getEntitiesOfClass(PortalProjectile.class,new AABB(p.blockPosition()).inflate(3));}
    @GameTest(template="mineral_empty")
    public static void actualManualShotsUseSourceFluidAndNativeEnergyConfiguration(GameTestHelper h){
        int nativeCost=Config.PORTAL_GUN_FE_COST.get(),legacy=JDTEConfig.COMMON.ultimatePortalGun.energyCost.get();
        try{
            Config.PORTAL_GUN_FE_COST.set(173);JDTEConfig.COMMON.ultimatePortalGun.energyCost.set(99);
            for(boolean cross:List.of(false,true)){
                var gun=gun();var p=player(h,gun);charge(gun,30000,5000);manual(gun,destination(p,cross,10));int before=projectiles(h,p).size();
                var result=UltimatePortalGunItem.spawnProjectile(h.getLevel(),p,gun,true);
                h.assertTrue(result.getResult()==InteractionResult.SUCCESS&&projectiles(h,p).size()==before+1,"successful shot adds one actual native projectile");
                h.assertTrue(energy(gun).getEnergyStored()==4827&&fluid(gun).getFluidInTank(0).getAmount()==30000-(cross?500:250),"native 173-FE price and source manual fluid price used");
                projectiles(h,p).forEach(net.minecraft.world.entity.Entity::discard);
            }
        }finally{Config.PORTAL_GUN_FE_COST.set(nativeCost);JDTEConfig.COMMON.ultimatePortalGun.energyCost.set(legacy);}h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void previousDestinationUsesNativePriceDespiteSelectedManualSlot(GameTestHelper h){
        var gun=gun();var p=player(h,gun);charge(gun,30000,100000);manual(gun,destination(p,false,1000));PortalGunV2.setPrevious(p,gun);p.setShiftKeyDown(true);
        var previous=PortalGunV2.getPrevious(gun);int cost=PortalGunV2.calculateFluidCost(h.getLevel(),p,previous);int before=fluid(gun).getFluidInTank(0).getAmount(),fe=energy(gun).getEnergyStored();
        h.assertTrue(cost>0&&UltimatePortalGunItem.calculateManualFluidCost(h.getLevel().dimension(),p.position(),previous)==0,"native previous price differs from zero-distance manual price");
        h.assertTrue(UltimatePortalGunItem.spawnProjectile(h.getLevel(),p,gun,true).getResult()==InteractionResult.SUCCESS,"previous shot succeeds");
        h.assertTrue(fluid(gun).getFluidInTank(0).getAmount()==before-cost&&energy(gun).getEnergyStored()==fe-Config.PORTAL_GUN_FE_COST.get(),"previous destination preserves actual native pricing and FE cost");projectiles(h,p).forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void configuredCapacitiesAndBothResourcesSurviveItemNbtReload(GameTestHelper h){
        int fluidCap=JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.get(),energyCap=JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.get();
        try{
            JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.set(30000);JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.set(12000);
            var gun=gun();charge(gun,40000,20000);h.assertTrue(fluid(gun).getTankCapacity(0)==30000&&fluid(gun).getFluidInTank(0).getAmount()==30000&&energy(gun).getMaxEnergyStored()==12000&&energy(gun).getEnergyStored()==12000,"actual capabilities honor configured capacities and reject excess");
            var p=player(h,gun);manual(gun,destination(p,false,1.001));var restored=ItemStack.of(gun.save(new net.minecraft.nbt.CompoundTag()));
            h.assertTrue(fluid(restored).getFluidInTank(0).getAmount()==30000&&energy(restored).getEnergyStored()==12000&&UltimatePortalGunItem.isManualSlot(restored,0),"resource values and manual flags survive actual ItemStack NBT");
            h.assertTrue(fluid(restored).fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),IFluidHandler.FluidAction.EXECUTE)==0&&UltimatePortalGunItem.getFullness(restored)==3,"wrong fluid rejected and fullness uses configured capacity");
        }finally{JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.set(fluidCap);JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.set(energyCap);}h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void insufficientFluidAndEnergyNeverSpawnOrDebitResources(GameTestHelper h){
        int nativeCost=Config.PORTAL_GUN_FE_COST.get();try{
            Config.PORTAL_GUN_FE_COST.set(173);
            for(boolean shortFluid:List.of(false,true)){
                var gun=gun();var p=player(h,gun);charge(gun,shortFluid?499:500,shortFluid?173:172);manual(gun,destination(p,true,10));
                int mb=fluid(gun).getFluidInTank(0).getAmount(),fe=energy(gun).getEnergyStored(),entities=projectiles(h,p).size();
                h.assertTrue(UltimatePortalGunItem.spawnProjectile(h.getLevel(),p,gun,true).getResult()==InteractionResult.FAIL,"incomplete payment cannot spawn a portal shot");
                h.assertTrue(fluid(gun).getFluidInTank(0).getAmount()==mb&&energy(gun).getEnergyStored()==fe&&projectiles(h,p).size()==entities,"no partial FE/fluid debit or extra projectile");
            }
        }finally{Config.PORTAL_GUN_FE_COST.set(nativeCost);}h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void zeroDistanceManualShotNeedsNoFluidButStillPaysNativeEnergy(GameTestHelper h){
        var gun=gun();var p=player(h,gun);charge(gun,0,100000);manual(gun,destination(p,false,0));int before=energy(gun).getEnergyStored();
        h.assertTrue(UltimatePortalGunItem.spawnProjectile(h.getLevel(),p,gun,true).getResult()==InteractionResult.SUCCESS&&fluid(gun).getFluidInTank(0).isEmpty()&&energy(gun).getEnergyStored()==before-Config.PORTAL_GUN_FE_COST.get(),"source zero-fluid manual distance still charges native FE");projectiles(h,p).forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void cancelledNativeProjectileDoesNotChargeEitherResource(GameTestHelper h){
        var gun=gun();var p=player(h,gun);charge(gun,30000,100000);manual(gun,destination(p,true,5));int before=energy(gun).getEnergyStored();
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> listener=event->{if(event.getEntity() instanceof PortalProjectile projectile&&projectile.getOwner()==p)event.setCanceled(true);};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.entity.EntityJoinLevelEvent.class,listener);
        try{h.assertTrue(UltimatePortalGunItem.spawnProjectile(h.getLevel(),p,gun,true).getResult()==InteractionResult.FAIL&&energy(gun).getEnergyStored()==before&&fluid(gun).getFluidInTank(0).getAmount()==30000&&projectiles(h,p).isEmpty(),"cancelled real native projectile preserves FE and fluid");}
        finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);}h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void recordedManualAndPreviousRoutesKeepSeparatePricing(GameTestHelper h){
        var gun=gun();var p=player(h,gun);var dest=destination(p,false,1.001);UltimatePortalGunItem.fillDestination(gun,0,dest);
        h.assertTrue(!UltimatePortalGunItem.isManualSlot(gun,0)&&UltimatePortalGunItem.calculateDestinationFluidCost(gun,false,h.getLevel().dimension(),p.position(),dest,()->777)==777,"recorded favorite uses native price");
        UltimatePortalGunItem.setDestination(gun,0,dest);h.assertTrue(UltimatePortalGunItem.calculateDestinationFluidCost(gun,false,h.getLevel().dimension(),p.position(),dest,()->777)==26,"manual distance rounds up at 25 mB per block");
        h.assertTrue(UltimatePortalGunItem.calculateDestinationFluidCost(gun,true,h.getLevel().dimension(),p.position(),dest,()->888)==888,"previous uses native callback even while manual flag is selected");
        h.assertTrue(UltimatePortalGunItem.calculateManualFluidCost(h.getLevel().dimension(),p.position(),destination(p,false,5000))==25000,"manual same-dimension cap matches pinned source");h.succeed();
    }
}
