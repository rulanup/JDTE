package com.jdte.smoke;

import com.direwolf20.justdirethings.common.entities.PortalProjectile;
import com.direwolf20.justdirethings.common.items.PortalGunV2;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.NBTHelpers;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/** One production-JAR native-projectile/resource check; no graphical or multiplayer claim. */
public final class PortalGunProbe {
    private MinecraftServer server;
    private int ticks;
    private boolean active;
    private net.minecraft.world.entity.player.Player owner;
    private final java.util.ArrayList<PortalProjectile> spawned=new java.util.ArrayList<>();
    private static void check(boolean condition,String reason){if(!condition)throw new IllegalStateException("JDTE portal production probe: "+reason);}
    @SubscribeEvent public void started(ServerStartedEvent event){
        active=Boolean.getBoolean("jdte.portalGunSmoke");if(!active)return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"requires a marked disposable server");server=event.getServer();
    }
    @SubscribeEvent public void joined(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if(active&&event.getEntity() instanceof PortalProjectile projectile&&projectile.getOwner()==owner)spawned.add(projectile);
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws IOException{
        if(!active||event.phase!=TickEvent.Phase.END||event.getServer()!=server||++ticks<5)return;
        int nativeCost=Config.PORTAL_GUN_FE_COST.get(),legacyCost=JDTEConfig.COMMON.ultimatePortalGun.energyCost.get();
        int fluidCapacity=JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.get(),energyCapacity=JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.get();
        try{
            Config.PORTAL_GUN_FE_COST.set(173);JDTEConfig.COMMON.ultimatePortalGun.energyCost.set(99);
            JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.set(30000);JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.set(12000);
            var level=server.overworld();var pos=new BlockPos(3304,90,3304);level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
            var player=FakePlayerFactory.getMinecraft(level);owner=player;player.setPos(Vec3.atCenterOf(pos));player.setShiftKeyDown(false);
            for(boolean cross:List.of(false,true)){
                var gun=new ItemStack(JDTEItems.ULTIMATE_PORTAL_GUN.get());player.setItemInHand(InteractionHand.MAIN_HAND,gun);
                var fluid=gun.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);var energy=gun.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
                check(fluid.getTankCapacity(0)==30000&&energy.getMaxEnergyStored()==12000,"production capabilities use configured capacities");
                check(fluid.fill(new FluidStack(Registration.PORTAL_FLUID_SOURCE.get(),40000),IFluidHandler.FluidAction.EXECUTE)==30000&&energy.receiveEnergy(20000,false)==12000,"real buffers cannot overfill");
                UltimatePortalGunItem.getDestinations(gun);UltimatePortalGunItem.setDestination(gun,0,new NBTHelpers.PortalDestination(cross?Level.NETHER:Level.OVERWORLD,player.position().add(10,0,0),Direction.NORTH,"production"));
                int joins=spawned.size();
                check(UltimatePortalGunItem.spawnProjectile(level,player,gun,true).getResult()==InteractionResult.SUCCESS,"production native projectile actually spawns");
                check(energy.getEnergyStored()==11827&&fluid.getFluidInTank(0).getAmount()==30000-(cross?500:250),"source manual price and native 173 FE charged exactly");
                check(spawned.size()==joins+1&&!spawned.get(joins).isRemoved()&&spawned.get(joins).level()==level,"exactly one actual native projectile joined the production level");spawned.get(joins).discard();
                var restored=ItemStack.of(gun.save(new CompoundTag()));var savedFluid=restored.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);var savedEnergy=restored.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
                check(savedEnergy.getEnergyStored()==11827&&savedFluid.getFluidInTank(0).getAmount()==30000-(cross?500:250)&&UltimatePortalGunItem.isManualSlot(restored,0),"actual item NBT preserves resources and manual flags");
                player.setItemInHand(InteractionHand.MAIN_HAND,restored);PortalGunV2.setPrevious(player,restored);player.setShiftKeyDown(true);
                joins=spawned.size();
                int previousCost=PortalGunV2.calculateFluidCost(level,player,PortalGunV2.getPrevious(restored)),before=savedFluid.getFluidInTank(0).getAmount();
                check(previousCost>0&&UltimatePortalGunItem.spawnProjectile(level,player,restored,true).getResult()==InteractionResult.SUCCESS,"production previous destination uses its native nonzero price");
                check(savedEnergy.getEnergyStored()==11654&&savedFluid.getFluidInTank(0).getAmount()==before-previousCost,"selected manual flag does not override previous-destination native price");
                check(spawned.size()==joins+1,"one actual previous-destination projectile joined");spawned.get(joins).discard();player.setShiftKeyDown(false);
            }
            check(UltimatePortalGunItem.calculateManualFluidCost(Level.OVERWORLD,player.position(),new NBTHelpers.PortalDestination(Level.OVERWORLD,player.position().add(5000,0,0),Direction.NORTH,"cap"))==25000,"production distance cap is 25000 mB");
        }finally{
            Config.PORTAL_GUN_FE_COST.set(nativeCost);JDTEConfig.COMMON.ultimatePortalGun.energyCost.set(legacyCost);
            JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunFluidCapacity.set(fluidCapacity);JDTEConfig.COMMON.ultimatePortalGun.ultimatePortalGunEnergyCapacity.set(energyCapacity);
        }
        Files.writeString(Path.of("portal-gun-success.txt"),"PASS: actual production native projectiles charge 500/250 mB manual prices and native 173 FE; configured 30000 mB/12000 FE capacities, item NBT restoration, previous routing and 25000 mB cap verified\n");
        active=false;server.halt(false);
    }
}
