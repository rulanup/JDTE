package com.jdte.client.smoke;

import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.setup.*;
import com.direwolf20.justdirethings.client.screens.widgets.*;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

@Mod.EventBusSubscriber(modid="jdte",value=Dist.CLIENT)
public final class ExperienceHolderClientSmoke {
    private static final BlockPos A=new BlockPos(32,110,32);
    private static int stage,ticks,at;
    private static volatile boolean done;
    private static volatile RuntimeException failure;
    private static void check(boolean ok,String text){if(!ok)throw new IllegalStateException("Experience GUI stage "+stage+": "+text);}
    private static void next(){stage++;at=ticks;com.mojang.logging.LogUtils.getLogger().info("Experience GUI stage {}",stage);}
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){var s=mc.getSingleplayerServer();var id=mc.player.getUUID();s.execute(()->{try{action.accept(s.getPlayerList().getPlayer(id));}catch(RuntimeException e){failure=e;throw e;}});}
    private static ExtendedExperienceHolderBE be(ServerPlayer p){return (ExtendedExperienceHolderBE)p.serverLevel().getBlockEntity(A);}
    private static void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),msg->{});}
    private static void click(Minecraft mc,String translation){
        var button=mc.screen.children().stream().filter(c->c instanceof GrayscaleButton b&&b.getLocalization().equals(net.minecraft.network.chat.Component.translatable(translation))).map(c->(GrayscaleButton)c).findFirst().orElseThrow();
        check(mc.screen.mouseClicked(button.getX()+4,button.getY()+4,0),"actual native control receives mouse event: "+translation);mc.screen.mouseReleased(button.getX()+4,button.getY()+4,0);
    }
    private static mezz.jei.api.runtime.IJeiRuntime jei(){try{var f=com.jdte.common.jei.JDTEJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event)throws Exception{
        if(event.phase!=TickEvent.Phase.END||!Boolean.getBoolean("jdte.experienceSmoke"))return;
        if(failure!=null)throw failure;var mc=Minecraft.getInstance();int elapsed=++ticks-at;check(ticks<5000,"timeout");if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        switch(stage){
            case 0->{if(mc.player.tickCount<60)return;server(mc,p->{var l=p.serverLevel();l.setDayTime(6000);for(var pos:BlockPos.betweenClosed(A.offset(-3,-1,-3),A.offset(3,3,3))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(A,JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());be(p).setPlacedBy(p.getUUID());be(p).exp=1234567890;be(p).targetExp=12;be(p).markDirtyClient();p.getInventory().clearContent();p.getInventory().setItem(9,new ItemStack(JDTEItems.RANGE_UPGRADE.get(),2));p.experienceLevel=0;p.experienceProgress=0;p.totalExperience=0;p.giveExperiencePoints(400);p.teleportTo(A.getX()+.5,A.getY(),A.getZ()+3.5);done=true;});next();}
            case 1->{if(!done||elapsed<35||!(mc.level.getBlockEntity(A) instanceof ExtendedExperienceHolderBE))return;mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(A),Direction.UP,A,false));next();}
            case 2->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<25)return;
                check(m.storedXp()==1234567890&&m.targetXp()==12&&m.slots.size()==44,"full32-bit XP/source target/eight card slots synced");
                var far=new BlockPos(1000000,120,1000000);var view=new ExtendedExperienceHolderContainer(99,mc.player.getInventory(),far);for(int i=0;i<10;i++)view.setData(i,i==0?65535:i==1?32767:0);check(view.storedXp()==Integer.MAX_VALUE&&view.slots.size()==44&&mc.level.getChunkSource().getChunk(far.getX()>>4,far.getZ()>>4,net.minecraft.world.level.chunk.ChunkStatus.FULL,false)==null,"missing-chunk client view matches ten properties/eight cards without chunk load");
                shot(mc,"jdte-experience-menu.png");click(mc,"justdirethings.screen.storeexp");next();}
            case 3->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<25)return;check(m.storedXp()==1234567896&&ExperienceUtils.getPlayerTotalExperience(mc.player)==394,"real one-level deposit preserves six partial XP");click(mc,"justdirethings.screen.retrieveexp");next();}
            case 4->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<25)return;check(m.storedXp()==1234567849&&ExperienceUtils.getPlayerTotalExperience(mc.player)==441,"real one-level extract pays exact47 XP");click(mc,"justdirethings.screen.owneronly");click(mc,"justdirethings.screen.collectexp");click(mc,"justdirethings.screen.showparticles");
                var number=mc.screen.children().stream().filter(c->c instanceof NumberButton n&&n.getWidth()==24&&n.getValue()==12).map(c->(NumberButton)c).findFirst().orElseThrow();number.setValue(37);number.onPress();next();}
            case 5->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<30)return;check(m.ownerOnly()&&m.collectExp()&&!m.showParticles()&&m.targetXp()==37,"all native settings synchronized through authoritative menu/payload");
                int slot=java.util.stream.IntStream.range(8,m.slots.size()).filter(i->m.slots.get(i).getItem().is(JDTEItems.RANGE_UPGRADE.get())).findFirst().orElseThrow();mc.gameMode.handleInventoryMouseClick(m.containerId,slot,0,ClickType.QUICK_MOVE,mc.player);next();}
            case 6->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<25)return;check(m.slots.get(0).getItem().is(JDTEItems.RANGE_UPGRADE.get())&&m.slots.get(0).getItem().getCount()==1&&m.slots.get(1).getItem().is(JDTEItems.RANGE_UPGRADE.get())&&m.slots.get(1).getItem().getCount()==1&&mc.player.getInventory().countItem(JDTEItems.RANGE_UPGRADE.get())==0,"actual shift-click installs both legal Range cards exactly once in separate slots");shot(mc,"jdte-experience-settings-upgrade.png");mc.gameMode.handleInventoryMouseClick(m.containerId,0,0,ClickType.QUICK_MOVE,mc.player);mc.gameMode.handleInventoryMouseClick(m.containerId,1,0,ClickType.QUICK_MOVE,mc.player);mc.gameMode.handleInventoryButtonClick(m.containerId,2);next();}
            case 7->{if(!(mc.player.containerMenu instanceof ExtendedExperienceHolderContainer m)||elapsed<25)return;check(m.storedXp()==1234568290&&ExperienceUtils.getPlayerTotalExperience(mc.player)==0&&mc.player.getInventory().countItem(JDTEItems.RANGE_UPGRADE.get())==2,"all deposit and card removal conserve exact XP/cards");var runtime=jei();check(runtime!=null,"real JEI runtime");var list=runtime.getRecipeManager().createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING).get().filter(r->r.getId().equals(com.jdte.JDTE.id("extended_experience_holder"))).toList();check(list.size()==1,"actual preserving recipe visible in JEI");runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(mezz.jei.api.constants.RecipeTypes.CRAFTING),list,java.util.List.of());next();}
            case 8->{if(elapsed<25)return;shot(mc,"jdte-experience-jei.png");mc.screen.onClose();mc.player.closeContainer();mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A));next();}
            case 9->{mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(A));if(elapsed<50)return;var jade=snownee.jade.impl.ObjectDataCenter.getServerData();check(jade!=null&&jade.getInt("jdte_xp")==1234568290&&jade.getInt("jdte_target_xp")==37,"actual Jade server data carries exact XP/target");shot(mc,"jdte-experience-world-jade.png");Files.writeString(mc.gameDirectory.toPath().resolve("experience-smoke-success.txt"),"PASS: actual graphical Forge client/integrated server, source native buttons mouse actions, full32-bit XP/target/eight upgrade slots, missing-chunk protocol, authoritative toggles/target packet, real card install/remove and exact XP/player ledger, JEI preserving recipe displayed. Actual Jade server packet contains exact XP/target; not independent multiplayer.\n");mc.stop();stage=10;}
        }
    }
}
