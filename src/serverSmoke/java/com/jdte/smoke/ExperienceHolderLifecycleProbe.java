package com.jdte.smoke;

import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.mixin.ExperienceOrbAccessor;
import com.jdte.setup.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.common.blockentities.ExperienceHolderBE;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.nio.file.*;
import java.util.*;

/** Two actual reobfuscated server JVMs; natural XP-orb work and real chunk lifecycle. */
public final class ExperienceHolderLifecycleProbe {
    private static final BlockPos A=new BlockPos(1023,110,1024),B=A.east(3);
    private static final UUID OWNER=UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2001");
    private MinecraftServer server;
    private String mode="";
    private int ticks,stage;
    private CompoundTag expected;
    private ExtendedExperienceHolderBE oldOwner;
    private IFluidHandler retained;
    private static void check(boolean ok,String text){if(!ok)throw new IllegalStateException("Experience lifecycle: "+text);}
    private ExtendedExperienceHolderBE be(){return (ExtendedExperienceHolderBE)server.overworld().getBlockEntity(B);}
    private void force(boolean value){for(int x=(A.getX()>>4)-1;x<=(B.getX()>>4)+1;x++)for(int z=(A.getZ()>>4)-1;z<=(B.getZ()>>4)+1;z++)server.overworld().setChunkForced(x,z,value);}
    @SubscribeEvent public void start(ServerStartedEvent event)throws Exception{
        mode=System.getProperty("jdte.experienceLifecycle","");if(mode.isEmpty())return;server=event.getServer();force(true);var level=server.overworld();
        if(mode.equals("verify")){
            expected=NbtIo.readCompressed(Path.of("experience-expected.nbt").toFile());level.getChunkAt(B);
            check(expected.equals(be().saveWithoutMetadata()),"new JVM restores complete XP/settings/owner/area/eight-card NBT before ticking");stage=4;return;
        }
        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
        for(var p:BlockPos.betweenClosed(A.offset(-2,-1,-2),B.offset(2,2,2))){level.removeBlockEntity(p);level.setBlockAndUpdate(p,p.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}
        level.setBlockAndUpdate(A,Registration.ExperienceHolder.get().defaultBlockState());
        var nativeHolder=(ExperienceHolderBE)level.getBlockEntity(A);nativeHolder.exp=500000;nativeHolder.targetExp=15;nativeHolder.collectExp=true;nativeHolder.showParticles=false;nativeHolder.setPlacedBy(OWNER);
        nativeHolder.areaAffectingData.xOffset=nativeHolder.areaAffectingData.yOffset=nativeHolder.areaAffectingData.zOffset=0;
        nativeHolder.areaAffectingData.xRadius=nativeHolder.areaAffectingData.yRadius=nativeHolder.areaAffectingData.zRadius=2;
        var cards=UpgradeHelper.getUpgradeHandler(nativeHolder);
        for(int i=0;i<4;i++)check(cards.insertItem(i,new ItemStack(i<2?JDTEItems.RANGE_UPGRADE.get():i==2?JDTEItems.CAPACITY_UPGRADE.get():JDTEItems.FLUID_UPGRADE.get()),false).isEmpty(),"source four cards");
        var actor=FakePlayerFactory.get(level,new GameProfile(OWNER,"ExperiencePack"));actor.setPos(Vec3.atCenterOf(A.south()));
        var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());actor.setItemInHand(InteractionHand.MAIN_HAND,card);
        check(card.useOn(new UseOnContext(actor,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(A),Direction.UP,A,false)))==InteractionResult.SUCCESS&&card.isEmpty(),"actual native tier conversion consumes one card");
        var converted=(ExtendedExperienceHolderBE)level.getBlockEntity(A);check(converted.exp==500000&&nativeHolder.exp==0&&UpgradeHelper.getUpgradeHandler(converted).getSlots()==8,"native XP/card ownership moved once");
        check(UpgradeHelper.getUpgradeHandler(converted).insertItem(7,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false).isEmpty(),"eighth source-compatible capacity card");
        // Source XP capacity remains fixed: this card does not create extra XP.
        orb(A,37,5);
    }
    private void orb(BlockPos pos,int value,int count){var l=server.overworld();var orb=new ExperienceOrb(l,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,value);((ExperienceOrbAccessor)orb).jdte$setCount(count);check(l.addFreshEntity(orb),"actual orb admitted");}
    private void pack(){
        var l=server.overworld();var machine=(ExtendedExperienceHolderBE)l.getBlockEntity(A);var before=machine.saveWithoutMetadata();
        var p=FakePlayerFactory.get(l,new GameProfile(OWNER,"ExperiencePack"));p.setPos(Vec3.atCenterOf(A.south()));for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        check(EclipseAlloyWrenchItem.pickupMachine(l,A,p),"real full-inventory survival wrench");
        var packages=l.getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(3)).stream().filter(e->e.getItem().is(JDTEItems.EXTENDED_EXPERIENCE_HOLDER.get())).toList();check(packages.size()==1,"one package, no duplicated holder");
        var stack=packages.get(0).getItem().copy();packages.get(0).discard();p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.setPos(Vec3.atCenterOf(B.south()));
        check(stack.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(B.below()),Direction.UP,B.below(),false))).consumesAction()&&stack.isEmpty(),"actual crosschunk survival placement consumes one package");
        var after=be().saveWithoutMetadata();check(before.equals(after),"packed transport retains complete machine XP/area/settings/eight slots exactly");
        check(machine.getXpFluidHandler().drain(1000,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"packed old XP endpoint inert");
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws Exception{
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;
        check(++ticks<2000,"timeout stage "+stage);var l=server.overworld();
        if(stage==0){var source=(ExtendedExperienceHolderBE)l.getBlockEntity(A);if(source.exp==500000)return;check(source.exp==500185,"five real merged orb units paid exactly185XP");pack();orb(B,35,1);stage=1;}
        else if(stage==1){if(be().exp==500185)return;check(be().exp==500220,"relocated machine resumes natural orb collection once");expected=be().saveWithoutMetadata();retained=be().getXpFluidHandler();oldOwner=be();force(false);stage=2;}
        else if(stage==2){if(l.getChunkSource().hasChunk(A.getX()>>4,A.getZ()>>4)||l.getChunkSource().hasChunk(B.getX()>>4,B.getZ()>>4))return;check(retained.drain(1000,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"captured endpoint inactive after actual chunk unload");force(true);l.getChunkAt(B);check(expected.equals(be().saveWithoutMetadata())&&oldOwner!=be(),"fresh actual unload/reload restores exact full NBT");stage=3;}
        else if(stage==3){NbtIo.writeCompressed(expected,Path.of("experience-expected.nbt").toFile());finish("experience-seed-success","PASS: native tier conversion/card and XP ownership, merged natural orb work+185, full-inventory survival wrench/crosschunk placement/full NBT, resumed+35, actual unload/reload/inert retained capability and exact saved state.");}
        else if(stage==4){check(expected.equals(be().saveWithoutMetadata()),"fresh JVM state still exact");orb(B,11,3);stage=5;}
        else if(stage==5){if(be().exp==500220)return;check(be().exp==500253,"fresh production JVM resumes exactly33XP from three merged units");check(UpgradeHelper.getUpgradeHandler(be()).getStackInSlot(7).is(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),"real eighth slot survives restart");finish("experience-verify-success","PASS: fresh production JVM exact XP/settings/owner/area/eight slots before work, then natural merged-orb collection adds exactly33XP. Minimal optional-mod-absent production profile; independent multiplayer/cross-dimensional transfer/prolonged load not claimed.");}
    }
    private void finish(String marker,String text)throws Exception{Files.writeString(Path.of(marker),text+"\n");mode="";server.saveEverything(false,true,true);server.halt(false);}
}
